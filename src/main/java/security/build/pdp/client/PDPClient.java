package security.build.pdp.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class PDPClient {

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
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

    public PDPClient() {
        this.retryTemplate = createRetryTemplate();
        this.restTemplate = createRestTemplate();
    }

    public PDPClient(String port, String host, String schema, String policyPath) {
        this.port = port;
        this.host = host;
        this.schema = schema;
        this.policyPath = policyPath;
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

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPolicyPath() {
        return policyPath;
    }

    public void setPolicyPath(String policyPath) {
        this.policyPath = policyPath;
    }

//    public RestTemplate getRestTemplate(RestTemplateBuilder restTemplateBuilder) {
//
//        ClientHttpRequestFactorySupplier
//
//        return  restTemplateBuilder.factory(HttpComponentsClientHttpRequestFactory.class)
//                .setConnectTimeout(Duration.ofSeconds(5))
//                .setReadTimeout(Duration.ofSeconds(5))
//                .build();
//    }

    private ResponseEntity<String> evaluate(Map<String, Object> input) throws Exception {
        //        retryTemplate.execute(
//                context -> {
//                    evaluateExec(input);
//                    return true; //TODO
//                });
        HttpEntity<?> request = new HttpEntity<>(new PDPDataRequest(input));
        ResponseEntity<String> responseEntityStr = restTemplate.postForEntity(getQueryUrl(), request, String.class);

        responseEntityStr.getStatusCode(); //TODO throw exception on invalid ret vals

        return responseEntityStr;

    }

    //TODO consider returning POJO with the return strcuture of OPA {result, decisionlog}

    public JsonNode getJsonResponse(Map<String, Object> input) throws Exception {

        ResponseEntity<String> responseEntityStr = evaluate(input);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseEntityStr.getBody());

        return jsonNode;
    }

    public Map<String, Object> getMappedResponse(Map<String, Object> input) throws Exception {

        ResponseEntity<String> responseEntityStr = evaluate(input);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> objectsMap = objectMapper.readValue(responseEntityStr.getBody(), new TypeReference<Map<String, Object>>() {
        });

        return objectsMap;
    }

    private String getQueryUrl() {
        return getSchema() + "://" + getHost() + ":" + getPort() + "/v1/data" + getPolicyPath();
    }
}
