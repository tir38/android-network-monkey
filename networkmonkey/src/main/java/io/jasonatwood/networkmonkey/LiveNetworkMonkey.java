package io.jasonatwood.networkmonkey;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import okhttp3.Request;
import okhttp3.Response;

public class LiveNetworkMonkey implements NetworkMonkey {

    private static final String TAG = "NETWORK MONKEY";

    private static final int DURATION_TO_KEEP_WIFI_OFF_MILLISECONDS = 5000;

    private final Context applicationContext;

    private Set<Action> enabledActions;
    private boolean jerkMode;
    private int delayInMilliseconds;

    public LiveNetworkMonkey(Context context) {
        applicationContext = context.getApplicationContext();
        enabledActions = new HashSet<>();
    }

    @Override
    public void shouldMonkeyWithWifiConnection() {
        enabledActions.add(Action.WIFI_CONNECTION);
    }

    @Override
    public void shouldMonkeyWithResponseCode() {
        enabledActions.add(Action.RESPONSE_CODE);
    }

    @Override
    public void shouldMonkeyWithResponseTime(int delayInMilliseconds) {
        if (delayInMilliseconds > 0) {
            enabledActions.add(Action.DELAY);
            this.delayInMilliseconds = delayInMilliseconds;
        }
    }

    @Override
    public void shouldMonkeyWithRequestSuccess() {
        enabledActions.add(Action.REQUEST_FAILURE);
    }

    @Override
    public void enableJerkMode() {
        jerkMode = true;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (enabledActions.isEmpty() || !shouldRandomlyMonkey()) {
            return chain.proceed(request);
        }

        Action randomAction = getRandomAction();
        if (randomAction == null) {
            return chain.proceed(request);
        }

        String urlString = request.url().toString();
        switch (randomAction) {
            case WIFI_CONNECTION:
                disableWifi(urlString);
                break;

            case REQUEST_FAILURE:
                simulateRequestFailure(urlString);
                break;

            case DELAY:
                addResponseDelay(urlString);
                break;
        }

        Response response = chain.proceed(request);

        if (randomAction == Action.RESPONSE_CODE) {
            return setResponseCodeTo409(urlString, response);
        }
        return response;
    }

    @SuppressLint("MissingPermission")
    private void disableWifi(String urlString) {
        checkChangeWifiPermissions();

        WifiManager wifi = (WifiManager) applicationContext
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        if (wifi == null) {
            Log.d(TAG, "No wifi manager; can't monkey with wifi connection");
            return;
        }

        wifi.setWifiEnabled(false);
        Log.w(TAG, "Turning off wifi for request " + urlString);

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
        Log.d(TAG, "Turning wifi back on");
    }

    private Response setResponseCodeTo409(String urlString, Response response) {
        // if something really did go wrong, we want to know about it.
        if (!response.isSuccessful()) {
            return response;
        }

        Log.w(TAG, "Changing response code to 409 for request " + urlString);
        return response.newBuilder()
                .code(409)
                .build();
    }

    private void addResponseDelay(String urlString) {
        Log.w(TAG, "Delaying response by " + delayInMilliseconds
                + " milliseconds for request " + urlString);
        try {
            Thread.sleep(delayInMilliseconds);
        } catch (InterruptedException e) {
            // eat it
        }
    }

    private void simulateRequestFailure(String urlString) throws IOException {
        Log.w(TAG, "Simulating request failure  for request " + urlString);
        throw new IOException("Monkey Exception");
    }

    private boolean shouldRandomlyMonkey() {
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

    private Action getRandomAction() {
        int size = enabledActions.size();
        int randomInt = new Random().nextInt(size);
        int i = 0;
        for (Action action : enabledActions) {
            if (i == randomInt) {
                return action;
            }
            i++;
        }
        return null;
    }

    private enum Action {
        WIFI_CONNECTION,
        RESPONSE_CODE,
        REQUEST_FAILURE,
        DELAY
    }
}
