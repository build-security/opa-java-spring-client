package build.security.pdp.client;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class PdpInterceptor extends HandlerInterceptorAdapter  {
    
    @Autowired
    private PdpEnforcer pdpEnforcer;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        System.out.println("amiramir string: " + handler.toString());
        System.out.println("amiramir class: " + handler.getClass());
        HandlerMethod method = (HandlerMethod) handler;
        AuthorizeAnnotation annotation = method.getMethodAnnotation(AuthorizeAnnotation.class);
        Boolean present = method.getBeanType().isAnnotationPresent(AuthorizeAnnotation.class);
        Boolean present2 = method.getBeanType().isAnnotationPresent(RequestMapping.class);
        System.out.println("amiramir present: " + present);
        System.out.println("amiramir present2: " + present2);
        RequestMapping annotation2 = method.getMethodAnnotation(RequestMapping.class);

        System.out.println("amiramir annotation: " + annotation);
        System.out.println("amiramir annotation2: " + annotation2);

        String[] resources = annotation == null ? new String[0] : annotation.resources();

        Boolean result = pdpEnforcer.RunAuthorization(request, resources);
        if (result) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        return false;
    }
}
