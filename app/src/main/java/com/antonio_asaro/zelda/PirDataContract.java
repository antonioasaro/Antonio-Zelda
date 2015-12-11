package com.antonio_asaro.zelda;

import android.net.Uri;
import android.provider.BaseColumns;

public class PirDataContract {

    public static final String AUTHORITY = "com.antonio_asaro.zelda.provider";
    public static final String URL = "content://" + AUTHORITY + "/pirdata";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String DATABASE_NAME = "deposits.db";
    public static final String TABLE_NAME = "deposits";

    public static final class DepositEntry implements BaseColumns {
        public static final String DAY_OF = "day_of";
        public static final String TIME_OF = "time_of";
        public static final String DURATION_OF = "duration_of";
    }

}