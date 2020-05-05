package com.notemasterv10.takenote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public class WebService extends AppCompatActivity implements Constants {

    private final String base_url = "http://192.168.178.69:8080/notemaster/";

    private static Boolean webservice_online = false;
    private String json_response;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public void checkForWebService(final Context context, final View view){

        final String action = "test"; // <-- This action is used to test the webservice. It only checks if the
                                      //      the webservice is online and if the database can be connected.

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Boolean> asynctask = new AsyncTask<Void, Void, Boolean>() {

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
                ImageView im = (ImageView) view.findViewById(R.id.imgStatus);
                TextView tv = (TextView) view.findViewById(R.id.textview_status);
                try {
                    if (aBoolean) {
                        webservice_online = true; // this method is the only method that can set this value
                        im.setImageResource(R.mipmap.webservice_online);
                        tv.setText(R.string.ws_avail);
                        } else {
                        Log.d("DB", "offline");
                        webservice_online = false;
                        im.setImageResource(R.mipmap.webservice_offline);
                        tv.setText(R.string.ws_unavail);
                    }
                } catch(Exception e){
                    webservice_online = false;
                    im.setImageResource(R.mipmap.webservice_offline);
                    tv.setText(R.string.ws_unavail);
                    // do nothing
                }
            }
        }.execute();

    }

    public Boolean isWebServiceOnline(){
        return webservice_online;
    }

    public void createSharedPreferenceObject(final Context context) {

        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        SharedPreferencePayload spp = new SharedPreferencePayload(android_id);
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);

        Map<String, ?> keys = prefs.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            spp.addElement(android_id, entry.getKey(), entry.getValue().toString(), "dennisbr");
        }

        uploadSharedPreferencePayload(spp, "sharedpreference");

    }

    public void uploadSharedPreferencePayload(final SharedPreferencePayload spp, final String action){

        final Callresult cr = new Callresult();

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {

                String json_payload = "";
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    json_payload = objectMapper.writeValueAsString(spp);
                } catch (JsonProcessingException e) {
                    cr.setAnswer(false);
                    cr.setMessage(e.getMessage());
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
                            cr.setAnswer(false);
                            cr.setMessage(e.getMessage());
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object = null;
                            try {
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has("status")) {
                                    if ((j_object.getString("status")).equalsIgnoreCase("1")) {
                                        cr.setAnswer(true);
                                        cr.setMessage(getString(R.string.upload_success));
                                        readAnswer(cr);
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
                                cr.setAnswer(false);
                                cr.setMessage(e.getMessage());
                                readAnswer(cr);
                            }
                        }
                    });
                } catch (Exception e) {
                    cr.setAnswer(false);
                    cr.setMessage(e.getMessage());
                    readAnswer(cr);
                }
                return null;
            }
        }.execute();
    }

    private void readAnswer(Callresult cr){
        Log.d(getString(R.string.takenote_infotag), String.valueOf(cr.getAnswer()));
        Log.d(getString(R.string.takenote_infotag), cr.getMessage());
    }

    private class Callresult{
        private Boolean answer = false;
        private String message = "";

        public Callresult() {
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
