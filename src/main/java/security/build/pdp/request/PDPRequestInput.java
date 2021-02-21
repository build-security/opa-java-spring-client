package security.build.pdp.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class PDPRequestInput {
    PDPRequestIncomingHttp request;
    PDPRequestResources resources;
    PDPConnectionTuple source;
    PDPConnectionTuple destination;

    public PDPRequestInput(PDPRequestIncomingHttp request, PDPRequestResources resources, String source, String destination) {
        this.request = request;
        this.resources = resources;
        this.source = new PDPConnectionTuple(source, 0)
        this.destination = new PDPConnectionTuple(destination, 0);
    }

    public PDPRequestInput() {
        this.request = new PDPRequestIncomingHttp();
        this.resources = new PDPRequestResources();
    }

    public PDPConnectionTuple getSource() {
        return source;
    }

    public PDPConnectionTuple getDestination() {
        return destination;
    }

    public PDPRequestResources getResources() {
        return resources;
    }

    public PDPRequestIncomingHttp getRequest() {
        return request;
    }
}