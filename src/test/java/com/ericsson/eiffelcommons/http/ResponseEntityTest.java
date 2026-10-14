package com.ericsson.eiffelcommons.http;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.ericsson.eiffelcommons.http.ResponseEntity;

public class ResponseEntityTest {

    private static final int STATUS_CODE = 200;
    private static final String CHARSET = "UTF-8";
    private static final String BODY = "{\"message\":\"dummy\"}";
    private static final Header[] HEADERS = new Header[0];

    private HttpResponse httpResponse;
    private StatusLine statusLine;
    private HttpEntity entity;
    private MockedStatic<EntityUtils> mockedEntityUtils;

    @Test
    public void testResponseEntityConstructor() throws ParseException, IOException {
        setUpMockObjects();

        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        mockedEntityUtils.when(() -> EntityUtils.toString(entity, CHARSET)).thenReturn(BODY);
        Mockito.when(httpResponse.getAllHeaders()).thenReturn(HEADERS);

        ResponseEntity responseEntity = new ResponseEntity(httpResponse);
        assertEquals(STATUS_CODE, responseEntity.getStatusCode());
    }

    @Test
    public void testResponseEntityConstructorNullEntity() throws ParseException, IOException {
        setUpMockObjects();
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        Mockito.when(statusLine.getStatusCode()).thenReturn(STATUS_CODE);
        Mockito.when(httpResponse.getEntity()).thenReturn(null);
        Mockito.when(httpResponse.getAllHeaders()).thenReturn(new Header[0]);

        ResponseEntity responseEntity = new ResponseEntity(httpResponse);
        assertEquals(STATUS_CODE, responseEntity.getStatusCode());
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

        mockedEntityUtils = Mockito.mockStatic(EntityUtils.class);
    }

    @After
    public void tearDown() {
        if (mockedEntityUtils != null) {
            mockedEntityUtils.close();
        }
    }
}
