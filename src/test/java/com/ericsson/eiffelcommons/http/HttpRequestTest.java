package com.ericsson.eiffelcommons.httptest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Test;

import com.ericsson.eiffelcommons.http.HttpExecutor;
import com.ericsson.eiffelcommons.http.HttpRequest;
import com.ericsson.eiffelcommons.http.HttpRequest.HttpMethod;

public class HttpRequestTest {
    private static final String URL_1 = "http://some0thing.com";
    private static final String URL_2 = "http://something.com/";
    private static final String URL_BAD_PROTOCOL = "httpl://something.com/";
    private static final String URL_BAD_SYNTAX = "http:<<something.com/";
    private static final String ENDPOINT_1 = "/testing/test/";
    private static final String ENDPOINT_2 = "testing/test/";
    private static final String HEADER_KEY_1 = "Cache-Control";
    private static final String HEADER_VALUE_1 = "no-cache";
    private static final String HEADER_KEY_2 = "Authorization";
    private static final String HEADER_VALUE_2 = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==";
    private static final String PARAMETER_KEY_1 = "subscriptionName";
    private static final String PARAMETER_VALUE_1 = "myTestSubscription";
    private static final String PARAMETER_KEY_2 = "otherKey";
    private static final String PARAMETER_VALUE_2 = "otherValue";
    private static final String BODY_CONTENT = "{\"subscriptionName\":\"myTestSubscription\"}";
    private static final String BODY_CONTENT_FILE = "src/test/resources/body-content.json";
    private static final String BODY_CONTENT_FILE_INVALID = "src/test/resources/none.json";
    private static final String BODY_HEADER = "Content-Type: application/json";
    private static final String BODY_HEADER_DEFAULT = "Content-Type: text/plain";

    private static final String EXPECTED_URI = "http://something.com/testing/test/";
    private static final String EXPECTED_HEADER = "Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=";

    @Test
    public void testBuildingOfURI() throws Exception {
        HttpRequest request = new HttpRequest(HttpMethod.POST);
        request.setBaseUrl(URL_2);
        request.setEndpoint(ENDPOINT_1);
        URIBuilder builder = (URIBuilder) MethodUtils.invokeMethod(request, true, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());

        request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_2);
        request.setEndpoint(ENDPOINT_1);
        builder = (URIBuilder) MethodUtils.invokeMethod(request, true, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());

        request = new HttpRequest(HttpMethod.DELETE);
        request.setBaseUrl(URL_2);
        request.setEndpoint(ENDPOINT_2);
        builder = (URIBuilder) MethodUtils.invokeMethod(request, true, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());

