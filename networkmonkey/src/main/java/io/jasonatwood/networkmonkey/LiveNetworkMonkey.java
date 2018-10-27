package io.jasonatwood.networkmonkey;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class LiveNetworkMonkey implements NetworkMonkey {

    private static final String TAG = "NETWORK MONKEY";

    private static final int DURATION_TO_KEEP_WIFI_OFF_MILLISECONDS = 5000;

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
        String urlString = request.url().toString();
        disableWifi(urlString);
        addResponseDelay(urlString);
        simulateRequestFailure(urlString);
        Response response = chain.proceed(request);
        return setResponseCodeTo404(urlString, response);
    }

    @SuppressLint("MissingPermission")
    private void disableWifi(String urlString) {
        if (!shouldMonkeyWithWifiConnection || !shouldRandomlyDoSomething()) {
            return;
        }

        checkChangeWifiPermissions();

        WifiManager wifi = (WifiManager) applicationContext
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        if (wifi == null) {
            Log.d(TAG, "No wifi manager; can't monkey with wifi connection");
            return;
        }

        wifi.setWifiEnabled(false);
        Log.e(TAG, "Turning off wifi for request " + urlString);

        // sleep so that wifi manager has time to turn off wifi antenna
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // this thread may not have a looper. So just use main thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                turnWifiOn();
            }
        }, DURATION_TO_KEEP_WIFI_OFF_MILLISECONDS);
    }

    @SuppressLint("MissingPermission")
    private void turnWifiOn() {
        checkChangeWifiPermissions();

        WifiManager wifi = (WifiManager) applicationContext
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        if (wifi == null) {
            return;
        }

        wifi.setWifiEnabled(true);
        Log.e(TAG, "Turning wifi back on");
    }

    private Response setResponseCodeTo404(String urlString, Response response) {
        if (!shouldMonkeyWithResponseCode || !shouldRandomlyDoSomething()) {
            return response;
        }

        // if something really did go wrong, we want to know about it.
        if (!response.isSuccessful()) {
            return response;
        }

        Log.e(TAG, "Changing response code to 404 for request " + urlString);
        return response.newBuilder()
                .code(404)
                .build();
    }

    private void addResponseDelay(String urlString) {
        if (delayInMilliseconds == 0 || !shouldRandomlyDoSomething()) {
            return;
        }

        Log.e(TAG, "Delaying response by " + delayInMilliseconds
                + " milliseconds for request " + urlString);
        try {
            Thread.sleep(delayInMilliseconds);
        } catch (InterruptedException e) {
            // eat it
        }
    }

    private void simulateRequestFailure(String urlString) throws IOException {
        if (shouldMonkeyWithRequestSuccess && shouldRandomlyDoSomething()) {
            Log.e(TAG, "Simulating request failure  for request " + urlString);
            throw new IOException("Monkey Exception");
        }
    }

    private boolean shouldRandomlyDoSomething() {
        if (jerkMode) {
            return (System.currentTimeMillis() % 2 == 0); // 1:2 chance of doing something
        }

        return (System.currentTimeMillis() % 10 == 0); // 1:10 chance of doing something
    }

    private void checkChangeWifiPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean granted = applicationContext
                    .checkSelfPermission("android.permission.CHANGE_WIFI_STATE")
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                Log.w(TAG, "You need to add android.permission.CHANGE_WIFI_STATE permission " +
                        "before monkeying with wifi");
                return;
            }
        }
    }
}
