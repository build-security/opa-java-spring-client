package security.build;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import security.build.pdp.client.PDPClient;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {

    @Autowired
    private PDPClient pdpClient;

    //calling this endpoint will go through the web-security filter
    @RequestMapping("/websecurity")
    public String webSecurityExample() {
        //In this case, the OPAVoter will be called and the response will be simply ACCESS_DENIED or ACCESS_GRANTED
        //in other words, no addition response is included.
        return "passed authorization by web-security filter check";
    }

    //calling this endpoint does not go through the web-security filter
    @RequestMapping("/sdk")
    public String sdkExample(HttpServletRequest request) throws Exception {
        //In this case, we will use the SDK to query the PDP and we will return its result

        Map<String, String> headers = new HashMap<String, String>();

        for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }

        String[] path = request.getRequestURI().replaceAll("^/|/$", "").split("/");

        //define the input for evaluation
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("method", request.getMethod());
        input.put("path", path);
        input.put("headers", headers);
        input.put("pod_group", "pod_developers");
        input.put("environment", "staging");
        input.put("requested_role", "keti_read");

        JsonNode node = null;
        try {
            node = pdpClient.getJsonResponse(input);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return node.toPrettyString();
    }

}