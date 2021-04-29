package io.jasonatwood.networkmonkey

import okhttp3.Interceptor

/**
 * An OkHttp [Interceptor] that intercepts requests and responses
 * and monkeys around with them.
 */
interface NetworkMonkey : Interceptor {
    /**
     * Tell NetworkMonkey to randomly turn off wifi connection
     */
    @Deprecated("Network monkey no longer monkeys with wifi connection. Programmatically changing "
            + "wifi state is no longer supported in Android Q. This method will be removed in "
            + "future versions.")
    fun shouldMonkeyWithWifiConnection()

    /**
     * Tell NetworkMonkey to randomly change response code to the provided request code (or 404
     * if not supplied.
     */
    fun shouldMonkeyWithResponseCode(code: Int = 404)

    /**
     * Tell NetworkMonkey to randomly delay responses
     * @param delayInMilliseconds time to delay in milliseconds
     */
    fun shouldMonkeyWithResponseTime(delayInMilliseconds: Int)

    /**
     * Tell NetworkMonkey to randomly fail network request
     */
    fun shouldMonkeyWithRequestSuccess()

    /**
     * Tell NetworkMonkey to do all the things more often
     */
    fun enableJerkMode()
}