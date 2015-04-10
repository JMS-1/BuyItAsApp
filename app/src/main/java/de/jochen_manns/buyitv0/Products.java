package de.jochen_manns.buyitv0;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/*
    Mit den Methoden dieser Hilfsklasse erfolgt die Pflege der Produkte in der lokalen Datenbank.
 */
class Products {
    // Der Name der Tabelle mit den Produkten.
    private static final String Table = "items";

    // Der SQL Befehl zum Entfernen der Produkttabelle.
    public static final String DropSql = "DROP TABLE IF EXISTS " + Table;

    // Der SQL Befehl zum Entfernen aller Produkte aus der lokalen Datenbank.
    public static final String CleanupSql = "DELETE FROM " + Table;

    // Der Name der Spalte (und JSON Eigenschaft) mit der eindeutigen Identifikation eines Produktes.
    private static final String Identifier = "id";

    // Der Name der Spalte (und JSON Eigenschaft) mit dem Bearbeitungsstand eines Produktes.
    private static final String State = "state";

    // Der Name der Spalte (und JSON Eigenschaft) mit dem Namen eines Produktes.
    private static final String Name = "name";

    // Der Name der Spalte (und JSON Eigenschaft) mit der Beschreibung eines Produktes.
    private static final String Description = "description";

    // Der Name der Spalte (und JSON Eigenschaft) mit dem Zeitpunkt, an dem ein Produkt angelegt wurde.
    private static final String CreateTime = "created";

    // Der Name der Spalte (und JSON Eigenschaft) mit dem Zeitpunkt, zu dem ein Produkt in einem Markt gekauft wurde.
    private static final String BuyTime = "bought";

    // Der Name der Spalte (und JSON Eigenschaft) mit dem Markt, in dem ein Produkt gekauft werden soll oder sogar gekauft wurde.
    private static final String BuyMarket = "market";

    // Die Liste der Spalten (respektive JSON Eigenschaften), die zur Anzeige der Liste der Produkte benötigt wird.
    private final static String[] s_ItemListColumns = {Identifier, Name, BuyMarket, BuyTime};

    // Der Name der Spalte (und JSON Eigenschaft) mit der Wichtigkeit eines Produktes.
    private static final String Order = "priority";
    // Die berechneten Spalten, die zur Festlegung der Eckdaten eines lokal neu angelegten produktes benötigt werden.
    private final static String[] s_ItemLimitColumns = {"COUNT(*)", "MIN(" + Identifier + ")", "MAX(" + Order + ")"};
    // Der Name der Spalte mit der ursprünglichen Wichtigkeit eines Produktes.
    private static final String OriginalOrder = "originalPriority";
    // Der SQL Befehl zum Anlegen der Produkttabelle.
    public static final String CreateSql = "CREATE TABLE " + Table + "(" + Identifier + " INTEGER, " + State + " INTEGER, " + Name + " TEXT, " + Description + " TEXT, " + CreateTime + " TEXT, " + BuyTime + " TEXT, " + BuyMarket + " TEXT, " + Order + " INTEGER, " + OriginalOrder + " INTEGER)";

    // Wertet eine Abfrage an die Produkttabelle aus.
    private static JSONObject[] build(Cursor query) throws JSONException {
        try {
            JSONObject[] items = new JSONObject[query.getCount()];
            if (items.length > 0) {
                // Die laufenden Nummern der Spalten ermitteln - im Allgemeinen werden nicht immer alle Spalten abgerufen
                int originalOrderColumn = query.getColumnIndex(OriginalOrder);
                int descriptionColumn = query.getColumnIndex(Description);
                int createColumn = query.getColumnIndex(CreateTime);
                int marketColumn = query.getColumnIndex(BuyMarket);
                int idColumn = query.getColumnIndex(Identifier);
                int buyColumn = query.getColumnIndex(BuyTime);
                int orderColumn = query.getColumnIndex(Order);
                int stateColumn = query.getColumnIndex(State);
                int nameColumn = query.getColumnIndex(Name);

                for (int i = 0; i < items.length; i++) {
                    query.moveToNext();

                    // Die Werte aus der lokalen Datenbank in die JSON Protokollstruktur übertragen - sofern die zugehörigen Spalten auch abgerufen wurden
                    JSONObject item = new JSONObject();
                    if (descriptionColumn >= 0)
                        item.put(Description, query.getString(descriptionColumn));
                    if (createColumn >= 0)
                        item.put(CreateTime, query.getString(createColumn));
                    if (marketColumn >= 0)
                        item.put(BuyMarket, query.getString(marketColumn));
                    if (buyColumn >= 0)
                        item.put(BuyTime, query.getString(buyColumn));
                    if (idColumn >= 0)
                        item.put(Identifier, query.getInt(idColumn));
                    if (orderColumn >= 0)
                        item.put(Order, query.getInt(orderColumn));
                    if (originalOrderColumn >= 0)
                        item.put(OriginalOrder, query.getInt(originalOrderColumn));
                    if (stateColumn >= 0)
                        item.put(State, query.getInt(stateColumn));
                    if (nameColumn >= 0)
                        item.put(Name, query.getString(nameColumn));

                    items[i] = item;
                }
            }

            return items;
        } finally {
            query.close();
        }
    }

