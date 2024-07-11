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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("CallToPrintStackTrace")
public class Requests {
    private CloseableHttpClient httpClient;


    private static final Logger logger = Logger.getLogger(Requests.class.getName());
    public Response httpGet(String endpoint, Map<String,String>headers,Map<String,String> queryParams) throws IOException {
        int max_tries=10;
        int delay =10*1000;
        int retryCount=0;
        while(retryCount<=max_tries) {
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
                 logger.info("Received status code " + response.getStatusLine().getStatusCode() + ", retrying after " + delay / 1000 + " seconds");
                    Thread.sleep(delay);
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


    public Response httpPost(String endpoint, String jsonbody,Map<String, String> headers) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(endpoint);
        httpPost.setHeader("Content-Type", "application/json");
        int max_tries=10;
        int delay =10*1000;
        int retryCount=0;
        while(retryCount<=max_tries) {

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
                if (statuscode == 429 || statuscode == 500) {
                    logger.info("Received status code " + statuscode+ ", retrying after " + delay / 1000 + " seconds");
                    Thread.sleep(delay);
                    retryCount++;
                    continue;
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


        return null;
    }


    public Response httpDelete(String endpoint,Map<String,String> headers) {
        int max_tries = 10;
        int delay = 10 * 1000;
        int retryCount = 0;
        while (retryCount <= max_tries) {
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
                    logger.info("Received status code " + statusCode+ ", retrying after " + delay / 1000 + " seconds");
                    Thread.sleep(delay);
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









/*
public Response httpPost(String endpoint, String jsonbody,Map<String, String> headers) throws IOException {
    HttpClient client = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(endpoint);
    httpPost.setHeader("Content-Type", "application/json");
    int max_tries=10;
    int delay =10*1000;
    int retryCount=0;

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


        Map<String, String> responseHeaders = Arrays.stream(httpres.getAllHeaders())
                .collect(Collectors.toMap(org.apache.http.Header::getName, org.apache.http.Header::getValue));

        String responseBody = null;
        HttpEntity res = httpres.getEntity();
        if (res != null) {
            responseBody = EntityUtils.toString(res);
        }

        return new Response(responseHeaders, statuscode, responseBody);
    } catch (IOException e) {
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
*/