        request = new HttpRequest(HttpMethod.PUT);
        request.setBaseUrl(URL_2);
        request.setEndpoint(ENDPOINT_2);
        builder = (URIBuilder) MethodUtils.invokeMethod(request, true, "createURIBuilder");
        assertEquals(EXPECTED_URI, builder.toString());
    }

    @Test
    public void testAuthHeader() throws UnsupportedEncodingException, IllegalAccessException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBasicAuth("username", "password");
        HttpRequestBase client = (HttpRequestBase) FieldUtils.readField(request, "request", true);
        String actualAuthHeader = client.getAllHeaders()[0].toString();
        assertEquals(EXPECTED_HEADER, actualAuthHeader);
    }

    @Test
    public void testSetHttpMethod() throws IllegalAccessException {
        HttpRequest request = new HttpRequest();
        request.setHttpMethod(HttpMethod.GET);
        HttpRequestBase method = (HttpRequestBase) FieldUtils.readField(request, "request", true);
        assertTrue(method instanceof HttpGet);
    }

    @Test
    public void testHttpExecutorConstructorAndPersistancy() throws IllegalAccessException {
        HttpExecutor executor = new HttpExecutor();
        new HttpRequest(HttpMethod.GET, executor);
        HttpExecutor instance = (HttpExecutor) FieldUtils.readStaticField(HttpExecutor.class, "instance", true);
        assertNull(instance);

        new HttpRequest(HttpMethod.GET, true);
        instance = (HttpExecutor) FieldUtils.readStaticField(HttpExecutor.class, "instance", true);
        assertNotNull(instance);

        // Reset static instance
        instance = null;
        FieldUtils.writeStaticField(HttpExecutor.class, "instance", instance, true);
    }

    @Test
    public void testGettersAndSetters() {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1);
        assertEquals(URL_1, request.getBaseUrl());

        request.setEndpoint(ENDPOINT_1);
        assertEquals(ENDPOINT_1, request.getEndpoint());
    }

    @Test
    public void testBodyProperty()
            throws UnsupportedOperationException, IOException, IllegalAccessException {
        HttpRequest request = new HttpRequest(HttpMethod.POST);
        request.setBody(BODY_CONTENT, ContentType.APPLICATION_JSON);
        HttpEntityEnclosingRequestBase requestBase = (HttpEntityEnclosingRequestBase) FieldUtils
                .readField(request, "request", true);
        HttpEntity entity = requestBase.getEntity();
        String actualHeader = entity.getContentType().toString();
        InputStream stream = entity.getContent();
        String actualBody = IOUtils.toString(entity.getContent(), "UTF-8");
        stream.close();
        assertTrue(actualHeader.contains(BODY_HEADER));
        assertEquals(BODY_CONTENT, actualBody);

        request.setBody(BODY_CONTENT);
        requestBase = (HttpEntityEnclosingRequestBase) FieldUtils.readField(request, "request", true);
        entity = requestBase.getEntity();
        actualHeader = entity.getContentType().toString();
        stream = entity.getContent();
        actualBody = IOUtils.toString(entity.getContent(), "UTF-8");
        stream.close();
        assertTrue(actualHeader.contains(BODY_HEADER_DEFAULT));
        assertEquals(BODY_CONTENT, actualBody);

        File file = new File(BODY_CONTENT_FILE);
        request.setBody(file);
        requestBase = (HttpEntityEnclosingRequestBase) FieldUtils.readField(request, "request", true);
        entity = requestBase.getEntity();
        actualHeader = entity.getContentType().toString();
        stream = entity.getContent();
        actualBody = IOUtils.toString(entity.getContent(), "UTF-8");
        stream.close();
        assertTrue(actualHeader.contains(BODY_HEADER_DEFAULT));
        assertEquals(BODY_CONTENT + "\n", actualBody);
    }

    @Test(expected = IOException.class)
    public void testBodyException() throws IOException {
        HttpRequest request = new HttpRequest(HttpMethod.POST);
        File file = new File(BODY_CONTENT_FILE_INVALID);
        request.setBody(file);
    }

    @Test
    public void testParameterProperty() {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        assertEquals(0, request.getParams().size());

        request.addParameter(PARAMETER_KEY_1, PARAMETER_VALUE_1);
        assertEquals(1, request.getParams().size());

        request.resetHttpRequestObject();
        assertEquals(0, request.getParams().size());

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(PARAMETER_KEY_1, PARAMETER_VALUE_1);
        parameters.put(PARAMETER_KEY_2, PARAMETER_VALUE_2);
        request.addParameters(parameters);
        assertEquals(2, request.getParams().size());
    }

    @Test
    public void testHeaderProperty() throws IllegalAccessException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.addHeader(HEADER_KEY_1, HEADER_VALUE_1);
        request.setHeader(HEADER_KEY_2, HEADER_VALUE_2);
        HttpRequestBase method = (HttpRequestBase) FieldUtils.readField(request, "request", true);
        String expectedHeader = HEADER_KEY_1 + ": " + HEADER_VALUE_1;
        String actualHeader = method.getAllHeaders()[0].toString();
        assertEquals(expectedHeader, actualHeader);

        expectedHeader = HEADER_KEY_2 + ": " + HEADER_VALUE_2;
        actualHeader = method.getAllHeaders()[1].toString();
        assertEquals(expectedHeader, actualHeader);

        request.removeHeader(HEADER_KEY_1);
        expectedHeader = HEADER_KEY_2 + ": " + HEADER_VALUE_2;
        actualHeader = method.getAllHeaders()[0].toString();
        assertEquals(expectedHeader, actualHeader);
    }

    @Test
    public void testGetURI() throws URISyntaxException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1).setEndpoint(ENDPOINT_1);
        URI uri = request.getURI();
        String fullURI = uri.toString();
        assertEquals(URL_1 + ENDPOINT_1, fullURI);
    }

    @Test(expected = UnknownHostException.class)
    public void testPerformRequestUnknownHost()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1).setEndpoint(ENDPOINT_1);
        request.performRequest();
    }

    @Test(expected = ClientProtocolException.class)
    public void testPerformRequestBadProtocol()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_BAD_PROTOCOL);
        request.performRequest();
    }

    @Test(expected = URISyntaxException.class)
    public void testPerformRequestBadSyntax()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_BAD_SYNTAX);
        request.performRequest();
    }

    @Test(expected = UnknownHostException.class)
    public void testPerformRequestNoEndpoint()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1);
        request.performRequest();
    }

    @Test(expected = UnknownHostException.class)
    public void testPerformRequestWithParameters()
            throws ClientProtocolException, URISyntaxException, IOException {
        HttpRequest request = new HttpRequest(HttpMethod.GET);
        request.setBaseUrl(URL_1).addParameter(PARAMETER_KEY_1, PARAMETER_VALUE_1);
        request.performRequest();
    }
}

