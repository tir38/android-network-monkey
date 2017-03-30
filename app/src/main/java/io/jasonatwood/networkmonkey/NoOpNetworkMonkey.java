package io.jasonatwood.networkmonkey;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

/**
 * NetworkMonkey that does nothing. Just passes request straight through.
 */
public class NoOpNetworkMonkey implements NetworkMonkey {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        return chain.proceed(request);
    }

    @Override
    public void shouldMonkeyWithWifiConnection() {
        // noop
    }

    @Override
    public void shouldMonkeyWithResponseCode() {
        // noop
    }

    @Override
    public void shouldMonkeyWithResponseTime(int delayInMilliseconds) {
        // noop
    }

    @Override
    public void shouldMonkeyWithRequestSuccess() {
        // noop
    }

    @Override
    public void enableJerkMode() {
        // noop
    }
}
