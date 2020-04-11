package com.example.notematser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SharedResource extends AppCompatActivity {

    private static final String BACKGROUND_COLOR = "BackGroundColor";
    private static final String SHAREDPREF_NAME = "TakeNote";
    private static final String NOTE_FILENAME = "sometextfile";

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
        prefsEdit.putInt(BACKGROUND_COLOR, iColor);
        prefsEdit.apply(); // apply is background, commit is not
    }

    public int getSharedBackgroundColor(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHAREDPREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(BACKGROUND_COLOR, -1);
    }

    public void saveNoteText(Context context, byte[] byteArray) {
        try {
            FileOutputStream fos = context.openFileOutput(NOTE_FILENAME, Context.MODE_PRIVATE);
            fos.write(byteArray);
            fos.close();
        } catch (FileNotFoundException e) {
            // todo error-handling
        } catch (IOException e) {
            // todo error-handling
        }
    }


    public void askUserConfirmationDialog(final Context context){

        final BooleanAnswerObject answer = new BooleanAnswerObject();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.ConfirmDialogTitle);
        builder.setMessage(R.string.AreYouSure);
        builder.setIcon(R.mipmap.dialog_orange_warning);

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
                Log.d(context.getString(R.string.DefaultTag), context.getString(R.string.debug_dialog_listener)  + String.valueOf(dialogAnswerListener != null));
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
        dlg.show();

        // set on-click-listeners on the buttons
        Button btnCamera = (Button) dlg.findViewById(R.id.button_camera);
        Button btnGallery = (Button) dlg.findViewById(R.id.button_gallery);
        Button btnCancel = (Button) dlg.findViewById(R.id.button_cancel);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(context.getString(R.string.DefaultTag),context.getString(R.string.camera_button_dlg));
                answer.setAnswer(1);
                dlg.dismiss();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // leave out 'context' to get null pointer exception
                Log.d(context.getString(R.string.DefaultTag),context.getString(R.string.gallery_button_dlg));
                answer.setAnswer(2);
                dlg.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(context.getString(R.string.DefaultTag),context.getString(R.string.cancel_button_dlg));
                answer.setAnswer(0);
                dlg.dismiss();
            }
        });

        // custom dialogs (using their own xml) own the onDismiss event so you need to bind it to the object itself, not the builder
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(context.getString(R.string.DefaultTag), context.getString(R.string.debug_dialog_listener) + String.valueOf(dialogAnswerListener != null));
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
