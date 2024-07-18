
package requests;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A utility class for making HTTP requests using the Apache HttpClient
 * This class provides methods to perform HTTP GET, POST and DELETE requests with
 * built-in support for retrying requests on specific HTTP status codes
 * <p>
 * Configuration for retry behavior is loaded from a properties file named "config.properties"
 * </p>
 * @author Ojas Geet
 */
@SuppressWarnings("CallToPrintStackTrace")
public class Requests {
    private CloseableHttpClient httpClient;
    private int maxRetryCount;
    private long retryDelay;

    private static final Logger logger = Logger.getLogger(Requests.class.getName());

    /**
     * Constructs a new object and loads properties from the "config.properties" file
     * for configuring retry behavior
     */

    public Requests(){
        loadProperties(null);
    }

    /**
     * Constructs a new object and loads properties from the given properties object
     * for configuring retry behavior
     *
     * @param overrideProperties Properties object to override the defaults from the "config.properties" file
     */
    public Requests(Properties overrideProperties){
        loadProperties(overrideProperties);
    }

    /**
     * Loads retry configuration properties from the "config.properties" file.
     * Properties include max retry count and retry delay.
     *
     * @param overrideProperties Properties object to override the defaults from the "config.properties" file
     */
    private void loadProperties(Properties overrideProperties) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                properties.load(input);
                maxRetryCount = Integer.parseInt(properties.getProperty("retry.maxCount", "3"));
                retryDelay = Long.parseLong(properties.getProperty("retry.delay", "10000"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (overrideProperties != null) {
            if (overrideProperties.containsKey("retry.maxCount")) {
                maxRetryCount = Integer.parseInt(overrideProperties.getProperty("retry.maxCount"));
            }
            if (overrideProperties.containsKey("retry.delay")) {
                retryDelay = Long.parseLong(overrideProperties.getProperty("retry.delay"));
            }
        }
    }

    /**
     * Sets the maximum retry count for HTTP requests
     *
     * @param maxRetryCount Maximum retry count
     */
    public void setMaxRetryCount(int maxRetryCount){
        this.maxRetryCount=maxRetryCount;
    }

    /**
     * Sets the retry delay for HTTP requests
     *
     * @param retryDelay Retry delay in milliseconds
     */
    public void setRetryDelay(long retryDelay){
        this.retryDelay=retryDelay;
    }



    /**
     * This method performs an HTTP GET request to te specified endpoint with headers and query parameters.
     * Retries the request if the response status code is 429(Too many requests) or 500 (Internal Server Error)
     *
     *
     * @param endpoint the URL of the resource to be fetched.
     * @param headers  A map of HTTP headers to include in the request.
     * @param queryParams A map of query parameters to include in the request URL.
     * @return A Response object containing the response headers, status code and body.
     * @throws IOException If an I/O error occurs while making a request.
     */
    public Response httpGet(String endpoint, Map<String,String>headers,Map<String,String> queryParams) throws IOException {
        int retryCount=0;
        while(retryCount<=maxRetryCount) {
            HttpClient client = HttpClients.createDefault();
            URIBuilder builder;
            try {
                builder = new URIBuilder(endpoint);
                if (queryParams != null) {
                    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                        builder.setParameter(entry.getKey(), entry.getValue());
                    }
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            URI uri;
            try {
                uri = builder.build();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            HttpGet request = new HttpGet(uri);
            CloseableHttpResponse response = null;
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.setHeader(header.getKey(), header.getValue());
            }
            logger.info("Request Headers:");
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.setHeader(header.getKey(), header.getValue());
                logger.info(header.getKey() + ": " + header.getValue());
            }
            try {
                response = (CloseableHttpResponse) client.execute(request);
                int statuscode = response.getStatusLine().getStatusCode();
                if (response.getStatusLine().getStatusCode() == 429 || response.getStatusLine().getStatusCode() == 500) {
                 logger.info("Received status code " + response.getStatusLine().getStatusCode() + ", retrying after " + retryDelay/ 1000 + " seconds");
                    Thread.sleep(retryDelay);
                    retryCount++;
                    continue;
                }
                Map<String, String> responseHeaders = Arrays.stream(response.getAllHeaders()).collect(Collectors.toMap(org.apache.http.Header::getName, org.apache.http.Header::getValue));

                String responseBody = null;
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    responseBody = EntityUtils.toString(entity);
                }
                return new Response(responseHeaders, statuscode, responseBody);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (response != null) {
                        response.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    /**
     * This method performs an HTTP post to the specified endpoint with a JSON body and headers.
     * Retries the request if the response status code is 429(Too many requests) or 500 (Internal Server Error).
     *
     * @param endpoint The URL of the resource to be posted to.
     * @param jsonbody The JSON string to be sent as the request body.
     * @param headers A map of HTTP headers to be included in the request.
     * @return An object containing the response headers, status code and body.
     * @throws IOException If an I/O error occurs while making the request.
     */
    public Response httpPost(String endpoint, String jsonbody,Map<String, String> headers) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Content-Type", "application/json");

        int retryCount=0;
        while(retryCount<=maxRetryCount) {

            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPost.setHeader(header.getKey(), header.getValue());
            }

            StringEntity entity = new StringEntity(jsonbody);
            httpPost.setEntity(entity);

            HttpResponse httpres = null;
            BufferedReader reader = null;

            try {
                httpres = client.execute(httpPost);
                int statuscode = httpres.getStatusLine().getStatusCode();
                logger.info("Post response status :" + statuscode);
                if (statuscode == 429 ) {
                    logger.info("Received status code " + statuscode+ ", retrying after " + retryDelay / 1000 + " seconds");
                    Thread.sleep(retryDelay);
                    retryCount++;
                    continue;
                } else if (statuscode==500) {
                    throw new RuntimeException("Internal Server Error: HTTP status code 500");

                }


                Map<String, String> responseHeaders = Arrays.stream(httpres.getAllHeaders())
                        .collect(Collectors.toMap(org.apache.http.Header::getName, org.apache.http.Header::getValue));

                String responseBody = null;
                HttpEntity res = httpres.getEntity();
                if (res != null) {
                    responseBody = EntityUtils.toString(res);
                }

                return new Response(responseHeaders, statuscode, responseBody);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (httpres != null) {
                    EntityUtils.consumeQuietly(httpres.getEntity());
                }
            }
        }

        throw new RuntimeException("Max retry attempts reached for POST request");

    }

    /**
     * This method performs an HTTP DELETE request to the specified endpoint with headers.
     * Retries the request if the response status code is 429(Too many requests) or 500 (Internal Server Error).
     *
     * @param endpoint The URL of the resource to be deleted.
     * @param headers A map of HTTP headers to include in the request.
     * @return An object containing the response headers, status code and body.
     */
    public Response httpDelete(String endpoint,Map<String,String> headers) {

        int retryCount = 0;
        while (retryCount <= maxRetryCount) {
            HttpClient client = HttpClients.createDefault();
            int id = 1;
            HttpDelete delete = new HttpDelete(endpoint + id);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                delete.setHeader(header.getKey(), header.getValue());
            }

            try {
                HttpResponse httpResponse = client.execute(delete);
                logger.info("deleted todo with id :" + id);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 429 || statusCode == 500) {
                    logger.info("Received status code " + statusCode+ ", retrying after " + retryDelay / 1000 + " seconds");
                    Thread.sleep(retryDelay);
                    retryCount++;
                    continue;
                }

                Map<String, String> responseHeaders = Arrays.stream(httpResponse.getAllHeaders())
                        .collect(Collectors.toMap(org.apache.http.Header::getName, org.apache.http.Header::getValue));
                String responseBody = null;
                HttpEntity responseEntity = httpResponse.getEntity();
                if (responseEntity != null) {
                    responseBody = EntityUtils.toString(responseEntity);
                }
                return new Response(responseHeaders, statusCode, responseBody);


            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    ((CloseableHttpClient) client).close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;

        }
        return null;
    }

}









