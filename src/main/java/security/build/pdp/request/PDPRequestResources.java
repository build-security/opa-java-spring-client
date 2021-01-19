package security.build.pdp.request;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class PDPRequestResources {
    String[] requirements;
    Map<String, String> attributes;

    public PDPRequestResources(String[] requirements, Map<String, String> attributes) {
        this.requirements = requirements;
        this.attributes = attributes;
    }

    public PDPRequestResources() {
        this.requirements = new String[0];
        this.attributes = new HashMap<String, String>();
    }

    public Map<String, String> getAttrubutes() {
        return attributes;
    }

    public String[] getRequirements() {
        return requirements;
    }
}
