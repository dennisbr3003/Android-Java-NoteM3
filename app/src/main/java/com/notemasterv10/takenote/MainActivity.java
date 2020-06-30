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
import androidx.appcompat.app.AlertDialog;
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
import com.notemasterv10.takenote.database.ImageTable;
import com.notemasterv10.takenote.database.NoteTable;
import com.notemasterv10.takenote.interaction.ExtendedBooleanAnswer;
import com.notemasterv10.takenote.interaction.ExtendedStringAnswer;
import com.notemasterv10.takenote.interaction.SingleIntegerAnswer;
import com.notemasterv10.takenote.library.PassPointImage;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listeners.DialogAnswerListener;
import com.notemasterv10.takenote.listeners.WebEventListener;
import com.notemasterv10.takenote.listing.Note;
import com.notemasterv10.takenote.listing.NoteListFragment;
import com.notemasterv10.takenote.webservice.ArrayItemObject;
import com.notemasterv10.takenote.webservice.UserDataResponse;
import com.notemasterv10.takenote.webservice.WebServiceConnectService;
import com.notemasterv10.takenote.webservice.WebServiceMethods;

public class MainActivity extends AppCompatActivity implements DialogAnswerListener,
        WebEventListener,
        NoteMasterConstants,
        NoteListFragment.OnListFragmentInteractionListener {

    private Boolean showItemUploadDownload = false;
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

            case R.id.action_change_passpoint_picture:
                sr.selectPassPointImageCustomDialog(this);
                return true;

            case R.id.action_reset_passpoint_picture:
                sr.saveSharedPasspointImage(SETTING_UNKNOWN, this);
                resetAndAquirePasspoints();
                return true;

            case R.id.action_upload_preferences:
                ws.createUserDataObject(this);
                return true;

            case R.id.action_download_preferences:
                ws.preDownloadCheck(this);
                return true;
            case R.id.action_find_notes:
                showNoteList();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showNoteList() {

        /* Architecture fragments MainActivity:
           MainActivity --> NavigationHostFragment --> FirstFragment --> NoteListFragment
                            (level 1)                  (level 2)         (level 3)
           at any given time (in this case) only one of the child fragments can be loaded
        */
        int noteCount = sr.getNoteCount(this);
        boolean isEmpty = (noteCount == 0);
        // level 1 -->
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // level 2 -->
        Fragment ff = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        // ff is parent to level 3 -->
        if (ff != null) {
            if(isEmpty) {
                ((FirstFragment) ff).showChildFragment(EMPTYLIST_FRAGMENT_TAG);
            } else {
                ((FirstFragment) ff).showChildFragment(NOTELIST_FRAGMENT_TAG);
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
    public void integerAnswerConfirmed (SingleIntegerAnswer answer){

        switch (answer.getAnswer()) {

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
                Log.e(getString(R.string.ErrorTag), getString(R.string.ActivityUnknown));
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
                                    sr.saveSharedPasspointImage(data.getStringExtra(CAMERA_ABSOLUTE_FILEPATH), MainActivity.this);
                                } catch (Exception e) {
                                    Log.d(getString(R.string.ErrorTag), e.getMessage());
                                    return 0; // didn't work, do nothing and leave everything the same
                                }
                            } else {
                                Log.d(getString(R.string.ErrorTag), getString(R.string.NoBinaryData));
                                return 0; // didn't work, do nothing and leave everything the same
                            }
                            return 1; // Selected and USED a picture so reset the passpoints
                        }

                        @Override
                        protected void onPostExecute(Integer aInteger) {
                            super.onPostExecute(aInteger);
                            if (aInteger == 1) {
                                Log.d(getString(R.string.DefaultTag), "Successfully selected a picture from the camera");
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
                                    sr.saveSharedPasspointImage(MediaUriWrapper.getRealPath(MainActivity.this, data.getData()), MainActivity.this);
                                } catch (Exception e) {
                                    Log.d(getString(R.string.ErrorTag), e.getMessage());
                                    return 0; // didn't work, do nothing and leave everything the same
                                }
                            } else {
                                Log.d(getString(R.string.ErrorTag), getString(R.string.NoBinaryData));
                                return 0; // didn't work, do nothing and leave everything the same
                            }
                            return 1; // Selected and USED a picture from the gallery so reset the passpoints
                        }

                        @Override
                        protected void onPostExecute(Integer aInteger) {
                            super.onPostExecute(aInteger);
                            if (aInteger == 1) {
                                Log.d(getString(R.string.DefaultTag), "Successfully selected a picture from the gallery");
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
                Log.e(getString(R.string.ErrorTag), getString(R.string.ActivityUnknown));
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
                            tv.setText(R.string.WebServiceDead);
                        } catch(Exception e){
                            // do nothing...
                        }
                        break;
                    case SHOW_UPL_DL:
                        try {
                            im.setImageResource(R.mipmap.webservice_online);
                            tv.setText(R.string.WebServiceAlive);
                        } catch(Exception e){
                            // do nothing...
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void loadDownLoadedUserData(UserDataResponse spr, AlertDialog cDlg){

        // load everything (if data is present) and ask for new pass points
        // if no notes are retrieved everything is deleted (user choice)
        NoteTable noteTable = new NoteTable(this);
        noteTable.clearTable();
        for (Note note : spr.getNoteList()){
            noteTable.saveNote(note.getName(), note.getFile());
        }

        // if no images are retrieved everything is deleted (user choice)
        ImageTable imageTable  =new ImageTable(this);
        imageTable.clearTable();
        for(PassPointImage passPointImage : spr.getPassPointImageList()){
            imageTable.savePassPointImage(passPointImage.getName(), passPointImage.getFile());
        }

        for (ArrayItemObject aio : spr.getShared_preference()) {
            SharedPreferences prefs = getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEdit = prefs.edit();
            prefsEdit.putString(aio.getItemName(), aio.getItemValue());
            prefsEdit.apply(); // apply is background, commit is not
        }

        // now reset password and show whatever picture is needed...
        cDlg.dismiss();
        resetAndAquirePasspoints();
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public void saveNote(final ExtendedStringAnswer answer){

        TextView tv = (TextView) findViewById(R.id.text_view_currentnote);
        EditText et = (EditText) findViewById(R.id.editText);

        final Context context = this;

        if (answer.getAnswer() == null || answer.getAnswer().equals("")) {

            tv.setText(String.format("%s", NO_FILENAME));

            if(answer != null) { // <-- save from FirstFragment view group

                if (answer.getExtraInstructions().equals(OPEN_SAVED_NOTE)) {
                    // open an existing previously saved note -->
                    tv.setText(answer.getNewNoteName());
                    et.setText(new String(answer.getNewNoteContent()));
                    sr.setOpenNoteName(context, answer.getNewNoteName());
                }
            }

        } else {
            // save the 'old' current note -->
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    sr.saveNote(context, answer.getCurrentNoteContent(), answer.getAnswer());
                    return null;
                }
            }.execute();

            if (answer.getExtraInstructions().equals(OPEN_NEW_NOTE)) {
                // open an new empty note -->
                tv.setText(NO_FILENAME);
                et.setText("");
                sr.setOpenNoteName(context, NO_FILENAME);
            } else {
                if (answer.getExtraInstructions().equals(OPEN_SAVED_NOTE)) {
                    // open an existing previously saved note -->
                    tv.setText(answer.getNewNoteName());
                    et.setText(new String(answer.getNewNoteContent()));
                    sr.setOpenNoteName(context, answer.getNewNoteName());
                } else {
                    // the current notes is saved, visually nothing changes unless it's a new note -->
                    Toast.makeText(this, getString(R.string.ToastSaveSucces), Toast.LENGTH_SHORT).show();
                    tv.setText(answer.getAnswer());
                    sr.setOpenNoteName(context, answer.getAnswer());
                }
            }
        }
    }

    public void popNoteList(){
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment ff = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if (ff != null) {
            ((FirstFragment) ff).hideChildFragment();
        }
    }

    @Override
    @SuppressLint("StaticFieldLeak")
    public void onListFragmentInteraction (final Note note){

        TextView tv = (TextView) findViewById(R.id.text_view_currentnote);
        EditText et = (EditText) findViewById(R.id.editText);

        // unload current child fragment -->
        popNoteList();

        // selected note is the current note, do nothing -->
        if (note.isCurrentNote()){
            return;
        }

        // note already saved and named before; save directly -->
        if (!(sr.getOpenNoteName(this).equals(NO_FILENAME))) {
            sr.saveNote(this, et.getText().toString().getBytes(), sr.getOpenNoteName(this));
        }
        else {// note NOT already saved and named; show dialog and get a filename -->
            sr.noteNameDialog(this, et.getText().toString().getBytes(), NoteAction.SAVE_AND_OPEN, note.getName(), note.getFile());
            // this will be picked up by the dialog listener saveAnswerConfirmed with the new action; exit here -->
            return;
        }

        // saved without dialog; we are not going to the database here, just use the object to fill the new screen -->
        et.setText(new String(note.getFile()));
        tv.setText(note.getName());
        sr.setOpenNoteName(this, note.getName());

    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void renameNote(final ExtendedStringAnswer answer) {

        final Context context = this;

        // execute the rename action in the filesystem (SQLite database) asynchronous
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                sr.renameNote(context, answer.getCurrentNoteName(), answer.getNewNoteName());
                return null;
            }
        }.execute();

        // this section can be executed independent from the database interaction -->
        if(answer.getCurrentNote()) {
            // if the rename if for the current note, then set the current note value -->
            sr.setOpenNoteName(this, answer.getNewNoteName());
            TextView tv = (TextView) findViewById(R.id.text_view_currentnote);
            tv.setText(answer.getNewNoteName());
        }

        // try to reach the list fragment to update the list -->
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        Fragment ff = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
        if(ff != null){
            Fragment cf = ff.getChildFragmentManager().findFragmentByTag(NOTELIST_FRAGMENT_TAG);
            if(cf != null){
                ((NoteListFragment) cf).renameItemInList(answer.getPosition(), answer.getNewNoteName());
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public void deleteNote(ExtendedBooleanAnswer answer) {

        if(answer.getNote() == null){

            EditText et = findViewById(R.id.editText);
            TextView tv = findViewById(R.id.text_view_currentnote);
            final Context context = this;

            if(answer.isAnswer()){
                // if position = -1 (NO_POSITION) then the delete was fired from FirstFragment
                if(answer.getExtraInstructions().equals(String.valueOf(NOT_INDEXED))){
                    if(!sr.getOpenNoteName(this).equals(NO_FILENAME)){
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                sr.deleteNote(context, sr.getOpenNoteName(context));
                                return null;
                            }
                        }.execute();
                    }
                    et.setText("");
                    sr.setOpenNoteName(this, NO_FILENAME);
                    tv.setText(NO_FILENAME);
                }
            }

        } else {

            // try to reach the list fragment to update the list -->
            Fragment cf = null;
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            Fragment ff = navHostFragment.getChildFragmentManager().getPrimaryNavigationFragment();
            if (ff != null) {
                cf = ff.getChildFragmentManager().findFragmentByTag(NOTELIST_FRAGMENT_TAG);
            }

            if (answer.isAnswer()) {
                // try to reach the list fragment to update the list -->
                if (cf != null) {
                    ((NoteListFragment) cf).removeItemFromList(answer.getNote().getListPosition());
                }
                // delete the note from SQLite -->
                sr.deleteNote(this, answer.getNote());
                // show the correct fragment -->
                if (sr.getNoteCount(this) == 0) {
                    popNoteList();
                    showNoteList();
                }
            } else { // cancel -->
                if (cf != null) {
                    ((NoteListFragment) cf).resetSelection();
                }
            }
        }
    }

    public class WebServiceConnectReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WebServiceConnectService.SERVICE_ACTION)){
                boolean is_alive = intent.getExtras().getBoolean(WebServiceConnectService.IS_ALIVE);
                if(is_alive) {
                    showHideMenuItem(WebEventListener.Action.SHOW_UPL_DL);
                } else {
                    showHideMenuItem(WebEventListener.Action.HIDE_UPL_DL);
                }
            }
        }
    }

}