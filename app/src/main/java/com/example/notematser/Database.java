package com.example.notematser;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Point;

import androidx.annotation.Nullable;

import java.util.List;

public class Database extends SQLiteOpenHelper {

    private static final String TABLE_NAME = "POINTS";
    private static final String COL_ID = "ID";
    private static final String COL_X = "X";
    private static final String COL_Y = "Y";

    public Database(@Nullable Context context) {
        super(context, "takenote.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // What if the table already exists?
        String sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER NOT NULL, %s INTEGER NOT NULL)", TABLE_NAME, COL_ID, COL_X, COL_Y);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void setPoints(List<Point> points) {
        SQLiteDatabase sdb = getWritableDatabase();
        //First clear the table
        sdb.delete("POINTS",null, null); // delete all so no arguments
        int i=0;
        for(Point p: points){
            ContentValues values = new ContentValues();
            values.put(COL_ID, i);
            values.put(COL_X, p.x);
            values.put(COL_Y, p.y);
            sdb.insert(TABLE_NAME,null, values);
            i++;
        }
        sdb.close();
    }
}
