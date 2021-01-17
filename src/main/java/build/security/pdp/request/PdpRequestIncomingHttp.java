package build.security.pdp.request;

public class PdpRequestIncomingHttp {
    String method;
    String path;

    public PdpRequestIncomingHttp(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public PdpRequestIncomingHttp() {
    }
}
