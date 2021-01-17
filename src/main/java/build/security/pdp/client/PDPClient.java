package build.security.pdp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import build.security.pdp.request.PdpRequest;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * The PDPClient component allow making data POST request to an OPA compatible server.
 * The client has a defined retry policy and connection timeout settings.
 */
@Component
public class PdpClient {

    private RestTemplate restTemplate;

    private RetryTemplate retryTemplate;

    @Value("${pdp.port:8181}")
    private String port;
    @Value("${pdp.hostname:localhost}")
    private String host;
    @Value("${pdp.schema:http}")
    private String schema;
    @Value("${pdp.policy.path}")
    private String policyPath;
    @Value("${pdp.readTimeout.milliseconds}")
    private int readTimeout;
    @Value("${pdp.connectionTimeout.milliseconds}")
    private int connectionTimeout;
    @Value("${pdp.retry.maxAttempts}")
    private int retryMaxAttempts;
    @Value("${pdp.retry.backoff.milliseconds}")
    private int retryBackoffMilliseconds;

    @PostConstruct
    private void postConstruct() {
        this.retryTemplate = PdpClient.createRetryTemplate(this.retryMaxAttempts, this.retryBackoffMilliseconds);
        this.restTemplate = createRestTemplate(this.readTimeout, this.connectionTimeout);
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    public static RestTemplate createRestTemplate(int readTimeout, int connectionTimeout) {
        //RestTemplate does not use a connection pool by default, therefore we need to use HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setReadTimeout(readTimeout);
        requestFactory.setConnectTimeout(connectionTimeout);

        //RestTemplate is safe for concurrency
        return new RestTemplate(requestFactory);
    }

    public static RetryTemplate createRetryTemplate(int retryMaxAttempts, int retryBackoffMilliseconds) {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        // Set the max retry attempts
        simpleRetryPolicy.setMaxAttempts(retryMaxAttempts);
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        // set fixed backoff period in ms
        backOffPolicy.setBackOffPeriod(retryBackoffMilliseconds);
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(new InternalServerErrorRetryPolicy(simpleRetryPolicy));
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }


    private ResponseEntity<String> evaluateEx(PdpRequest request) throws Exception {
        HttpEntity<?> requestBody = new HttpEntity<>(request);
        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(getQueryUrl(), requestBody, String.class);

        return responseEntityStr;
    }

    private ResponseEntity<String> evaluate(PdpRequest request) throws Throwable {
        return retryTemplate.execute(
                (RetryCallback<ResponseEntity<String>, Throwable>) retryContext -> {
                    return evaluateEx(request);
                });

    }

    /**
     * Performs a POST request to the data endpoint of the PDP.
     * Only if an HttpServerErrorException is thrown then a retry will be attempted.
     *
     * @param request a map containing JSON serializable objects to set as request
     * @return a JsonNode response for the given request.
     * @throws org.springframework.web.client.RestClientException
     */
    public JsonNode getJsonResponse(PdpRequest request) throws Throwable {

        ResponseEntity<String> responseEntityStr = evaluate(request);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(responseEntityStr.getBody());
    }

    /**
     * Perfoms a POST request to the data endpoint of the PDP.
     * Only if an HttpServerErrorException is thrown then a retry will be attempted.
     *
     * @param request a map containing JSON serializable objects to set as request
     * @return a Map containing attributes and the Object values
     * @throws org.springframework.web.client.RestClientException
     */
    public Map<String, Object> getMappedResponse(PdpRequest request) throws Throwable {

        ResponseEntity<String> responseEntityStr = evaluate(request);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(responseEntityStr.getBody(), new TypeReference<Map<String, Object>>() {
        });
    }

    private String getQueryUrl() {
        return this.schema + "://" + this.host + ":" + this.port + "/v1/data" + this.policyPath;
    }
}
