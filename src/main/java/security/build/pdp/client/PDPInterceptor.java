package security.build.pdp.client;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class PDPInterceptor extends HandlerInterceptorAdapter  {
    
    @Autowired
    private PDPEnforcer pdpEnforcer;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HandlerMethod method = (HandlerMethod) handler;
        Class<Authorize> authorizeClass = Authorize.class;
        Authorize annotation = method.getMethodAnnotation(authorizeClass);
        String[] requirements = annotation != null ? annotation.resources() : new String[0];

        Boolean result = pdpEnforcer.AuthorizeRequest(request, requirements);
        if (result) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        return false;
    }
}
