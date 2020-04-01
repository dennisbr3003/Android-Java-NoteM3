package com.example.notematser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SharedResource {

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

        final AnswerObject answer = new AnswerObject();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.ConfirmDialogTitle);
        builder.setMessage(R.string.AreYouSure);
        builder.setIcon(R.mipmap.dialog_orange_warning);

        builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                answer.setConfirmed(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                answer.setConfirmed(false);
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(context.getString(R.string.DefaultTag), "Is the listener actually listening? " + String.valueOf(dialogAnswerListener != null));
                if (dialogAnswerListener != null) {
                    dialogAnswerListener.answerConfirmed(answer.isConfirmed()); // this method is actually an interface method overridden in MainActivity
                                                                                // Also check MainActivity.java and DialogAnswerListener.java (interface)
                                                                                // Actually dialogAnswerListener IS IN FACT an instance of MainActivity (!)
                }
            }
        });

        AlertDialog dlg = builder.create();
        dlg.show();

    }

    private class AnswerObject {

        private boolean confirmed;

        public AnswerObject() {
            this.setConfirmed(false);
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public void setConfirmed(boolean confirmed) {
            this.confirmed = confirmed;
        }

    }

}
