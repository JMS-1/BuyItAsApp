package de.jochen_manns.buyitv0;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
    Kapselt die Datenbankzugriffe auf Märkte.
 */
class Markets {
    // Der Name der Datenbanktabelle der Märkte.
    private static final String Table = "markets";

    // Der SQL Befehl zum Entfernen der Datenbanktabelle der Märkte.
    public static final String DropSql = "DROP TABLE IF EXISTS " + Table;

    // Der SQL Befehl zum Entfernen aller Märke aus der Datenbank.
    public static final String CleanupSql = "DELETE FROM " + Table;

    // Der Name der Spalte (und der JSON Eigenschaft) mit der Löschmarkierung eines Marktes.
    private static final String Deleted = "deleted";

    // Der Name der Spalte (und der JSON Eigenschaft) mit dem aktuellen lokalen Namen eines Marktes.
    private static final String Name = "name";

    // Der Name der Spalte (und der JSON Eigenschaft) mit dem Online bekannten Namen eines Marktes.
    private static final String OriginalName = "originalName";

    // Der SQL Befehl zum Anlegen der Datenbanktabelle der Märkte.
    public static final String CreateSql = "CREATE TABLE " + Table + "(" + Name + " TEXT, " + OriginalName + " TEXT, " + Deleted + " INTEGER)";

    // Die für die Liste der Märkte benötigten Eigenschaften respektive Spalten eines Marktes.
    private final static String[] s_MarketListColumns = {Name, OriginalName};

    // Liest Märkte aus der Datenbank aus.
    private static JSONObject[] build(Cursor query) throws JSONException {
        try {
            // Zur internen Kommunikation verwendet wird die selben JSON Strukturen wie sie auch der Web Service kennt
            JSONObject[] markets = new JSONObject[query.getCount()];
            if (markets.length > 0) {
                // Einmal die Positionen der Spalten ermitteln.
                int nameColumn = query.getColumnIndex(Name);
                int originalNameColumn = query.getColumnIndex(OriginalName);
                int deleteColumn = query.getColumnIndex(Deleted);

                for (int i = 0; i < markets.length; i++) {
                    query.moveToNext();

                    // Spalten aus der Datenbank in die JSON Repräsentation übertragen
                    JSONObject market = new JSONObject();
                    if (nameColumn >= 0)
                        market.put(Name, query.getString(nameColumn));
                    if (originalNameColumn >= 0)
                        market.put(OriginalName, query.getString(originalNameColumn));
                    if (deleteColumn >= 0)
                        market.put(Deleted, query.getInt(deleteColumn) != 0);

                    markets[i] = market;
                }
            }

            return markets;
        } finally {
            query.close();
        }
    }

    // Führt eine Suche über die Datenbanktabelle der Märkte aus.
    private static JSONObject[] query(Database database, String[] columns, String selection, String order) throws JSONException {
        SQLiteDatabase db = database.getReadableDatabase();
        try {
            return build(db.query(Table, columns, selection, null, null, null, order));
        } finally {
            db.close();
        }
    }

    // Ermittelt alle Märkte zur Übertragung an den Web Service.
    public static JSONArray queryForUpdate(SQLiteDatabase database) throws JSONException {
        JSONArray markets = new JSONArray();

        // In diesem Fall fordern wir alle Spalten (JSON Eigenschaften) aus der Datenbanktabelle an
        for (JSONObject market : build(database.query(Table, null, null, null, null, null, null)))
            markets.put(market);

        return markets;
    }

    // Ermittelt alle Märkte zur Auswahl durch den Anwender.
    public static JSONObject[] query(Database database) throws JSONException {
        // Hier benötigen wir nur die Märkte, die nicht lokal gelöscht wurden
        return query(database, s_MarketListColumns, Deleted + "=0", Name);
    }

    // Aktualisiert die Daten eines Marktes oder legt einen neuen Markt an
    public static void update(Database database, String originalName, String name) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            // Auf jeden Fall wird der Name an die Datenbank übertragen
            ContentValues values = new ContentValues();
            values.put(Name, name);

            // Je nach Exisitenz des Marktes müssen wir aber nun unterschiedlich agieren
            if (originalName == null) {
                // Beim Neuanlegen wird auch ein neuer Name vergeben - TODO: theoretisch kann es hier zu Kollisionen mit eigenartigen Effekten bei der Auswahl kommen
                values.put(OriginalName, name);
                values.put(Deleted, 0);

                db.insert(Table, null, values);
            } else {
                // Das Aktualisieren ist hingegen unkritisch
                db.update(Table, values, OriginalName + "=?", new String[]{originalName});
            }
        } finally {
            db.close();
        }

    }

    // Entfernt einen Markt aus der Datenbank
    public static void delete(Database database, String originalName) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            // Lokal wird niemals wirklich gelöscht, sondern nur eine entsprechende Markierung gesetzt - nur der Web Service darf Märkte wirklich löschen
            ContentValues values = new ContentValues();
            values.put(Deleted, 1);

            db.update(Table, values, OriginalName + "=?", new String[]{originalName});
        } finally {
            db.close();
        }
    }

    // Überträgt die vom Web Service gemeldeten Onlien verfügbaren Märkte in die lokale Datenbank.
    public static void synchronize(SQLiteDatabase database, JSONArray markets) throws JSONException {
        for (int i = 0; i < markets.length(); i++) {
            JSONObject market = markets.getJSONObject(i);

            // Dazu werden die Daten vom Web Service in die entsprechenden Spalten der lokalen Datenbank übertragen - im Moment wird eine einfache Namensgleichheit von Spalten und JSON Eigenschaften verwendet
            ContentValues values = new ContentValues();
            values.put(Name, xJsonTools.getStringFromJSON(market, Name));
            values.put(OriginalName, xJsonTools.getStringFromJSON(market, OriginalName));
            values.put(Deleted, market.getBoolean(Deleted) ? 1 : 0);

            database.insert(Table, null, values);
        }
    }

    // Meldet den Anzeigenamen eines Marktes.
    public static String getName(JSONObject market) throws JSONException {
        return xJsonTools.getStringFromJSON(market, Name);
    }

    // Ändert den Anzeigenamen eines Marktes.
    public static void setName(JSONObject market, String name) throws JSONException {
        market.put(Name, name);
    }

    // Meldet den Online Namen und damit eindeutigen Schlüssel eines Marktes.
    public static String getOriginalName(JSONObject market) throws JSONException {
        return xJsonTools.getStringFromJSON(market, OriginalName);
    }

    // Ändert den Online Namen und damit eindeutigen Schlüssel eines Marktes.
    public static void setOriginalName(JSONObject market, String name) throws JSONException {
        market.put(OriginalName, name);
    }
}
