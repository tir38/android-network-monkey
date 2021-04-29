## 2.0.0-alpha01

* Complete redesign to prepare for custom operators [#8](https://github.com/tir38/android-network-monkey/issues/8)
* Convert everything to Kotlin
* Bump to latest version of most libraries. Still depends on OkHttp3.
* Remove all dependencies on Android framework.

 **BREAKING CHANGES**:

* `shouldMonkeyWithWifiConnection()` is now a Deprecated no-op method. It will be removed in later versions of the library. See [#19](https://github.com/tir38/android-network-monkey/issues/19)
* `shouldMonkeyWithResponseCode()` now takes an optional request code integer. When called from Java, this method will now require a value.
* `LiveNetworkMonkey` constructor no longer needs a `Context` argument.

## 1.1.1
* Fixes "monkey before sending request" (#2)
* Fix 1-in-10 bug (#6)

## 1.1.0
* Clarify README
* Tone down Log.e to Log.w
* Turn wifi back on after 5 seconds (#4)
* Update AGP, Gradle, compileSdk, targetSdk, OkHttp

## 1.0.2
* Remove dependency on support lib

## 1.0.1
* Better logging to include URL string.