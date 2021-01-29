package com.notemasterv10.takenote.ui.login;

import android.app.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listeners.LoginEventListener;
import com.notemasterv10.takenote.webservice.WebUser;

public class LoginActivity extends AppCompatActivity implements LoginEventListener {

    private LoginViewModel loginViewModel;
    LoginDataSource loginDataSource = new LoginDataSource();
    SharedResource sr = new SharedResource();
    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.pBar);

        loginDataSource.setLoginEventListener(this); // listener
        loginDataSource.setContext(this);            // context

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    hideKeyBoard();
                    if (((String) loginButton.getText()).equalsIgnoreCase("Register")){
                        loginDataSource.register(usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    } else {
                        loginDataSource.login(usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    }
                }
                return false;

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                loadingProgressBar.setVisibility(View.VISIBLE);
                hideKeyBoard();
                if (((String) loginButton.getText()).equalsIgnoreCase("Register")){
                  loginDataSource.register(usernameEditText.getText().toString(),
                          passwordEditText.getText().toString());
                } else {
                    loginDataSource.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }

            }
        });

        if (sr.getUserRegistration(this)){
            loginButton.setText(R.string.login_caption);
        }else{
            loginButton.setText(R.string.register_caption);
        }
    }

    private void hideKeyBoard(){
        // this code works if executed from an activity (not from a fragment) -->
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void processLogin(WebUser webuser, Action action) {

        Log.d("DENNIS_BRINK", "Event listener of the registration and login event (LoginActivity.processLogin)");

        // shut down the progress bar -->
        ProgressBar loadingProgressBar = findViewById(R.id.pBar);
        loadingProgressBar.setVisibility(View.INVISIBLE);

        // the values will be passed back to the calling activity -->
        if (webuser != null) {
            Login.getInstance().setWebuser(webuser);
            setResult(Activity.RESULT_OK);
            if(action.equals(Action.REGISTER)) {
                sr.saveUserRegistration(true, this); // shared preference
                sr.insertUser(this, webuser); // database
                showRegistrationDialog(); // finish() is executed in the onClickListener of this dialog -->
            } else {
                finish();
            }
        } else {
            if(action.equals(Action.REGISTER)) {
                Login.getInstance().setWebuser(null);
                sr.saveUserRegistration(false, this);
            }
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

    }

    private void showRegistrationDialog(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showClickableRegistrationDialog();
            }
        });
    }

    private void showClickableRegistrationDialog(){

        ProgressBar pb = findViewById(R.id.pBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inf = LayoutInflater.from(this);
        View registrationDialogExtraLayout = inf.inflate(R.layout.registration_dialog, null);
        builder.setView(registrationDialogExtraLayout);
        AlertDialog dlg = builder.create();
        dlg.setCancelable(false);
        dlg.setCanceledOnTouchOutside(false);
        final AlertDialog f_dlg = dlg; // because it is used in another thread it has to be final

        if(pb != null) {
            pb.setVisibility(View.INVISIBLE);
        }

        registrationDialogExtraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f_dlg.dismiss();
                finish();
            }
        });

        dlg.show();
    }

}