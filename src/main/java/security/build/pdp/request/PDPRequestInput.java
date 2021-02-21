package security.build.pdp.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class PDPRequestInput {
    PDPRequestIncomingHttp request;
    PDPRequestResources resources;
    ObjectNode source;
    String destination;

    public PDPRequestInput(PDPRequestIncomingHttp request, PDPRequestResources resources, String source, String destination) {
        this.request = request;
        this.resources = resources;
        final ObjectMapper mapper = new ObjectMapper();
        this.source = mapper.createObjectNode();
        this.source.put("ipAddress", source);
        this.destination = destination;
    }

    public PDPRequestInput() {
        this.request = new PDPRequestIncomingHttp();
        this.resources = new PDPRequestResources();
    }

    public ObjectNode getSource() {
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