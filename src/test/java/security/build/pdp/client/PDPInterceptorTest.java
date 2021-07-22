package security.build.pdp.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class PDPInterceptorTest {

    private static class TestController {

        @Authorize(resources = {""})
        public void testMethodWithAuthz() {}
        
        public void testMethodWithoutAuthz() {};
    }

    @InjectMocks
    private PDPInterceptor pdpInterceptor;

    @Mock
    private PDPEnforcer pdpEnforcer;

    private HandlerMethod handlerMethodWithAuthz;
    private HandlerMethod handlerMethodWithoutAuthz;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private HttpServletResponse response = new MockHttpServletResponse();

    private String[] requirementsWithAuthz;
    private String[] requirementsWithoutAuthz;

    @BeforeEach
    public void initMocks() throws Throwable {
        MockitoAnnotations.openMocks(this);

        Method methodWithAuthz = TestController.class.getMethod("testMethodWithAuthz");
        Method methodWithoutAuthz = TestController.class.getMethod("testMethodWithoutAuthz");

        TestController controller = new TestController();

        handlerMethodWithAuthz = new HandlerMethod(controller, methodWithAuthz);
        handlerMethodWithoutAuthz = new HandlerMethod(controller, methodWithoutAuthz);

        Authorize annotation = handlerMethodWithAuthz.getMethodAnnotation(Authorize.class);
        requirementsWithAuthz = annotation.resources();

        requirementsWithoutAuthz = new String[0];
    }

    @Test
    void PreHandle_Enabled_NotAuthorized() throws  Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(true);

        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(false, isAuthorized);
    }

    @Test
    void PreHandle_Enabled_Authorized() throws  Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(true);

        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(true);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_Disabled_Authorized() throws Throwable {
        pdpInterceptor.setEnable(false);
        pdpInterceptor.setInterceptAllEndpoints(true);

        Mockito.when(pdpEnforcer.AuthorizeRequest(null, requirementsWithAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(null, response, handlerMethodWithAuthz);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_NotInterceptAllEndpoints_NoAuthorizeAnnotation() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(false);

        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithoutAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithoutAuthz);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_NotInterceptAllEndpoints_WithAuthorizeAnnotation_Authorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(false);

        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(true);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_NotInterceptAllEndpoints_WithAuthorizeAnnotation_NotAuthorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(false);

        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(false, isAuthorized);
    }

    @Test
    void PreHandle_NotInterceptAllEndpoints_IgnoreEndpoints_IOException() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(false);

        IOException thrown = Assertions.assertThrows(
                IOException.class,
                () -> pdpInterceptor.setIgnoreEndpoints(new String[] {"/path1", "/path2"}),
                ""
        );

        Assertions.assertTrue(thrown.getMessage().contains("cannot"));
    }

    @Test
    void PreHandle_NotInterceptAllEndpoints_IgnoreRegex_IOException() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(false);

        IOException thrown = Assertions.assertThrows(
                IOException.class,
                () -> pdpInterceptor.setIgnoreEndpoints(new String[] {"/a+/b+", "/a?b?c"}),
                ""
        );

        Assertions.assertTrue(thrown.getMessage().contains("cannot"));
    }

    @Test
    void PreHandle_InterceptAllEndpoints_IgnoreEndpoints_Authorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(true);
        pdpInterceptor.setIgnoreEndpoints(new String[] {"/path1", "/path2"});

        request.setRequestURI("/path2");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_InterceptAllEndpoints_IgnoreEndpoints_NotAuthorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(true);
        pdpInterceptor.setIgnoreEndpoints(new String[] {"/path1", "/path2"});

        request.setRequestURI("/path3");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(false, isAuthorized);
    }

    @Test
    void PreHandle_InterceptAllEndpoints_IgnoreRegex_Authorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(true);
        pdpInterceptor.setIgnoreRegex(new String[] {"/a+/b+", "/a?b?c"});

        request.setRequestURI("/aaaa/bbbbbbbb");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_InterceptAllEndpoints_IgnoreRegex_NotAuthorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setInterceptAllEndpoints(true);
        pdpInterceptor.setIgnoreEndpoints(new String[] {"/a+/b+", "/a?b?c"});

        request.setRequestURI("/axbxd");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirementsWithAuthz)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethodWithAuthz);
        Assertions.assertEquals(false, isAuthorized);
    }
}
