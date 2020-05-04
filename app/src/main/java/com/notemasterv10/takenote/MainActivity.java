package com.notemasterv10.takenote;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements DialogAnswerListener,Constants {

    SharedResource sr = new SharedResource();
    WebService ws = new WebService();

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
        switch(id) {

            case R.id.action_reset_passpoints:
                resetAndAquirePasspoints();
                return true;

            case R.id.action_reset_backgroundcolor:
                // No dialog needed here, can be easily reset -->
                EditText et = (EditText) findViewById(R.id.editText);
                et.setBackgroundColor(DEFAULT_EDITOR_BACKGROUND_COLOR);
                sr.saveSharedBackgroundColor(DEFAULT_EDITOR_BACKGROUND_COLOR, this);
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

            case R.id.action_upload_preferences:
                ws.createSharedPreferenceObject(this);
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

        String myString = savedInstanceState.getString(SAVED_INSTANCE_EDITORTEXT_TAG);

        // assign texteditor value here -->
        EditText et = (EditText) findViewById(R.id.editText);
        et.setText(myString);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        EditText et = (EditText) findViewById(R.id.editText);
        String x = et.getText().toString();
        savedInstanceState.putString(SAVED_INSTANCE_EDITORTEXT_TAG, x);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void booleanAnswerConfirmed(Boolean answer) {
        EditText et = (EditText) findViewById(R.id.editText);
        if(answer){
            et.setText("");
            sr.saveNoteText(this, et.getText().toString().getBytes());
        }
    }

    @Override
    public void integerAnswerConfirmed(int answer) {

        switch(answer){

            case CANCEL_CLICKED:
                // cancel, do nothing -->
                break;

            case CAMERA_CLICKED:
                // take photo, start new intent -->
                Intent intent_camera = new Intent(this, CameraWrapperActivity.class);
                startActivityForResult(intent_camera, REQUEST_ID_CAMERA);
                break;

            case GALLERY_CLICKED:
                // check gallery for photo -->
                Intent intent_gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent_gallery, REQUEST_ID_GALLERY);
                break;

            default:
                // unknown parameter, log error -->
                Log.e(getString(R.string.takenote_errortag), getString(R.string.activity_unknown));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {

        super.onActivityResult(requestCode, resultCode, data); // <-- the photo URL is in here

        switch(requestCode) {

            case REQUEST_ID_CAMERA:

                if(resultCode == RESULT_OK) {

                    // save filepath in prefs, reset pass points and launch ImageActivity to get new pass points for new photo -->
                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Integer> asynctask = new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... voids) {

                            // put new file path into shared prefs -->
                            if(data != null) {
                                try {
                                    sr.saveSharedPasspointPhoto(data.getStringExtra(CAMERA_ABSOLUTE_FILEPATH), MainActivity.this);
                                } catch(Exception e){
                                    return 0; // didn't work, do nothing and leave everything the same
                                }
                            }
                            else {
                                return 0; // didn't work, do nothing and leave everything the same
                            }
                            return 1; // Selected and USED a picture so reset the passpoints
                        }

                        @Override
                        protected void onPostExecute(Integer aInteger) {
                            super.onPostExecute(aInteger);
                            if (aInteger == 1) {

                                // New picture successfully selected so change the passpoints (also in a asynchronous task) -->
                                @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        resetAndAquirePasspoints();
                                        return null;
                                    }
                                };
                                asynctask.execute();
                            }
                        }
                    };
                    asynctask.execute();
                }
                break; // Ends REQUEST_ID_CAMERA

            case REQUEST_ID_GALLERY:

                if(resultCode == RESULT_OK){

                    // save filepath in prefs, reset pass points and launch ImageActivity to get new pass points for new photo -->
                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Integer> asynctask = new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... voids) {
                            // put new file patch into shared prefs -->
                            if(data != null) {
                                try {
                                    // Uri is passed back through the data object but it's not an absolute path...it's through an UriWrapper -->
                                    sr.saveSharedPasspointPhoto(MediaUriWrapper.getRealPath(MainActivity.this, data.getData()), MainActivity.this);
                                } catch(Exception e){
                                    return 0; // didn't work, do nothing and leave everything the same
                                }
                            }
                            else {
                                return 0; // didn't work, do nothing and leave everything the same
                            }
                            return 1; // Selected and USED a picture from the gallery so reset the passpoints
                        }

                        @Override
                        protected void onPostExecute(Integer aInteger) {
                            super.onPostExecute(aInteger);
                            if (aInteger == 1) {

                                // New picture successfully selected so change the passpoints (also in a asynchronous task) -->
                                @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>() {
                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        resetAndAquirePasspoints();
                                        return null;
                                    }
                                };
                                asynctask.execute();
                            }
                        }
                    };
                    asynctask.execute();
                }
                break; // Ends REQUEST_ID_GALLERY

            default:
                // unknown parameter, log error -->
                Log.e(getString(R.string.takenote_errortag), getString(R.string.activity_unknown));
                break;
        }
    }
}