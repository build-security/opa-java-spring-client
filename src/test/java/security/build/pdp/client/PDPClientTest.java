package security.build.pdp.client;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

class PDPClientTest {

    private static PDPClient pdpClient;

    private RestTemplate mockRestTemplate;

    @BeforeAll
    public static void setup() {
        //use the same PDPClient for all test with the same retry template
        pdpClient = new PDPClient();
        pdpClient.setRetryTemplate(PDPClient.createRetryTemplate(2, 50));
    }

    @BeforeEach
    public void beforeEach() {
        //Use a different mock RestTemplate for each test (since mocking is different for each one)
        this.mockRestTemplate = mock(RestTemplate.class);
        pdpClient.setRestTemplate(mockRestTemplate);
    }

    @Test()
    void getJsonResponse_statusOk_noRetry() throws Throwable {


        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class))).thenReturn(mockResponse);

        HashMap<String, Object> map = new HashMap<>();
        JsonNode node = pdpClient.getJsonResponse(map);

        //assert that there was no retry on a successful attempt
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));

        //assert that the returned JSON node is correct
        Assertions.assertEquals("1", node.get("a").asText());
        Assertions.assertEquals("2", node.get("b").asText());
    }

    @Test()
    void getJsonResponse_serverError_retry() throws Throwable {

        ResponseEntity<String> mockResponseSuccess = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).thenReturn(mockResponseSuccess);

        HashMap<String, Object> map = new HashMap<>();
        JsonNode node = pdpClient.getJsonResponse(map);

        //assert that there were exactly 2 attempts
        verify(mockRestTemplate, times(2)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));

        //assert that the returned JSON node is correct
        Assertions.assertEquals("1", node.get("a").asText());
        Assertions.assertEquals("2", node.get("b").asText());
    }

    @Test()
    void getJsonResponse_serverError_retriesExhausted() throws Throwable {

        ResponseEntity<String> mockResponseSuccess = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        HashMap<String, Object> map = new HashMap<>();
        try {
            pdpClient.getJsonResponse(map);
        } catch (HttpServerErrorException e) {
            //we are expecting a 5xx exception to be thrown
        }

        //assert that there were exactly 2 attempts
        verify(mockRestTemplate, times(2)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));
    }

    @Test()
    void getJsonResponse_clientError_noRetry() throws Throwable {

        ResponseEntity<String> mockResponseSuccess = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        HashMap<String, Object> map = new HashMap<>();
        try {
            pdpClient.getJsonResponse(map);
        } catch (HttpClientErrorException e) {
            //we are expecting a 4xx exception to be thrown
        }

        //assert that there was exactly 1 attempt
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void getMappedResponse_statusOk_noRetry() throws Throwable {

        ResponseEntity<String> mockResponse = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class))).thenReturn(mockResponse);

        HashMap<String, Object> map = new HashMap<>();
        Map<String, Object> mappedResponse = pdpClient.getMappedResponse(map);

        //assert that there was no retry on a successful attempt
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));

        //assert that the returned JSON node is correct
        Assertions.assertEquals("1", mappedResponse.get("a"));
        Assertions.assertEquals("2", mappedResponse.get("b"));
    }


    @Test()
    void getMappedResponse_serverError_retry() throws Throwable {

        ResponseEntity<String> mockResponseSuccess = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).thenReturn(mockResponseSuccess);

        HashMap<String, Object> map = new HashMap<>();
        Map<String, Object> mappedResponse = pdpClient.getMappedResponse(map);

        //assert that there were exactly 2 attempts
        verify(mockRestTemplate, times(2)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));

        //assert that the returned JSON node is correct
        Assertions.assertEquals("1", mappedResponse.get("a"));
        Assertions.assertEquals("2", mappedResponse.get("b"));
    }

    @Test()
    void mappedResponse_serverError_retriesExhausted() throws Throwable {

        ResponseEntity<String> mockResponseSuccess = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        HashMap<String, Object> map = new HashMap<>();
        Map<String, Object> mappedResponse;
        try {
            mappedResponse = pdpClient.getMappedResponse(map);
        } catch (HttpServerErrorException e) {
            //we are expecting a 5xx exception to be thrown
        }

        //assert that there were exactly 2 attempts
        verify(mockRestTemplate, times(2)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));
    }

    @Test()
    void mappedResponse_clientError_noRetry() throws Throwable {

        ResponseEntity<String> mockResponseSuccess = new ResponseEntity<>("{\"a\":\"1\",\"b\":\"2\"}", HttpStatus.OK);
        when(mockRestTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        HashMap<String, Object> map = new HashMap<>();
        Map<String, Object> mappedResponse;
        try {
            mappedResponse = pdpClient.getMappedResponse(map);
        } catch (HttpClientErrorException e) {
            //we are expecting a 4xx exception to be thrown
        }

        //assert that there was exactly 1 attempt
        verify(mockRestTemplate, times(1)).postForEntity(any(String.class), any(HttpEntity.class), eq(String.class));
    }
}