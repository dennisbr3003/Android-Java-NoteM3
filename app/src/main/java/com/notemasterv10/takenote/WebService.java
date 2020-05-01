package com.notemasterv10.takenote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebService extends AppCompatActivity {

    private final String base_url = "http://192.168.178.69:8080/notemaster/";

    public void checkForWebService(final Context context, final View view){

        final String action = "test"; // <-- This action is used to test the webservice. It only checks if the
                                      //      the webservice is online and if the database can be connected.

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Boolean> asynctask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {

                String json_response;

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
                try {
                    ImageView im = (ImageView) view.findViewById(R.id.imgStatus);
                    if (aBoolean) {
                        Log.d("DB", "online");
                        im.setImageResource(R.mipmap.webservice_online);
                    } else {
                        Log.d("DB", "offline");
                        im.setImageResource(R.mipmap.webservice_offline);
                    }
                } catch(Exception e){
                    // do nothing
                }
            }
        }.execute();

    }

}
