package com.antonio_asaro.zelda;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;

public class PirDataProvider extends ContentProvider {
    private static final String TAG = "pirdataprovider";

    static final int PIRDATA = 100;
    static final int PIRDATA_ID = 101;
    static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(PirDataContract.AUTHORITY, "pirdata", PIRDATA);
        sUriMatcher.addURI(PirDataContract.AUTHORITY, "pirdata/#", PIRDATA_ID);
    }

    private SQLiteDatabase mDatabase;
    SQLiteQueryBuilder mQueryBuilder;
    private PirDataDbHelper mPirDataDbHelper = null;
    private static HashMap<String, String> mPirDataMap;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "Calling oncreate of provider");
        mPirDataDbHelper = new PirDataDbHelper(getContext());
        mDatabase = mPirDataDbHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "Calling query of provider");
        mQueryBuilder = new SQLiteQueryBuilder();
        mQueryBuilder.setTables(PirDataContract.TABLE_NAME);
        mQueryBuilder.setProjectionMap(mPirDataMap);
        Cursor cursor = mQueryBuilder.query(mDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        Log.d(TAG, "Return from query of provider");
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.d(TAG, "Returning cursor of provider");
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PIRDATA:
                return "vnd.android.cursor.dir/vnd.com.antonio_asaro.zelda.provider.pirdata";
            case PIRDATA_ID:
                return "vnd.android.cursor.item/vnd.com.antonio_asaro.zelda.provider.pirdata";
        }
        return "";
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long _id = mDatabase.insert(PirDataContract.TABLE_NAME, null, values);
        Log.d(TAG, "Calling insert of provider ");
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "Calling delete of provider");
        return mDatabase.delete(PirDataContract.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

