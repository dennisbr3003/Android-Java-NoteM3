package com.notemasterv10.takenote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.Nullable;

import com.notemasterv10.takenote.constants.DatabaseConstants;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public abstract class Database extends SQLiteOpenHelper implements DatabaseConstants {

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // coordinates
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL, %s INTEGER NOT NULL)",
                TABLE_PNTS, PNTS_ID, PNTS_X, PNTS_Y);
        db.execSQL(sql);

        Log.d("DENNIS_BRINK", "Created table POINTS");

        // notes
        sql = String.format("CREATE TABLE %s (%s VARCHAR(50), %s VARCHAR(20) PRIMARY KEY NOT NULL UNIQUE, " +
                        "%s DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), %s BLOB, %s DATETIME "+
                        "DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')))",
                TABLE_NTS, NTS_ID, NTS_NAME, NTS_CREATED, NTS_FILE, NTS_UPDATED);
        db.execSQL(sql);

        Log.d("DENNIS_BRINK", "Created table NOTES");

        // image
        sql = String.format("CREATE TABLE %s (%s VARCHAR(50), %s VARCHAR(20) PRIMARY KEY NOT NULL UNIQUE, " +
                        "%s DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), %s BLOB, %s DATETIME "+
                        "DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')))",
                TABLE_PPI, PPI_ID, PPI_NAME, PPI_CREATED, PPI_FILE, PPI_UPDATED);
        db.execSQL(sql);

        Log.d("DENNIS_BRINK", "Created table PPIMAGE");

        // user
        sql = String.format("CREATE TABLE %s (%s VARCHAR(50), %s VARCHAR(30) PRIMARY KEY NOT NULL UNIQUE, %s VARCHAR(100) NOT NULL, " +
                        "%s VARCHAR(20) DEFAULT ('USER'), %s DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), %s DATETIME "+
                        "DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')))",
                TABLE_USER, USER_ID, USER_NAME, USER_PASSWORD, USER_ROLE, USER_CREATED, USER_UPDATED);
        db.execSQL(sql);

        Log.d("DENNIS_BRINK", "Created table ACCOUNT (user)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getCurrentTimestamp(){
        // Formatted like (datetime(CURRENT_TIMESTAMP, 'localtime')) -->
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf.format(timestamp);
    }

    public void clearTable(String tableName){

        SQLiteDatabase db = getWritableDatabase();
        db.enableWriteAheadLogging();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("DELETE FROM %s ", tableName);

            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.executeUpdateDelete();

            db.setTransactionSuccessful();

        } catch(Exception e){
            // do nothing
        }
        finally{
            db.endTransaction();
            db.close();
        }

    }

}
