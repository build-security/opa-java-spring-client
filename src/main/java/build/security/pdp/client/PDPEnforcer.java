package build.security.pdp.client;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import build.security.pdp.request.PdpRequest;
import build.security.pdp.request.PdpRequestProvider;
import build.security.pdp.response.PdpResponseHandler;

@Component
public class PdpEnforcer {

    @Autowired
    private PdpRequestProvider pdpRequestProvider;

    @Autowired
    private PdpClient pdpClient;

    @Autowired
    private PdpResponseHandler pdpResponseHandler;

    public Boolean RunAuthorization(HttpServletRequest request, String[] requirements) {
        
        PdpRequest input = pdpRequestProvider.Provide(request, requirements);

        boolean allowRequest;
        
        try {
            Map<String, Object> response = pdpClient.getMappedResponse(input);
            allowRequest = pdpResponseHandler.HandleResponse(response);
            
        } catch (Throwable throwable) {
            allowRequest = false;
        }

        return allowRequest;
    }
}
