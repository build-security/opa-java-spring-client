package security.build;

import security.build.pdp.client.Authorize;
import security.build.pdp.client.PDPClient;
import security.build.pdp.request.PDPRequest;
import security.build.pdp.request.PDPRequestProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;

@RestController
public class MainController {

    @Autowired
    private PDPClient pdpClient;

    @Autowired
    private PDPRequestProvider pdpRequestProvider;

    //calling this endpoint will go through the web-security filter
    @Authorize(resources = {"demo.edit"})
    @RequestMapping("/edit")
    public String webSecurityExample(HttpServletRequest request) {
        //In this case, the OPAVoter will be called and the response will be simply ACCESS_DENIED or ACCESS_GRANTED
        //in other words, no addition response is included.
        return "passed authorization by web-security filter check";
    }

    //calling this endpoint will go through the web-security filter
    @Authorize(resources = {"demo.view"})
    @RequestMapping("/view")
    public String webSecurityExample2(HttpServletRequest request) {
        //In this case, the OPAVoter will be called and the response will be simply ACCESS_DENIED or ACCESS_GRANTED
        //in other words, no addition response is included.
        return "passed authorization by web-security filter check";
    }

    //calling this endpoint does not go through the web-security filter
    @Authorize(resources = {"sdk.view"})
    @RequestMapping("/sdk")
    public String sdkExample(HttpServletRequest request) throws Exception {
        PDPRequest pdpRequest = pdpRequestProvider.Provide(request, new String[] {"sdk.view"});

        JsonNode node = null;
        try {
            node = pdpClient.getJsonResponse(pdpRequest);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return node.toPrettyString();
    }

}
