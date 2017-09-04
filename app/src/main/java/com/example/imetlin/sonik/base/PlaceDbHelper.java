package com.example.imetlin.sonik.base;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by i.metlin on 04.09.2017.
 */

public class PlaceDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 2;


    public PlaceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_PLACE_TABLE = "CREATE_TABLE" + MyBase.PlaceEntry.TABLE_NAME + "(" +
                MyBase.PlaceEntry._ID + "INTEGER PRIMARY KEY AUTOINCREMENT," +
                MyBase.PlaceEntry.COLUMN_PLACE_ID + "TEXT NOT NULL, " +
                "UNIQUE (" + MyBase.PlaceEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_PLACE_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + MyBase.PlaceEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
