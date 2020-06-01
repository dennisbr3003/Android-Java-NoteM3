package com.notemasterv10.takenote.library;

import android.app.ActionBar;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.notemasterv10.takenote.Database;
import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.listeners.DialogAnswerListener;
import com.notemasterv10.takenote.listing.Note;

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

    public void deleteNote(Context context, Note note){
        Database sdb = new Database(context);
        sdb.deleteNote(note.getName());
        if (note.isCurrentNote()){
            // clear textview and open a new one
            ComplexDialogAnswer answer = new ComplexDialogAnswer();
            answer.setAnswer(NO_FILENAME);
            answer.setExtraInstruction("X");
            setOpenNoteName(context, NO_FILENAME);
            if (dialogAnswerListener != null) {
                dialogAnswerListener.saveAnswerConfirmed(answer);
            }
        }
    }

    public int getNoteCount(Context context){
        Database sdb = new Database(context);
        return sdb.getNoteListCount();
    }

    public boolean noteExists(Context context, String name){
        Database sdb = new Database(context);
        return sdb.noteExists(name);
    }

    public boolean renameNote(Context context, String old_name, String new_name){
        Database sdb = new Database(context);
        return sdb.renameNote(old_name, new_name);
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

        final ComplexDialogAnswer answer = new ComplexDialogAnswer();
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

        builder.setPositiveButton(R.string.btn_caption_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing. We override the button after the show() event. This way we can
                // override the onDismiss and keep the user 'trapped'in this dialog and force
                // a valid input.
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
                if (dialogAnswerListener != null) {
                    if(noteAction.equals(NoteAction.CHANGE_NAME)) {
                        dialogAnswerListener.renameAnswerConfirmed(answer);
                    } else {
                        dialogAnswerListener.saveAnswerConfirmed(answer);
                    }
                } else{
                    Log.d("DB", "dialogAnswerListener null!!");
                }
            }
        });

        final AlertDialog dlg = builder.create();

        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                // if the error object are not visible dynamically extend the input EditText
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
                    if (cdet.getText().toString().equals("") || cdet.getText().toString().equals(null)){

                        // First shrink the EditText object to make room for the error objects -->
                        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(etd.getWidthBase() ,cdet.getHeight());
                        lparams.leftMargin = etd.getX();
                        cdet.setLayoutParams(lparams);
                        // show the error objects -->
                        cdiv.setVisibility(View.VISIBLE);
                        cdtv.setVisibility(View.VISIBLE);
                        cdtv.setText(R.string.valid_filename_required);
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
                        cdtv.setText(R.string.note_exists);
                        cancelDismiss = true; // <-- do not dismiss dialog
                        return;
                    }

                    answer.setPosition(position);
                    answer.setRename_oldname(oldNoteName);
                    answer.setRename_newname(newNoteName);
                    answer.setRename_iscurrent(isCurrentNote);

                } else {

                    // save -->
                    if (cdet.getText().toString().equals("") || cdet.getText().toString().equals(null)) {
                        newNoteName = String.format("%s-%s", "Note", getCurrentTimestamp());
                    } else {
                        newNoteName = cdet.getText().toString();
                    }

                    answer.setAnswer(newNoteName);
                    answer.setCurrent_content(currentNoteContent);

                    if (noteAction.equals(NoteAction.SAVE_NEW)) {
                        answer.setExtraInstruction(OPEN_NEW_NOTE);
                    } else {
                        if (noteAction.equals(NoteAction.SAVE_AND_OPEN)) {
                            answer.setExtraInstruction(OPEN_SAVED_NOTE);
                            answer.setOpen_existing_note(openNoteName); // use the name of the note to be opened
                            answer.setNew_content(openNoteContent); // pass the new note through to the listener to be opened
                        }
                    }
                }
                if(!cancelDismiss) {
                    dlg.dismiss();
                }
            }
        });
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

    private class EditTextDimensions{

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
