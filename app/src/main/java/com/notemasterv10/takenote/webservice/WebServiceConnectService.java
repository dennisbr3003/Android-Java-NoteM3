package com.notemasterv10.takenote.webservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.notemasterv10.takenote.constants.WebServiceConstants;

import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebServiceConnectService extends IntentService implements WebServiceConstants {

    // specific for this service (action should be unique) -->
    public static final String SERVICE_ACTION = "com.notemasterv10.takenote.webservice.WebServiceConnectService.CheckConnection";
    public static final String IS_ALIVE = "connection_alive";

    private volatile boolean isRunning = false; // <-- volatile is used to avoid caching of this boolean

    public WebServiceConnectService() {
        super("WebServiceConnectService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false; // <-- stop the loop
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        super.onStartCommand(intent, startId, startId);
        isRunning = true; // <-- to get the loop running
        return START_STICKY; // <-- To keep the service alive, otherwise it will be destroyed after onHandleIntent is executed
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String base_url = BASE_URL;
        String json_response ;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(String.format("%s%s", base_url, TEST_CONNECTION))
                .method("GET", null)
                .build();

        Response response = null;

        try {
            while (isRunning) {
                try {
                    SystemClock.sleep(5000);
                    /*
                       Not using enqueue because it will result in a asynchronous call with it's own callback methods.
                       If the webservice is not available is can take 10 - 15 seconds to receive confirmation. In this time
                       the asynchronous variant would have fired up to 3 times enqueueing the requests (interval is 5 sec.).
                       That is undesired behaviour. Therefor I use this way, where we await the answer before checking again.
                    */
                    response = client.newCall(request).execute(); // <-- actual call to the server
                    json_response = response.body().string(); //<-- this is closable or read once and then never again
                    JSONObject j_object = new JSONObject(json_response);

                    if(j_object.has(RESPONSE_STATUS)){
                        if(((String) j_object.get(RESPONSE_STATUS)).equalsIgnoreCase(IS_SUCCESS)) {
                            sendResponse(true);
                        }
                    } else {
                        sendResponse(false);
                    }
                } catch (Exception e) {
                    sendResponse(false);
                }
            }
        } catch (Exception e) {
            sendResponse(false);
        }

    }

    public void sendResponse(boolean is_alive){
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SERVICE_ACTION);
        broadcastIntent.putExtra(IS_ALIVE, is_alive);
        sendBroadcast(broadcastIntent);
    }

}
