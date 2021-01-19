package security.build.pdp.client;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import security.build.pdp.request.PDPRequest;
import security.build.pdp.request.PDPRequestProvider;
import security.build.pdp.response.PDPResponseHandler;

@Component
public class PDPEnforcer {

    @Autowired
    private PDPRequestProvider pdpRequestProvider;

    @Autowired
    private PDPClient pdpClient;

    @Autowired
    private PDPResponseHandler pdpResponseHandler;

    public Boolean RunAuthorization(HttpServletRequest request, String[] requirements) {
        
        PDPRequest input = pdpRequestProvider.Provide(request, requirements);

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
