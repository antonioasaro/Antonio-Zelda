package com.antonio_asaro.zelda;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PirDataDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "pirdatadbhelper";

    static final int DATABASE_VERSION = 2;
    private static final String SQL_CREATE =
                    " CREATE TABLE " + PirDataContract.TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY, " +
                    " DAY_OF TEXT NOT NULL , " +
                    " TIME_OF TEXT NOT NULL ," +
                    " DURATION_OF TEXT NOT NULL );";
    private static final String SQL_DROP = "DROP TABLE IF EXISTS " + PirDataContract.TABLE_NAME;

    PirDataDbHelper(Context context) {
        super(context, PirDataContract.DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "Calling PirDataDbHelper constructor");    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Calling PirDataDbHelper oncreate");
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        onCreate(db);
    }
}
