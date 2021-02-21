package security.build.pdp.request;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class PDPConnectionTuple {
    String ipAddress;
    int port;

    public PDPConnectionTuple(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public PDPConnectionTuple() {
        this.ipAddress = new String();
        this.port = 0;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public int getPort() {
        return this.port;
    }
}
