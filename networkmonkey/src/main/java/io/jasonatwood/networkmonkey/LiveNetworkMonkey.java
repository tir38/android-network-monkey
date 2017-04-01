package io.jasonatwood.networkmonkey;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;


public class LiveNetworkMonkey implements NetworkMonkey {

    private static final String TAG = "NETWORK MONKEY";
    private final Context applicationContext;

    private boolean shouldMonkeyWithWifiConnection;
    private boolean shouldMonkeyWithResponseCode;
    private boolean shouldMonkeyWithRequestSuccess;
    private boolean jerkMode;
    private int delayInMilliseconds;

    public LiveNetworkMonkey(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @Override
    public void shouldMonkeyWithWifiConnection() {
        shouldMonkeyWithWifiConnection = true;
    }

    @Override
    public void shouldMonkeyWithResponseCode() {
        shouldMonkeyWithResponseCode = true;
    }

    @Override
    public void shouldMonkeyWithResponseTime(int delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    @Override
    public void shouldMonkeyWithRequestSuccess() {
        shouldMonkeyWithRequestSuccess = true;
    }

    @Override
    public void enableJerkMode() {
        jerkMode = true;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        disableWifi();
        addResponseDelay();
        simulateRequestFailure();
        Response response = chain.proceed(request);
        return setResponseCodeTo404(response);
    }

    private void disableWifi() {
        if (!shouldMonkeyWithWifiConnection || !shouldRandomlyDoSomething()) {
            return;
        }

        boolean granted
                = ContextCompat.checkSelfPermission(applicationContext,
                "android.permission.CHANGE_WIFI_STATE")
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if (!granted) {
            Log.w(TAG, "You need to add android.permission.CHANGE_WIFI_STATE permission before" +
                    " monkeying with wifi");
            return;
        }

        WifiManager wifi = (WifiManager) applicationContext
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        //noinspection MissingPermission suppress since we check and early return above.
        wifi.setWifiEnabled(false);
        Log.e(TAG, "Turning off wifi.");

        // sleep so that wifi manager has time to turn off wifi antenna
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO turn wifi back on
    }

    private Response setResponseCodeTo404(Response response) {
        if (!shouldMonkeyWithResponseCode || !shouldRandomlyDoSomething()) {
            return response;
        }

        // if something really did go wrong, we want to know about it.
        if (!response.isSuccessful()) {
            return response;
        }

        Log.e(TAG, "Changing response code to 404.");
        return response.newBuilder()
                .code(404)
                .build();
    }

    private void addResponseDelay() {
        if (delayInMilliseconds == 0 || !shouldRandomlyDoSomething()) {
            return;
        }

        Log.e(TAG, "Delaying response by " + delayInMilliseconds + " milliseconds");
        try {
            Thread.sleep(delayInMilliseconds);
        } catch (InterruptedException e) {
            // eat it
        }
    }

    private void simulateRequestFailure() {
        if (shouldMonkeyWithRequestSuccess && shouldRandomlyDoSomething()) {
            Log.e(TAG, "Simulating request failure");
            throw new RuntimeException("Monkey Exception");
        }
    }

    private boolean shouldRandomlyDoSomething() {
        if (jerkMode) {
            return (System.currentTimeMillis() % 2 == 0); // 1:2 chance of doing something
        }

        return (System.currentTimeMillis() % 10 == 0); // 1:10 chance of doing something
    }
}
