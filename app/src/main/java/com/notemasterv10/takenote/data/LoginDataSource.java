package com.notemasterv10.takenote.data;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.constants.WebServiceConstants;
import com.notemasterv10.takenote.data.model.LoggedInUser;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listeners.LoginEventListener;
import com.notemasterv10.takenote.webservice.Encryption;
import com.notemasterv10.takenote.webservice.WebUser;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource implements WebServiceConstants {

    private static final MediaType JSON = MediaType.parse(JSON_UTF8);
    private Handler mHandler = new Handler();
    private Context context;
    private LoginEventListener loginEventListener;

    public void setContext(Context context) {
        this.context = context;
    }

    public LoginEventListener getLoginEventListener() {
        return loginEventListener;
    }

    public void setLoginEventListener(LoginEventListener loginEventListener) {
        this.loginEventListener = loginEventListener;
    }

    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Result<LoggedInUser> login(final String username, final String password) {

        Encryption encryption = new Encryption();
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // encrypt sensitive information before it's being send -->
        android_id = String.format("%s%s%s", System.currentTimeMillis() / 1000L, "-", android_id);
        final String f_Android_id = encryption.encrypt(android_id);
        final String f_User_Password = encryption.encrypt(password);

        // create the websuer with the credentials so it can be verified -->
        final WebUser webuser = new WebUser(username, f_User_Password, f_Android_id, USER_ROLE);

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
                                                showErrorDialog(e.getMessage());
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
                                                                        loginEventListener.processLogin(webuser);
                                                                    }
                                                                }
                                                            }else { // bad credentials, user could not be verified -->
                                                                showErrorDialog(context.getResources().getString(R.string.bad_credentials));
                                                            }
                                                        } catch (JSONException e) {
                                                            showErrorDialog(e.getMessage());
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                                        showErrorDialog(e.getMessage());
                                                    }

                                                });

                                            } catch (Exception e) { // call failed completely -->
                                                showErrorDialog(e.getMessage());
                                            }
                                        }
                                    } else {
                                        showErrorDialog(context.getResources().getString(R.string.unknown_format_error));
                                    }
                                } catch (JSONException e) {
                                    showErrorDialog(e.getMessage());
                                }
                            }

                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                // webservice could not be pinged. It probably is not alive -->
                                showErrorDialog(e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        showErrorDialog(e.getMessage());
                    }
                    return null;
                }
            }.execute();
        } catch (Exception e) {
            showErrorDialog(e.getMessage());
        }
        return null;
    }

    public void logout() {
        // TODO: revoke authentication
    }

    private void showErrorDialog(final String message){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showClickableSyncErrorDialog(new LoginDataSource.Callresult(false, message));
            }
        });
    }

    private void showClickableSyncErrorDialog(LoginDataSource.Callresult cr){

        if (!cr.getAnswer()) {

            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inf = LayoutInflater.from(context);
                View cloudDialogExtraLayout = inf.inflate(R.layout.cloud_sync_error, null);
                builder.setView(cloudDialogExtraLayout);
                TextView tv = cloudDialogExtraLayout.findViewById(R.id.txtViewErrorMessage);
                tv.setText(cr.getMessage());
                TextView errorHeader = cloudDialogExtraLayout.findViewById(R.id.txtViewErrorHeader);
                errorHeader.setText(R.string.cloud_auth_failed);
                final AlertDialog dlg = builder.create();

                cloudDialogExtraLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                        if (loginEventListener != null){
                            loginEventListener.processLogin(null);
                        }
                    }
                });

                dlg.show();

            } catch (Exception e) {
                Log.d("DENNIS_BRINK", "Error when trying to build and show a dialog. Details: " + e.getMessage());
                if (loginEventListener != null){
                    loginEventListener.processLogin(null);
                }
            }
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