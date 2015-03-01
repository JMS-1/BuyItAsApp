package de.jochen_manns.buyitv0;

import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Verwaltet die Liste aller Produkte.
 */
class ProductAdapter extends ItemAdapter {
    // Erstellt eine neue Liste.
    public ProductAdapter(ListActivity<?, ?, ?> context) {
        super(context);
    }

    @Override
    public int getCount() {
        return (m_items == null) ? 0 : m_items.length;
    }

    @Override
    protected boolean initializeTextView(TextView text, JSONObject product, int position) throws JSONException {
        // Name des Produktes und des optionalen Marktes ermitteln
        String market = Products.getMarket(product);
        String name = Products.getName(product);

        // Abhängig vom Einkaufsstand kann die Anzeige leicht variieren
        int res = 0;
        if (Products.isBought(product))
            res = R.string.product_suffix_bought;
        else if ((market != null) && (market.length() > 0))
            res = R.string.product_prefix_market;

        // Im einfachsten Fall wird nur der Name des Produktes ausgegeben, ansonsten muss der Markt noch geeignet eingemischt werden
        if (res > 0)
            text.setText(getContext().getResources().getString(res, name, market));
        else
            text.setText(name);

        // Produkte können immer verändert werden
        return true;
    }

    @Override
    public long getItemId(int position) {
        try {
            // Hier verwenden wir die eindeutige Kennung, die jedes Produkt hat - negative Werte werden dabei für nur lokal bekannte Produkte verwendet
            return Products.getIdentifier(m_items[position]);
        } catch (Exception e) {
            // Fehler werden geeignet gemeldet
            return Long.MAX_VALUE;
        }
    }

    @Override
    public Object getItem(int position) {
        return m_items[position];
    }

    @Override
    public JSONObject[] load() {
        // Verbindung zu Datenbank herstellen
        Database database = createDatabase();
        try {
            // Die Liste aller Produkte aus der lokalen Datenbank auslesen
            return Products.query(database, true);
        } catch (Exception e) {
            // Fehler werden ignoriert
            return null;
        } finally {
            database.close();
        }
    }
}
