package build.security.pdp.request;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class PdpRequestProvider {

    public PdpRequest Provide(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<String, String>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }

        String method = request.getMethod();
        String path = request.getRequestURI().replaceAll("^/|/$", "");
        PdpRequestIncomingHttp incomingHttp = new PdpRequestIncomingHttp(method, path);
        return new PdpRequest(new PdpRequestInput(incomingHttp , "", ""));
    }
}
