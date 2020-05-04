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
                    Log.d("DB", json_response);
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
                        Log.d("DB", "online");
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
                    Log.d("DB", "offline");
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

        Log.d("DB", android_id);

        SharedPreferencePayload spp = new SharedPreferencePayload(android_id);
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);

        Map<String, ?> keys = prefs.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());

            spp.addElement(android_id, entry.getKey(), entry.getValue().toString(), "dennisbr");

        }

        Log.d("DB", spp.toString());

        uploadSharedPreferencePayload(spp, "sharedpreference");

    }

    public void uploadSharedPreferencePayload(final SharedPreferencePayload spp, final String action){

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Boolean> asynctask = new AsyncTask<Void, Void, Boolean>(){

            @Override
            protected Boolean doInBackground(Void... voids) {

                final Callresult cr = new Callresult();

                String json_payload = "";
                ObjectMapper objectMapper = new ObjectMapper();


                try {
                    json_payload = objectMapper.writeValueAsString(spp);
                    Log.d("DB-----12416", json_payload);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    cr.setAnswer(false);
                }

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();

                RequestBody body = RequestBody.create(json_payload, JSON);
                Request request = new Request.Builder()
                        .url(String.format("%s%s", base_url, action))
                        .method("POST", body)
                        .build();

                Response response = null;

                try{
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.d("DB", "Call failed " + e.getMessage());
                            cr.setAnswer(false);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object = null;
                            try {
                                j_object = new JSONObject(response.body().string());
                                if(j_object.has("status")) {
                                   if (((String) j_object.get("status")).equalsIgnoreCase("1")){
                                      Log.d("DB", "Call success; correct answer");
                                       cr.setAnswer(true);
                                   } else {
                                       Log.d("DB", "Call success; but wrong answer");
                                       cr.setAnswer(false);
                                   }
                                } else {
                                    Log.d("DB", "Call success; but wrong answer");
                                    cr.setAnswer(false);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                cr.setAnswer(false);
                            }
                        }
                    });
                }catch (Exception e) {
                    cr.setAnswer(false);
                }
                return cr.getAnswer();

            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
            }

        }.execute();

    }

    private class Callresult{
        private Boolean answer;

        public Callresult() {
        }

        public Boolean getAnswer() {
            return answer;
        }

        public void setAnswer(Boolean answer) {
            this.answer = answer;
        }
    }

}