    // Führt eine Sucht auf der Produkttabelle aus.
    private static JSONObject[] query(Database database, String[] columns, String filter, String[] filterArgs, String order) throws JSONException {
        SQLiteDatabase db = database.getReadableDatabase();
        try {
            // Abruf der Datensätze und Umwandeln in eine JSON Repräsentation
            return build(db.query(Table, columns, filter, filterArgs, null, null, order));
        } finally {
            db.close();
        }
    }

    // Ermittelt alle Produkte, die bei einer Synchronisation an den Web Service übermittelt werden müssen
    public static JSONArray queryForUpdate(SQLiteDatabase database) throws JSONException {
        JSONArray items = new JSONArray();

        // Schauen wir einmal, ob eine Aktualisierung notwendig ist
        boolean mustUpdate = false;

        // Wir nehmen alle Produkte, der Server muss damit klarkommen
        for (JSONObject item : build(database.query(Table, null, null, null, null, null, Order))) {
            // Der Index, den wir verwenden würden
            int order = items.length();

            // Das ist auch der neue Online Stand
            item.put(Order, order);

            // Auf jeden Fall merken wir uns das
            items.put(item);

            // Auf Änderung prüfen
            if (order == item.getInt(OriginalOrder))
                if (item.getInt(State) == ProductStates.Unchanged.ordinal())
                    continue;

            // Da müssen wir ran
            mustUpdate = true;
        }

        // Je nach Situation melden
        return mustUpdate ? items : new JSONArray();
    }

    // Ermittelt alle Produkte zur Anzeige in der Hauptaktivität.
    public static JSONObject[] query(Database database, boolean all) throws JSONException {
        // Wir können theoretisch auch alle Produkte ausblenden, die bereits eingekauft wurden - TODO: das kann in der Oberfläche noch nicht umgeschaltet werden
        String filter = State + "<>?";
        if (!all)
            filter += " AND " + BuyTime + " IS NULL";

        // Grundsätzlich werden alle nicht als gelöscht markierten Produkte gemeldet
        return query(database, s_ItemListColumns, filter, new String[]{Integer.toString(ProductStates.Deleted.ordinal())}, Order);
    }

    // Ermittelt ein einzelnes Produkt.

    public static JSONObject query(Database database, long id) throws JSONException {
        // Die direkte Suche nach der eindeutigen Identifikation des Produktes
        JSONObject[] items = query(database, null, Identifier + "=?", new String[]{Long.toString(id)}, null);

        return (items.length == 1) ? items[0] : null;
    }

    // Ändert die Daten eines existierenden Produktes in der lokalen Datenbank - oder legt lokal ein neues Produkt an.
    public static void update(Database database, Long identifier, String name, String description, String market) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            // Die Eckdaten des Produktes, die verändert werden können
            ContentValues values = new ContentValues();
            values.put(Name, name);
            values.put(Description, description);
            values.put(BuyMarket, market);

