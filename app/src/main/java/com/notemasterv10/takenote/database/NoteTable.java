package com.notemasterv10.takenote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.Nullable;

import com.notemasterv10.takenote.constants.DatabaseConstants;
import com.notemasterv10.takenote.listing.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoteTable extends Database implements DatabaseConstants {

    private Context context;

    public NoteTable(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public boolean saveNote(String name, byte[] note){
        // First check if we have to do an update (if the record already exists)
        if(noteExists(name)){ // update
            return updateNote(name, note);
        } else { // insert
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
            Note note = new Note(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getBlob(4));
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

    public boolean renameNote(String old_name, String new_name){

        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("UPDATE %s SET %s = ? WHERE %s = ?", TABLE_NTS, NTS_NAME, NTS_NAME);

            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.bindString(1, new_name);
            stmt.bindString(2, old_name);
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

    public void clearTable(){
        super.clearTable(TABLE_NTS);
    }

    public void testUpdateInsertNote(){

        SQLiteDatabase sdb = getReadableDatabase();
        sdb.beginTransaction();

        String query = String.format("SELECT %s, %s, %s, %s FROM %s ORDER BY %s", NTS_ID, NTS_NAME, NTS_CREATED, NTS_UPDATED, TABLE_NTS, NTS_ID);
        Cursor cursor = sdb.rawQuery(query, null);

        while(cursor.moveToNext()){ // iterate through the result
            Log.d("Record: ", String.format("%s - %s - %s - %s",
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)));
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

    }

}
