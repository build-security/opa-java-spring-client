package security.build.pdp.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import security.build.pdp.request.PDPRequest;
import security.build.pdp.request.PDPRequestProvider;
import security.build.pdp.response.PDPResponseHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class PDPEnforcerTest {
    @InjectMocks
    private PDPEnforcer pdpEnforcer;

    @Mock
    private PDPClient pdpClient;

    @Mock
    private PDPRequestProvider pdpRequestProvider;

    @Spy
    private PDPResponseHandler pdpResponseHandler;

    private HttpServletRequest request = new MockHttpServletRequest();
    private String[] requirements = new String[0];
    private PDPRequest pdpRequest = new PDPRequest();
    private HashMap<String, Object> resultMap = new HashMap<>();

    @BeforeEach
    public void initMocks() throws Throwable {
        MockitoAnnotations.openMocks(this);

        Mockito.when(pdpRequestProvider.Provide(request, requirements)).thenReturn(pdpRequest);
        Mockito.when(pdpClient.getMappedResponse(pdpRequest)).thenReturn(resultMap);

        pdpEnforcer.setAllowOnFailure(false);
    }

    @Test
    void AuthorizeRequest_EmptyResponse_NotAuthorized() {
        Boolean isAuthorized = pdpEnforcer.AuthorizeRequest(request, requirements);
        Assertions.assertEquals(isAuthorized, false);
    }

    @Test
    void AuthorizeRequest_OtherResponse_NotAuthorized() {
        resultMap.put("result", "other");

        Boolean isAuthorized = pdpEnforcer.AuthorizeRequest(request, requirements);
        Assertions.assertEquals(isAuthorized, false);
    }

    @Test
    void AuthorizeRequest_ValidResponse_Authorized() throws Throwable {
        resultMap.put("result", true);

        Boolean isAuthorized = pdpEnforcer.AuthorizeRequest(request, requirements);
        Assertions.assertEquals(isAuthorized, true);
    }

    @Test
    void AuthorizeRequest_ExceptionAllowOnFailure_Authorized() throws Throwable {
        Mockito.when(pdpClient.getMappedResponse(pdpRequest)).thenThrow(new RuntimeException());

        pdpEnforcer.setAllowOnFailure(true);

        Boolean isAuthorized = pdpEnforcer.AuthorizeRequest(request, requirements);
        Assertions.assertEquals(isAuthorized, true);
    }
}
