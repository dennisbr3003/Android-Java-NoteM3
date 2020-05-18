package com.notemasterv10.takenote.webservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notemasterv10.takenote.Constants.NoteMasterConstants;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.listeners.WebEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebService extends AppCompatActivity implements NoteMasterConstants {

    private final String base_url = "http://192.168.178.69:8080/notemaster/";

    private static Boolean webservice_online = false;
    private String json_response;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private WebEventListener webEventListener;

    public WebEventListener getWebEventListener() {
        return webEventListener;
    }

    public void setWebEventListener(WebEventListener webEventListener) {
        this.webEventListener = webEventListener;
    }

    @SuppressLint("StaticFieldLeak")
    public void checkForWebService(){

        final String action = "test"; // <-- This action is used to test the webservice. It only checks if the
                                      //      the webservice is online and if the database can be connected.

        new AsyncTask<Void, Void, Boolean> () {

            @Override
            protected Boolean doInBackground(Void... voids) {

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                Request request = new Request.Builder()
                        .url(String.format("%s%s", base_url, action))
                        .method("GET", null)
                        .build();

                Response response = null;
                try {
                    response = client.newCall(request).execute(); // <-- actual call to the server
                    json_response = response.body().string(); //<-- this is closable or read once and then never again
                    JSONObject j_object = new JSONObject(json_response);
                    if(j_object.has("status")){
                        if(((String) j_object.get("status")).equalsIgnoreCase("1")){
                            return true;
                        } else {return false;}
                    } else {return false;}
                } catch (Exception e) {return false;}

            }

            @Override
            protected void onPostExecute(final Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                try {
                    webservice_online = aBoolean; // this method is the only method that can set this value
                    if (webEventListener != null) {
                        if(aBoolean) {
                            webEventListener.showHideMenuItem(WebEventListener.Action.SHOW_UPL_DL);
                        } else {
                            webEventListener.showHideMenuItem(WebEventListener.Action.HIDE_UPL_DL);
                        }
                    }
                } catch(Exception e){
                    // do nothing, there's virtually no chance this gets executed.
                }
            }
        }.execute();

    }

    public Boolean isWebServiceOnline(){
        return webservice_online;
    }

    @SuppressLint("StaticFieldLeak")
    public void createSharedPreferenceObject(final Context context) {

        @SuppressLint("HardwareIds") final String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        final SharedPreferencePayload spp = new SharedPreferencePayload(android_id);

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);

                Map<String, ?> keys = prefs.getAll();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    spp.addElement(android_id, entry.getKey(), entry.getValue().toString(), "DBRV1");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try{
                    uploadSharedPreferencePayload(spp, "sharedpreference", context); // <-- a asynchronous task is defined in here
                } catch(Exception e){
                    Log.e(context.getString(R.string.takenote_errortag), e.getMessage());
                }
            }
        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    public void downloadSharedPreferencePayload(final Context context){

        @SuppressLint("HardwareIds") final String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        final Callresult cr = new Callresult();

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                // okHttp3 does not support a body for GET, using the device_id as a path variable -->
                Request request = new Request.Builder()
                        .url(String.format("%s%s/%s", base_url, "sharedpreference", android_id))
                        .method("GET", null)
                        .build();

                Response response = null;

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            readAnswer(new Callresult(false, e.getMessage()));
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object = null;
                            try {
                                // first cast the answer to a json object for easy handling -->
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has("device_id")) {
                                    readAnswer(new Callresult(true, context.getString(R.string.download_success)));

                                    // Now cast the answer to the expected format by using a pojo -->
                                    ObjectMapper mapper = new ObjectMapper();
                                    SharedPreferenceResponse spr = mapper.readValue(j_object.toString(), SharedPreferenceResponse.class);

                                    // Handle the pojo and execute some actions -->
                                    if(spr != null){
                                        if (webEventListener != null) {
                                            webEventListener.loadDownLoadedPreferences(spr);
                                        }
                                    } else {
                                        readAnswer(new Callresult(false, context.getString(R.string.mapping_failed)));
                                    }

                                } else { // error object is returned
                                    cr.setAnswer(false);
                                    if (j_object.has("message")) {
                                        cr.setMessage(j_object.getString("message"));
                                    }
                                }
                            } catch (JSONException e) {
                                readAnswer(new Callresult(false, e.getMessage()));
                            }
                        }
                    });
                } catch (Exception e) {
                    readAnswer(new Callresult(false, e.getMessage()));
                }
                return null;
            }
        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    public void uploadSharedPreferencePayload(final SharedPreferencePayload spp, final String action, final Context context){

        final Callresult cr = new Callresult();

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                String json_payload = "";
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    json_payload = objectMapper.writeValueAsString(spp);
                } catch (JsonProcessingException e) {
                    readAnswer(new Callresult(false, e.getMessage()));
                    return null;
                }

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();

                RequestBody body = RequestBody.create(json_payload, JSON);
                Request request = new Request.Builder()
                        .url(String.format("%s%s", base_url, action))
                        .method("POST", body)
                        .build();

                Response response = null;

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            readAnswer(new Callresult(false, e.getMessage()));
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object = null;
                            try {
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has("status")) {
                                    if ((j_object.getString("status")).equalsIgnoreCase("1")) {
                                        readAnswer(new Callresult(true, context.getString(R.string.upload_success)));
                                    } else {
                                        cr.setAnswer(false);
                                        if (j_object.has("message")) {
                                            cr.setMessage(j_object.getString("message"));
                                        }
                                        readAnswer(cr);
                                    }
                                } else {
                                    cr.setAnswer(false);
                                    if (j_object.has("message")) {
                                        cr.setMessage(j_object.getString("message"));
                                    }
                                    readAnswer(cr);
                                }
                            } catch (JSONException e) {
                                readAnswer(new Callresult(false, e.getMessage()));
                            }
                        }
                    });
                } catch (Exception e) {
                    readAnswer(new Callresult(false, e.getMessage()));
                }
                return null;
            }
        }.execute();
    }

    private void readAnswer(Callresult cr){
        Log.d("DB", String.valueOf(cr.getAnswer()));
        Log.d("DB", cr.getMessage());
    }

    private class Callresult{
        private Boolean answer = false;
        private String message = "";

        public Callresult() {
        }

        public Callresult(Boolean answer, String message) {
            this.answer = answer;
            this.message = message;
        }

        public Boolean getAnswer() {
            return answer;
        }

        public void setAnswer(Boolean answer) {
            this.answer = answer;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }


    }

}
