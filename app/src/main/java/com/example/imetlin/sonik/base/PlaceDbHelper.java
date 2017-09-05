package com.example.imetlin.sonik.base;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.imetlin.sonik.base.MyBase.PlaceEntry;
/**
 * Created by i.metlin on 04.09.2017.
 */

public class PlaceDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;


    public PlaceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PLACES_TABLE = "CREATE_TABLE " +
                PlaceEntry.TABLE_NAME + "(" +
                PlaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PlaceEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " + "UNIQUE (" +
                PlaceEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE" +
                "); ";
        sqLiteDatabase.execSQL(SQL_CREATE_PLACES_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + PlaceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
