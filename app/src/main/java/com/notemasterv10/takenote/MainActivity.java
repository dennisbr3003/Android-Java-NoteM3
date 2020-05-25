package com.notemasterv10.takenote;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.library.ComplexDialogAnswer;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listeners.DialogAnswerListener;
import com.notemasterv10.takenote.listeners.WebEventListener;
import com.notemasterv10.takenote.listing.Note;
import com.notemasterv10.takenote.listing.NoteListFragment;
import com.notemasterv10.takenote.webservice.ArrayItemObject;
import com.notemasterv10.takenote.webservice.SharedPreferenceResponse;
import com.notemasterv10.takenote.webservice.WebServiceConnectService;
import com.notemasterv10.takenote.webservice.WebServiceMethods;

public class MainActivity extends AppCompatActivity implements DialogAnswerListener,
        WebEventListener,
        NoteMasterConstants,
        NoteListFragment.OnListFragmentInteractionListener {

    private Boolean showItemUploadDownload = false;
    private FirstFragment nlf;
    SharedResource sr = new SharedResource();
    WebServiceMethods ws = new WebServiceMethods();
    WebServiceConnectReceiver webServiceConnectReceiver = new WebServiceConnectReceiver();
    Intent wscs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sr.setDialogAnswerListener(this);
        ws.setWebEventListener(this);
        supportInvalidateOptionsMenu();

        wscs = new Intent(this, WebServiceConnectService.class);
        startService(wscs);
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
        switch (id) {

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

            case R.id.action_download_preferences:
                ws.downloadSharedPreferencePayload(this);
                return true;
            case R.id.action_find_notes:
                showNoteList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showNoteList(){

        /* Architecture fragments MainActivity:
           MainActivity --> NavigationHostFragment --> FirstFragment --> NoteListFragment
                            (level 1)                  (level 2)         (level 3)
        */

        int noteCount = sr.getNoteCount(this);

        // level 1 -->
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        // level 2 -->
        Fragment ff = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();

        // ff is parent to level 3 -->
        if(ff != null) {
            // level 3 (is the child (the note list) already loaded, don't load it again) -->
            int t = ff.getChildFragmentManager().getFragments().size();
            if(t >= 1) { // at least one child is loaded, but there could be more. Make sure you get the correct one -->
                Fragment cf = ff.getChildFragmentManager().findFragmentByTag(NOTELIST_FRAGMENT_TAG); // this is set in showChildFragment method of ff
                Fragment el = ff.getChildFragmentManager().findFragmentByTag(EMPTYLIST_FRAGMENT_TAG);
                if ((cf != null) || (el != null)) {
                    // a child is already loaded, don't load it again -->
                    return;
                } else {
                    try{
                        // Cast it to the type of the fragment that it is to get to the method forced there by an interface -->
                        FirstFragment firstFrag = (FirstFragment) ff;
                        if (noteCount > 0) {
                            firstFrag.showChildFragment(NOTELIST_FRAGMENT_TAG);
                        } else {
                            firstFrag.showChildFragment(EMPTYLIST_FRAGMENT_TAG);
                        }
                    } catch(Exception e){
                        return;
                    }

                }
            } else { // no child loaded yet -->
                try{
                    // Cast it to the type of the fragment that it is to get to the method forced there by an interface -->
                    FirstFragment firstFrag = (FirstFragment) ff;
                    if (noteCount > 0) {
                        firstFrag.showChildFragment(NOTELIST_FRAGMENT_TAG);
                    } else {
                        firstFrag.showChildFragment(EMPTYLIST_FRAGMENT_TAG);
                    }
                } catch(Exception e){
                    return;
                }
            }
        }
    }

    private void resetAndAquirePasspoints(){
        Intent i = new Intent(this, ImageActivity.class);
        i.putExtra(getString(R.string.ClearPassPoints), true); // = received in the activity as a bundle in onCreate of the target activity
        startActivity(i);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu){

        MenuItem itemUpload = menu.findItem(R.id.action_upload_preferences);
        MenuItem itemDownload = menu.findItem(R.id.action_download_preferences);

        itemUpload.setVisible(showItemUploadDownload);
        itemDownload.setVisible(showItemUploadDownload);

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onRestoreInstanceState (Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);

        String myString = savedInstanceState.getString(SAVED_INSTANCE_EDITORTEXT_TAG);

        // assign text editor value here -->
        EditText et = (EditText) findViewById(R.id.editText);
        et.setText(myString);

    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        EditText et = (EditText) findViewById(R.id.editText);
        String x = et.getText().toString();
        savedInstanceState.putString(SAVED_INSTANCE_EDITORTEXT_TAG, x);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void booleanAnswerConfirmed (Boolean answer){
        EditText et = (EditText) findViewById(R.id.editText);
        if (answer) {
            et.setText("");
            // TODO sr.saveNote(this, et.getText().toString().getBytes(), );
        }
    }

    @Override
    public void integerAnswerConfirmed ( int answer){

        switch (answer) {

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
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable final Intent data){

        super.onActivityResult(requestCode, resultCode, data); // <-- the photo URL is in here

        switch (requestCode) {

            case REQUEST_ID_CAMERA:

                if (resultCode == RESULT_OK) {

                    // save filepath in prefs, reset pass points and launch ImageActivity to get new pass points for new photo -->
                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Integer> asynctask = new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... voids) {

                            // put new file path into shared prefs -->
                            if (data != null) {
                                try {
                                    sr.saveSharedPasspointPhoto(data.getStringExtra(CAMERA_ABSOLUTE_FILEPATH), MainActivity.this);
                                } catch (Exception e) {
                                    return 0; // didn't work, do nothing and leave everything the same
                                }
                            } else {
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

                if (resultCode == RESULT_OK) {

                    // save filepath in prefs, reset pass points and launch ImageActivity to get new pass points for new photo -->
                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Integer> asynctask = new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... voids) {
                            // put new file patch into shared prefs -->
                            if (data != null) {
                                try {
                                    // Uri is passed back through the data object but it's not an absolute path...it's through an UriWrapper -->
                                    sr.saveSharedPasspointPhoto(MediaUriWrapper.getRealPath(MainActivity.this, data.getData()), MainActivity.this);
                                } catch (Exception e) {
                                    return 0; // didn't work, do nothing and leave everything the same
                                }
                            } else {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(wscs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(webServiceConnectReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(webServiceConnectReceiver, new IntentFilter(WebServiceConnectService.SERVICE_ACTION));

    }


    public void showHideMenuItem (final Action action){

        runOnUiThread(new Runnable(){

            @Override
            public void run() {
                showItemUploadDownload = (action == Action.SHOW_UPL_DL);

                ImageView im = (ImageView) findViewById(R.id.imgStatus);
                TextView tv = (TextView) findViewById(R.id.textview_status);

                switch (action){
                    case HIDE_UPL_DL:
                        try {
                            im.setImageResource(R.mipmap.webservice_offline);
                            tv.setText(R.string.ws_unavail);
                        } catch(Exception e){
                            // do nothing...
                        }
                        break;
                    case SHOW_UPL_DL:
                        try {
                            im.setImageResource(R.mipmap.webservice_online);
                            tv.setText(R.string.ws_avail);
                        } catch(Exception e){
                            // do nothing...
                        }
                        break;
                }

            }
        });

    }

    @Override
    public void loadDownLoadedPreferences (SharedPreferenceResponse spr){

        for (final ArrayItemObject aio : spr.getShared_preference()) {

            SharedPreferences prefs = getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEdit = prefs.edit();
            prefsEdit.putString(aio.getItem_name(), aio.getItem_value());
            prefsEdit.apply(); // apply is background, commit is not

            // Direct load for BGC only (may be changed later when more preferences will be loaded) -->
            if (aio.getItem_name().equals(BACKGROUND_COLOR)) {
                runOnUiThread(new Runnable() { // <-- must be run on the UI thread
                    @Override
                    public void run() {
                        EditText et = (EditText) findViewById(R.id.editText);
                        et.setBackgroundColor(Integer.valueOf(aio.getItem_value())); // <-- quick load from the object not the saved value itself (both are the same)                    }
                    }
                });
            }
        }
    }


    @Override
    public void stringAnswerConfirmed (ComplexDialogAnswer answer){

        TextView tv = (TextView) findViewById(R.id.text_view_currentnote);
        EditText et = (EditText) findViewById(R.id.editText);
        if (answer.getAnswer().equals(null) || answer.getAnswer().equals("")) {
            tv.setText(String.format("%s", NO_FILENAME));
        } else {
            if (answer.getExtraInstruction().equals("X")) {
                tv.setText(NO_FILENAME);
                et.setText("");
            } else {
                tv.setText(answer.getAnswer());
                Toast.makeText(this, getString(R.string.ToastSaveSucces), Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    public void onListFragmentInteraction (Note note){

        Log.d("DB", "Item in recyclerview clicked");

    }

    @Override
    public void onListFragmentInteractionDelete(Note note) {
        Log.d("DB", "Item in recyclerview <delete-image> clicked");
        // dialog in adapter had the user confirm the action so just do it -->
        sr.deleteNote(this, note);

    }

    public class WebServiceConnectReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WebServiceConnectService.SERVICE_ACTION)){
                boolean is_alive = intent.getExtras().getBoolean(WebServiceConnectService.IS_ALIVE);
                Log.d("DB", "Broadcast received of (boolean) value " + is_alive);
                if(is_alive) {
                    showHideMenuItem(WebEventListener.Action.SHOW_UPL_DL);
                } else {
                    showHideMenuItem(WebEventListener.Action.HIDE_UPL_DL);
                }
            }
        }
    }

}