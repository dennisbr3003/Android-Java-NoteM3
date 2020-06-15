package com.notemasterv10.takenote.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;

import androidx.annotation.Nullable;

import com.notemasterv10.takenote.constants.DatabaseConstants;

import java.util.ArrayList;
import java.util.List;

public class PointTable extends Database implements DatabaseConstants {

    private Context context;

    public PointTable(@Nullable Context context) {
        super(context);
        this.context = context;
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
