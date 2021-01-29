package com.notemasterv10.takenote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.notemasterv10.takenote.constants.DatabaseConstants;
import com.notemasterv10.takenote.webservice.WebUser;

import java.util.UUID;

public class UserTable extends Database implements DatabaseConstants  {

    private Context context;

    public UserTable(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean insertUser(WebUser webuser){

        UUID uuid = UUID.randomUUID();
        SQLiteDatabase db = getWritableDatabase();
        db.enableWriteAheadLogging();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?,?,?,?)",
                    TABLE_USER, USER_ID, USER_NAME, USER_PASSWORD, USER_ROLE);
            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.bindString(1, uuid.toString());
            stmt.bindString(2, webuser.getName());
            stmt.bindString(3, webuser.getPassword()); /* encrypted */
            stmt.bindString(4, webuser.getRemark());
            stmt.executeInsert();

            db.setTransactionSuccessful();
            return true;
        } catch(Exception e){
            return false;
        }
        finally{
            db.endTransaction();
            db.close();
        }
    }

    public WebUser getFirstUser(){
        WebUser webuser = new WebUser();

        SQLiteDatabase db = getReadableDatabase();
        try {
            db.beginTransaction();

            // create SQL statement   columnIndex 0   1 (starts with 0)
            String query = String.format("SELECT %s, %s, %s, %s " +
                    "FROM %s ORDER BY %s LIMIT 1", USER_ID, USER_NAME, USER_PASSWORD, USER_ROLE, TABLE_USER, USER_NAME);

            Cursor c = db.rawQuery(query, null);

            if (!(c.getCount() <= 0)) {
                if (c.moveToFirst()) {
                    webuser.setName(c.getString(1));
                    webuser.setPassword(c.getString(2)); /* encrypted */
                    webuser.setDevice_id(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
                    webuser.setRemark(c.getString(3));
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e){
            Log.d("DENNIS_BRINK", "Retrieve webuser without name argument failed: " + e.getMessage());
            return null;
        }
        finally {
            db.endTransaction();
            db.close();
            return webuser; // may be null may be not null
        }
    }

    public WebUser getUser(String name){

        WebUser webuser = new WebUser();

        SQLiteDatabase db = getReadableDatabase();
        try {
            db.beginTransaction();

            Cursor c = db.query(TABLE_USER, new String[]{USER_NAME, USER_PASSWORD, USER_ROLE}, String.format("%s = ?", USER_NAME),
                    new String[]{name},
                    null, null, null);

            if (!(c.getCount() <= 0)) {
                if (c.moveToFirst()) {
                    webuser.setName(c.getString(0));
                    webuser.setPassword(c.getString(1));
                    webuser.setDevice_id(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
                    webuser.setRemark(c.getString(2));
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e){
            Log.d("DENNIS_BRINK", "Retrieve webuser with name argument failed: " + e.getMessage());
            return null;
        }
        finally {
            db.endTransaction();
            db.close();
            return webuser; // may be null may be not null
        }

    }

    public void clearTable(){
        super.clearTable(TABLE_USER);
    }

}
