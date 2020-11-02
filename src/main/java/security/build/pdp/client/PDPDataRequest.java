package security.build.pdp.client;

import java.util.Map;

public class PDPDataRequest {

    Map<String, Object> input;

    public PDPDataRequest(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, Object> getInput() {
        return this.input;
    }

}