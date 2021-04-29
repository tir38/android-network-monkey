package io.jasonatwood.networkmonkey;

import static okhttp3.Protocol.HTTP_1_1;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Class needed to fake the {@link Interceptor.Chain} class in tests
 */
class FakeChain implements Interceptor.Chain {

    private final Request initialRequest;
    private Request suppliedRequest;
    private Response initialResponse;

    FakeChain(Request initialRequest) {
        this.initialRequest = initialRequest;
    }

    private static Response buildMockResponse(Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(HTTP_1_1) // just set any ole protocol
                .code(200)
                .message("this is a mock")
                .build();
    }

    /**
     * @return the request that was sent on to this chain by the {@link Interceptor} under test
     */
    Request getUpdatedRequest() {
        return suppliedRequest;
    }

    /**
     * @return the request that was sent to the {@link Interceptor} under test by the downstream chain
     */
    Response getInitialResponse() {
        return initialResponse;
    }

    @Override
    public Request request() {
        return initialRequest;
    }

    @Override
    public Response proceed(final Request request) {
        suppliedRequest = request;
        initialResponse = buildMockResponse(request);
        return initialResponse;
    }

    @Override
    public Connection connection() {
        throw new RuntimeException("not faked");
    }

    @Override
    public Call call() {
        throw new RuntimeException("not faked");
    }

    @Override
    public int connectTimeoutMillis() {
        throw new RuntimeException("not faked");
    }

    @Override
    public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
        throw new RuntimeException("not faked");
    }

    @Override
    public int readTimeoutMillis() {
        throw new RuntimeException("not faked");
    }

    @Override
    public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
        throw new RuntimeException("not faked");
    }

    @Override
    public int writeTimeoutMillis() {
        throw new RuntimeException("not faked");
    }

    @Override
    public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
        throw new RuntimeException("not faked");
    }
}