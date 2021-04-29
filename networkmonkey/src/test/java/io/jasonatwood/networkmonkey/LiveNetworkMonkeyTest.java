package io.jasonatwood.networkmonkey;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Unit tests for {@link LiveNetworkMonkey}
 */
@RunWith(JUnit4.class)
public class LiveNetworkMonkeyTest {

    private LiveNetworkMonkey networkMonkey;

    @Before
    public void setUp() {
        networkMonkey = new LiveNetworkMonkey();
        networkMonkey.enableTestMode();
    }

    @Test
    public void ifNoOperations_RequestShouldBeUnchanged() {
        // arrange
        Request initialRequest = buildMockRequest("http://google.com", "GET");
        FakeChain fakeChain = new FakeChain(initialRequest);

        // act
        networkMonkey.intercept(fakeChain);

        // assert
        assertThat(fakeChain.getUpdatedRequest()).isEqualTo(initialRequest);
    }

    @Test
    public void ifNoOperations_ResponseShouldBeUnchanged() {
        // arrange
        Request initialRequest = buildMockRequest("http://google.com", "GET");
        FakeChain fakeChain = new FakeChain(initialRequest);

        // act
        Response updatedResponse = networkMonkey.intercept(fakeChain);

        // assert
        assertThat(updatedResponse).isEqualTo(fakeChain.getInitialResponse());
    }

    @Test
    public void ifMonkeyWithResponseCode_ResponseShouldContain418() {
        // arrange
        networkMonkey.shouldMonkeyWithResponseCode(418);

        Request initialRequest = buildMockRequest("http://google.com", "GET");
        FakeChain fakeChain = new FakeChain(initialRequest);

        // act
        Response updatedResponse = networkMonkey.intercept(fakeChain);

        // assert
        assertThat(updatedResponse.code()).isEqualTo(418);
    }

    @Test
    public void ifMonkeyWithRequestSuccess_ShouldThrowIOException() {
        // arrange
        networkMonkey.shouldMonkeyWithRequestSuccess();

        Request initialRequest = buildMockRequest("http://google.com", "GET");
        FakeChain fakeChain = new FakeChain(initialRequest);

        // act
        // assert
        try {
            Response response = networkMonkey.intercept(fakeChain);
            Assert.fail("Should have thrown IOException");
        } catch (Exception exception) {
            assertThat(exception).isInstanceOf(IOException.class);
        }
    }

    private Request buildMockRequest(final String url, final String method) {
        return new Request.Builder()
                .url(url)
                .method(method, null)
                .build();
    }
}