package security.build.pdp.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockHttpServletRequest;

public class PDPRequestProviderTest {
    private static PDPRequestProvider PdpRequestProvider;

    @BeforeAll
    public static void setup() {
        PdpRequestProvider = new PDPRequestProvider();
    }

    @Test
    void Provide_ValidRequest_ContainsHeader() {
        String headerName = "header-name";
        String headerValue = "header-value";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(headerName, headerValue);

        PDPRequest pdpRequest = PdpRequestProvider.Provide(request, new String[0]);
        Assertions.assertEquals(headerValue, pdpRequest.input.request.headers.get(headerName));
    }

    @Test
    void Provide_ForwardedRequest_ContainsOriginalSource() {
        String sourceIp = "some-ip";
        String wrongIp = "other-ip";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(wrongIp);
        request.addHeader("X-Forwarded-For", sourceIp);

        PDPRequest pdpRequest = PdpRequestProvider.Provide(request, new String[0]);
        Assertions.assertEquals(sourceIp, pdpRequest.input.source.get("ipAddress").asText());
    }

    @Test
    void Provide_MultipleForwardedRequest_ContainsSingleSource() {
        String sourceIp = "origin-ip";
        String forwardedHeader = "origin-ip, chain-ip, another-chain-ip";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", forwardedHeader);

        PDPRequest pdpRequest = PdpRequestProvider.Provide(request, new String[0]);
        Assertions.assertEquals(sourceIp, pdpRequest.input.source.get("ipAddress").asText());
    }

    @Test
    void Provide_ValidRequest_ContainsSource() {
        String sourceIp = "some-ip";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(sourceIp);

        PDPRequest pdpRequest = PdpRequestProvider.Provide(request, new String[0]);
        Assertions.assertEquals(sourceIp, pdpRequest.input.source.get("ipAddress").asText());
    }

    @Test
    void Provide_ValidRequest_ContainsDestination() {
        String serverIp = "some-ip";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setLocalAddr(serverIp);

        PDPRequest pdpRequest = PdpRequestProvider.Provide(request, new String[0]);
        Assertions.assertEquals(serverIp, pdpRequest.input.destination);
    }

    @ParameterizedTest
    @CsvSource({"GET", "POST"})
    void Provide_ValidRequest_ContainsHttpMethod(String method) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);

        PDPRequest pdpRequest = PdpRequestProvider.Provide(request, new String[0]);
        Assertions.assertEquals(method, pdpRequest.input.request.method);
    }

    @Test
    void Provide_ValidRequest_ContainsRequirements() {
        String[] resources = {"Hello", "World"};
        MockHttpServletRequest request = new MockHttpServletRequest();

        PDPRequest pdpRequest = PdpRequestProvider.Provide(request, resources);
        Assertions.assertArrayEquals(resources, pdpRequest.input.resources.requirements);
    }
}
