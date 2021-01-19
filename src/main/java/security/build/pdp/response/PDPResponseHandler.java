package security.build.pdp.response;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PDPResponseHandler {

    public Boolean HandleResponse(Map<String, Object> response) {
        return response.get("result").equals(true);
    }
}
