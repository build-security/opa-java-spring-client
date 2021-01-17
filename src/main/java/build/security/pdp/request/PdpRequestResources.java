package build.security.pdp.request;

import java.util.HashMap;
import java.util.Map;

public class PdpRequestResources {
    String[] requirements;
    Map<String, String> attributes;

    public PdpRequestResources(String[] requirements, Map<String, String> attributes) {
        this.requirements = requirements;
        this.attributes = attributes;
    }

    public PdpRequestResources() {
        this.requirements = new String[0];
        this.attributes = new HashMap<String, String>();
    }
}
