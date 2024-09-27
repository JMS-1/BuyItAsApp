package de.jochen_manns.buyitv0;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
    Mit dieser Hilfsklasse wird der Zugriff auf die lokale Datenhaltung
    abstrahiert. Im Wesentlichen geht es dabei um die Auskapselung des
    Lebenszyklus der Datenbank. In dieser aktuellen Anfangsphase gibt es
    noch keine automatische Migration bei neuen Datenbankversionen: alle
    vorherigen Daten werden bei einem Versionswechsel automatisch entfernt.
    Das ist eigentlich auch unkritisch, da sämtliche Inhalte aus der Online
    Datenbank übernommen werden können. Wichtig ist lediglich ein
    regelmäßiger Abgleich vor allem vor einem Versionswechsel der App.
 */
class Database extends SQLiteOpenHelper {
    // Die aktuelle Version des Datenbank Schemas.
    private static final int Sql_Version = 10;

    // Der Name, unter dem die Datenbank lokal gespeichert wird.
    private static final String Sql_Database = "BuyIt.db";

    // Erstellt eine neue Instanz der Hilfsklasse.
    private Database(Context context) {
        super(context, Sql_Database, null, Sql_Version);
    }

    // Erstellt eine neue Instanz der Hilfsklasse.
    public static Database create(Context context) {
        if (context == null)
            throw new NullPointerException("Database.create: context");

        return new Database(context);
    }

    // Wird aufgerufen, wenn die Datenbank erstmalig angelegt werden muss.
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Markets.CreateSql);
        db.execSQL(Products.CreateSql);
    }

    // Wird aufgerufen, wenn sich die Version des Schemas verändert hat.
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Wir legen immer alle Tabellen ganz neu an
        db.execSQL(Products.DropSql);
        db.execSQL(Markets.DropSql);
        onCreate(db);
    }

    // Wird aufgerufen, wenn sich die Version des Schemas verringert hat.
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Wird wie jede andere Änderung des Schemas bearbeitet
        onUpgrade(db, oldVersion, newVersion);
    }

    // Entfernt alle Inhalte aus der Datenbank über eine bereits existierende Zugriffsinstanz.
    public void reset(SQLiteDatabase database) {
        database.execSQL(Products.CleanupSql);
        database.execSQL(Markets.CleanupSql);
    }

    // Entfernt alle Inhalte aus der Datenbank.
    public void reset() {
        // Hier brauchen wir nun wirklich eine Transaktion, um alle Tabellen konsistent zu leeren
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
