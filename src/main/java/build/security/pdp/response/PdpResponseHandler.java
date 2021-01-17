package build.security.pdp.response;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PdpResponseHandler {

    public Boolean HandleResponse(Map<String, Object> response) {
        return response.get("result").equals(true);
    }
}
