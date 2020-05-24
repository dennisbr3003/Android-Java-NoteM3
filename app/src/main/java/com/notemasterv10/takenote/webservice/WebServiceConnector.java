package com.notemasterv10.takenote.webservice;

import com.notemasterv10.takenote.constants.WebServiceConstants;
import com.notemasterv10.takenote.listeners.WebEventListener;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebServiceConnector extends Thread implements WebServiceConstants {

    /*
       The checking of the connection to the webservice every 5 seconds needs to be done in a separate thread
       of the Thread class. Using an Asynchronous task is not enough. If the webservice cannot be reached it will
       still block the user interface, ven if you put it in a asynchronous task. There is an interface to the
       MainActivity where the user interface will be updated if needed. Another possibility is to enqueue the
       the request. You would create a Asynchronous call in the thread.
    */

    private volatile boolean isRunning = false; // <-- volatile is used to avoid caching of this boolean

    private String base_url = BASE_URL;
    private String json_response;
    private String action;

    private WebEventListener webEventListener;

    public WebEventListener getWebEventListener() {
        return webEventListener;
    }

    public void setWebEventListener(WebEventListener webEventListener) {
        this.webEventListener = webEventListener;
    }

    public WebServiceConnector(){
    }

    public WebServiceConnector(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public void startChecker(){
        this.isRunning = true;
    }

    public void stopChecker(){
        this.isRunning = false;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public void run() {

        super.run();

        while (isRunning) {
            try {
                Thread.sleep(5000);
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Request request = new Request.Builder()
                        .url(String.format("%s%s", base_url, action))
                        .method("GET", null)
                        .build();

                Response response = null;
                updateUserInterface(false);
                try {
                    response = client.newCall(request).execute(); // <-- actual call to the server
                    json_response = response.body().string(); //<-- this is closable or read once and then never again
                    JSONObject j_object = new JSONObject(json_response);
                    if(j_object.has(RESPONSE_STATUS)){
                        if(((String) j_object.get(RESPONSE_STATUS)).equalsIgnoreCase(IS_SUCCESS)) {
                            updateUserInterface(true);
                        }
                    }
                } catch (Exception e) {
                    // do nothing
                }
            } catch (InterruptedException e) {
                // do nothing
            }
        }

    }

    private void updateUserInterface(boolean isAlive){
        // Unlock certain menu items and update the UI with connection status information -->
        if (webEventListener != null) {
            if(isAlive) {
                webEventListener.showHideMenuItem(WebEventListener.Action.SHOW_UPL_DL);
            } else {
                webEventListener.showHideMenuItem(WebEventListener.Action.HIDE_UPL_DL);
            }
        }
    }

}


