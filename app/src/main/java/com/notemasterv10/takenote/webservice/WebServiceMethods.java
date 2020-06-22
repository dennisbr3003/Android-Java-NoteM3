package com.notemasterv10.takenote.webservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.constants.WebServiceConstants;
import com.notemasterv10.takenote.database.ImageTable;
import com.notemasterv10.takenote.database.NoteTable;
import com.notemasterv10.takenote.listeners.WebEventListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebServiceMethods extends AppCompatActivity implements NoteMasterConstants, WebServiceConstants {

    private final String base_url = BASE_URL;

    private String json_response;
    public static final MediaType JSON = MediaType.parse(JSON_UTF8);

    private WebEventListener webEventListener;
    private Context context;
    private Handler mHandler = new Handler();

    public WebEventListener getWebEventListener() {
        return webEventListener;
    }

    public void setWebEventListener(WebEventListener webEventListener) {
        this.webEventListener = webEventListener;
    }

    @SuppressLint("StaticFieldLeak")
    public void createUserDataObject(final Context context) {

        @SuppressLint("HardwareIds") final String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        this.context = context;

        final UserDataPayload spp = new UserDataPayload(android_id);
        final NoteTable noteTable = new NoteTable(context);
        final ImageTable imageTable = new ImageTable(context);

        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                UUID uuid = UUID.randomUUID(); // <-- used in the webservice to identify a batch
                SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);

                // shared preferences
                Map<String, ?> keys = prefs.getAll();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    spp.addElement(android_id, entry.getKey(), entry.getValue().toString(), uuid.toString());
                }

                // notes
                spp.setNoteList(noteTable.getNoteListing());

                // passpoint image
                spp.setPassPointImageList(imageTable.getPassPointImageListing());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try{
                    uploadUserDataPayload(spp, WEBSERVICE_PATH, context); // <-- a asynchronous task is defined in here
                } catch(Exception e){
                    Log.e(context.getString(R.string.ErrorTag), e.getMessage());
                }
            }
        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    public void downloadUserDataPayload(final Context context){

        @SuppressLint("HardwareIds") final String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        this.context = context;

        Log.d("DB", android_id);

        final Callresult cr = new Callresult();

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                // okHttp3 does not support a body for GET, using the device_id as a path variable -->
                Request request = new Request.Builder()
                        .url(String.format("%s%s/%s", base_url, "userdata", android_id))
                        .method("GET", null)
                        .build();

                Response response = null;

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            //readAnswer(new Callresult(false, e.getMessage()));
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object = null;
                            try {
                                // first cast the answer to a json object for easy handling -->
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has("device_id")) {
                                    //readAnswer(new Callresult(true, context.getString(R.string.DownloadSuccess)));

                                    // Now cast the answer to the expected format by using a pojo -->
                                    ObjectMapper mapper = new ObjectMapper();
                                    UserDataResponse spr = mapper.readValue(j_object.toString(), UserDataResponse.class);

                                    Log.d("DB", String.valueOf(spr!=null));
                                    if(spr!=null){
                                        Log.d("DB", String.valueOf(spr.getNoteArraySize()));
                                        Log.d("DB", String.valueOf(spr.getPasspointImageArraySize()));
                                    }

                                    // Handle the pojo and execute some actions -->
                                    if(spr != null){
                                        if (webEventListener != null) {
                                            webEventListener.loadDownLoadedPreferences(spr);
                                        }
                                    } else {
                                        //readAnswer(new Callresult(false, context.getString(R.string.MappingFailed)));
                                    }

                                } else { // error object is returned
                                    cr.setAnswer(false);
                                    if (j_object.has("message")) {
                                        cr.setMessage(j_object.getString("message"));
                                    }
                                }
                            } catch (JSONException e) {
                                //readAnswer(new Callresult(false, e.getMessage()));
                            }
                        }
                    });
                } catch (Exception e) {
                    //readAnswer(new Callresult(false, e.getMessage()));
                }
                return null;
            }
        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    public void uploadUserDataPayload(final UserDataPayload spp, final String action, final Context context){

        this.context = context;

        AlertDialog.Builder builder = new AlertDialog.Builder(context); // no title, icon or message
        builder.setCancelable(false); // block back-button
        LayoutInflater inf = LayoutInflater.from(context); // set op the dialog extra layout (cloud_up_dialog.xml)
        View cloudDialogExtraLayout = inf.inflate(R.layout.cloud_up_dialog, null);
        builder.setView(cloudDialogExtraLayout); // load the view into the dialog
        final AlertDialog dlg = builder.create();

        dlg.show();

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                String json_payload = "";
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    json_payload = objectMapper.writeValueAsString(spp);
                } catch (final JsonProcessingException e) {
                    dlg.dismiss();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showClickableErrorDialog(new Callresult(false, e.getMessage()));
                        }
                    });
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
                    Thread.sleep(2000); // fake slow upload so the dialog will show
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                            dlg.dismiss();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showClickableErrorDialog(new Callresult(false, e.getMessage()));
                                }
                            });
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object = null;
                            try {
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has(RESPONSE_STATUS)) {
                                    if ((j_object.getString(RESPONSE_STATUS)).equalsIgnoreCase(IS_SUCCESS)) {
                                        // interact with UI
                                        dlg.dismiss();
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showClickableErrorDialog(new Callresult(true, context.getString(R.string.UploadSuccess)));
                                            }
                                        });
                                    } else {
                                        dlg.dismiss();
                                        final Callresult cr = new Callresult(false, context.getString(R.string.NoErrorMessage));
                                        if (j_object.has("message")) {
                                            cr.setMessage(j_object.getString("message"));
                                        }
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showClickableErrorDialog(cr);
                                            }
                                        });
                                    }
                                } else {
                                    dlg.dismiss();
                                    final Callresult cr = new Callresult(false, context.getString(R.string.NoErrorMessage));
                                    if (j_object.has("message")) {
                                        cr.setMessage(j_object.getString("message"));
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showClickableErrorDialog(cr);
                                        }
                                    });                                    }
                            } catch (final JSONException e) {
                                dlg.dismiss();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showClickableErrorDialog(new Callresult(false, e.getMessage()));
                                    }
                                });
                            }
                        }
                    });
                } catch (final Exception e) {
                    dlg.dismiss();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showClickableErrorDialog(new Callresult(false, e.getMessage()));
                        }
                    });
                }
                dlg.dismiss(); // everything went ok
                return null;
            }
        }.execute();
    }

    private void showClickableErrorDialog(Callresult cr){

        Log.d("DB", "showCilckableErrorDialog");
        Log.d("DB", String.valueOf(cr.getAnswer()));
        Log.d("DB", cr.getMessage());

        if (!cr.getAnswer()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inf = LayoutInflater.from(context);
            View cloudDialogExtraLayout = inf.inflate(R.layout.cloud_sync_error, null);
            builder.setView(cloudDialogExtraLayout);
            TextView tv = (TextView) cloudDialogExtraLayout.findViewById(R.id.txtViewErrorMessage);
            tv.setText(cr.getMessage());
            final AlertDialog dlg = builder.create();

            cloudDialogExtraLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });

            dlg.show();
        }
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
