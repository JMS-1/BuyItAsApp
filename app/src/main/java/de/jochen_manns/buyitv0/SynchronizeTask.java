package de.jochen_manns.buyitv0;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Eine Hintergrundaufgabe zur Durchführung des Abgleichs der lokalen Datenbank
    mit dem Online Datenbestand.
 */
abstract class SynchronizeTask extends JsonRequestTask {
    // Der Name der JSON Eigenschaft mit der Liste der Märkte.
    private static final String Name_Markets = "markets";

    // Der Name der JSON Eigenschaft mit der Liste der Produkte.
    private static final String Name_Items = "items";

    // Gesetzt, wenn die lokale Datenbank auf jeden Fall gelöscht werden soll - etwa auch im Fehlerfall nach einem Benutzerwechsel
    private final boolean m_clear;

    // Initialisiert die Aufgabe.
    protected SynchronizeTask(Activity activity, boolean alwaysClearDatabase) {
        super(activity, "sync.php");

        m_clear = alwaysClearDatabase;
    }

    @Override
    protected JSONObject doInBackground() {
        // Falls der Benutzer seine Anmeldung verändert hat löschen wir auf jeden Fall die Datenbank
        if (m_clear) {
            try (Database database = Database.create(Context)) {
                database.reset();
            }
        }

        // Nun erst wird der Web Service aufgerufen
        return super.doInBackground();
    }

    protected void fillRequest(JSONObject postData) throws JSONException {
        // Die Registrierung des Benutzers wird immer in einer separaten JSON Eigenschaft übertragen
        User user = User.load(Context);
        postData.put(REQUEST_USER, (user == null) ? null : user.Identifier);

        // Zugriffsinstanz auf die lokale Datenbank erstellen
        try (Database database = Database.create(Context)) {
            // Zum Auslesen verwenden wir eine Lesetransaktion
            try (SQLiteDatabase db = database.getReadableDatabase()) {
                db.beginTransaction();
                try {
                    // Die JSON Eigenschaften der Anfrage mit den benötigten Liste füllen
                    postData.put(Name_Items, Products.queryForUpdate(db));
                    postData.put(Name_Markets, Markets.queryForUpdate(db));
                } finally {
                    db.endTransaction();
                }
            }
        }
    }

    // Verarbeitet die Antwort des Web Service.
    protected void processResponse(JSONObject postReply) throws JSONException {
        // Die Datenbank wir in einer Transaktion konsistent befüllt
        try (Database database = Database.create(Context)) {
            try (SQLiteDatabase db = database.getWritableDatabase()) {
                db.beginTransaction();
                try {
                    // Nach dem Leeren der Tabellen werden Märkte und Produkte unverändert in die lokale Datenbank übertragen
                    database.reset(db);
                    Markets.synchronize(db, postReply.getJSONArray(Name_Markets));
                    Products.synchronize(db, postReply.getJSONArray(Name_Items));

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }
        }
    }
}


