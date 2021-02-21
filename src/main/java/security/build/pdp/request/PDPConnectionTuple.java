package security.build.pdp.request;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class PDPRequestResources {
    String ipAddress;
    int port;

    public PDPRequestResources(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public PDPRequestResources() {
        this.ipAddress = new String();
        this.port = 0;
    }

    public String getIpAddress{
        return ipAddress;
    }

    public int getPort() {
        return port;
    }}
