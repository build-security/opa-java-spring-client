package security.build.pdp.request;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class PDPRequestProvider {

    public PDPRequest Provide(HttpServletRequest request, String[] requirements) {
        Map<String, String> headers = new HashMap<String, String>();
        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }

        String method = request.getMethod();
        String path = request.getRequestURI().replaceAll("^/|/$", "");
        PDPRequestIncomingHttp incomingHttp = new PDPRequestIncomingHttp(method, path, headers);
        PDPRequestResources resources = new PDPRequestResources(requirements, new HashMap<String, String>());
        PDPRequestInput input = new PDPRequestInput(incomingHttp, resources, "", "");
        return new PDPRequest(input);
    }
}
