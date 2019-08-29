package io.jasonatwood.networkmonkey

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * NetworkMonkey that does nothing. Just passes request straight through.
 */
class NoOpNetworkMonkey : NetworkMonkey {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }

    override fun shouldMonkeyWithWifiConnection() {
        // noop
    }

    override fun shouldMonkeyWithResponseCode(code: Int) {
        // noop
    }

    override fun shouldMonkeyWithResponseTime(delayInMilliseconds: Int) {
        // noop
    }

    override fun shouldMonkeyWithRequestSuccess() {
        // noop
    }

    override fun enableJerkMode() {
        // noop
    }
}