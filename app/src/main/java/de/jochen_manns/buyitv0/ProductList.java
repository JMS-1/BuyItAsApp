package de.jochen_manns.buyitv0;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Die primäre Aktivitität zeigt die Liste der Produkte und erlaubt den Einstieg
    in alle anderen Bereiche der Anwendung. Sie ist der Einzige Zugangspunkt in
    die Anwendung.
 */
public class ProductList extends ListActivity<Long, ProductEdit, ProductAdapter> {
    // Die Antwortkennung bei der Auswahl eine Marktes, bei dem ein Produkt eingekauft wurde.
    private static final int RESULT_SELECT_MARKET = 1;

    // Die laufende Nummer des Anmeldetdialogs.
    private static final int DIALOG_LOGON = 1;

    // Der Markt, bei dem zuletzt ein Produkt gekauft wurde - vermutlich auch der als nächstes verwendete Markt.
    private String m_market;

    @Override
    protected Long getIdentifier(JSONObject item) throws JSONException {
        // Einfach die Kennung aus der Datenbank auslesen
        return new Long(Products.getIdentifier(item));
    }

    @Override
    protected boolean canEdit(Long identifier) {
        // Wir haben keine Platzhalter in der Liste, wie es bei der Auswahl der Märkte üblich ist.
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.menu.menu_product_list, savedInstanceState);

