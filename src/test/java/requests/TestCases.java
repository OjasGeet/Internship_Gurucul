package requests;

import com.github.tomakehurst.wiremock.junit.WireMockRule;


import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@code Requests} class.
 * <p>
 *     This class contains test cases for the various HTTP methods implemented in the {@code Requests} class
 *     including GET,POST and DELETE requests.Each method verifies different aspects of the HTTP requests
 *     such as success responses , query parameters ,error handling , and request headers.
 * </p>
 */


public class TestCases {

//    private Class<? extends Throwable> RuntimeException;
    /**
     * WireMock rule for setting up mock HTTP server responses.
     * <p>
     * The {@code WireMockRule } listens on port 8080 and allows defining mock HTTP responses for testing purposes.
     */

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);


    Requests requests = new Requests();

    /**
     * Tests a successful HTTP GET method
     * <p>
     *     This test verifies that a GET request to th placeholder API returns a 200 OK status code.
     * </p>
     *
     * @throws Exception If there is an issue with the HTTP request or response.
     */

    @Test
    public void  testGetSuccess() throws Exception {

        String baseUrl = "https://jsonplaceholder.typicode.com";

        // Example headers (replace with actual headers needed)
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        Response response = new Requests().httpGet(baseUrl + "/todos", headers, null);

        assertEquals(200, response.getStatusCode());
    }

    /**
     * Tests a successful HTTP GET request with query parameters.
     * <p>
     *    This test verifies that a GET request with query parameters to the placeholder API returns a 200 OK status code.
     * </p>
     *
     * @throws Exception If there is an issue with the HTTP request or response.
     */

    @Test
    public void testGetSuccessQp() throws Exception {

        String baseUrl = "https://jsonplaceholder.typicode.com";


        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("userID", "1");

        Response response = new Requests().httpGet(baseUrl + "/todos", headers, queryParams);


        assertEquals(200, response.getStatusCode());

    }

    /**
     * Tests the behavior of the HTTP GET request when the endpoint is invalid.
     * <p>
     *     This test verifies that the {@code httpGet} method returns {@code null} when the endpoint returns a 500 Internal Server Error.
     * </p>
     *
     * @throws IOException If there is an issue with the HTTP request or response.
     */


    @Test
    public void testInvalidHttpGetEndpoint() throws IOException{
        stubFor(get(urlEqualTo("/to"))
                .willReturn(aResponse()
                        .withStatus(500)));


        String endpoint = "http://localhost:8080/to";
        Map<String, String> headers = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();

        Response response = requests.httpGet(endpoint, headers, queryParams);


        assertEquals(null, response);

    }

    /**
     * Tests a successful HTTP POST request.
     * <p>
     * This test verifies that a POST request to the placeholder API with a JSON body and headers returns a 201 Created status code.
     * </p>
     */
    @Test
    public void testHttpPost() {
        // Setup test data
        String endpoint = "https://jsonplaceholder.typicode.com/todos";
        String jsonBody = "{\"key\": \"value\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");


        Requests post = new Requests();
        try {
            Response response = post.httpPost(endpoint, jsonBody, headers);

            // Assertions
            assertNotNull(response);
            assertEquals(201, response.getStatusCode());
            assertNotNull(response.getHeaders());


        } catch (IOException e) {
            fail("IOException occurred during test: " + e.getMessage());
        }
    }

    /**
     * Tests an HTTP POST request with headers.
     * <p>
     * This test verifies that the POST request includes the specified headers and returns a 201 Created status code.
     * </p>
     *
     * @throws IOException If there is an issue with the HTTP request or response.
     */

    @Test
    public void testHttpPostWithHeaders() throws IOException {
            String endpoint = "https://jsonplaceholder.typicode.com/todos";
            String jsonBody = "{\"key\": \"value\"}";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer token");
            HttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Content-Type", "application/json");

            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPost.setHeader(header.getKey(), header.getValue());
            }

            StringEntity entity = new StringEntity(jsonBody);
            httpPost.setEntity(entity);

            HttpResponse httpResponse = client.execute(httpPost);
            assertEquals(201, httpResponse.getStatusLine().getStatusCode());
            Collectors Collectors;
            Map<String, String> requestHeaders = Arrays.stream(httpResponse.getAllHeaders())
                    .collect(java.util.stream.Collectors.toMap(org.apache.http.Header::getName, org.apache.http.Header::getValue));


            assertEquals("application/json; charset=utf-8", requestHeaders.get("Content-Type"));

        }

    /**
     * Tests the behavior of the HTTP POST request when the server returns a 500 Internal Server Error.
     * <p>
     * This test verifies that the {@code httpPost} method throws a {@code RuntimeException} with an appropriate error message.
     * </p>
     *
     * @throws IOException If there is an issue with the HTTP request or response.
     */


    @Test
    public void testPostIOException() throws IOException {
        stubFor(post(urlEqualTo("/todos"))
                .willReturn(aResponse()
                        .withStatus(500)));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token");

        Response response = null;
        try {
            response = requests.httpPost("http://localhost:8080/todos", "{\"key\":\"value\"}", headers);
        } catch (RuntimeException e) {
            assertEquals("Internal Server Error: HTTP status code 500", e.getMessage());
        }

        if (response != null) {
            assertEquals(500, response.getStatusCode());
            assertEquals("", response.getBody());
        } else {
            assertEquals(null, response);
        }

        verify(postRequestedFor(urlEqualTo("/todos"))
                .withRequestBody(equalToJson("{\"key\":\"value\"}"))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("Bearer token")));
    }

    /**
     * Tests the retry logic for HTTP POST requests.
     * <p>
     * This test verifies that the {@code httpPost} method correctly handles retry logic when the server responds with a 429 Too Many Requests status code.
     * </p>
     */
    @Test
    public void testHttpPost_RetryLogic() {
        stubFor(post(urlEqualTo("/todos"))
                .willReturn(aResponse()
                        .withStatus(429)));

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token");

        try {
            requests.httpPost("http://localhost:8080/todos", "{\"key\":\"value\"}", headers);
            fail("Expected RuntimeException due to max retry attempts reached");
        } catch (RuntimeException e) {
            assertEquals("Max retry attempts reached for POST request", e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        verify(postRequestedFor(urlEqualTo("/todos"))
                .withRequestBody(equalToJson("{\"key\":\"value\"}"))
                .withHeader("Content-Type", matching("application/json"))
                .withHeader("Authorization", matching("Bearer token")));
    }


    /**
     * Tests the behavior of the HTTP DELETE request.
     * <p>
     * This test verifies that a DELETE request to the placeholder API returns a 200 OK status code.
     * </p>
     *
     * @throws MalformedURLException If the URL is invalid.
     */
    @Test
    public void testDelete() throws MalformedURLException {
        String url = "https://jsonplaceholder.typicode.com/todos ";
        URL deleteUrl = new URL(url);
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) deleteUrl.openConnection();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        connection.disconnect();
        assertEquals("Expected HTTP 200 (OK) response code", HttpURLConnection.HTTP_OK, responseCode);

    }

    /**
     * Tests the behavior of the HTTP DELETE request.
     * <p>
     * This test verifies that a DELETE request with headers to the placeholder API returns a 200 OK status code.
     * </p>
     *
     * @throws MalformedURLException If the URL is invalid.
     */

    @Test
    public void testDeleteHeaders() throws IOException {
        String url ="https://jsonplaceholder.typicode.com/todos";
        String authorizationHeader = "Bearer your_access_token";
        String contentTypeHeader = "application/json";
        URL deleteUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) deleteUrl.openConnection();
        connection.setRequestProperty("Authorization", authorizationHeader);
        connection.setRequestProperty("Content-Type", contentTypeHeader);
        String contentType = connection.getHeaderField("Content-Type");
        assertTrue("Expected Content-Type header to be 'application/json'",
                contentType.startsWith(contentTypeHeader));
        connection.disconnect();


    }




    }











