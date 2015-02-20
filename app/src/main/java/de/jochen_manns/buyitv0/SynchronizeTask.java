package de.jochen_manns.buyitv0;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

abstract class SynchronizeTask extends JsonRequestTask {
    private static final String Name_Markets = "markets";

    private static final String Name_Items = "items";

    private final boolean m_clear;

    protected SynchronizeTask(Context context, boolean alwaysClearDatabase) {
        super(context, "sync.php");

        m_clear = alwaysClearDatabase;
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        // Falls der Benutzer seine Anmeldung verändert hat löschen wir auf jeden Fall die Datenbank
        if (m_clear) {
            Database database = Database.create(Context);
            try {
                database.reset();
            } finally {
                database.close();
            }
        }

        return super.doInBackground(urls);
    }

    protected void fillRequest(JSONObject postData) throws JSONException {
        User user = User.load(Context);
        postData.put(REQUEST_USER, (user == null) ? null : user.Identifier);

        Database database = Database.create(Context);
        try {
            postData.put(Name_Items, Products.queryForUpdate(database));
            postData.put(Name_Markets, Markets.queryForUpdate(database));
        } finally {
            database.close();
        }
    }

    protected void processResponse(JSONObject postReply) throws JSONException {
        Database database = Database.create(Context);
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            try {
                db.beginTransaction();
                try {
                    database.reset(db);
                    Markets.synchronize(db, postReply.getJSONArray(Name_Markets));
                    Products.synchronize(db, postReply.getJSONArray(Name_Items));

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            } finally {
                db.close();
            }
        } finally {
            database.close();
        }
    }
}


