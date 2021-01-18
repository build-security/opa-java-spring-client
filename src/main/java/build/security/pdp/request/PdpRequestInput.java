package build.security.pdp.request;

public class PdpRequestInput {
    PdpRequestIncomingHttp request;
    PdpRequestResources resources;
    String source;
    String destination;

    public PdpRequestInput(PdpRequestIncomingHttp request, PdpRequestResources resources, String source, String destination) {
        this.request = request;
        this.resources = resources;
        this.source = source;
        this.destination = destination;
    }

    public PdpRequestInput() {
        this.request = new PdpRequestIncomingHttp();
        this.resources = new PdpRequestResources();
    }
}