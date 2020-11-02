package security.build.pdp.client;

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

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * The PDPClient component allow making data POST request to an OPA compatible server.
 * The client has a defined retry policy and connection timeout settings.
 */
@Component
public class PDPClient {

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
        this.retryTemplate = createRetryTemplate();
        this.restTemplate = createRestTemplate();
    }

    private RestTemplate createRestTemplate() {
        //RestTemplate does not use a connection pool by default, therefore we need to use HttpComponentsClientHttpRequestFactory
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setReadTimeout(readTimeout);
        requestFactory.setConnectTimeout(connectionTimeout);

        //RestTemplate is safe for concurrency
        return new RestTemplate(requestFactory);
    }

    private RetryTemplate createRetryTemplate() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        // Set the max retry attempts
        retryPolicy.setMaxAttempts(retryMaxAttempts);
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        // set fixed backoff period in ms
        backOffPolicy.setBackOffPeriod(retryBackoffMilliseconds);
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);

        return template;
    }


    private ResponseEntity<String> evaluateEx(Map<String, Object> input) throws Exception {
        HttpEntity<?> request = new HttpEntity<>(new PDPDataRequest(input));
        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(getQueryUrl(), request, String.class);

        responseEntityStr.getStatusCode();

        return responseEntityStr;
    }

    private ResponseEntity<String> evaluate(Map<String, Object> input) throws Throwable {
        return retryTemplate.execute(
                (RetryCallback<ResponseEntity<String>, Throwable>) retryContext -> {
                    return evaluateEx(input);
                });

    }

    /**
     * Performs a POST request to the data endpoint of the PDP.
     *
     * @param input a map containing JSON serializable objects to set as input
     * @return a JsonNode response for the given input.
     * @throws Throwable
     */
    public JsonNode getJsonResponse(Map<String, Object> input) throws Throwable {

        ResponseEntity<String> responseEntityStr = evaluate(input);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readTree(responseEntityStr.getBody());
    }

    /**
     * Perfoms a POST request to the data endpoint of the PDP.
     *
     * @param input a map containing JSON serializable objects to set as input
     * @return a Map containing attributes and the Object values
     * @throws Throwable
     */
    public Map<String, Object> getMappedResponse(Map<String, Object> input) throws Throwable {

        ResponseEntity<String> responseEntityStr = evaluate(input);

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(responseEntityStr.getBody(), new TypeReference<Map<String, Object>>() {
        });
    }

    private String getQueryUrl() {
        return this.schema + "://" + this.host + ":" + this.port + "/v1/data" + this.policyPath;
    }
}
