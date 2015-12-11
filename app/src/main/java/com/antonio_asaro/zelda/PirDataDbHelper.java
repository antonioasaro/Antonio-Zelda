package com.antonio_asaro.zelda;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PirDataDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE = "CREATE TABLE " + PirDataContract.TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY, DAY_OF TEXT , TIME_OF TEXT , DURATION_OF TEXT )";
    private static final String SQL_DROP = "DROP TABLE IS EXISTS " + PirDataContract.TABLE_NAME;

    PirDataDbHelper(Context context) {
        super(context, PirDataContract.DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        onCreate(db);
    }
}
