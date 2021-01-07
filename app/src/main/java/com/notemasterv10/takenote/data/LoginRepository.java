package com.notemasterv10.takenote.data;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.notemasterv10.takenote.ImageActivity;
import com.notemasterv10.takenote.data.model.LoggedInUser;
import com.notemasterv10.takenote.ui.login.LoginActivity;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository extends Application {

    private static volatile LoginRepository instance;

    private LoginDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private LoggedInUser user = null;

    // private constructor : singleton access
    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
        //dataSource.setLoginEventListener();
    }


    public static LoginRepository getInstance(LoginDataSource dataSource) {
        if (instance == null) {
            instance = new LoginRepository(dataSource);
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public void logout() {
        user = null;
        dataSource.logout();
    }

    private void setLoggedInUser(LoggedInUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<LoggedInUser> login(final String username, final String password) {
        // handle login

        Log.d("DENNIS_BRINK", "Volgens mij gaan we naar 'LoginDataSource' hier... ");

        dataSource.setContext(this);
/*
        new AsyncTask<Void, Void, Result<LoggedInUser>>(){

            @Override
            protected Result<LoggedInUser> doInBackground(Void... voids) {
                Log.d("DENNIS_BRINK", "do in background 1");
                return dataSource.login(username, password);
            }

            @Override
            protected void onPostExecute(Result<LoggedInUser> loggedInUserResult) {
                super.onPostExecute(loggedInUserResult);
                Log.d("DENNIS_BRINK", "klaar");
                throwbackobject(loggedInUserResult);
            }
        }.execute();
*/
        //Result<LoggedInUser> result = dataSource.login(username, password);

        //Log.d("DENNIS_BRINK", "na het inloggen hier, we gaan het resultaat teruggeven");
/*
        if (result instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());
        }
        return result;

 */
        return null;

    }

    public Result<LoggedInUser> throwbackobject(Result<LoggedInUser> loggedInUserResult){

        Log.d("DENNIS_BRINK", "na het inloggen hier, we gaan het resultaat teruggeven");

        if (loggedInUserResult instanceof Result.Success) {
            setLoggedInUser(((Result.Success<LoggedInUser>) loggedInUserResult).getData());
        }
        return loggedInUserResult;
    }

}