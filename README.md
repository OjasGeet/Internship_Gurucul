# Requests Utility Library
 
## Overview
The `Requests` class is a Java utility for making HTTP requests using the Apache HttpClient library. It provides methods
to perform HTTP GET, POST, and DELETE requests with built-in support for retrying requests on specific HTTP status codes.

## Features
- **HTTP GET:** Perform GET requests to retrieve data from a specified endpoint.
- **HTTP POST:** Send JSON payloads to a specified endpoint.
- **HTTP DELETE:** Delete resources identified by a specified endpoint.

## Configuration
The retry behavior (max retry count and delay) is configurable via the `config.properties` file located in the classpath.

## Dependencies
This project relies on the Apache HttpClient library for handling HTTP requests and responses. Ensure you have the
necessary dependencies configured in your project's build system.

## Data Transfer Object Class
The `Response` class is used as a DTO (Data Transfer Object) to encapsulate HTTP response details.
```java
public Response(Map<String, String> headers, int statusCode, String body) {
        this.headers = headers;
        this.statusCode = statusCode;
        this.body = body;
}
```


## Tests
### Many tests have been configured in `TestCases` class to ensure proper working of the request methods.
The tests included are:
- **GET success:** This test asserts the status code after execution of the request.
- **GET success with Query Parameters:** This asserts the status code after execution of the request.
- **Invalid Endpoint:** This test verifies that the  httpGet method returns  null when the endpoint returns a 500 Internal Server Error.
- **POST success:** This test verifies that a POST request to the placeholder API with a JSON body and headers returns a 201 Created status code.
- **POST success with headers:** This test verifies that the POST request includes the specified headers and returns a 201 Created status code.
- **IO Exception in POST:**  This test verifies that the  httpPost method throws a  RuntimeException with an appropriate error message.
- **Retry request in POST:** This test verifies that the httpPost method correctly handles retry logic when the server responds with a 429 Too Many Requests status code.
- **DELETE success:**  This test verifies that a DELETE request to the placeholder API returns a 200 OK status code.
- **DELETE success with headers:** This test verifies that a DELETE request with headers to the placeholder API returns a 200 OK status code.

## Usage
### Initialization
  ```java
Requests requests = new Requests();

 



