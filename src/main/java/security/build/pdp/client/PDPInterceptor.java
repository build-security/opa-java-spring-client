package security.build.pdp.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class PDPInterceptor extends HandlerInterceptorAdapter  {

    @Value("${pdp.enable:true}")
    private boolean enable;
    @Value("${pdp.interceptAllEndpoints:true}")
    private boolean interceptAllEndpoints;
    @Value("${pdp.ignoreEndpoints:}")
    private String[] ignoreEndpoints = new String[0];
    @Value("${pdp.ignoreRegex:}")
    private String[] ignoreRegex = new String[0];
    
    @Autowired
    private PDPEnforcer pdpEnforcer;

    private Set<String> ignoreEndpointsSet;
    private Pattern[] ignoreRegexCompiled;

    public PDPInterceptor() throws IOException {
        super();

        checkConfigurationIntegrity();

        updateIgnoreEndpointsSet();
        updateIgnoreRegexCompiled();
    }

    public void updateIgnoreEndpointsSet() {
        ignoreEndpointsSet = new HashSet<String>();

        for (String e: ignoreEndpoints) {
            ignoreEndpointsSet.add(e);
        }
    }

    public void updateIgnoreRegexCompiled() {
        ignoreRegexCompiled = new Pattern[ignoreRegex.length];

        for (int i = 0; i < ignoreRegex.length; i++) {
            ignoreRegexCompiled[i] = Pattern.compile(ignoreRegex[i]);
        }
    }

    public void checkConfigurationIntegrity() throws IOException {
        if (!interceptAllEndpoints) {
            if ((ignoreEndpoints != null) && (ignoreEndpoints.length > 0)) {
                throw new IOException("cannot define pdp.ignoreEndpoints when pdp.interceptAllEndpoints is false");
            }

            if ((ignoreRegex != null) && (ignoreRegex.length > 0)) {
                throw new IOException("cannot define pdp.ignoreRegex when pdp.interceptAllEndpoints is false");
            }
        }
    }

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
	    if (!enable) {
	        return true;
        }

	    if (interceptAllEndpoints) {
            if (ignoreEndpointsSet.contains(request.getRequestURI())) {
                return true;
            }

            for (Pattern p : ignoreRegexCompiled) {
                if (p.matcher(request.getRequestURI()).matches()) {
                    return true;
                }
            }
        }

        HandlerMethod method = (HandlerMethod) handler;
        Class<Authorize> authorizeClass = Authorize.class;
        Authorize annotation = method.getMethodAnnotation(authorizeClass);

        String[] requirements;

        if (annotation == null) {
            if (!interceptAllEndpoints) {
                return true;
            } else {
                requirements = new String[0];
            }
        } else {
            requirements = annotation.resources();
        }

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

    public boolean getInterceptAllEndpoints() {
        return this.interceptAllEndpoints;
    }

    public String[] getIgnoreEndpoints() {
        return this.ignoreEndpoints;
    }

    public String[] getIgnoreRegex() {
        return this.ignoreRegex;
    }

    public void setEnable(boolean enable) throws IOException {
        boolean oldValue = this.enable;
        this.enable = enable;

        try {
            checkConfigurationIntegrity();
        } catch (Exception e) {
            this.enable = oldValue;
            throw e;
        }
    }

    public void setInterceptAllEndpoints(boolean interceptAllEndpoints) throws IOException {
        boolean oldValue = this.interceptAllEndpoints;
	    this.interceptAllEndpoints = interceptAllEndpoints;

        try {
            checkConfigurationIntegrity();
        } catch (Exception e) {
            this.interceptAllEndpoints = oldValue;
            throw e;
        }
    }

    public void setIgnoreEndpoints(String[] ignoreEndpoints) throws IOException {
        String[] oldValue = this.ignoreEndpoints;
	    this.ignoreEndpoints = ignoreEndpoints;

        try {
            checkConfigurationIntegrity();
            updateIgnoreEndpointsSet();
        } catch (Exception e) {
            this.ignoreEndpoints = oldValue;
            throw e;
        }
    }

    public void setIgnoreRegex(String[] ignoreRegex) throws IOException {
        String[] oldValue = this.ignoreRegex;
	    this.ignoreRegex = ignoreRegex;

        try {
            checkConfigurationIntegrity();
            updateIgnoreRegexCompiled();
        } catch (Exception e) {
            this.ignoreRegex = oldValue;
            throw e;
        }
    }
}
