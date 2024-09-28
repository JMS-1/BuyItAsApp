package de.jochen_manns.buyitv0;

import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.time.LocalDate;

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
    protected boolean initializeTextView(TextView text, JSONObject product) throws JSONException {
        // Name des Produktes und des optionalen Marktes ermitteln
        String market = Products.getMarket(product);
        String name = Products.getName(product);
        String category = Products.getCategory(product);

        // Gruppe berücksichtigen
        if (category != null && !category.isEmpty()) name += " [" + category + "]";

        // Abhängig vom Einkaufsstand kann die Anzeige leicht variieren
        int res = 0;
        if (Products.isBought(product))
            res = R.string.product_suffix_bought;
        else if ((market != null) && !market.isEmpty())
            res = R.string.product_prefix_market;

        // Im einfachsten Fall wird nur der Name des Produktes ausgegeben, ansonsten muss der Markt noch geeignet eingemischt werden
        if (res > 0)
            text.setText(getContext().getResources().getString(res, name, market));
        else
            text.setText(name);

        // Dauereinträge markieren
        text.setTypeface(null, Products.getPermanent(product) ? Typeface.ITALIC : Typeface.NORMAL);

        // Abgelaufene Einträge markieren
        LocalDate to = Products.parseFromTo(Products.getTo(product));

        text.setTextColor(to == null || !to.isBefore(LocalDate.now()) ? Color.BLACK : Color.RED);

        // Produkte können immer verändert werden
        return true;
    }

    @Override
    protected String getPrefixText(JSONObject item) throws JSONException {
        LocalDate from = Products.parseFromTo(Products.getFrom(item));

        if (from == null || !from.isAfter(LocalDate.now())) return "";

        return MessageFormat.format("{0,number,00}. ", from.getDayOfMonth());
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
    public JSONObject[] load(String order) {
        // Verbindung zu Datenbank herstellen
        try (Database database = createDatabase()) {
            // Die Liste aller Produkte aus der lokalen Datenbank auslesen
            return Products.query(database, true, order);
        } catch (Exception e) {
            // Fehler werden ignoriert
            return null;
        }
    }
}
