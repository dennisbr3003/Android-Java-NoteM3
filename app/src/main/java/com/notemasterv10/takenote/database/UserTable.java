package com.notemasterv10.takenote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.Settings;

import androidx.annotation.Nullable;
import com.notemasterv10.takenote.constants.DatabaseConstants;
import com.notemasterv10.takenote.webservice.WebUser;

import java.util.UUID;

public class UserTable extends Database implements DatabaseConstants  {

    private Context context;

    public UserTable(@Nullable Context context) {
        super(context);
        this.context = context;
    }

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

    public WebUser getUser(String name){

        WebUser webuser = null;

        SQLiteDatabase db = getReadableDatabase();
        try {
            db.beginTransaction();

            Cursor c = db.query(TABLE_USER, new String[]{USER_NAME, USER_PASSWORD, USER_ROLE}, String.format("%s = ?", USER_NAME),
                    new String[]{name},
                    null, null, null);

            if (!(c.getCount() <= 0)) {
                if (c.moveToFirst()) {
                    //note = c.getBlob(0);
                    webuser.setName(c.getString(0));
                    webuser.setPassword(c.getString(1));
                    webuser.setDevice_id(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
                    webuser.setRemark(c.getString(2));
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e){
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
