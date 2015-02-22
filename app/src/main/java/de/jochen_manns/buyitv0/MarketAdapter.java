package de.jochen_manns.buyitv0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Verwaltet die aktuelle Liste der Märkte. Der erste Eintrag ist dabei
    immer ein Platzhalter und ermöglicht die Auswahl keines Marktes.
 */
class MarketAdapter extends ItemAdapter {
    // Erstellt aus Ressourcen die passenden Views
    private final LayoutInflater m_inflater;
    // Der Platzhalter für die Auswahl keines Marktes
    private final JSONObject m_noMarket = new JSONObject();
    // Die Liste aller bekannten Märkte
    private JSONObject[] m_markets = null;

    // Erstellt eine neue Liste
    public MarketAdapter(Context context, String emptyName) throws JSONException {
        // Der Anzeigename der leeren Auswahl ist von der Situation des Aufrufs abhängig
        Markets.setName(m_noMarket, emptyName);
        Markets.setOriginalName(m_noMarket, null);

        m_inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // Bei der Anzahl der Märkte muss die Leerauswahl berücksichtigt werden
        return (m_markets == null) ? 0 : (1 + m_markets.length);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Einen neuen View erzeugen, wenn kein existierender wiederverwendet werden soll
        if (convertView == null)
            convertView = m_inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);

        // Zugriff auf den Zielview und die Quelldaten
        TextView textView = (TextView) convertView;
        JSONObject market = (JSONObject) getItem(position);

        try {
            // Listenelement initialisieren
            textView.setText(Markets.getName(market));
        } catch (Exception e) {
            // Fehler werden damit eigentlich ignoriert, aber das sollte hier auch kein Problem sein
            textView.setText("### ERROR ###");
        }

        return convertView;
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
        Database database = Database.create(m_inflater.getContext());
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
