package com.notemasterv10.takenote.webservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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

    private static final MediaType JSON = MediaType.parse(JSON_UTF8);

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

        final UserDataPayload userDataPayload = new UserDataPayload(android_id);
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
                    userDataPayload.addElement(android_id, entry.getKey(), entry.getValue().toString(), uuid.toString());
                }

                // notes
                userDataPayload.setNoteList(noteTable.getNoteListing());

                // passpoint image
                userDataPayload.setPassPointImageList(imageTable.getPassPointImageListing());

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                try{
                    uploadUserDataPayload(userDataPayload, context); // <-- a asynchronous task is defined in here
                } catch(Exception e){
                    Log.e(context.getString(R.string.ErrorTag), e.getMessage());
                }
            }
        }.execute();

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("StaticFieldLeak")
    private void downloadUserDataPayload(final Context context){

        final Encryption encryption = new Encryption();

        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        this.context = context;

        android_id = String.format("%s%s%s", System.currentTimeMillis() / 1000L, "-", android_id);
        final String f_Android_id = encryption.encrypt(android_id);

        final AlertDialog dlg = createProgressDialog(SyncDirection.DOWN);
        dlg.show();

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                // okHttp3 does not support a body for GET, using the device_id as a path variable -->
                Request request = new Request.Builder()
                        .url(String.format("%s%s/%s", BASE_URL, PROC_USER_DATA, f_Android_id))
                        .method("GET", null)
                        .build();

                Response response = null;

                try {
                    Thread.sleep(2000); // fake slow download so the dialog will show
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    dlg.dismiss();
                                    showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                                }
                            });
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object;
                            try {
                                // first cast the answer to a json object for easy handling -->
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has(SIGNATURE_KEY)) {

                                    // Now cast the answer to the expected format by using a pojo -->
                                    ObjectMapper mapper = new ObjectMapper();
                                    UserDataResponse userDataResponse = mapper.readValue(j_object.toString(), UserDataResponse.class);

                                    // Handle the pojo and execute some actions -->
                                    if(userDataResponse != null){

                                        // pass the dialog along to the listener and dismiss it there
                                        if (webEventListener != null) {
                                            webEventListener.loadDownLoadedUserData(userDataResponse, dlg);
                                        }

                                    } else {
                                        dlg.dismiss();
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showClickableSyncErrorDialog(new Callresult(false, context.getString(R.string.MappingFailed)));
                                            }
                                        });
                                    }

                                } else { // error object is returned
                                    final Callresult cr = new Callresult(false, context.getString(R.string.NoErrorMessage));
                                    if (j_object.has(SIGNATURE_FIELD)) {
                                        cr.setMessage(j_object.getString(SIGNATURE_FIELD));
                                    }
                                    dlg.dismiss();
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showClickableSyncErrorDialog(cr);
                                        }
                                    });

                                }
                            } catch (final JSONException e) {
                                dlg.dismiss();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
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
                            showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                        }
                    });
                }
                return null;
            }
        }.execute();

    }

    @SuppressLint("StaticFieldLeak")
    private void uploadUserDataPayload(final UserDataPayload userDataPayload, final Context context){

        this.context = context;

        final AlertDialog dlg = createProgressDialog(SyncDirection.UP);
        dlg.show();

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                String json_payload;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    json_payload = objectMapper.writeValueAsString(userDataPayload);
                } catch (final JsonProcessingException e) {
                    dlg.dismiss();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                        }
                    });
                    return null;
                }

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();

                RequestBody body = RequestBody.create(json_payload, JSON);
                Request request = new Request.Builder()
                        .url(String.format("%s%s", BASE_URL, PROC_USER_DATA))
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
                                    showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                                }
                            });
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object;
                            try {
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has(RESPONSE_STATUS)) {
                                    if ((j_object.getString(RESPONSE_STATUS)).equalsIgnoreCase(IS_SUCCESS)) {
                                        // interact with UI
                                        dlg.dismiss();
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showClickableSyncErrorDialog(new Callresult(true, context.getString(R.string.UploadSuccess)));
                                            }
                                        });
                                    } else {
                                        dlg.dismiss();
                                        final Callresult cr = new Callresult(false, context.getString(R.string.NoErrorMessage));
                                        if (j_object.has(SIGNATURE_FIELD)) {
                                            cr.setMessage(j_object.getString(SIGNATURE_FIELD));
                                        }
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showClickableSyncErrorDialog(cr);
                                            }
                                        });
                                    }
                                } else {
                                    dlg.dismiss();
                                    final Callresult cr = new Callresult(false, context.getString(R.string.NoErrorMessage));
                                    if (j_object.has(SIGNATURE_FIELD)) {
                                        cr.setMessage(j_object.getString(SIGNATURE_FIELD));
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showClickableSyncErrorDialog(cr);
                                        }
                                    });
                                }
                            } catch (final JSONException e) {
                                dlg.dismiss();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
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
                            showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                        }
                    });
                }
                dlg.dismiss(); // everything went ok
                return null;
            }
        }.execute();
    }

    private void showClickableSyncErrorDialog(Callresult cr){

        if (!cr.getAnswer()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inf = LayoutInflater.from(context);
            View cloudDialogExtraLayout = inf.inflate(R.layout.cloud_sync_error, null);
            builder.setView(cloudDialogExtraLayout);
            TextView tv = cloudDialogExtraLayout.findViewById(R.id.txtViewErrorMessage);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("StaticFieldLeak")
    public void preDownloadCheck(final Context context){

        final Encryption encryption = new Encryption();

        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        this.context = context;

        android_id = String.format("%s%s%s", System.currentTimeMillis() / 1000L, "-", android_id);
        final String f_Android_id = encryption.encrypt(android_id);

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                // okHttp3 does not support a body for GET, using the device_id as a path variable -->
                Request request = new Request.Builder()
                        .url(String.format("%s%s/%s", BASE_URL, DEVICE_HAS_DATA, f_Android_id))
                        .method("GET", null)
                        .build();

                Response response = null;

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                                }
                            });
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object;
                            try {
                                // first cast the answer to a json object for easy handling -->
                                j_object = new JSONObject(response.body().string());

                                if (j_object.has(RESPONSE_STATUS)) {

                                    if(j_object.getString(RESPONSE_STATUS).equals(IS_SUCCESS)){
                                        // device has uploaded data
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showConfirmDialog(true); // actual download is triggered here
                                            }
                                        });
                                    } else {
                                        // device does not have uploaded data
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showConfirmDialog(false); // actual download is triggered here
                                            }
                                        });

                                    }

                                } else { // error object is returned
                                    final Callresult cr = new Callresult(false, context.getString(R.string.NoErrorMessage));
                                    if (j_object.has(SIGNATURE_FIELD)) {
                                        cr.setMessage(j_object.getString(SIGNATURE_FIELD));
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showClickableSyncErrorDialog(cr);
                                        }
                                    });

                                }
                            } catch (final JSONException e) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                                    }
                                });
                            }
                        }
                    });
                } catch (final Exception e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showClickableSyncErrorDialog(new Callresult(false, e.getMessage()));
                        }
                    });
                }
                return null;
            }
        }.execute();
    }

    private void showConfirmDialog(final boolean deviceHasData){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        StringBuilder sb = new StringBuilder();

        builder.setTitle(R.string.ConfirmAction);
        builder.setIcon(R.mipmap.dialog_orange_warning);
        if(deviceHasData) {
            sb.append(String.format("%s\n\n", context.getString(R.string.DeviceHasData)));
            sb.append(String.format("%s", context.getString(R.string.AreYouSure)));
            builder.setMessage(sb.toString());
        } else {
            sb.append(String.format("%s\n\n", context.getString(R.string.DeviceHasNoData)));
            sb.append(String.format("%s", context.getString(R.string.AreYouSure)));
            builder.setMessage(sb.toString());
        }
        builder.setCancelable(false); // <-- block back-button

        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadUserDataPayload(context);
            }
        });

        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dlg = builder.create();
        dlg.show();

    }

    private AlertDialog createProgressDialog(SyncDirection syncDirection){

        AlertDialog.Builder builder = new AlertDialog.Builder(context); // no title, icon or message
        builder.setCancelable(false); // block back-button

        LayoutInflater inf = LayoutInflater.from(context); // set op the dialog extra layout (cloud_sync_dialog_animated.xml)
        View cloudDialogExtraLayout = inf.inflate(R.layout.cloud_sync_dialog_animated, null);

        TextView tv = cloudDialogExtraLayout.findViewById(R.id.txtViewProgressHeader);
        if(syncDirection.equals(SyncDirection.UP)) {
            tv.setText(R.string.UploadingUserData);
        } else {
            tv.setText(R.string.DownloadingUserData);
        }

        builder.setView(cloudDialogExtraLayout); // load the view into the dialog

        AlertDialog dlg = builder.create();
        return dlg;

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
