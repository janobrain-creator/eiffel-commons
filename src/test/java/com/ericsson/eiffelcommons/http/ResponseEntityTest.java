package com.ericsson.eiffelcommons.http;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ericsson.eiffelcommons.http.ResponseEntity;

public class ResponseEntityTest {

    private static final int STATUS_CODE = 200;
    private static final String CHARSET = "utf-8";
    private static final String BODY = "{\"message\":\"dummy\"}";
    private static final Header[] HEADERS = new Header[0];

    private HttpResponse httpResponse;
    private StatusLine statusLine;
    private HttpEntity entity;

    @Test
    public void testResponseEntityConstructor() throws ParseException, IOException {
        setUpMockObjects();

        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpResponse.getAllHeaders()).thenReturn(HEADERS);

        try (MockedStatic<EntityUtils> entityUtils = mockStatic(EntityUtils.class)) {
            entityUtils.when(() -> EntityUtils.toString(entity, CHARSET)).thenReturn(BODY);

            ResponseEntity responseEntity = new ResponseEntity(httpResponse);
            assertEquals(STATUS_CODE, responseEntity.getStatusCode());
            assertEquals(BODY, responseEntity.getBody());
            assertArrayEquals(HEADERS, responseEntity.getHeaders());
            entityUtils.verify(() -> EntityUtils.toString(entity, CHARSET));
        }
    }

    @Test
    public void testResponseEntityConstructorNullEntity() throws ParseException, IOException {
        setUpMockObjects();
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);
        when(httpResponse.getEntity()).thenReturn(null);
        when(httpResponse.getAllHeaders()).thenReturn(new Header[0]);

        ResponseEntity responseEntity = new ResponseEntity(httpResponse);
        assertEquals(STATUS_CODE, responseEntity.getStatusCode());
        assertEquals("", responseEntity.getBody());
        assertArrayEquals(HEADERS, responseEntity.getHeaders());
    }

    @Test
    public void testResponseEntityValuesConstructor() throws ParseException, IOException {
        ResponseEntity responseEntity = new ResponseEntity(STATUS_CODE, BODY, HEADERS);
        assertEquals(String.valueOf(STATUS_CODE), responseEntity.getStatusCodeValue());
    }

    private void setUpMockObjects() {
        httpResponse = Mockito.mock(HttpResponse.class);
        statusLine = Mockito.mock(StatusLine.class);
        entity = Mockito.mock(HttpEntity.class);
    }
}
