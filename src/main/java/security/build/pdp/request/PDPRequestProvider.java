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
        String sourceIp = GetSourceIp(request);
        String destIp = request.getLocalAddr();
        PDPRequestIncomingHttp incomingHttp = new PDPRequestIncomingHttp(method, path, headers);
        PDPRequestResources resources = new PDPRequestResources(requirements, new HashMap<String, String>());
        PDPRequestInput input = new PDPRequestInput(incomingHttp, resources, sourceIp, destIp);
        return new PDPRequest(input);
    }

    private String GetSourceIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");  
        if (ipAddress == null) {  
            return request.getRemoteAddr();
        }

        String[] parts = ipAddress.split(",");
        return parts[0];
    }
}
