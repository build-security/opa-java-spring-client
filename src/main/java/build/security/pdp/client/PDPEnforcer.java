package build.security.pdp.client;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import build.security.pdp.request.PdpRequest;
import build.security.pdp.request.PdpRequestProvider;

@Component
public class PdpEnforcer {

    @Autowired
    private PdpClient pdpClient;

    @Autowired
    private PdpRequestProvider pdpRequestProvider;

    public Boolean RunAuthorization(HttpServletRequest request, String[] resources) {
        
        PdpRequest input = pdpRequestProvider.Provide(request);

        boolean allowRequest;
        try {
            Map<String, Object> response = pdpClient.getMappedResponse(input);
            allowRequest = response.get("allow").equals(true);
        } catch (Throwable throwable) {
            allowRequest = false;
        }

        return allowRequest;
    }
}
