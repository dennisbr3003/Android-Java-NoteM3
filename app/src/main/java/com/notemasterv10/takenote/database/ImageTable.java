package com.notemasterv10.takenote.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import androidx.annotation.Nullable;

import com.notemasterv10.takenote.constants.DatabaseConstants;
import com.notemasterv10.takenote.library.PassPointImage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ImageTable extends Database implements DatabaseConstants {

    private Context context;

    public ImageTable(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public boolean savePassPointImage(String name, byte[] passpointimage){
        // First check if we have to do an update (if the record already exists)
        if(passPointImageExists(name)){ // update
            return updatePassPointImage(name, passpointimage);
        } else { // insert
            return insertPassPointImage(name, passpointimage);
        }
    }

    public boolean deletePasspointImage(String name){

        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("DELETE FROM %s WHERE %s = ?", TABLE_PPI, PPI_NAME);

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

    private boolean updatePassPointImage(String name, byte[] passpointimage){

        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("UPDATE %s SET %s = ?, %s = ? WHERE %s = ?", TABLE_PPI, PPI_FILE, PPI_UPDATED, PPI_NAME);

            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.bindBlob(1, passpointimage);
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

    private boolean insertPassPointImage(String name, byte[] passpointimage){

        UUID uuid = UUID.randomUUID();
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();
            String sqlQuery = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?,?,?)",
                    TABLE_PPI, PPI_ID, PPI_NAME, PPI_FILE);
            SQLiteStatement stmt = db.compileStatement(sqlQuery);

            stmt.clearBindings();
            stmt.bindString(1, uuid.toString());
            stmt.bindString(2, name);
            stmt.bindBlob(3, passpointimage);
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

    private boolean passPointImageExists(String name){

        Boolean isFound = false;
        SQLiteDatabase db = getReadableDatabase();
        try {
            db.beginTransaction();

            Cursor c = db.query(TABLE_PPI, new String[]{PPI_NAME}, String.format("%s = ?", PPI_NAME),
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

    public byte[] getPassPointImage(String name){

        byte[] passpointimage = null;
        SQLiteDatabase db = getReadableDatabase();
        try {
            db.beginTransaction();

            Cursor c = db.query(TABLE_PPI, new String[]{PPI_FILE}, String.format("%s = ?", PPI_NAME),
                    new String[]{name},
                    null, null, null);

            if (!(c.getCount() <= 0)) {
                if (c.moveToFirst()) {
                    passpointimage = c.getBlob(0);
                }
            }
            db.setTransactionSuccessful();
        } catch(Exception e){
            return null;
        }
        finally {
            db.endTransaction();
            db.close();
            return passpointimage; // may be null may be not null
        }

    }

    public List<PassPointImage> getPassPointImageListing(){

        List<PassPointImage> passPointImageList = new ArrayList<PassPointImage>();

        SQLiteDatabase sdb = getReadableDatabase();
        sdb.beginTransaction();

        String query = String.format("SELECT %s, %s, %s, %s, %s FROM %s ORDER BY %s", PPI_ID, PPI_NAME, PPI_CREATED, PPI_UPDATED, PPI_FILE, TABLE_PPI, PPI_NAME);
        Cursor cursor = sdb.rawQuery(query, null);

        while(cursor.moveToNext()){ // iterate through the result
            PassPointImage passPointImage = new PassPointImage(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getBlob(4));
            passPointImageList.add(passPointImage);
        }

        sdb.setTransactionSuccessful();
        sdb.endTransaction();
        sdb.close();

        return passPointImageList;
    }

    public void clearTable(){
        super.clearTable(TABLE_PPI);
    }

    public void testUpdateInsertPassPointImage(){

        SQLiteDatabase sdb = getReadableDatabase();
        sdb.beginTransaction();

        String query = String.format("SELECT %s, %s, %s, %s FROM %s ORDER BY %s", PPI_ID, PPI_NAME, PPI_CREATED, PPI_UPDATED, TABLE_PPI, PPI_ID);
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
