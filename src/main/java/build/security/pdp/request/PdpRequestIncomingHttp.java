package build.security.pdp.request;

import java.util.HashMap;
import java.util.Map;

public class PdpRequestIncomingHttp {
    String method;
    String path;
    Map<String, String> headers;

    public PdpRequestIncomingHttp(String method, String path, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public PdpRequestIncomingHttp() {
        this.headers = new HashMap<String, String>();
    }
}
