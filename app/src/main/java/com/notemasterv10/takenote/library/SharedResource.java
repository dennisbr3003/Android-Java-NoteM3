package com.notemasterv10.takenote.library;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.notemasterv10.takenote.Database;
import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.listeners.DialogAnswerListener;
import com.notemasterv10.takenote.listing.NoteListFragment;

public class SharedResource extends AppCompatActivity implements NoteMasterConstants {

    private DialogAnswerListener dialogAnswerListener;

    public DialogAnswerListener getDialogAnswerListener() {
        return dialogAnswerListener;
    }

    // The PointCollectorListener is set from outside this class (by another class). If it is
    // not set, it will be a null reference. This is checked when running the onTouchListener
    // in this class. If the refrence is not null it will execute the method and pass the
    // collected points. Actually ImageActivity is passed to here (it implements the interface!).
    public void setDialogAnswerListener(DialogAnswerListener dialogAnswerListener) {
        this.dialogAnswerListener = dialogAnswerListener;
    }

    public SharedResource() {
    }

    // @Override
    public void initListFragment(NoteListFragment nlf) {
        /*
        Log.d("DB", "Fragment start");
        nlf = (NoteListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_l);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Log.d("DB", "After fragment init ");
        ft.hide(nlf);
        ft.commit();
        Log.d("DB", "nlf niet zichtbaar? ");

         */
    }

    public void saveSharedBackgroundColor(int iColor, Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(BACKGROUND_COLOR, String.valueOf(iColor));
        prefsEdit.apply(); // apply is background, commit is not
    }

    public void saveSharedPasspointPhoto(String filepath, Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(PASSPOINT_IMAGE, filepath);
        prefsEdit.apply(); // apply is background, commit is not
    }

    public void setImageviewBitmapFromAbsolutePath(ImageView im, String absoluteFilePath){
        im.setImageBitmap(createBitmapFromOSFile(absoluteFilePath));
    }

    public Bitmap getImageviewBitmapFromAbsolutePath (String absoluteFilePath){
        return createBitmapFromOSFile(absoluteFilePath);
    }

    public int getSharedBackgroundColor(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return Integer.valueOf(prefs.getString(BACKGROUND_COLOR, "-1"));
    }

