package com.example.imetlin.sonik.base;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by i.metlin on 04.09.2017.
 */

public class MyBase {

    public static final String AUTHORITY = "com.example.imetlin";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_PLACES = "places";




    public static final class PlaceEntry implements BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_PLACE_ID = "placeID";

    }
}
