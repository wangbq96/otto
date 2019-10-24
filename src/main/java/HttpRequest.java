import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String location;
    private String version;
    private Map<String, String> headers = new HashMap<>();

    public HttpRequest(String method, String location, String version, Map<String, String> headers) {
        this.method = method;
        this.location = location;
        this.version = version;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
