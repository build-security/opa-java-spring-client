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
        System.out.println("amiramir method: " + method.toString());
        Class<AuthorizeAnnotation> authorizeAnnotationClass = AuthorizeAnnotation.class;
        System.out.println("amiramir annotation class: " + authorizeAnnotationClass.toString());
        AuthorizeAnnotation annotation = method.getMethodAnnotation(authorizeAnnotationClass);
        System.out.println("amiramir annotation instance: " + annotation.toString());
        String[] requirements = annotation != null ? annotation.resources() : new String[0];
        System.out.println("amiramir annotation requirements: " + requirements.length);

        Boolean result = pdpEnforcer.AuthorizeRequest(request, requirements);
        System.out.println("amiramir authz result: " + result);
        if (result) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        return false;
    }
}
