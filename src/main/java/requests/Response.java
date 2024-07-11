package requests;
import java.util.Map;

public class Response {
    private Map<String, String> headers;
    private int statusCode;
    private String body;


    public Response(Map<String, String> headers, int statusCode, String body) {
        this.headers = headers;
        this.statusCode = statusCode;
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String toString() {
        return "Response{" +
                "headers=" + headers + "\n" +
                ", statusCode=" + statusCode + "\n" +
                ", body='" + body + '\'' +
                '}';
    }

}
