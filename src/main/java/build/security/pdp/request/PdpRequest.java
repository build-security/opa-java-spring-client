package build.security.pdp.request;

public class PdpRequest {
    PdpRequestInput input;

    public PdpRequest(PdpRequestInput input) {
        this.input = input;
    }

    public PdpRequest() {
        this.input = new PdpRequestInput();
    }
}
