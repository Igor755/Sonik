package com.example.imetlin.sonik.base;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaCasException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by i.metlin on 04.09.2017.
 */

public class PlaceContentProvider extends ContentProvider {

    public static final int PLACES = 100;
    public static final int PLACE_WITH_ID = 101;

    public static final UriMatcher mUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MyBase.AUTHORITY, MyBase.PATH_PLACES, PLACES);
        uriMatcher.addURI(MyBase.AUTHORITY, MyBase.PATH_PLACES + "/#", PLACE_WITH_ID);


        return uriMatcher;
    }

    private PlaceDbHelper mPlaceDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mPlaceDbHelper = new PlaceDbHelper(context);
        return true;
    }



    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();

        int match = mUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case PLACES:
                long id = db.insert(MyBase.PlaceEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MyBase.PlaceEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into" + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }



    @Override
    public Cursor query(@NonNull Uri uri,  String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mPlaceDbHelper.getReadableDatabase();
        int match = mUriMatcher.match(uri);
        Cursor retCursor;
        switch (match) {
            case PLACES:
                retCursor = db.query(MyBase.PlaceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknoun uri:" + uri);

        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;

    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);
        int placesDeleted;
        switch (match) {
            case PLACE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                placesDeleted = db.delete(MyBase.PlaceEntry.TABLE_NAME, "_id = ?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);

        }
        if (placesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return placesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {

        final SQLiteDatabase db = mPlaceDbHelper.getWritableDatabase();
        int match = mUriMatcher.match(uri);

        int placesUpdate;

        switch (match) {
            case PLACE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                placesUpdate = db.update(MyBase.PlaceEntry.TABLE_NAME, contentValues, "_id = ?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);

        }
        if (placesUpdate != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return placesUpdate;
    }
    @Override
    public String getType(@NonNull Uri uri){
        throw new UnsupportedOperationException("Not yet emplemented");

    }

}
