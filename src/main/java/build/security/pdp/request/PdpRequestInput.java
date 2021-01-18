package build.security.pdp.request;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
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

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public PdpRequestResources getResources() {
        return resources;
    }

    public PdpRequestIncomingHttp getRequest() {
        return request;
    }
}