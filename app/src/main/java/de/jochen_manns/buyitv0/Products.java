package de.jochen_manns.buyitv0;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

class Products {
    private static final String Table = "items";
    public static final String DropSql = "DROP TABLE IF EXISTS " + Table;
    public static final String CleanupSql = "DELETE FROM " + Table;
    private static final String Identifier = "id";
    private static final String State = "state";
    private static final String Name = "name";
    private static final String Description = "description";
    private static final String CreateTime = "created";
    private static final String BuyTime = "bought";
    private static final String BuyMarket = "market";
    private final static String[] s_ItemListColumns = {Identifier, Name, BuyMarket, BuyTime};
    private static final String Order = "priority";
    public static final String CreateSql = "CREATE TABLE " + Table + "(" + Identifier + " INTEGER, " + State + " INTEGER, " + Name + " TEXT, " + Description + " TEXT, " + CreateTime + " TEXT, " + BuyTime + " TEXT, " + BuyMarket + " TEXT, " + Order + " INTEGER)";
    private final static String[] s_ItemLimitColumns = {"COUNT(*)", "MIN(" + Identifier + ")", "MAX(" + Order + ")"};

    private static JSONObject[] build(Cursor query) throws JSONException {
        try {
            JSONObject[] items = new JSONObject[query.getCount()];
            if (items.length > 0) {
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

    private static JSONObject[] query(Database database, String[] columns, String filter, String[] filterArgs, String order) throws JSONException {
        SQLiteDatabase db = database.getReadableDatabase();
        try {
            db.beginTransaction();
            try {
                return build(db.query(Table, columns, filter, filterArgs, null, null, order));
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    public static JSONArray queryForUpdate(Database database) throws JSONException {
        JSONArray items = new JSONArray();

        for (JSONObject item : query(database, null, State + "<>?", new String[]{Integer.toString(ProductStates.Unchanged.ordinal())}, null))
            items.put(item);

        return items;
    }

    public static JSONObject[] query(Database database, boolean all) throws JSONException {
        String filter = State + "<>?";
        if (!all)
            filter += " AND " + BuyTime + " IS NULL";

        return query(database, s_ItemListColumns, filter, new String[]{Integer.toString(ProductStates.Deleted.ordinal())}, Order);
    }

    public static JSONObject query(Database database, long id) throws JSONException {
        JSONObject[] items = query(database, null, Identifier + "=?", new String[]{Long.toString(id)}, null);

        return (items.length == 1) ? items[0] : null;
    }

    public static void update(Database database, Long identifier, String name, String description, String market) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(Name, name);
                values.put(Description, description);
                values.put(BuyMarket, market);

                if (identifier == null) {
                    Cursor findMax = db.query(Table, s_ItemLimitColumns, null, null, null, null, null);
                    try {
                        values.put(State, ProductStates.NewlyCreated.ordinal());
                        values.put(CreateTime, Tools.dateToISOString(Calendar.getInstance().getTime()));

                        if (findMax.moveToNext() && (findMax.getLong(0) > 0)) {
                            values.put(Order, findMax.getInt(2) + 1);
                            values.put(Identifier, Math.min(findMax.getInt(1), 0) - 1);
                        } else {
                            values.put(Order, 0);
                            values.put(Identifier, -1);
                        }
                    } finally {
                        findMax.close();
                    }

                    db.insert(Table, null, values);
                } else {
                    values.put(State, ((identifier >= 0) ? ProductStates.Modified : ProductStates.NewlyCreated).ordinal());

                    db.update(Table, values, Identifier + "=?", new String[]{Long.toString(identifier)});
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    public static void buy(Database database, Long identifier, String market) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(State, ((identifier >= 0) ? ProductStates.Modified : ProductStates.NewlyCreated).ordinal());
                values.put(BuyMarket, market);

                if ((market == null) || (market.length() < 1))
                    values.put(BuyTime, (String) null);
                else
                    values.put(BuyTime, Tools.dateToISOString(Calendar.getInstance().getTime()));

                db.update(Table, values, Identifier + "=?", new String[]{Long.toString(identifier)});
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    public static void delete(Database database, long identifier) {
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(State, ProductStates.Deleted.ordinal());

                db.update(Table, values, Identifier + "=?", new String[]{Long.toString(identifier)});

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
    }

    public static void synchronize(SQLiteDatabase database, JSONArray items) throws JSONException {
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);

            ContentValues values = new ContentValues();
            values.put(Identifier, item.getInt(Identifier));
            values.put(State, item.getInt(State));
            values.put(Name, Tools.getStringFromJSON(item, Name));
            values.put(Description, Tools.getStringFromJSON(item, Description));
            values.put(CreateTime, Tools.getStringFromJSON(item, CreateTime));
            values.put(BuyTime, Tools.getStringFromJSON(item, BuyTime));
            values.put(BuyMarket, Tools.getStringFromJSON(item, BuyMarket));
            values.put(Order, item.getInt(Order));

            database.insert(Table, null, values);
        }
    }

    public static String getMarket(JSONObject item) throws JSONException {
        return Tools.getStringFromJSON(item, BuyMarket);
    }

    public static String getName(JSONObject item) throws JSONException {
        return Tools.getStringFromJSON(item, Name);
    }

    public static boolean isBought(JSONObject item) throws JSONException {
        String time = Tools.getStringFromJSON(item, BuyTime);

        return ((time != null) && (time.length() > 0));
    }

    public static String getDescription(JSONObject item) throws JSONException {
        return Tools.getStringFromJSON(item, Description);
    }

    public static int getIdentifier(JSONObject item) throws JSONException {
        return item.getInt(Identifier);
    }
}
