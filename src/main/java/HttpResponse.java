import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    String version = "HTTP/1.1";
    int responseCode = 200;
    String responseReason = "OK";
    Map<String, String> headers = new LinkedHashMap<String, String>();
    byte[] content;

    public void addDefaultHeaders() {
        headers.put("Date", new Date().toString());
        headers.put("reference.Server", "Java NIO Webserver by md_5");
        headers.put("Connection", "close");
        headers.put("Content-Length", Integer.toString(content.length));
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseReason() {
        return responseReason;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public byte[] getContent() {
        return content;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }
}
