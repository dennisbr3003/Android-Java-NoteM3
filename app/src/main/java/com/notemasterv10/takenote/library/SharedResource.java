package com.notemasterv10.takenote.library;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.database.ImageTable;
import com.notemasterv10.takenote.database.NoteTable;
import com.notemasterv10.takenote.database.UserTable;
import com.notemasterv10.takenote.interaction.ExtendedBooleanAnswer;
import com.notemasterv10.takenote.interaction.ExtendedStringAnswer;
import com.notemasterv10.takenote.interaction.SingleIntegerAnswer;
import com.notemasterv10.takenote.listeners.DialogAnswerListener;
import com.notemasterv10.takenote.listing.Note;
import com.notemasterv10.takenote.webservice.WebUser;

import java.io.ByteArrayOutputStream;

public class SharedResource extends AppCompatActivity implements NoteMasterConstants {

    private DialogAnswerListener dialogAnswerListener;

    public DialogAnswerListener getDialogAnswerListener() {
        return dialogAnswerListener;
    }

    // The PointCollectorListener is set from outside this class (by another class). If it is
    // not set, it will be a null reference. This is checked when running the onTouchListener
    // in this class. If the reference is not null it will execute the method and pass the
    // collected points. Actually ImageActivity is passed to here (it implements the interface!).
    public void setDialogAnswerListener(DialogAnswerListener dialogAnswerListener) {
        this.dialogAnswerListener = dialogAnswerListener;
    }

    public SharedResource() {
    }

    private String getCurrentTimestamp(){
        return String.valueOf(System.currentTimeMillis());
    }

    //---------------------------------------------------------------------------------------------->

    public void saveUserRegistration(boolean registered, Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(USER_IS_REGISTERED, String.valueOf(registered));
        prefsEdit.apply(); // apply is background, commit is not
    }

    public boolean getUserRegistration(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return Boolean.parseBoolean(prefs.getString(USER_IS_REGISTERED, "false"));
    }

    //---------------------------------------------------------------------------------------------->

    public void saveSharedBackgroundColor(int iColor, Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(BACKGROUND_COLOR, String.valueOf(iColor));
        prefsEdit.apply(); // apply is background, commit is not
    }

    public int getSharedBackgroundColor(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return Integer.parseInt(prefs.getString(BACKGROUND_COLOR, "-1"));
    }

    //---------------------------------------------------------------------------------------------->

    public void saveSharedPasspointImage(String fileName, Context context) {

        ImageTable imageTable = new ImageTable(context);

        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();

        //this is actually the filepath (from camera or gallery)
        Log.d("DB", "saveSharedPasspointImage" );
        Log.d("DB", fileName);

        if (fileName.equals(SETTING_UNKNOWN)) {
            // reset, remove image from db
            if(imageTable.deletePasspointImage(PASSPOINT_IMAGE)){
                prefsEdit.putString(PASSPOINT_IMAGE_NAME, fileName);
                prefsEdit.apply(); // apply is background, commit is not
            } // if this fails do nothing...
        } else {
            try {
                Bitmap bm = createBitmapFromOSFile(fileName);
                Log.d("DB", String.format("Size decoded bitmap (#bytes) %s", bm.getByteCount()));
                byte[] ba = convertBitmapToByteArray(bm);
                Log.d("DB", String.format("Size bitmap converted to byte array (#bytes) %s", ba.length));
                if((ba.length / 1024) >= MAX_IMAGE_SIZE_KB){
                    // bigger then MAX_IMAGE_SIZE_KB so downsize without losing quality and aspect ratio
                    Double dFactor =  ((((double)ba.length) / 1024) / MAX_IMAGE_SIZE_KB);
                    Log.d("DB", String.format("File size (from byte array) is %s times bigger then %s (exactly) ", String.format("%.02f", dFactor), MAX_IMAGE_SIZE_KB));
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inJustDecodeBounds = true;
                    int sampleSize = (int)Math.ceil(dFactor); // factor of downsizing the image rounded up
                    Log.d("DB", String.format("Calculated sample size (based on size) = %s ", sampleSize));
                    sampleSize = calculateNearestPowerOf2(sampleSize);
                    Log.d("DB", String.format("Calculated sample size (nearest power of 2) = %s ", sampleSize));
                    opt.inSampleSize =  sampleSize;
                    opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    opt.inJustDecodeBounds = false;
                    // overwrite bm with new file
                    bm = createBitmapFromOSFile(fileName, opt);
                }
                imageTable.savePassPointImage(PASSPOINT_IMAGE, convertBitmapToByteArray(bm));
                prefsEdit.putString(PASSPOINT_IMAGE_NAME, PASSPOINT_IMAGE);
                prefsEdit.apply();
            } catch(Exception e){
                Log.d(getString(R.string.ErrorTag), e.getMessage());
                // just log, do nothing else
            }
        }
        imageTable.testUpdateInsertPassPointImage(); // test contents
    }

