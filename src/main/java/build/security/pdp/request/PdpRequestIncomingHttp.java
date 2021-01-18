package build.security.pdp.request;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
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

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
