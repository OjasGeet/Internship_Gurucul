package requests;
import java.util.Map;

/**
 * This class represents a Http response containing headers,status code and body.
 * <p>
 * This Data Transfer Object class is used to encapsulate the details of an HTTP response received from the server
 * It provides methods to retrieve ans set the response headers,status codes and body.
 * </p>
 */

public class Response {
    private Map<String, String> headers;
    private int statusCode;
    private String body;

    /**
     * Constructs a new object with specified headers, status codes and body.
     * @param headers A map of HTTP headers included in the response.
     * @param statusCode The Http status code of the response.
     * @param body The body of the response as a string.
     */


    public Response(Map<String, String> headers, int statusCode, String body) {
        this.headers = headers;
        this.statusCode = statusCode;
        this.body = body;
    }
    /**
     * Returns the headers of the response.
     *
     * @return A map of HTTP headers included in the response.
     */

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Sets the headers of the response.
     *
     * @param headers A map of HTTP headers to set for the response.
     */

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Returns the HTTP status code of the response.
     *
     * @return The HTTP status code of the response.
     */

    public int getStatusCode() {
        return statusCode;
    }
    /**
     * Sets the HTTP status code of the response.
     *
     * @param statusCode The HTTP status code to set for the response.
     */

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    /**
     * Returns the body of the response.
     *
     * @return The body of the response as a string.
     */
    public String getBody() {
        return body;
    }
    /**
     * Sets the body of the response.
     *
     * @param body The body of the response to set.
     */

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Returns a string representation of the object.
     * @return A String representation of the object
     */
    @Override
    public String toString() {
        return "Response{" +
                "headers=" + headers + "\n" +
                ", statusCode=" + statusCode + "\n" +
                ", body='" + body + '\'' +
                '}';
    }

}