    private int calculateNearestPowerOf2(int sampleSize){

        int power; // check if the sampleSize a power of 2 or otherwise take the closest one
        int diff = 0;
        int prev_power = 0;

        for(int i = 0; i < sampleSize; i++){
            power = i;
            power *= 2;
            if(power >= sampleSize){
                if(diff < (power - sampleSize)){
                    sampleSize = prev_power;
                }else {
                    sampleSize = power;
                }
                break;
            } else {
                diff = sampleSize - power;
                prev_power = power;
            }
        }
        return sampleSize;
    }

    public Bitmap getImageviewBitmapFromAbsolutePath (String absoluteFilePath){
        return createBitmapFromOSFile(absoluteFilePath);
    }

    public String getSharedPrefsPasspointImage(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PASSPOINT_IMAGE_NAME, SETTING_UNKNOWN);
    }

    public Bitmap getSavedPasspointImageAsBitmap(Context context){
        byte[] img = getSavedPasspointImage(context);
        return BitmapFactory.decodeByteArray(img, 0, img.length);
    }

    public byte[] getSavedPasspointImage(Context context){
        ImageTable imageTable = new ImageTable(context);
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return imageTable.getPassPointImage(prefs.getString(PASSPOINT_IMAGE_NAME, PASSPOINT_IMAGE));
    }

    public void resetSharedPrefsPasspointImage(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        // remove the current saved passpoint image -->
        this.deletePasspointImage(context, prefs.getString(PASSPOINT_IMAGE_NAME, PASSPOINT_IMAGE));
        prefsEdit.putString(PASSPOINT_IMAGE_NAME, SETTING_UNKNOWN);
        prefsEdit.apply(); // apply is background, commit is not
    }

    public Bitmap createBitmapFromOSFile(String absoluteFilePath) {
        return BitmapFactory.decodeFile(absoluteFilePath); // should hold directory and filename (Attention this uses extra manifest permission)
    }

    public Bitmap createBitmapFromOSFile(String absoluteFilePath, BitmapFactory.Options options) {
        return BitmapFactory.decodeFile(absoluteFilePath, options); // should hold directory and filename (Attention this uses extra manifest permission)
    }

    public byte[] convertBitmapToByteArray(Bitmap bm) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    public void deletePasspointImage(Context context, String imageName){
        ImageTable imageTable = new ImageTable(context);
        imageTable.deletePasspointImage(imageName);
    }

    //---------------------------------------------------------------------------------------------->

    public boolean pointsSetInSharedPrefs(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return Boolean.parseBoolean(prefs.getString(PASSPOINTS_SET, "false"));
    }

    public void setSharedPasspointsSet(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(PASSPOINTS_SET, String.valueOf(true));
        prefsEdit.apply(); // apply does it's work in the background, commit does not.
    }

    public void resetSharedPasspointsSet(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(PASSPOINTS_SET, String.valueOf(false));
        prefsEdit.apply(); // apply does it's work in the background, commit does not.
    }

    //---------------------------------------------------------------------------------------------->

    public WebUser getUser (Context context){

        Log.d("DENNIS_BRINK", "SharedResource: getUser without name argument");

        UserTable userTable = new UserTable(context);
        return userTable.getFirstUser();
    }

    public WebUser getUser (Context context, String name){
        UserTable userTable = new UserTable(context);
        return userTable.getUser(name);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insertUser (Context context, WebUser webuser){
        UserTable userTable = new UserTable(context);
        userTable.insertUser(webuser);
    }

    //---------------------------------------------------------------------------------------------->

    public void setOpenNoteName(Context context, String name){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEdit = prefs.edit();
        prefsEdit.putString(OPEN_NOTE, name);
        prefsEdit.apply(); // apply does it's work in the background, commit does not.
    }

    public String getOpenNoteName(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(OPEN_NOTE, NO_FILENAME);
    }

    public byte[] getNote(Context context, String name){
        NoteTable noteTable = new NoteTable(context);
        return noteTable.getNote(name);
    }

    public void deleteNote(Context context, String noteName){
        NoteTable noteTable = new NoteTable(context);
        noteTable.deleteNote(noteName);
    }

    public void deleteNote(Context context, Note note){
        NoteTable noteTable = new NoteTable(context);
        noteTable.deleteNote(note.getName());
        if (note.isCurrentNote()){
            // clear textview and open a new one
            ExtendedStringAnswer answer = new ExtendedStringAnswer();
            answer.setAnswer(NO_FILENAME);
            answer.setExtraInstructions(OPEN_NEW_NOTE);
            setOpenNoteName(context, NO_FILENAME);
            if (dialogAnswerListener != null) {
                dialogAnswerListener.saveNote(answer);
            }
        }
    }

    public int getNoteCount(Context context){
        NoteTable noteTable = new NoteTable(context);
        return noteTable.getNoteListCount();
    }

    public boolean noteExists(Context context, String name){
        NoteTable noteTable = new NoteTable(context);
        return noteTable.noteExists(name);
    }

    public boolean renameNote(Context context, String old_name, String new_name){
        NoteTable noteTable = new NoteTable(context);
        return noteTable.renameNote(old_name, new_name);
    }

    public void saveNote(Context context, byte[] note, String name) {
        NoteTable noteTable = new NoteTable(context);
        try{
            noteTable.saveNote(name, note);
        } catch(Exception e){
            Log.d(getString(R.string.ErrorTag), String.format("%s %s", getString(R.string.CouldNotSaveToDatabase), e.getMessage()));
        }
        noteTable.testUpdateInsertNote(); // read the contents of the table (may be removed later)
    }

    //---------------------------------------------------------------------------------------------->

    public void noteNameDialog(Context context, byte[] note, NoteAction noteAction){
        noteNameDialog(context, note, noteAction, "", null, "", false, 0);
    }

    public void noteNameDialog(Context context, byte[] currentNoteContent, NoteAction noteAction, String openNoteName, byte[] openNoteContent){
        noteNameDialog(context, currentNoteContent, noteAction, openNoteName, openNoteContent, "", false, 0);
    }

    public void noteNameDialog(final Context context, NoteAction noteAction, String oldNoteName, boolean isCurrentNote, int position){
        noteNameDialog(context, null, noteAction, "", null, oldNoteName, isCurrentNote, position);
    }

    public void noteNameDialog(final Context context, final byte[] currentNoteContent, final NoteAction noteAction, final String openNoteName, final byte[] openNoteContent, final String oldNoteName,final boolean isCurrentNote, final int position){

        final ExtendedStringAnswer extendedStringAnswer = new ExtendedStringAnswer();
        final EditTextDimensions etd = new EditTextDimensions();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if(!noteAction.equals(NoteAction.CHANGE_NAME)) { // rename -->
            builder.setTitle(R.string.SaveNote);
            builder.setMessage(R.string.SaveNoteExtraText);
        } else{ // save -->
            builder.setTitle(R.string.RenameNote);
            builder.setMessage(R.string.RenameNoteExtraText);
        }

        builder.setIcon(R.mipmap.note_update);
        builder.setCancelable(false); // block back-button

        // set op the dialog extra layout (complex_dialog.xml) -->
        LayoutInflater inf = LayoutInflater.from(context);
        final View complexDialogExtraLayout = inf.inflate(R.layout.complex_dialog, null);

        final EditText cdet = complexDialogExtraLayout.findViewById(R.id.editTextFileName);
        final TextView cdtv = complexDialogExtraLayout.findViewById(R.id.textViewError);
        final ImageView cdiv = complexDialogExtraLayout.findViewById(R.id.imageViewError);

        // initially hide the error objects (text and image) -->
        cdtv.setVisibility(View.INVISIBLE);
        cdiv.setVisibility(View.INVISIBLE);

        // setup simple click listener for the filename field,so when the name text field
        // is clicked the error objects are reset for a new attempt -->
        cdet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide the error object for a new attempt -->
                cdtv.setVisibility(View.INVISIBLE);
                cdiv.setVisibility(View.INVISIBLE);
                // First enlarge the EditText object to reclaim space from the error object -->
                RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(etd.getWidthBase() + etd.getWidthExtension() ,cdet.getHeight());
                lparams.leftMargin = etd.getX();
                cdet.setLayoutParams(lparams);
            }
        });

        // load the view into the dialog -->
        builder.setView(complexDialogExtraLayout);

        // preload the filename if the the action is to rename a note -->
        if(noteAction.equals(NoteAction.CHANGE_NAME)){
            cdet.setText(oldNoteName);
            cdet.selectAll();
        }

        builder.setPositiveButton(R.string.ButtonCaptionOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing. We override the button after the show() event. This way we can
                // override the onDismiss and keep the user trapped' in this dialog and 'force'
                // a valid input.
            }
        });
        builder.setNegativeButton(R.string.ButtonCaptionCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                extendedStringAnswer.setAnswer("");
                // if the action is to open a new note it still has to be done even if the
                // save of the current (new) note is cancelled.
                if (noteAction.equals(NoteAction.SAVE_AND_OPEN)) {
                    extendedStringAnswer.setExtraInstructions(OPEN_SAVED_NOTE);
                    extendedStringAnswer.setNewNoteName(openNoteName); // use the name of the note to be opened
                    extendedStringAnswer.setNewNoteContent(openNoteContent); // pass the new note through to the listener to be opened
                }
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogAnswerListener != null) {
                    if(noteAction.equals(NoteAction.CHANGE_NAME)) {
                        dialogAnswerListener.renameNote(extendedStringAnswer);
                    } else {
                        dialogAnswerListener.saveNote(extendedStringAnswer);
                    }
                }
            }
        });

        final AlertDialog dlg = builder.create();

        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                // if the error objects are not visible dynamically extend the input EditText
                // We must do this here because here the objects are known and the dimensions have values -->
                etd.setWidthExtension(cdiv.getWidth());
                etd.setWidthBase(cdet.getWidth());
                etd.setX((int) cdet.getX());
                etd.setY((int) cdet.getY());

                RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(etd.getWidthBase() + etd.getWidthExtension(),cdet.getHeight());
                // we have to reassign the left margin because of this action, it will now jump to coordinate x = 0,
                // replace (or rather reassign) it with the original value of the design (complex_dialog.xml) -->
                lparams.leftMargin = etd.getX();
                cdet.setLayoutParams(lparams);

            }
        });
        dlg.show();

        // instantiate on click listener here (overriding the default) to be able to manipulate default behaviour. -->
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newNoteName;
                boolean cancelDismiss = false;

                // rename -->
                if (noteAction.equals(NoteAction.CHANGE_NAME)){

                    // input checks -->
                    if (cdet.getText().toString().equals("") || cdet.getText().toString() == null){

                        // First shrink the EditText object to make room for the error objects -->
                        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(etd.getWidthBase() ,cdet.getHeight());
                        lparams.leftMargin = etd.getX();
                        cdet.setLayoutParams(lparams);
                        // show the error objects -->
                        cdiv.setVisibility(View.VISIBLE);
                        cdtv.setVisibility(View.VISIBLE);
                        cdtv.setText(R.string.ValidFileNameRequired);
                        cancelDismiss = true; // <-- do not dismiss dialog
                        return;
                    }

                    newNoteName = cdet.getText().toString();

                    if(noteExists(context, newNoteName)){

                        // First shrink the EditText object to make room for the error objects -->
                        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(etd.getWidthBase() ,cdet.getHeight());
                        lparams.leftMargin = etd.getX();
                        cdet.setLayoutParams(lparams);
                        // show the error objects -->
                        cdiv.setVisibility(View.VISIBLE);
                        cdtv.setVisibility(View.VISIBLE);
                        cdtv.setText(R.string.NoteExists);
                        cancelDismiss = true; // <-- do not dismiss dialog
                        return;
                    }

                    extendedStringAnswer.setPosition(position);
                    extendedStringAnswer.setCurrentNoteName(oldNoteName);
                    extendedStringAnswer.setNewNoteName(newNoteName);
                    extendedStringAnswer.setCurrentNote(isCurrentNote);

                } else {

                    // save -->
                    if (cdet.getText().toString().equals("") || cdet.getText().toString() == null) {
                        newNoteName = String.format("%s-%s", "Note", getCurrentTimestamp());
                    } else {
                        newNoteName = cdet.getText().toString();
                    }

                    extendedStringAnswer.setAnswer(newNoteName);
                    extendedStringAnswer.setCurrentNoteContent(currentNoteContent);

                    if (noteAction.equals(NoteAction.SAVE_NEW)) {
                        extendedStringAnswer.setExtraInstructions(OPEN_NEW_NOTE);
                    } else {
                        if (noteAction.equals(NoteAction.SAVE_AND_OPEN)) {
                            extendedStringAnswer.setExtraInstructions(OPEN_SAVED_NOTE);
                            extendedStringAnswer.setNewNoteName(openNoteName); // use the name of the note to be opened
                            extendedStringAnswer.setNewNoteContent(openNoteContent); // pass the new note through to the listener to be opened
                        }
                    }
                }
                if(!cancelDismiss) {
                    dlg.dismiss();
                }
            }
        });
    }


    public void askUserConfirmationDialog(Context context, NoteAction noteAction){
        askUserConfirmationDialog(context, noteAction, null);
    }

    public void askUserConfirmationDialog(final Context context, NoteAction noteAction, final Note note){

        final ExtendedBooleanAnswer extendedBooleanAnswer = new ExtendedBooleanAnswer();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if(noteAction.equals(NoteAction.DELETE)) {
            builder.setTitle(R.string.ConfirmDelete);
            builder.setIcon(R.mipmap.note_delete);
        } else {
            builder.setTitle(R.string.ConfirmAction);
            builder.setIcon(R.mipmap.dialog_orange_warning);
        }

        builder.setMessage(R.string.AreYouSure);
        builder.setCancelable(false); // <-- block back-button

        if(note != null){ // extra values needed in the listener -->
            extendedBooleanAnswer.setExtraInstructions(String.valueOf(note.getListPosition()));
            extendedBooleanAnswer.setNote(note);
        } else {
            extendedBooleanAnswer.setExtraInstructions(String.valueOf(NOT_INDEXED));
        }

        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                extendedBooleanAnswer.setAnswer(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                extendedBooleanAnswer.setAnswer(false);
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dialogAnswerListener != null) {
                    dialogAnswerListener.deleteNote(extendedBooleanAnswer);  // <-- implemented in MainActivity
                }
            }
        });

        AlertDialog dlg = builder.create();
        dlg.show();

    }

    public void selectPassPointImageCustomDialog(final Context context){

        final SingleIntegerAnswer answer = new SingleIntegerAnswer();

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
        Button btnCamera = dlg.findViewById(R.id.button_camera);
        Button btnGallery = dlg.findViewById(R.id.button_gallery);
        Button btnCancel = dlg.findViewById(R.id.button_cancel);

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
                    dialogAnswerListener.integerAnswerConfirmed(answer);
                    // this method is actually an interface method overridden in MainActivity
                    // Also check MainActivity.java and DialogAnswerListener.java (interface)
                    // Actually dialogAnswerListener IS IN FACT an instance of MainActivity (!)
                }
            }
        });

    }

    private static class EditTextDimensions{

        private int x;
        private int y;
        private int widthBase;
        private int widthExtension;

        public EditTextDimensions() {
        }

        public EditTextDimensions(int x, int y, int widthBase, int widthExtension) {
            this.x = x;
            this.y = y;
            this.widthBase = widthBase;
            this.widthExtension = widthExtension;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidthBase() {
            return widthBase;
        }

        public void setWidthBase(int widthBase) {
            this.widthBase = widthBase;
        }

        public int getWidthExtension() {
            return widthExtension;
        }

        public void setWidthExtension(int widthExtension) {
            this.widthExtension = widthExtension;
        }
    }
}
