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
//todo check if both field are empty?
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
//todo check if both field are empty?
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
        //this code works if executed from an activity (not from a fragment) -->
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public void processLogin(WebUser webuser, Action action) {
        Log.d("DENNIS_BRINK", "Ok dit is het event dat de listener is van het login event");
        // the values will be passed back to the calling activity -->
        if (webuser != null) {
            Login.getInstance().setWebuser(webuser);
            // todo run some method to save to user to the db
            // todo run some code to set the correct preference
            setResult(Activity.RESULT_OK);
            showRegistrationDialog();
        } else {
            Login.getInstance().setWebuser(null);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inf = LayoutInflater.from(this);
        View registrationDialogExtraLayout = inf.inflate(R.layout.registration_dialog, null);
        builder.setView(registrationDialogExtraLayout);
        final AlertDialog dlg = builder.create();

        registrationDialogExtraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                finish();
            }
        });

        dlg.show();
    }

}