            if (identifier == null) {
                // Erst einmal kennzeichnen wird das Produkt als ein neues Produkt
                values.put(CreateTime, JsonTools.dateToISOString(Calendar.getInstance().getTime()));
                values.put(State, ProductStates.NewlyCreated.ordinal());
                values.put(OriginalOrder, -1);

                // Bei neuen Produken müssen wir noch eine neue eindeutige Identifikation und eine Wichtigket ermitteln
                Cursor findMax = db.query(Table, s_ItemLimitColumns, null, null, null, null, null);
                try {
                    if (findMax.moveToNext() && (findMax.getLong(0) > 0)) {
                        // Es gibt mindestens ein anders Produkt und wir ordnen uns dahinter an
                        values.put(Order, findMax.getInt(2) + 1);

                        // Bis zur Synchronisation mit dem Online Datenbestand wird eine neue eindeutige Kennung vergeben - beginnend mit -1 und absteigend
                        values.put(Identifier, Math.min(findMax.getInt(1), 0) - 1);
                    } else {
                        // Für das erste Produkt ist das alles viel einfacher
                        values.put(Order, 0);
                        values.put(Identifier, -1);
                    }
                } finally {
                    findMax.close();
                }

                // Legt das Produkt neu an
                db.insert(Table, null, values);
            } else {
                // Neue Produkte bleiben einfach nur neu, bei allen anderen erfolgt die explizite Markierung als verändert
                values.put(State, ((identifier >= 0) ? ProductStates.Modified : ProductStates.NewlyCreated).ordinal());

                // Lokale Datenbank geeignet aktualisieren
                db.update(Table, values, Identifier + "=?", new String[]{Long.toString(identifier)});
            }
        } finally {
            db.close();
        }
    }

    // Ein Produkt wird als eingekauft markiert.
    public static void buy(Database database, Long identifier, String market) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            // Ähnlich wie bei der allgemeinen Änderung eines existierenden Produktes, nur werden hier weniger Informationen verändert
            ContentValues values = new ContentValues();
            values.put(State, ((identifier >= 0) ? ProductStates.Modified : ProductStates.NewlyCreated).ordinal());
            values.put(BuyMarket, market);

            // Die Zeit des Einkaufs muss nun allerdings korrekt eingepflegt werden
            if ((market == null) || (market.length() < 1))
                values.put(BuyTime, (String) null);
            else
                values.put(BuyTime, JsonTools.dateToISOString(Calendar.getInstance().getTime()));

            // Die lokalen Datenbank wird schließlich aktualisiert
            db.update(Table, values, Identifier + "=?", new String[]{Long.toString(identifier)});
        } finally {
            db.close();
        }
    }

    // Markiert ein existierendes Produkt als gelöscht.
    public static void delete(Database database, long identifier) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            // Hier setzen wir einfach die Markierung - auch für lokal neu angelegte Produkte, der Web Service ist darauf entsprechend vorbereitet
            ContentValues values = new ContentValues();
            values.put(State, ProductStates.Deleted.ordinal());

            // Markierung in der lokalen Datenbank einstellen
            db.update(Table, values, Identifier + "=?", new String[]{Long.toString(identifier)});
        } finally {
            db.close();
        }
    }

    // Set die Ordnung eines Elementes.
    public static void setOrder(SQLiteDatabase database, long identifier, int order) {
        ContentValues values = new ContentValues();
        values.put(Order, order);

        database.update(Table, values, Identifier + "=?", new String[]{Long.toString(identifier)});
    }

    // Übernimmt den Online Datenbestand in die lokale Datenbank
    public static void synchronize(SQLiteDatabase database, JSONArray items) throws JSONException {
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            // In der aktuellen Implementierung werden die JSON Eigenschaften einfach als Spalten übernommen
            ContentValues values = new ContentValues();
            values.put(Identifier, item.getInt(Identifier));
            values.put(State, item.getInt(State));
            values.put(Name, JsonTools.getStringFromJSON(item, Name));
            values.put(Description, JsonTools.getStringFromJSON(item, Description));
            values.put(CreateTime, JsonTools.getStringFromJSON(item, CreateTime));
            values.put(BuyTime, JsonTools.getStringFromJSON(item, BuyTime));
            values.put(BuyMarket, JsonTools.getStringFromJSON(item, BuyMarket));
            values.put(OriginalOrder, item.getInt(Order));
            values.put(Order, item.getInt(Order));

            // Vor dem Abgleich wird die Produktabelle vollständig geleert, so dass wir hier einfach einfügen müssen - der Online Datenbestand ist immer die volle Wahrheit!
            database.insert(Table, null, values);
        }
    }

    // Meldet den Namen des Marktes zu einem Produkt.
    public static String getMarket(JSONObject item) throws JSONException {
        return JsonTools.getStringFromJSON(item, BuyMarket);
    }

    // Meldet den Namen eines Produktes.
    public static String getName(JSONObject item) throws JSONException {
        return JsonTools.getStringFromJSON(item, Name);
    }

    // Prüft und meldet, ob ein Produkt bereits eingekauft wurde.
    public static boolean isBought(JSONObject item) throws JSONException {
        String time = JsonTools.getStringFromJSON(item, BuyTime);

        return ((time != null) && (time.length() > 0));
    }

    // Meldet die Beschreibung eines Produktes.
    public static String getDescription(JSONObject item) throws JSONException {
        return JsonTools.getStringFromJSON(item, Description);
    }

    // Meldet die eindeutige Identifikation eines Produktes.
    public static int getIdentifier(JSONObject item) throws JSONException {
        return item.getInt(Identifier);
    }
}
