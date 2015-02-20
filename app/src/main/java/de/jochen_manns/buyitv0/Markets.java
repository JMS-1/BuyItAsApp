package de.jochen_manns.buyitv0;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class Markets {
    private static final String Table = "markets";

    private static final String Deleted = "deleted";

    private static final String Name = "name";

    private static final String OriginalName = "originalName";

    public static final String CreateSql = "CREATE TABLE " + Table + "(" + Name + " TEXT, " + OriginalName + " TEXT, " + Deleted + " INTEGER)";

    public static final String DropSql = "DROP TABLE IF EXISTS " + Table;

    public static final String CleanupSql = "DELETE FROM " + Table;

    private final static String[] s_MarketListColumns = {Name, OriginalName};

    private static JSONObject[] build(Cursor query) throws JSONException {
        try {
            JSONObject[] markets = new JSONObject[query.getCount()];
            if (markets.length > 0) {
                int nameColumn = query.getColumnIndex(Name);
                int originalNameColumn = query.getColumnIndex(OriginalName);
                int deleteColumn = query.getColumnIndex(Deleted);

                for (int i = 0; i < markets.length; i++) {
                    query.moveToNext();

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

    private static JSONObject[] query(Database database, String[] columns, String selection, String order) throws JSONException {
        SQLiteDatabase db = database.getReadableDatabase();
        try {
            db.beginTransaction();
            try {
                return build(db.query(Table, columns, selection, null, null, null, order));
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    public static JSONArray queryForUpdate(Database database) throws JSONException {
        JSONArray markets = new JSONArray();

        for (JSONObject market : query(database, null, null, null))
            markets.put(market);

        return markets;
    }

    public static JSONObject[] query(Database database) throws JSONException {
        return query(database, s_MarketListColumns, Deleted + "=0", Name);
    }

    public static void update(Database database, String originalName, String name) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(Name, name);

                if (originalName == null) {
                    values.put(OriginalName, name);
                    values.put(Deleted, 0);

                    db.insert(Table, null, values);
                } else {
                    db.update(Table, values, OriginalName + "=?", new String[]{originalName});
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    public static void delete(Database database, String originalName) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(Deleted, 1);

                db.update(Table, values, OriginalName + "=?", new String[]{originalName});

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    public static void synchronize(SQLiteDatabase database, JSONArray markets) throws JSONException {
        for (int i = 0; i < markets.length(); i++) {
            JSONObject market = markets.getJSONObject(i);

            ContentValues values = new ContentValues();
            values.put(Name, Tools.getStringFromJSON(market, Name));
            values.put(OriginalName, Tools.getStringFromJSON(market, OriginalName));
            values.put(Deleted, market.getBoolean(Deleted) ? 1 : 0);

            database.insert(Table, null, values);
        }
    }

    public static String getName(JSONObject market) throws JSONException{
       return Tools.getStringFromJSON(market, Name);
    }

    public static void setName(JSONObject market, String name) throws JSONException {
        market.put(Name, name);
    }

    public static String getOriginalName(JSONObject market) throws JSONException{
        return Tools.getStringFromJSON(market, OriginalName);
    }

    public static void setOriginalName(JSONObject market, String name) throws JSONException {
        market.put(OriginalName, name);
    }
}