        // Wenn der Anwender sich neu anmeldet, dann müssen wir alle Daten neu anfordern
        getSharedPreferences(User.PREFERENCES_NAME, 0)
                .registerOnSharedPreferenceChangeListener(
                        new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                updateUser(User.Name_Identifier.equals(key));
                            }
                        });

        // Listenverwaltung einrichten
        setListAdapter(new ProductAdapter(this));

        // Aktuelle Benutzerinformationen übernehmen
        updateUser(false);

        // Liste der Produkte aus der lokalen Datenbank anfordern
        load();
    }

    // Wechselt den Anwender, der die Anwendung gerade bedient.
    private void updateUser(boolean synchronize) {
        // Benutzeranmeldung ermitteln
        User user = User.load(this);

        // Beim ersten Aufruf haben wir keine Anmeldung
        String userName = (user == null) ? getResources().getString(R.string.app_name) : user.Name;

        // Name der Anwendung anpassen
        setTitle(userName);

        // Wird der Anwender gewechselt so müssen wir neu Laden
        if (synchronize)
            synchronize(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Alle Aktionen werden über die ActionBar angestossen
        switch (item.getItemId()) {
            case R.id.action_synchronize:
                onSynchronize();
                return true;

            case R.id.action_logon:
                onLogon();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Synchronisiert die lokale Datenbank mit dem Online Datenbestand.
    public void synchronize(boolean clearDatabase) {
        new ProductListSynchronizeTask(clearDatabase).start();
    }

    // Bearbeitet den Wunsch des Anwender zur Synchronisation mit dem Online Datenbestand.
    public void onSynchronize() {
        // Ohne eine Registrierung geht das nicht, daher wird diese bei Bedarf automatisch angefordert
        if (User.load(this) == null)
            onLogon();
        else
            try {
                // Wir versuchen das nur, wenn wir auch eine Netzwerkverbindung haben
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null)
                    return;
                if (!networkInfo.isConnected())
                    return;

                // Hintergrundaufgabe zur Abfrage des Web Services anstossen
                synchronize(false);
            } catch (Exception e) {
                // Fehler verschlucken wir im Moment stillschweigend
            }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        // Ist keine Registrierung bekannt, so wird der Dialog auch nicht vorbelegt
        User user = User.load(this);
        if (user == null)
            return;

        // Ansonsten erscheint der Anmeldedialog immer mit der aktuellen Registrierung im Eingabefeld
        AlertDialog alert = (AlertDialog) dialog;
        EditText userid = (EditText) alert.findViewById(R.id.dialog_register_userid);

        userid.setText(user.Identifier);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_LOGON:
                return
                        // Wir verwenden eine eigene Gestaltung des Anmeldedialogs
                        new AlertDialog.Builder(this)
                                .setView(getLayoutInflater().inflate(R.layout.dialog_register_user, null))
                                .setPositiveButton(R.string.button_register, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Eingabe des Anwenders auslesen
                                        AlertDialog alert = (AlertDialog) dialog;
                                        EditText key = (EditText) alert.findViewById(R.id.dialog_register_userid);

                                        // Und auf dieser Basis asynchron den zugehörigen Web Service aufrufen
                                        new LogonTask(ProductList.this, key.getText().toString()).start();

                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
        }

        return super.onCreateDialog(id);
    }

    // Der Anwender fordert eine neue Registrierung an.
    public void onLogon() {
        // Wir machen das aber nur, wenn wir auch eine Netzwerkverbindung haben
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null)
            return;
        if (!networkInfo.isConnected())
            return;

        // Zurzeit wird die alte Methode zur Erzeugung von (modalen) Dialogen eingesetzt
        showDialog(DIALOG_LOGON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Ohne Rückgabewerte brauchen wir erst gar nicht anzufangen
        if (data == null)
            return;

        switch (requestCode) {
            case RESULT_SELECT_MARKET:
                // Nur, wenn auch eine Auswahl stattgefunden hat
                if (resultCode == RESULT_OK) {
                    // Die Kennung des Produktes, für das der Markt angefordert wurde
                    Long id = (Long) data.getSerializableExtra(MarketList.EXTRA_PRODUCT_IDENTIFIER);

                    // Der vom Anwender ausgewählte Markt
                    m_market = data.getStringExtra(MarketList.EXTRA_MARKET_NAME);

                    // Nun wird das Produkt in der lokalen Datenbank entsprechend aktulaisiert
                    Database database = Database.create(this);
                    try {
                        Products.buy(database, id, m_market);
                    } finally {
                        database.close();
                    }

                    // In den meisten Fällen muss die Anzeige aktualisiert werden - TODO: das geht sicher auch eleganter, da ja (aus Sicht des Anwenders) nur die Anzeige eines Produkt verändert wurde
                    load();
                }

                break;
        }
    }

    @Override
    public void onClick(JSONObject product) {
        // Bei der Auswahl des Marktes übergeben wir auch die eindeutige Identifikation des ausgewählten Produktes
        try {
            Intent selectMarket = new Intent(this, MarketList.class);
            selectMarket.putExtra(MarketList.EXTRA_MARKET_NAME, m_market);
            selectMarket.putExtra(MarketList.EXTRA_PRODUCT_IDENTIFIER, new Long(Products.getIdentifier(product)));
            startActivityForResult(selectMarket, RESULT_SELECT_MARKET);
        } catch (Exception e) {
            // Im Moment ignorieren wir alle Fehler
        }
    }

    @Override
    public void onSwapWithNext(int leftPosition) {
        super.onSwapWithNext(leftPosition);

        // Wir nummerieren einmal ganz durch
        Database database = Database.create(this);
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            try {
                db.beginTransaction();
                try {
                    ProductAdapter adapter = getListAdapter();

                    for (int i = 0; i < adapter.getCount(); i++) {
                        long id = adapter.getItemId(i);

                        // Neue Ordnung ermitteln
                        int position = i;
                        if (i == leftPosition)
                            position++;
                        else if (i == leftPosition + 1)
                            position--;

                        // Lokal aktualisieren
                        Products.setOrder(db, id, position);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            } finally {
                db.close();
            }
        } catch (Exception e) {
            // Im Moment werden alle Fehler einfach ignoriert
        } finally {
            database.close();
        }

        // Und danach wird die Anzeige neu aufgebaut
        load();
    }

    /*
        Eine Hilfsklasse zur Synchronisation der lokalen Datenbank mit dem Online Datenbestand.
     */
    private class ProductListSynchronizeTask extends SynchronizeTask {
        // Erstellt eine neue Aufgabe.
        public ProductListSynchronizeTask(boolean clear) {
            super(ProductList.this, clear);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            // Es ist nun an der Zeit, die Anzeige zu aktualisieren
            if (jsonObject != null)
                load();
        }
    }
}