    public String getSharedPasspointPhoto(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PASSPOINT_IMAGE, SETTING_UNKNOWN);
    }

    public boolean pointsSetInSharedPrefs(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return Boolean.valueOf(prefs.getString(PASSPOINTS_SET, "false"));
    }

    public void setSharedPasspointsSet(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(PASSPOINTS_SET, String.valueOf(true));
        prefsEdit.apply(); // apply does it's work in th ebackground, commit does not.
    }

    public void setOpenNoteName(Context context, String name){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(OPEN_NOTE, name);
        prefsEdit.apply(); // apply does it's work in th ebackground, commit does not.
    }

    public String getOpenNoteName(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(OPEN_NOTE, NO_FILENAME);
    }


    public void resetSharedPasspointsSet(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(PASSPOINTS_SET, String.valueOf(false));
        prefsEdit.apply(); // apply does it's work in the background, commit does not.
    }

    public void resetSharedPasspointPhoto(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(PASSPOINT_IMAGE, SETTING_UNKNOWN);
        prefsEdit.apply(); // apply is background, commit is not
    }

    public byte[] getNote(Context context, String name){
        Database sdb = new Database(context);
        return sdb.getNote(name);
    }

    public void saveNote(Context context, byte[] note, String name) {

        Database sdb = new Database(context);
        try{
            sdb.saveNote(name, note);
        } catch(Exception e){
            Log.d("DB", "Database opslaan niet gelukt");
        }
        sdb.testUpdateInsertNote(); // read the contents of the table (may be removed later)
    }

    public Bitmap createBitmapFromOSFile(String absoluteFilePath) {
        Bitmap bm = BitmapFactory.decodeFile(absoluteFilePath); // should hold directory and filename (Attention this uses extra manifest permission)
        if(bm != null){
           return bm;
        }
        return null;
    }

    private String getCurrentTimestamp(){
        return String.valueOf(System.currentTimeMillis());
    }

    public void getNoteNameDialog(final Context context, final String currentNoteName, final byte[] note, final NoteAction noteAction){

        final ComplexDialogAnswer answer = new ComplexDialogAnswer();
        final EditText et = new EditText(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Save note");
        builder.setMessage("Name your note...");
        builder.setIcon(R.mipmap.dialog_orange_warning);
        builder.setCancelable(false); // block back-button

        // Set an EditText view to get user input
        builder.setView(et);

        builder.setPositiveButton(R.string.btn_caption_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (et.getText().toString().equals("") || et.getText().toString().equals(null)){
                    String systemFileName = String.format("%s-%s", "Note", getCurrentTimestamp());
                    //Log.d("DB", "Bestandsnaam " + String.format("%s-%s", "Note", getCurrentTimestamp()));
                    saveNote(context,note,systemFileName);
                    answer.setAnswer(systemFileName);
                    if (noteAction.equals(NoteAction.SAVE_NEW)) {
                        answer.setExtraInstruction("X");
                        setOpenNoteName(context, NO_FILENAME);
                    } else{
                        setOpenNoteName(context, answer.getAnswer());
                    }
                } else {
                    saveNote(context,note,et.getText().toString());
                    answer.setAnswer(et.getText().toString());
                    if (noteAction.equals(NoteAction.SAVE_NEW)) {
                        answer.setExtraInstruction("X");
                        setOpenNoteName(context, NO_FILENAME);
                    }else{
                        setOpenNoteName(context, answer.getAnswer());
                    }
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.btn_caption_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                answer.setAnswer("");
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("DB", "Is de listener null? " + String.valueOf((dialogAnswerListener == null)));
                Log.d("DB", "En we geven mee..." + answer.getAnswer());
                if (dialogAnswerListener != null) {
                    dialogAnswerListener.stringAnswerConfirmed(answer);
                    // this method is actually an interface method overridden in MainActivity
                    // Also check MainActivity.java and DialogAnswerListener.java (interface)
                    // Actually dialogAnswerListener IS IN FACT an instance of MainActivity (!)
                }
            }
        });

        AlertDialog dlg = builder.create();
        dlg.show();
    }

    public void askUserConfirmationDialog(final Context context){

        final BooleanAnswerObject answer = new BooleanAnswerObject();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.ConfirmDialogTitle);
        builder.setMessage(R.string.AreYouSure);
        builder.setIcon(R.mipmap.dialog_orange_warning);
        builder.setCancelable(false); // block back-button

        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                answer.setAnswer(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                answer.setAnswer(false);
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogAnswerListener != null) {
                    dialogAnswerListener.booleanAnswerConfirmed(answer.isAnswer());
                    // this method is actually an interface method overridden in MainActivity
                    // Also check MainActivity.java and DialogAnswerListener.java (interface)
                    // Actually dialogAnswerListener IS IN FACT an instance of MainActivity (!)
                }
            }
        });

        AlertDialog dlg = builder.create();
        dlg.show();
    }

    public void selectPassPointImageCustomDialog(final Context context){

        final IntegerAnswerObject answer = new IntegerAnswerObject();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // just do this...the photo_dialog.xml is in fact al layout that needs to be instantiated or inflated
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Getting the XML layout in...
        assert inflater != null;
        View v = inflater.inflate(R.layout.photo_dialog, null);

        builder.setView(v); // <-- title is already set in layout

        final AlertDialog dlg = builder.create();
        dlg.setCancelable(false); // block back-button
        dlg.show();

        // set on-click-listeners on the buttons
        Button btnCamera = (Button) dlg.findViewById(R.id.button_camera);
        Button btnGallery = (Button) dlg.findViewById(R.id.button_gallery);
        Button btnCancel = (Button) dlg.findViewById(R.id.button_cancel);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer.setAnswer(CAMERA_CLICKED);
                dlg.dismiss();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // leave out 'context' to get null pointer exception
                answer.setAnswer(GALLERY_CLICKED);
                dlg.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer.setAnswer(CANCEL_CLICKED);
                dlg.dismiss();
            }
        });

        // custom dialogs (using their own xml) own the onDismiss event so you need to bind it to the object itself, not the builder
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogAnswerListener != null) {
                    dialogAnswerListener.integerAnswerConfirmed(answer.getAnswer());
                    // this method is actually an interface method overridden in MainActivity
                    // Also check MainActivity.java and DialogAnswerListener.java (interface)
                    // Actually dialogAnswerListener IS IN FACT an instance of MainActivity (!)
                }
            }
        });

    }

    private class BooleanAnswerObject {

        private boolean answer;

        public BooleanAnswerObject() {
            this.setAnswer(false);
        }

        public boolean isAnswer() {
            return answer;
        }

        public void setAnswer(boolean answer) {
            this.answer = answer;
        }

    }

    private class IntegerAnswerObject {

        private int answer;

        public IntegerAnswerObject() {
        }

        public IntegerAnswerObject(int answer) {
            this.answer = answer;
        }

        public int getAnswer() {
            return answer;
        }

        public void setAnswer(int answer) {
            this.answer = answer;
        }

    }


}
