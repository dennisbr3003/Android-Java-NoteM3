package com.notemasterv10.takenote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.Nullable;

import com.notemasterv10.takenote.Constants.DatabaseConstants;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper implements DatabaseConstants {

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL, %s INTEGER NOT NULL)",
                                   TABLE_PNTS, PNTS_ID, PNTS_X, PNTS_Y);
        db.execSQL(sql);

        sql = String.format("CREATE TABLE %s (%s INTEGER AUTO_INCREMENT, %s VARCHAR(20) PRIMARY KEY NOT NULL UNIQUE, " +
                            "%s DATETIME DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')), %s BLOB, %s DATETIME "+
                            "DEFAULT (datetime(CURRENT_TIMESTAMP, 'localtime')))",
                            TABLE_NTS, NTS_ID, NTS_NAME, NTS_CREATED, NTS_FILE, NTS_UPDATED);
        db.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean noteExists(String name){

        Boolean isFound;
        SQLiteDatabase sdb = getReadableDatabase();

        sdb.beginTransaction();

        String query = String.format("SELECT %s FROM %s WHERE %s = '%s'", NTS_NAME, TABLE_NTS, NTS_NAME, name);
        Cursor cursor = sdb.rawQuery(query, null);

        if (cursor.getCount() <= 0) {
            isFound = false;
        } else {
            isFound = true;
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

        return isFound;

    }

    public boolean saveNote(String name, byte[] note){
        // First check if we have to do an update (if the record already exists)
        if(noteExists(name)){
            // update
            return processNote(name, note, TableAction.UPDATE);
        } else {
            // insert
            return processNote(name, note, TableAction.INSERT);
        }

    }

    public byte[] getNote(String name){

        byte[] note = null;
        SQLiteDatabase sdb = getReadableDatabase();

        sdb.beginTransaction();

        String query = String.format("SELECT %s FROM %s WHERE %s = '%s'", NTS_FILE, TABLE_NTS, NTS_NAME, name);
        Cursor cursor = sdb.rawQuery(query, null);

        if (!(cursor.getCount() <= 0)){
            if(cursor.moveToFirst()){
                note = cursor.getBlob(0);
            }
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

        return note; // may be null may be not null

    }

    private boolean processNote(String name, byte[] note, TableAction tableAction){

        ContentValues values = new ContentValues();

        SQLiteDatabase sdb = getWritableDatabase();
        sdb.beginTransaction();

        values.put(NTS_NAME, name);
        values.put(NTS_UPDATED, getCurrentTimestamp());
        values.put(NTS_FILE, note); // blob = byte array

        switch(tableAction){
            case INSERT:
                sdb.insert(TABLE_NTS, null, values); // put values in record
                break;
            case UPDATE:
                sdb.update(TABLE_NTS, values, String.format("%s = '%s'", NTS_NAME, name), null); // put values in record
                break;
            default:
                break;
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

        return true;

    }

    private String getCurrentTimestamp(){
        // Formatted like (datetime(CURRENT_TIMESTAMP, 'localtime')) -->
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf.format(timestamp);
    }

    public void testUpdateInsertNote(){

        SQLiteDatabase sdb = getReadableDatabase();
        sdb.beginTransaction();

        String query = String.format("SELECT %s, %s, %s, %s FROM %s ORDER BY %s", NTS_ID, NTS_NAME, NTS_CREATED, NTS_UPDATED, TABLE_NTS, NTS_ID);
        Cursor cursor = sdb.rawQuery(query, null);

        while(cursor.moveToNext()){ // iterate through the result
            Log.d("Record: ", String.format("%s - %s - %s - %s",
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)));
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

    }

    public void setPoints(List<Point> points) {

        SQLiteDatabase sdb = getWritableDatabase();
        sdb.beginTransaction();

        //First clear the table
        sdb.delete(TABLE_PNTS,null, null); // delete all so no arguments
        int i=0;

        for(Point p: points){
            ContentValues values = new ContentValues();
            values.put(PNTS_ID, i);
            values.put(PNTS_X, p.x);
            values.put(PNTS_Y, p.y);
            sdb.insert(TABLE_PNTS,null, values); // put values in record
            i++;
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

    }

    public List<Point> getPoints(){

        List<Point> points = new ArrayList<Point>();

        SQLiteDatabase sdb = getReadableDatabase(); // because we are going to read
        sdb.beginTransaction();

        // create SQL statement   columnIndex 0   1 (starts with 0)
        String query = String.format("SELECT %s, %s FROM %s ORDER BY %s", PNTS_X, PNTS_Y, TABLE_PNTS, PNTS_ID);
        Cursor cursor = sdb.rawQuery(query, null);

        while(cursor.moveToNext()){ // iterate through the result
            points.add(new Point(cursor.getInt(0), cursor.getInt(1)));
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

        return points;

    }

}
