package de.jochen_manns.buyitv0;

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

    // Die Liste aller bekannten Märkte
    private JSONObject[] m_markets = null;

    // Erstellt eine neue Liste
    public MarketAdapter(ListActivity<?, ?, ?> context, String emptyName) throws JSONException {
        super(context);

        // Der Anzeigename der leeren Auswahl ist von der Situation des Aufrufs abhängig
        Markets.setName(m_noMarket, emptyName);
        Markets.setOriginalName(m_noMarket, null);
    }

    @Override
    public int getCount() {
        // Bei der Anzahl der Märkte muss die Leerauswahl berücksichtigt werden
        return (m_markets == null) ? 0 : (1 + m_markets.length);
    }

    @Override
    protected boolean initializeView(TextView text, JSONObject market) throws JSONException {
        text.setText(Markets.getName(market));

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
            return m_markets[position - 1];
    }

    @Override
    public void refresh() {
        // Zugriff auf die lokale Datenhaltung vorbereiten
        Database database = createDatabase();
        try {
            // Die Liste aller aktuell bekannten Märkte einlesen
            m_markets = Markets.query(database);
        } catch (Exception e) {
            // Alle Fehler werden ignoriert
            m_markets = null;
        } finally {
            database.close();
        }
    }
}
