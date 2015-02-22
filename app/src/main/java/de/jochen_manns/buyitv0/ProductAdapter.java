package de.jochen_manns.buyitv0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

/*
    Verwaltet die Liste aller Produkte.
 */
class ProductAdapter extends ItemAdapter {
    // Erzeugt Views.
    private final LayoutInflater m_inflater;

    // Die aktuell bekannten Produkte.
    private JSONObject[] m_products = null;

    // Erstellt eine neue Liste.
    public ProductAdapter(Context context) {
        m_inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return (m_products == null) ? 0 : m_products.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Wird ein neuer View benötigt, so erzeugen wir diesen hier - bei einer Wiederverwendung müssen wir nur initialisieren
        if (convertView == null)
            convertView = m_inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        // Ziel- und Quellobjekte ermitteln
        TextView textView = (TextView) convertView;
        JSONObject product = m_products[position];

        try {
            // Name des Produktes und des optionalen Marktes ermitteln
            String market = Products.getMarket(product);
            String name = Products.getName(product);

            // Abhängig vom Einkaufsstand kann die Anzeige leich variieren
            int res = 0;
            if (Products.isBought(product))
                res = R.string.product_suffix_bought;
            else if ((market != null) && (market.length() > 0))
                res = R.string.product_prefix_market;

            // Im einfachsten Fall wird nur der Name des Produktes ausgegeben, ansonsten muss der Markt noch geeignet eingemischt werden
            if (res > 0)
                textView.setText(m_inflater.getContext().getResources().getString(res, name, market));
            else
                textView.setText(name);
        } catch (Exception e) {
            // Fehler werden letztlich alle ignoriert
            textView.setText("### ERROR ###");
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        try {
            // Hier verwenden wir die eindeutige Kennung, die jedes Produkt hat - negative Werte werden dabei für nur lokal bekannte Produkte verwendet
            return Products.getIdentifier(m_products[position]);
        } catch (Exception e) {
            // Fehler werden geeignet gemeldet
            return Long.MAX_VALUE;
        }
    }

    @Override
    public Object getItem(int position) {
        return m_products[position];
    }

    @Override
    public void refresh() {
        // Verbindung zu Datenbank herstellen
        Database database = Database.create(m_inflater.getContext());
        try {
            // Die Liste aller Produkte aus der lokalen Datenbank auslesen
            m_products = Products.query(database, true);
        } catch (Exception e) {
            // Fehler werden ignoriert
            m_products = null;
        } finally {
            database.close();
        }
    }
}
