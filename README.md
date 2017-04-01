# Android Network Monkey
Let Network Monkey loose to monkey test your OkHttp web requests. Inspired by Netflix's [Chaos Monkey](https://github.com/Netflix/chaosmonkey), Network Monkey will randomly: 

* turn off a device's wifi before making request
* replace `200` response codes with `400`
* insert response time delays
* throw network exceptions during request/response


[ ![Download](https://api.bintray.com/packages/jasonatwood/maven/networkmonkey/images/download.svg) ](https://bintray.com/jasonatwood/maven/networkmonkey/_latestVersion)


## Usage

`NetworkMonkey` extends OkHttp3's [Interceptor](https://github.com/square/okhttp/wiki/Interceptors) and to change something about the request/response. It's best to add `NetworkMonkey` as an [ApplicationInterceptor](https://github.com/square/okhttp/wiki/Interceptors#application-interceptors) with `.addInterceptor()` (vs. a [NetworkInterceptor](https://github.com/square/okhttp/wiki/Interceptors#network-interceptors)). It's also best to add `NetworkMonkey` as the *first* Interceptor, if you use more than one. This ensures it has first say in monkeying with the request, and last say in monkeying with the response.

```
OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
NetworkMonkey networkMonkey = ...
okHttpClientBuilder.addInterceptor(networkMonkey);
```

Network Monkey can monkey with several parts of a network request:


#### Wifi Connection

```
networkMonkey.shouldMonkeyWithWifiConnection();
```

This will tell Network Monkey to randomly disable a device's wifi connection. If this is your only data connection this is a good way to test your apps' response to 
`connectivityManager.getActiveNetworkInfo()`.

#### Request Success

```
networkMonkey.shouldMonkeyWithRequestSuccess();
```

Sometimes OkHttp will throw an exception if there is an error with the network. This method will tell Network Monkey to randomly throw a `RuntimeException` during a request/response.



#### Response Code

```
networkMonkey.shouldMonkeyWithResponseCode();
```

This will tell Network Monkey to randomly replace a 200 success code with a 400. If Network Monkey detects a non-200 code it will let that pass through. That way if your app really is experiencing a problem from your network you are alerted to it.


#### Response Time

```
networkMonkey.shouldMonkeyWithResponseTime(10000);
```

This will tell Network Monkey to randomly add a time delay (in milliseconds) to an existing request/response. This is a good way to test your UI to delays that high-latency-network users may experience.


#### Jerk Mode

```
networkMonkey.enableJerkMode();
```
            
Normally the randomness of the above adjustments is 1:10, meaning that 10% of the time Network Monkey will monkey with something.

However, when jerk mode is enabled this increases to 1:2, or half the time. While this can be more frustrating during development, it esures that your app is always under some sort of network stress.



## Debug vs Production

Obviously you don't want to run Network Monkey during production. This lib provides several ways to help achieve different behaviour for debug and production apps, based on your app's existing architecture. `NetworkMonkey` is just an interface implemented by two supplied classes: `LiveNetworkMonkey` and `NoOpNetworkMonkey`.

The simplest approach is to check `BuildConfig.DEBUG` and use `LiveNetworkMonkey`:

```
// add NetworkMonkey but only on debug builds
if (BuildConfig.DEBUG) {
    Context context = ....
    NetworkMonkey networkMonkey = new LiveNetworkMonkey(context);
    networkMonkey.shouldMonkeyWithResponseCode();
    networkMonkey.shouldMonkeyWithWifiConnection();
    networkMonkey.shouldMonkeyWithResponseTime(10000);
    networkMonkey.shouldMonkeyWithRequestSuccess();
    networkMonkey.enableJerkMode();
    okHttpClientBuilder.addInterceptor(networkMonkey);
}
````

If your app relies on dependency injection you can simply provide `LiveNetworkMonkey` in debug and `NoOpNetworkMonkey` in production:


```
@Provides
@Singleton
NetworkMonkey provideNetworkMonkey(Context context) {
    return new LiveNetworkMonkey(context);
}
```

```
@Provides
@Singleton
NetworkMonkey provideNetworkMonkey(Context context) {
    return new NoOpNetworkMonkey();
}
```

If you run instrumentation tests that depend on your `OkHttpClient`, it's suggested that you also use `NoOpNetworkMonkey`. You don't want flaky tests do you?

## Additional Notes
* The `Context` passed to `LiveNetworkMonkey` is turned into an application context, so don't worry about leaking your Activities.
