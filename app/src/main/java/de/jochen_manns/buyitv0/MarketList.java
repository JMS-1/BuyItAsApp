package de.jochen_manns.buyitv0;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Die Aktivität zur Auswahl eines Marktes - entweder beim Pflegen von Produkten
    oder beim Einkaufen.
 */
public class MarketList extends ListActivity<String, MarketEdit, MarketAdapter> {

    // Optional die im Intent hinterlegte Vorauswahl eines Marktes.
    public final static String EXTRA_MARKET_NAME = "market";

    // Optional die eindeutige Kennung eines Produktes, das eingekauft werden soll.
    public final static String EXTRA_PRODUCT_IDENTIFIER = "forBuy";

    // Der vorausgewählte Markt.
    private String m_market;

    // Das Produkt, das eingekauft werden soll.
    private Long m_product;

    @Override
    protected String getIdentifier(JSONObject item) throws JSONException {
        // Die eindeutige Identifikation eines Marktes ist der Name, unter dem er in der Online Datenbank abgelegt ist - unabhängig von einer etwaigen lokalen Umbenennung
        return Markets.getOriginalName(item);
    }

    @Override
    protected boolean canEdit(String identifier) {
        // Die Platzhalterauswahl für keinen Markt kann nicht verändert werden, da es sich um keinen realen Eintrag in der Datenbank handelt
        return identifier != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(ListView.CHOICE_MODE_SINGLE, R.menu.menu_market_list, savedInstanceState);

        // Diese Aktivität kann nur mit einem Intent gestartet werden
        Intent startInfo = getIntent();
        if (startInfo == null) {
            finish();
            return;
        }

        // Beide Intent Parameter sind optional, wobei für einen fehlenden Parameter auch der Wert null verwendet werden darf
        m_product = (Long) startInfo.getSerializableExtra(EXTRA_PRODUCT_IDENTIFIER);
        m_market = startInfo.getStringExtra(EXTRA_MARKET_NAME);

        // Die Überschrift der Auswahl ergibt sich aus der Aufrufsituation
        setTitle((m_product == null) ? R.string.market_list_forEdit : R.string.market_list_forBuy);

        // Das gilt auch für die Leerauswahl
        String emptyName;
        if (m_product == null)
            emptyName = getResources().getString(R.string.editSelect_item_nomarket);
        else
            emptyName = getResources().getString(R.string.editSelect_item_nobuy);

        // Nun kann die Verwaltung der Liste vorbereitet werden
        try {
            setListAdapter(new MarketAdapter(this, emptyName));
        } catch (Exception e) {
            // Alle Fehler führen zum frühzeitigen Beenden der Aktivität
            finish();
            return;
        }

        // Wenn eine Vorauswahl angegeben wurde, so müssen wir auf Veränderungen an der Liste der Märkte reagieren können
        if (m_market != null)
            getListAdapter().registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();

                    // Die Liste der Märkte absuchen - der erste Eintrag ist die Leerauswahl, die kann ignoriert werden
                    ListView view = getListView();
                    for (int i = 1; i < view.getCount(); i++)
                        try {
                            // Daten des Marktes auswählen
                            JSONObject market = (JSONObject) view.getItemAtPosition(i);
                            String name = Markets.getName(market);

                            // Vorauswahl auf Wunsch aktivieren und in den sichtbaren Bereich fahren - der MarketAdapter verwendet dazu ein geeignetes Item Template
                            if (m_market.equals(name)) {
                                view.setItemChecked(i, true);
                                view.smoothScrollToPosition(i);
                                break;
                            }
                        } catch (Exception e) {
                            // Fehler interessieren hier nicht
                        }
                }
            });

        // Im Hintergrund die Liste der Märkte laden
        load();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            // Daten der aktuellen Auswahl ermitteln
            JSONObject market = (position < 1) ? null : (JSONObject) l.getItemAtPosition(position);

            // Aktuelle Auswahl in eine Antwortstruktur packen - hierbei wir immer der aktuelle Anzeigename verwendet
            Intent result = new Intent();
            result.putExtra(EXTRA_MARKET_NAME, (market == null) ? null : Markets.getName(market));
            result.putExtra(EXTRA_PRODUCT_IDENTIFIER, m_product);

            // Diese Aktivität mit einem entsprechenden Antwortcode beenden
            setResult(RESULT_OK, result);

            finish();
        } catch (Exception e) {
            // Fehler interessieren hier nicht
        }
    }
}
