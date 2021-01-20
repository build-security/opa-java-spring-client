package security.build.pdp.request;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class PDPRequestInput {
    PDPRequestIncomingHttp request;
    PDPRequestResources resources;
    String source;
    String destination;

    public PDPRequestInput(PDPRequestIncomingHttp request, PDPRequestResources resources, String source, String destination) {
        this.request = request;
        this.resources = resources;
        this.source = source;
        this.destination = destination;
    }

    public PDPRequestInput() {
        this.request = new PDPRequestIncomingHttp();
        this.resources = new PDPRequestResources();
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public PDPRequestResources getResources() {
        return resources;
    }

    public PDPRequestIncomingHttp getRequest() {
        return request;
    }
}