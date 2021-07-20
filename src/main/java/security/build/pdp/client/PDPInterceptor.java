package security.build.pdp.client;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class PDPInterceptor extends HandlerInterceptorAdapter  {

    @Value("${pdp.enable:true}")
    private boolean enable;
    @Value("${pdp.ignoreEndpoints:}")
    private String[] ignoreEndpoints = new String[0];
    @Value("${pdp.ignoreRegex:}")
    private String[] ignoreRegex = new String[0];
    
    @Autowired
    private PDPEnforcer pdpEnforcer;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
	    if (!enable) {
	        return true;
        }

	    for (String e: ignoreEndpoints) {
	        if (e == request.getRequestURI()) {
	            return true;
            }
        }

	    for (String r: ignoreRegex) {
	        if (Pattern.compile(r).matcher(request.getRequestURI()).matches()) {
	            return true;
            }
        }

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

    public boolean getEnable() {
	    return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setIgnoreEndpoints(String[] ignoreEndpoints) {
	    this.ignoreEndpoints = ignoreEndpoints;
    }

    public void setIgnoreRegex(String[] ignoreRegex) {
	    this.ignoreRegex = ignoreRegex;
    }
}
