package requests;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Assert;
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





public class TestCases {

    private Class<? extends Throwable> RuntimeException;


    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);


    Requests requests = new Requests();

    private Assert JSONAssert;



    @Test
    public void  testGetSuccess() throws Exception {

        String baseUrl = "https://jsonplaceholder.typicode.com";

        // Example headers (replace with actual headers needed)
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        Response response = new Requests().httpGet(baseUrl + "/todos", headers, null);

        assertEquals(200, response.getStatusCode());
    }

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


    @Test
    public void testPostIOException() throws IOException{
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
    @Test
    public void testHttpPost_RetryLogic() throws IOException {
          stubFor(post(urlEqualTo("/todos"))
                  .willReturn(aResponse()
                          .withStatus(429)));

          Map<String, String> headers = new HashMap<>();
          headers.put("Content-Type", "application/json");
          headers.put("Authorization", "Bearer token");


          Response response = null;
          try {
              response = requests.httpPost("http://localhost:8080/todos", "{\"key\":\"value\"}", headers);
          } catch (RuntimeException e) {

              assertEquals("Internal Server Error: HTTP status code 429", e.getMessage());
          }

          if (response != null) {
              assertEquals(429, response.getStatusCode());
              assertEquals("", response.getBody());
          } else {
              assertEquals(null, response);
          }

          verify(postRequestedFor(urlEqualTo("/todos"))
                  .withRequestBody(equalToJson("{\"key\":\"value\"}"))
                  .withHeader("Content-Type", matching("application/json"))
                  .withHeader("Authorization", matching("Bearer token")));
    }

//    @Test
//    public void testHttpPost_RetryLogic() throws IOException {
//
//        wireMockRule.stubFor(post(urlEqualTo("/todos"))
//                .willReturn(aResponse()
//                        .withStatus(429)));
//
//
//        wireMockRule.stubFor(post(urlEqualTo("/todos-error"))
//                .willReturn(aResponse()
//                        .withStatus(500)));
//
//
//        String url429 = "http://localhost:8080/todos";
//        String url500 = "http://localhost:8080/todos-error";
//        String requestBody = "{\"key\":\"value\"}";
//
//        Response response429 = requests.httpPost(url429, requestBody, new HashMap<>());
//        Response response500 = requests.httpPost(url500, requestBody, new HashMap<>());
//
//
//        assertEquals(429, response429.getStatusCode());
//        assertEquals(500, response500.getStatusCode());
//
//
//        wireMockRule.verify(postRequestedFor(urlEqualTo("/todos")));
//        wireMockRule.verify(postRequestedFor(urlEqualTo("/todos-error")));
//    }

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











