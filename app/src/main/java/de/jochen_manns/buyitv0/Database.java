package de.jochen_manns.buyitv0;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class Database extends SQLiteOpenHelper {
    private static final int Sql_Version = 8;

    private static final String Sql_Database = "BuyIt.db";

    private Database(Context context) {
        super(context, Sql_Database, null, Sql_Version);
    }

    public static Database create(Context context) {
        if (context == null)
            throw new NullPointerException("Database.create: context");

        return new Database(context);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Markets.CreateSql);
        db.execSQL(Products.CreateSql);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Products.DropSql);
        db.execSQL(Markets.DropSql);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void reset(SQLiteDatabase database) {
        database.execSQL(Products.CleanupSql);
        database.execSQL(Markets.CleanupSql);
    }

    public void reset() {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                reset(db);

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }
}
