package io.jasonatwood.networkmonkey;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import okhttp3.Request;
import okhttp3.RequestBody;

public class OperationTest {

    @Test
    public void shouldMatchOnAny() {
        // arrange
        Operation operationOnAnyMethod = new Operation("",
                Operation.Method.ANY,
                null,
                1,
                false,
                null,
                null);

        // act
        Request getRequest = buildMockRequest("http://someurl.com", "GET", null);
        Request postRequest = buildMockRequest("http://someurl.com", "POST",
                RequestBody.create(null, new byte[]{}));
        Request putRequest = buildMockRequest("http://someurl.com", "PUT",
                RequestBody.create(null, new byte[]{}));
        Request deleteRequest = buildMockRequest("http://someurl.com", "DELETE", null);
        Request createRequest = buildMockRequest("http://someurl.com", "CREATE", null);
        Request patchRequest = buildMockRequest("http://someurl.com", "PATCH",
                RequestBody.create(null, new byte[]{}));

        // assert
        assertTrue(operationOnAnyMethod.matches(getRequest));
        assertTrue(operationOnAnyMethod.matches(postRequest));
        assertTrue(operationOnAnyMethod.matches(putRequest));
        assertTrue(operationOnAnyMethod.matches(deleteRequest));
        assertTrue(operationOnAnyMethod.matches(createRequest));
        assertTrue(operationOnAnyMethod.matches(patchRequest));
    }

    private Request buildMockRequest(final String url,
                                     final String method,
                                     final RequestBody requestBody) {
        return new Request.Builder()
                .url(url)
                .method(method, requestBody)
                .build();
    }
}
