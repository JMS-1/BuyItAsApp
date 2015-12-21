package de.jochen_manns.buyitv0;

import android.graphics.Typeface;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Verwaltet die aktuelle Liste der Märkte. Der erste Eintrag ist dabei
    immer ein Platzhalter und ermöglicht die Auswahl keines Marktes.
 */
class MarketAdapter extends ItemAdapter {
    // Der Platzhalter für die Auswahl keines Marktes
    private final JSONObject m_noMarket = new JSONObject();

    // Der Name des vorausgewählten Marktes.
    private final String m_defaultName;

    // Erstellt eine neue Liste
    public MarketAdapter(ListActivity<?, ?, ?> context, String emptyName, String preselected) throws JSONException {
        super(context);

        m_defaultName = preselected;

        // Der Anzeigename der leeren Auswahl ist von der Situation des Aufrufs abhängig
        Markets.setName(m_noMarket, emptyName);
        Markets.setOriginalName(m_noMarket, null);
    }

    // Meldet die Position des aktiven Elementes.
    public int getActivePosition() {
        if (m_defaultName != null)
            for (int i = 0; i < getCount(); i++)
                try {
                    // Der Vergleich erfolgt per Name
                    JSONObject market = (JSONObject) getItem(i);
                    String marketName = Markets.getName(market);
                    if (m_defaultName.equals(marketName))
                        return i;
                } catch (JSONException e) {
                    // Das ist uns egal
                }

        // Es gibt keine Vorauswahl
        return -1;
    }

    @Override
    public int getCount() {
        // Bei der Anzahl der Märkte muss die Leerauswahl berücksichtigt werden
        return (m_items == null) ? 0 : (1 + m_items.length);
    }

    @Override
    protected boolean initializeTextView(TextView text, JSONObject market) throws JSONException {
        String marketName = Markets.getName(market);

        text.setTypeface(null, ((m_defaultName != null) && m_defaultName.equals(marketName)) ? Typeface.BOLD : Typeface.NORMAL);
        text.setText(marketName);

        return (Markets.getOriginalName(market) != null);
    }

    @Override
    public long getItemId(int position) {
        // In der aktuellen Implementierung haben Märkte keine numerisch eindeutige Kennung, daher nehmen wir einfach die laufende Nummer
        return position;
    }

    @Override
    public Object getItem(int position) {
        // Hier müssen wir die Leerauswahl berücksichtigen
        if (position < 1)
            return m_noMarket;
        else
            return m_items[position - 1];
    }

    @Override
    public JSONObject[] load(String order) {
        // Zugriff auf die lokale Datenhaltung vorbereiten
        Database database = createDatabase();
        try {
            // Die Liste aller aktuell bekannten Märkte einlesen
            return Markets.query(database, order);
        } catch (Exception e) {
            // Alle Fehler werden ignoriert
            return null;
        } finally {
            database.close();
        }
    }
}
