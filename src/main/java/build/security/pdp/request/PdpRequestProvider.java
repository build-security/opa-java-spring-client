package build.security.pdp.request;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class PdpRequestProvider {

    public PdpRequest Provide(HttpServletRequest request, String[] requirements) {
        Map<String, String> headers = new HashMap<String, String>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }

        String method = request.getMethod();
        String path = request.getRequestURI().replaceAll("^/|/$", "");
        PdpRequestIncomingHttp incomingHttp = new PdpRequestIncomingHttp(method, path, headers);
        PdpRequestResources resources = new PdpRequestResources(requirements, new HashMap<String, String>());
        PdpRequestInput input = new PdpRequestInput(incomingHttp, resources, "", "");
        return new PdpRequest(input);
    }
}
