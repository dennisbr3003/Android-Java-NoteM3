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
import com.notemasterv10.takenote.listeners.LoginEventListener;
import com.notemasterv10.takenote.listeners.WebEventListener;
import com.notemasterv10.takenote.ui.login.Login;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebService extends AppCompatActivity implements NoteMasterConstants, WebServiceConstants {

    private static final MediaType JSON = MediaType.parse(JSON_UTF8);

    private WebEventListener webEventListener;
    private LoginEventListener loginEventListener;
    private Context context;
    private Handler mHandler = new Handler();

    public WebEventListener getWebEventListener() {
        return webEventListener;
    }

    public void setWebEventListener(WebEventListener webEventListener) {
        this.webEventListener = webEventListener;
    }

    public LoginEventListener getLoginEventListener() {
        return loginEventListener;
    }

    public void setLoginEventListener(LoginEventListener loginEventListener) {
        this.loginEventListener = loginEventListener;
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

                String basicAuthorization = Credentials.basic(Login.getInstance().getWebuser().getName(), Login.getInstance().getWebuser().getPassword());

                // okHttp3 does not support a body for GET, using the device_id as a path variable -->
                Request request = new Request.Builder()
                        .url(String.format("%s%s/%s", BASE_URL, PROC_USER_DATA, f_Android_id))
                        .method("GET", null)
                        .addHeader("Authorization", basicAuthorization)
                        .build();

                Response response = null;

                try {
                    Thread.sleep(2000); // fake slow download so the dialog will show
                } catch (InterruptedException e) {
                    showSyncErrorDialog(e.getMessage());
                }

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                            dlg.dismiss();
                            showSyncErrorDialog(e.getMessage());
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
                                        showSyncErrorDialog("userdata did not download successfully. Nothing was retrieved");
                                    }

                                } else { // error object is returned
                                    dlg.dismiss();
                                    if (j_object.has(SIGNATURE_FIELD)) {
                                        showSyncErrorDialog(j_object.getString(SIGNATURE_FIELD));
                                    }else {
                                        showSyncErrorDialog(context.getString(R.string.NoErrorMessage));
                                    }
                                }
                            } catch (final JSONException e) {
                                dlg.dismiss();
                                showSyncErrorDialog(e.getMessage());
                            }
                        }
                    });
                } catch (final Exception e) {
                    dlg.dismiss();
                    showSyncErrorDialog(e.getMessage());
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

        Log.d("DENNIS_BRINK", "1: Dialog alive ? " + dlg.isShowing());


        new AsyncTask<Void, Void, Void>(){

            @RequiresApi(api = Build.VERSION_CODES.O)
            protected Void doInBackground(Void... voids) {

                String json_payload;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    json_payload = objectMapper.writeValueAsString(userDataPayload);
                } catch (final JsonProcessingException e) {
                    showSyncErrorDialog(e.getMessage());
                    return null;
                }

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();

                String basicAuthorization = Credentials.basic(Login.getInstance().getWebuser().getName(), Login.getInstance().getWebuser().getPassword());

                Log.d("DENNIS_BRINK", "Username: " + Login.getInstance().getWebuser().getName());
                Log.d("DENNIS_BRINK", "Basic authorization string: " + basicAuthorization);

                RequestBody body = RequestBody.create(json_payload, JSON);
                Request request = new Request.Builder()
                        .url(String.format("%s%s", BASE_URL, PROC_USER_DATA))
                        .method("POST", body)
                        .addHeader("Authorization", basicAuthorization)
                        .build();

                Response response = null;

                try {
                    Thread.sleep(2000); // fake slow upload so the dialog will show
                } catch (InterruptedException e) {
                    showSyncErrorDialog(e.getMessage());
                }

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                            dlg.dismiss();
                            showSyncErrorDialog(e.getMessage());
                        }

                        // call m.b.v.enqueue is zelf asynchrone, onPostExecute is hier dan niet nodig; alles gaat via de callback
                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            JSONObject j_object;
                            try {
                                j_object = new JSONObject(response.body().string());
                                if (j_object.has(RESPONSE_STATUS)) {
                                    if ((j_object.getString(RESPONSE_STATUS)).equalsIgnoreCase(IS_SUCCESS)) {
                                        Log.d("DENNIS_BRINK", "Upload complete, showing confirmation dialog");
                                        showSyncCompleteDialog(context.getString(R.string.UploadSuccess), dlg);
                                    } else {
                                        dlg.dismiss();
                                        if (j_object.has(SIGNATURE_FIELD)) {
                                            showSyncErrorDialog(j_object.getString(SIGNATURE_FIELD));
                                        }else {
                                            showSyncErrorDialog(context.getString(R.string.NoErrorMessage));
                                        }
                                    }
                                } else {
                                    dlg.dismiss();
                                    if (j_object.has(SIGNATURE_FIELD)) {
                                        showSyncErrorDialog(j_object.getString(SIGNATURE_FIELD));
                                    }else {
                                        showSyncErrorDialog(context.getString(R.string.NoErrorMessage));
                                    }
                                }
                            } catch (final JSONException e) {
                                dlg.dismiss();
                                showSyncErrorDialog(e.getMessage());
                            }
                        }
                    });
                } catch (final Exception e) {
                    dlg.dismiss();
                    showSyncErrorDialog(e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void verifyLoginCredentials (final WebUser webuser, final Context context) {

        this.context = context;

        try {

            new AsyncTask<Void, Void, Void>(){

                @Override
                protected Void doInBackground(Void... voids) {

                    /*
                       since the webservice is hosted on Heroku (free) server we have to wake-up the
                       service first. It goes to sleep after half an hour without activity so we have to
                       ping the service to see it's alive before we send the credentials -->
                    */

                    OkHttpClient client = new OkHttpClient().newBuilder().build();

                    Request request = new Request.Builder()
                            .url(String.format("%s%s", BASE_URL, CONN_IS_ALIVE))
                            .method("GET", null)
                            .build();
                    try {
                        client.newCall(request).enqueue(new Callback(){

                            @SuppressLint("StaticFieldLeak")
                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                                JSONObject j_object;
                                try {
                                    j_object = new JSONObject(response.body().string());
                                    if(j_object.has(RESPONSE_STATUS)){
                                        if(((String) j_object.get(RESPONSE_STATUS)).equalsIgnoreCase(IS_SUCCESS)) {

                                            // The webservice is live, so we can send a new call to verify credentials -->
                                            String json_payload="";
                                            ObjectMapper objectMapper = new ObjectMapper();
                                            try {
                                                json_payload = objectMapper.writeValueAsString(webuser);
                                            } catch (JsonProcessingException e) {
                                                showLoginErrorDialog(e.getMessage());
                                            }

                                            OkHttpClient client = new OkHttpClient().newBuilder().build();

                                            RequestBody body = RequestBody.create(json_payload, JSON);
                                            Request request = new Request.Builder()
                                                    .url(String.format("%s%s", BASE_URL, AUTH_USER))
                                                    .method("POST", body)
                                                    .build();

                                            // try and verify the user with the credentials it is registered with -->
                                            try {
                                                client.newCall(request).enqueue(new Callback() {

                                                    @Override
                                                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                                                        JSONObject j_object;

                                                        try {
                                                            j_object = new JSONObject(response.body().string());
                                                            if (j_object.has(RESPONSE_STATUS)) {
                                                                if (((String) j_object.get(RESPONSE_STATUS)).equalsIgnoreCase(IS_SUCCESS)) {
                                                                    if (loginEventListener != null){ // user credentials verified successfully, continue -->
                                                                        loginEventListener.processLogin(webuser, LoginEventListener.Action.LOGIN);
                                                                    }
                                                                }
                                                            }else { // bad credentials, user could not be verified -->
                                                                showLoginErrorDialog(context.getResources().getString(R.string.bad_credentials));
                                                            }
                                                        } catch (JSONException e) {
                                                            showLoginErrorDialog(e.getMessage());
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                                        showLoginErrorDialog(e.getMessage());
                                                    }

                                                });

                                            } catch (Exception e) { // call failed completely -->
                                                showLoginErrorDialog(e.getMessage());
                                            }
                                        }
                                    } else {
                                        showLoginErrorDialog(context.getResources().getString(R.string.unknown_format_error));
                                    }
                                } catch (JSONException e) {
                                    showLoginErrorDialog(e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                // webservice could not be pinged. It probably is not alive -->
                                showLoginErrorDialog(e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        showLoginErrorDialog(e.getMessage());
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            showLoginErrorDialog(e.getMessage());
        }
    }

    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void registerUser(final WebUser webuser, final Context context){

        this.context = context;

        new AsyncTask<Void, Void, Void>(){

            protected Void doInBackground(Void... voids) {

                String json_payload=null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    json_payload = objectMapper.writeValueAsString(webuser);
                } catch (final JsonProcessingException e) {
                    showRegistrationErrorDialog(e.getMessage());
                }
                RequestBody body = RequestBody.create(json_payload, JSON);

                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();

                Request request = new Request.Builder()
                        .url(String.format("%s%s", BASE_URL, ADD_USER))
                        .method("POST", body)
                        .build();

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                            showRegistrationErrorDialog(e.getMessage());
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
                                        // user credentials uploaded, so do something with a listener in LoginActivity
                                        Log.d("DENNIS_BRINK", "User credentials verified and uploaded. Registration complete...");
                                        Log.d("DENNIS_BRINK", "Is the loginEventListener alive ? " + String.valueOf(loginEventListener != null));
                                        if(loginEventListener != null){
                                            loginEventListener.processLogin(webuser, LoginEventListener.Action.REGISTER);
                                        }
                                    }
                                    else {
                                        // something else
                                    }

                                } else { // error object is returned
                                    if (j_object.has(SIGNATURE_FIELD)) {
                                        showRegistrationErrorDialog(j_object.getString(SIGNATURE_FIELD));
                                    }else {
                                        showRegistrationErrorDialog(context.getString(R.string.NoErrorMessage));
                                    }
                                }
                            } catch (final JSONException e) {
                                showRegistrationErrorDialog(e.getMessage());
                            }
                        }
                    });
                } catch (final Exception e) {
                    showRegistrationErrorDialog(e.getMessage());
                }
                return null;
            }
        }.execute();

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

                String basicAuthorization = Credentials.basic(Login.getInstance().getWebuser().getName(), Login.getInstance().getWebuser().getPassword());

                Request request = new Request.Builder()
                        .url(String.format("%s%s", BASE_URL, String.format(DEVICE_HAS_DATA, f_Android_id)))
                        .method("GET", null)
                        .addHeader("Authorization", basicAuthorization)
                        .build();

                Response response = null;

                try {
                    client.newCall(request).enqueue(new Callback() {

                        @Override
                        public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                            showSyncErrorDialog(e.getMessage());
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
                                    if (j_object.has(SIGNATURE_FIELD)) {
                                        showSyncErrorDialog(j_object.getString(SIGNATURE_FIELD));
                                    }else {
                                        showSyncErrorDialog(context.getString(R.string.NoErrorMessage));
                                    }
                                }
                            } catch (final JSONException e) {
                                showSyncErrorDialog(e.getMessage());
                            }
                        }
                    });
                } catch (final Exception e) {
                    showSyncErrorDialog(e.getMessage());
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

    private void showLoginErrorDialog(final String message){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showErrorDialog(new Callresult(false, message), DialogType.LOGIN);
            }
        });
    }

    private void showSyncErrorDialog(final String message){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showErrorDialog(new Callresult(false, message), DialogType.SYNCHRONIZATION);
            }
        });
    }

    private void showRegistrationErrorDialog(final String message){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showErrorDialog(new Callresult(false, message), DialogType.REGISTRATION);
            }
        });
    }

    private void showSyncCompleteDialog(final String message, final AlertDialog pDialog){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showSyncConfirmationDialog(new Callresult(false, message), pDialog);
            }
        });
    }

    private void showSyncConfirmationDialog(Callresult cr, AlertDialog pDialog) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inf = LayoutInflater.from(context);
        View dialogExtraLayout;
        TextView tv;

        dialogExtraLayout = inf.inflate(R.layout.cloud_sync_success, null);
        tv = dialogExtraLayout.findViewById(R.id.txtViewMessage);

        builder.setView(dialogExtraLayout);
        tv.setText(cr.getMessage());
        AlertDialog dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        final AlertDialog f_dlg = dlg; // because it is used in another thread it has to be final

        dialogExtraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f_dlg.dismiss();
            }
        });
        pDialog.dismiss(); /* progress bar is on this dialog */
        dlg.show();

    }

    private void showErrorDialog(Callresult cr, final DialogType dialogType){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inf = LayoutInflater.from(context);
        View errorDialogExtraLayout;
        TextView tv;

        switch (dialogType){
            case REGISTRATION:
                errorDialogExtraLayout = inf.inflate(R.layout.registration_error_dialog, null);
                tv = errorDialogExtraLayout.findViewById(R.id.txtViewRegistrationErrorMessage);
                break;
            case LOGIN:
                errorDialogExtraLayout = inf.inflate(R.layout.login_error_dialog, null);
                tv = errorDialogExtraLayout.findViewById(R.id.txtViewLoginErrorMessage);
                break;
            case SYNCHRONIZATION:
                errorDialogExtraLayout = inf.inflate(R.layout.cloud_sync_error, null);
                tv = errorDialogExtraLayout.findViewById(R.id.txtViewErrorMessage);
                break;
            default:
                 errorDialogExtraLayout = inf.inflate(R.layout.impossible_error_dialog, null);
                 tv = errorDialogExtraLayout.findViewById(R.id.txtViewImpossibleErrorMessage);
                 break;
        }

        builder.setView(errorDialogExtraLayout);
        tv.setText(cr.getMessage());
        AlertDialog dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        final AlertDialog f_dlg = dlg; // because it is used in another thread it has to be final

        errorDialogExtraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (dialogType) {
                    case REGISTRATION:
                        if (loginEventListener != null) {
                            loginEventListener.processLogin(null, LoginEventListener.Action.REGISTER);
                        }
                        break;
                    case LOGIN:
                        if (loginEventListener != null) {
                            loginEventListener.processLogin(null, LoginEventListener.Action.LOGIN);
                        }
                        break;
                }
                f_dlg.dismiss();
            }
        });

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
        dlg.setCanceledOnTouchOutside(false);
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
