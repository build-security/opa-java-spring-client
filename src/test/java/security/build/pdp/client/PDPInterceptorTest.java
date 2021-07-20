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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class PDPInterceptorTest {

    private static class TestController {

        @Authorize(resources = {""})
        public void testMethod() {}
    }

    @InjectMocks
    private PDPInterceptor pdpInterceptor;

    @Mock
    private PDPEnforcer pdpEnforcer;

    private HandlerMethod handlerMethod;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private HttpServletResponse response = new MockHttpServletResponse();

    private String[] requirements;

    @BeforeEach
    public void initMocks() throws Throwable {
        MockitoAnnotations.openMocks(this);

        Method method = TestController.class.getMethod("testMethod");
        TestController controller = new TestController();
        handlerMethod = new HandlerMethod(controller, method);

        Authorize annotation = handlerMethod.getMethodAnnotation(Authorize.class);
        requirements = annotation != null ? annotation.resources() : new String[0];
    }

    @Test
    void PreHandle_Enabled_NotAuthorized() throws  Throwable {
        pdpInterceptor.setEnable(true);

        Mockito.when(pdpEnforcer.AuthorizeRequest(null, requirements)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(null, response, handlerMethod);
        Assertions.assertEquals(false, isAuthorized);
    }

    @Test
    void PreHandle_Enabled_Authorized() throws  Throwable {
        pdpInterceptor.setEnable(true);

        Mockito.when(pdpEnforcer.AuthorizeRequest(null, requirements)).thenReturn(true);

        Boolean isAuthorized = pdpInterceptor.preHandle(null, response, handlerMethod);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_Disabled_Authorized() throws Throwable {
        pdpInterceptor.setEnable(false);

        Mockito.when(pdpEnforcer.AuthorizeRequest(null, requirements)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(null, response, handlerMethod);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_IgnoreEndpoints_Authorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setIgnoreEndpoints(new String[] {"/path1", "/path2"});

        request.setRequestURI("/path2");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirements)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethod);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_IgnoreEndpoints_NotAuthorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setIgnoreEndpoints(new String[] {"/path1", "/path2"});

        request.setRequestURI("/path3");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirements)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethod);
        Assertions.assertEquals(false, isAuthorized);
    }

    @Test
    void PreHandle_IgnoreRegex_Authorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setIgnoreRegex(new String[] {"/a+/b+", "/a?b?c"});

        request.setRequestURI("/aaaa/bbbbbbbb");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirements)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethod);
        Assertions.assertEquals(true, isAuthorized);
    }

    @Test
    void PreHandle_IgnoreRegex_NotAuthorized() throws Throwable {
        pdpInterceptor.setEnable(true);
        pdpInterceptor.setIgnoreEndpoints(new String[] {"/a+/b+", "/a?b?c"});

        request.setRequestURI("/axbxd");
        Mockito.when(pdpEnforcer.AuthorizeRequest(request, requirements)).thenReturn(false);

        Boolean isAuthorized = pdpInterceptor.preHandle(request, response, handlerMethod);
        Assertions.assertEquals(false, isAuthorized);
    }
}
