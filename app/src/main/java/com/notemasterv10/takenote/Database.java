package com.notemasterv10.takenote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.Nullable;

import com.notemasterv10.takenote.constants.DatabaseConstants;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listing.Note;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database extends SQLiteOpenHelper implements DatabaseConstants {

    private Context context;

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
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

    public boolean saveNote(String name, byte[] note){
        // First check if we have to do an update (if the record already exists)
        if(noteExists(name)){
            // update
            return updateNote(name, note);
        } else {
            // insert
            return insertNote(name, note);
        }

    }

    public List<Note> getNoteListing(){
        List<Note> note_list = new ArrayList<Note>();

        SQLiteDatabase sdb = getReadableDatabase();
        sdb.beginTransaction();

        String query = String.format("SELECT %s, %s, %s, %s, %s FROM %s ORDER BY %s", NTS_ID, NTS_NAME, NTS_CREATED, NTS_UPDATED, NTS_FILE, TABLE_NTS, NTS_NAME);
        Cursor cursor = sdb.rawQuery(query, null);

        while(cursor.moveToNext()){ // iterate through the result
            Note note = new Note(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getBlob(4));
            note_list.add(note);
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

        return note_list;
    }

    public boolean deleteNote(String name){

        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("DELETE FROM %s WHERE %s = ?", TABLE_NTS, NTS_NAME);

            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.bindString(1, name);
            stmt.executeUpdateDelete();

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

    private boolean updateNote(String name, byte[] note){

        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?", TABLE_NTS, NTS_FILE, NTS_UPDATED, NTS_NAME);

            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.bindBlob(1, note);
            stmt.bindString(2, getCurrentTimestamp());
            stmt.bindString(3, name);
            stmt.executeUpdateDelete();

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

    public int getNoteListCount() {

        SQLiteDatabase sdb = getReadableDatabase();
        sdb.beginTransaction();

        String sqlQuery = String.format("SELECT COUNT(*) FROM %s", TABLE_NTS);
        SQLiteStatement stmt = sdb.compileStatement(sqlQuery);

        long lngCount = stmt.simpleQueryForLong();

        int intCount = (int) lngCount;
        if((long)intCount != lngCount) {
            return -1;
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

        return intCount;
    }

    private boolean insertNote(String name, byte[] note){

        UUID uuid = UUID.randomUUID();
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?,?,?)",
                    TABLE_NTS, NTS_ID, NTS_NAME, NTS_FILE);
            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.bindString(1, uuid.toString());
            stmt.bindString(2, name);
            stmt.bindBlob(3, note);
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
    public boolean noteExists(String name){

        Boolean isFound = false;
        SQLiteDatabase db = getReadableDatabase();
        try {
            db.beginTransaction();

            Cursor c = db.query(TABLE_NTS, new String[]{NTS_NAME}, String.format("%s = ?", NTS_NAME),
                                new String[]{name},
                               null, null, null);

            if (c.getCount() <= 0) {
                isFound = false;
            } else {
                isFound = true;
            }

            db.setTransactionSuccessful();
        } catch (Exception e){
            isFound = false;
        }
        finally {
            db.endTransaction();
            db.close();
            return isFound;
        }
    }

    public byte[] getNote(String name){

        byte[] note = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            db.beginTransaction();

            Cursor c = db.query(TABLE_NTS, new String[]{NTS_FILE}, String.format("%s = ?", NTS_NAME),
                                new String[]{name},
                               null, null, null);

            if (!(c.getCount() <= 0)) {
                if (c.moveToFirst()) {
                    note = c.getBlob(0);
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e){
            return null;
        }
        finally {
            db.endTransaction();
            db.close();
            return note; // may be null may be not null
        }

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
