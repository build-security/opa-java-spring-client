package build.security.pdp.request;

public class PdpRequestInput {
    PdpRequestIncomingHttp request;
    String source;
    String destination;

    public PdpRequestInput(PdpRequestIncomingHttp request, String source, String destination) {
        this.request = request;
        this.source = source;
        this.destination = destination;
    }

    public PdpRequestInput() {
        this.request = new PdpRequestIncomingHttp();
    }
}