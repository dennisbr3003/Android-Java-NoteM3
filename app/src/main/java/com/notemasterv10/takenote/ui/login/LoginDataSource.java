package com.notemasterv10.takenote.ui.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.notemasterv10.takenote.constants.WebServiceConstants;
import com.notemasterv10.takenote.listeners.LoginEventListener;
import com.notemasterv10.takenote.webservice.Encryption;
import com.notemasterv10.takenote.webservice.WebService;
import com.notemasterv10.takenote.webservice.WebUser;

import okhttp3.MediaType;

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

    private WebService ws = new WebService();

    public void setLoginEventListener(LoginEventListener loginEventListener) {
        this.loginEventListener = loginEventListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void register(String username, String password){

        Encryption encryption = new Encryption();
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // encrypt sensitive information before it's being send -->
        android_id = String.format("%s%s%s", System.currentTimeMillis() / 1000L, "-", android_id);
        String f_Android_id = encryption.encrypt(android_id);
        String f_User_Password = encryption.encrypt(password);

        // create the websuer with the credentials so it can be verified and uploaded -->
        WebUser webuser = new WebUser(username, f_User_Password, f_Android_id, USER_ROLE);
        ws.setLoginEventListener(getLoginEventListener());

        // this option is only available if the connection is established so we do not need to wake up the service -->
        ws.registerUser(webuser, this.context);

    }

    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void login(final String username, final String password) {

        Encryption encryption = new Encryption();
        @SuppressLint("HardwareIds") String android_id = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // encrypt sensitive information before it's being send -->
        android_id = String.format("%s%s%s", System.currentTimeMillis() / 1000L, "-", android_id);
        final String f_Android_id = encryption.encrypt(android_id);
        final String f_User_Password = encryption.encrypt(password);

        // create the websuer with the credentials so it can be verified -->
        final WebUser webuser = new WebUser(username, f_User_Password, f_Android_id, USER_ROLE);

        ws.setLoginEventListener(getLoginEventListener());
        ws.verifyLoginCredentials(webuser, this.context);

    }

    public void logout() {
        // TODO: revoke authentication
    }

}