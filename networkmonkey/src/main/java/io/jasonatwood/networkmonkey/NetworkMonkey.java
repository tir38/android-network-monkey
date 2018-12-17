package io.jasonatwood.networkmonkey;

import okhttp3.Interceptor;

/**
 * An OkHttp {@link Interceptor} that intercepts requests and responses
 * and monkeys around with them.
 */
public interface NetworkMonkey extends Interceptor {

    /**
     * Tell NetworkMonkey to randomly turn off wifi connection
     */
    void shouldMonkeyWithWifiConnection();

    /**
     * Tell NetworkMonkey to randomly change response code to 409.
     */
    void shouldMonkeyWithResponseCode();

    /**
     * Tell NetworkMonkey to randomly delay responses
     * @param delayInMilliseconds time to delay in milliseconds
     */
    void shouldMonkeyWithResponseTime(int delayInMilliseconds);

    /**
     * Tell NetworkMonkey to randomly fail network request
     */
    void shouldMonkeyWithRequestSuccess();

    /**
     * Tell NetworkMonkey to do all the things more often
     */
    void enableJerkMode();
}
