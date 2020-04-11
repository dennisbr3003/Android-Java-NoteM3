package com.notemasterv10.takenote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DialogAnswerListener {

    private static final int REQUEST_ID = 263;
    private static final String SETTING_UNKNOWN = "Unknown";

    SharedResource sr = new SharedResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sr.setDialogAnswerListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_reset_passpoints:
                resetAndAquirePasspoints();
                return true;
            case R.id.action_reset_backgroundcolor:
                // No dialog needed here, can be easily reset
                EditText et = (EditText) findViewById(R.id.editText);
                et.setBackgroundColor(-1);
                sr.saveSharedBackgroundColor(-1, this);
                return true;
            case R.id.action_clear_notetext:
                sr.askUserConfirmationDialog(this);
                return true;
            case R.id.action_change_passpoint_picture:
                sr.selectPassPointImageCustomDialog(this);
                return true;
            case R.id.action_reset_passpoint_picture:
                sr.saveSharedPasspointPhoto(SETTING_UNKNOWN, this);
                resetAndAquirePasspoints();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetAndAquirePasspoints(){
        Intent i = new Intent(this, ImageActivity.class);
        i.putExtra(getString(R.string.ClearPassPoints), true); // = received in the activity as a bundle in onCreate of the target activity
        startActivity(i);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String myString = savedInstanceState.getString("EditorText");
        Log.i(getString(R.string.DefaultTag), "Restored data: " + myString);
        // assign texteditor value here -->
        EditText et = (EditText) findViewById(R.id.editText);
        et.setText(myString);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        EditText et = (EditText) findViewById(R.id.editText);
        Log.d(getString(R.string.DefaultTag), getString(R.string.FoundValue) + et.getText().toString());
        String x = et.getText().toString();
        savedInstanceState.putString("EditorText", x);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void booleanAnswerConfirmed(Boolean answer) {
        EditText et = (EditText) findViewById(R.id.editText);
        if(answer){
            Log.d(getString(R.string.DefaultTag), "Confirmed");
            et.setText("");
            sr.saveNoteText(this, et.getText().toString().getBytes());
        } else {
            Log.d(getString(R.string.DefaultTag), "Not confirmed");
        }
    }

    @Override
    public void integerAnswerConfirmed(int answer) {

        Log.d(getString(R.string.DefaultTag), "method integeranswerconfirmed");

        switch(answer){
            case 0:
                // cancel, do nothing
                break;
            case 1:
                // take photo, start new intent
                Log.d(getString(R.string.action_settings), getString(R.string.start_camera_wrapper));
                Intent i = new Intent(this, CameraWrapperActivity.class);
                startActivityForResult(i, REQUEST_ID);
                break;
            case 2:
                // check gallery
                break;
            default:
                // unknown parameter, show error
                Toast.makeText(this, R.string.unknown_from_dlg,Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // <-- the photo URL is in here

        switch(requestCode) {
            case REQUEST_ID:
                if(resultCode == RESULT_OK) {
                    // save filepath in prefs, reset pass points and launch ImageActivity to get new pass points for new photo.
                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            // put new file patch into shared prefs
                            assert data != null;
                            sr.saveSharedPasspointPhoto(data.getStringExtra("new_photo_filepath"), MainActivity.this);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            resetAndAquirePasspoints();
                        }
                    };
                    asynctask.execute();
                }
                break;
            default:
                Log.d(getString(R.string.DefaultTag), getString(R.string.activity_unknown));
                break;
        }
    }
}