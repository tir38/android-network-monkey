# Android Network Monkey
Let Network Monkey loose to monkey test your OkHttp web requests. Inspired by Netflix's [Chaos Monkey](https://github.com/Netflix/chaosmonkey), Network Monkey will randomly: 

* turn off a device's wifi before making request
* replace `200` response codes with `400`
* insert response time delays
* throw network exceptions during request/response


[ ![Download](https://api.bintray.com/packages/jasonatwood/maven/networkmonkey/images/download.svg) ](https://bintray.com/jasonatwood/maven/networkmonkey/_latestVersion)


[ ![MethodCount](https://img.shields.io/badge/Methods%20and%20size-core:%2048%20%7C%20deps:%202784%20%7C%205%20KB-e91e63.svg) ](http://www.methodscount.com/?lib=io.jasonatwood%3Anetworkmonkey%3A1.0.2)


## Usage

`NetworkMonkey` extends OkHttp3's [Interceptor](https://github.com/square/okhttp/wiki/Interceptors) to change something about the request/response. It's best to add `NetworkMonkey` as an [ApplicationInterceptor](https://github.com/square/okhttp/wiki/Interceptors#application-interceptors) with `.addInterceptor()` (vs. a [NetworkInterceptor](https://github.com/square/okhttp/wiki/Interceptors#network-interceptors)). It's also best to add `NetworkMonkey` as the *first* Interceptor, if you use multiple Interceptors. This ensures it has first say in monkeying with the request, and last say in monkeying with the response.

```java
OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
NetworkMonkey networkMonkey = ...
okHttpClientBuilder.addInterceptor(networkMonkey);
```

Network Monkey can monkey with several parts of a network request:


#### Wifi Connection

```java
networkMonkey.shouldMonkeyWithWifiConnection();
```

This will tell Network Monkey to randomly disable a device's wifi connection. If this is your device's only data connection this is a good way to test your apps' response to 
`connectivityManager.getActiveNetworkInfo()`. See [Additional Notes](#additional_notes)
 section about runtime permissions.

#### Request Success

```java
networkMonkey.shouldMonkeyWithRequestSuccess();
```

Sometimes OkHttp will throw an exception if there is an error with the network. This method will tell Network Monkey to randomly throw a `IOException` during a request/response to mimic OkHttp's behavior.



#### Response Code

```java
networkMonkey.shouldMonkeyWithResponseCode();
```

This will tell Network Monkey to randomly replace a 200 success code with a 400. If Network Monkey detects a non-200 code it will let that pass through. That way if your app really is experiencing a problem from your network you are alerted to it.


#### Response Time

```java
networkMonkey.shouldMonkeyWithResponseTime(10000);
```

This will tell Network Monkey to randomly add a time delay (in milliseconds) to an existing request/response. This is a good way to test your UI to delays that high-latency-network users may experience.


#### Jerk Mode

```java
networkMonkey.enableJerkMode();
```
            
Normally the randomness of the above adjustments is 1:10, meaning that 10% of the time Network Monkey will monkey with something.

However, when jerk mode is enabled this increases to 1:2, or half the time. While this can be more frustrating during development, it ensures that your app is always under some sort of network stress.



## Debug vs Production

Obviously you don't want to run Network Monkey during production. This lib provides several ways to help achieve different behavior for debug and production apps, based on your app's existing architecture. `NetworkMonkey` is just an interface implemented by two supplied classes: `LiveNetworkMonkey` and `NoOpNetworkMonkey`.

The simplest approach is to check `BuildConfig.DEBUG` and use `LiveNetworkMonkey`:

```java
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


```java
@Provides
@Singleton
NetworkMonkey provideNetworkMonkey(Context context) {
    return new LiveNetworkMonkey(context);
}
```

```java
@Provides
@Singleton
NetworkMonkey provideNetworkMonkey(Context context) {
    return new NoOpNetworkMonkey();
}
```

If you run instrumentation tests that depend on your `OkHttpClient`, it's suggested that you also use `NoOpNetworkMonkey`. You don't want flaky tests do you?

## Additional Notes
* The `Context` passed to `LiveNetworkMonkey` is turned into an application context, so don't worry about leaking your Activities.

* If you call `networkMonkey.shouldMonkeyWithWifiConnection()` your app will need the     `<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>` permission. Unless your app already requests this permission, you should put this in the `AndroidManifest.xml` specific to your debug builds.

* Every time Network Monkey decides to monkey with a connection, you will be notified by a log in Logcat.


## Download

Download via Maven:
```xml
<dependency>
  <groupId>io.jasonatwood</groupId>
  <artifactId>networkmonkey</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```

or Gradle:
```groovy
compile 'io.jasonatwood:networkmonkey:1.0.2'
```

Network Monkey requires at minimum Android API 14.


## License

    Copyright 2017 Jason Atwood

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    
## Release Steps
 * build .aar locally `./gradlew clean assemble`
 * move .aar into dogfood project and run project
 * bump `libraryVersion` and `libraryVersionCode` in build.gradle
 * update CHANGELOG.md
 * updated README
 * build and upload to bintray `./gradlew clean bintrayUpload`
 * ensure latest version is uploaded `https://bintray.com/jasonatwood/maven/networkmonkey`
 * bump version number in dogfood project and ensure it builds
 * commit and push