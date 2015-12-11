package com.antonio_asaro.zelda;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

public class PirDataProvider extends ContentProvider {

    private static final String TAG = "pirdata";
    static final int PIRDATA = 100;
    static final int PIRDATA_ID = 101;
    static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(PirDataContract.AUTHORITY, "pirdata", PIRDATA);
        sUriMatcher.addURI(PirDataContract.AUTHORITY, "pirdata/#", PIRDATA_ID);
    }

    private PirDataDbHelper mPirDataDbHelper = null;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "Calling oncreate of provider");
        mPirDataDbHelper = new PirDataDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PIRDATA:
                return "vnd.android.cursor.dir/vnd.com.antonio_asaro.zelda.provider.pirdata";
            case PIRDATA_ID:
                return "vnd.android.cursor.item/vnd.com.antonio_asaro.zelda.provider.pirdata";

        }
        return "";
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mPirDataDbHelper.getWritableDatabase();
        long _id = db.insert(PirDataContract.TABLE_NAME, null, values);
        Log.d(TAG, "Calling insert of provider");

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}

