package com.example.notematser;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SharedResource {

    private static final String BACKGROUND_COLOR = "BackGroundColor";
    private static final String SHAREDPREF_NAME = "TakeNote";
    private static final String NOTE_FILENAME = "sometextfile";

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

}
