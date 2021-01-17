package build.security.pdp.client;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class PdpInterceptor extends HandlerInterceptorAdapter  {
    
    @Autowired
    private PdpEnforcer pdpEnforcer;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        HandlerMethod method = (HandlerMethod) handler;

        String[] resources = new String[0];
        Boolean result = pdpEnforcer.RunAuthorization(request, resources);
        if (result) {
            return true;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        return false;
    }
}
