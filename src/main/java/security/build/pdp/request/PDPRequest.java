package security.build.pdp.request;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class PDPRequest {
    PDPRequestInput input;

    public PDPRequest(PDPRequestInput input) {
        this.input = input;
    }

    public PDPRequest() {
        this.input = new PDPRequestInput();
    }

    public PDPRequestInput getInput() {
        return input;
    }
}
