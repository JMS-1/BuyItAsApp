package de.jochen_manns.buyitv0;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Die Aktivität zum Ändern der Daten eines existierenden Produktes respektive
    zum Anlegen eines neuen Produktes.
 */
public class ProductEdit extends EditActivity<Long, JSONObject> {

    // Die Ergebniskennung für die Auswahl eines Marktes.
    private static final int RESULT_SELECT_MARKET = 1;

    // Das Eingabefeld mit der Beschreibung des Produktes.
    private EditText m_description;

    // Die Schaltfläche zur Auswahl eines Marktes.
    private TextView m_market;

    // Umschalter für dauerhafte Einträge.
    private Switch m_permanent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.activity_product_edit, R.menu.menu_product_edit, savedInstanceState);

        // Der Markt wird ausgewählt, nicht eingegeben
        m_market = findViewById(R.id.edit_product_market);
        m_description = findViewById(R.id.edit_product_description);
        m_permanent = findViewById(R.id.edit_product_permanent);

        m_market.setText(R.string.editSelect_item_nomarket);
        m_market.setTag(null);
        m_market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ruft die Aktivität zur Auswahl eines Marktes aus - die aktuelle Auswahl wird dabei mit übergeben
                Intent showSelector = new Intent(ProductEdit.this, MarketList.class);
                showSelector.putExtra(MarketList.EXTRA_MARKET_NAME, (String) m_market.getTag());
                startActivityForResult(showSelector, RESULT_SELECT_MARKET);
            }
        });
    }

    @Override
    protected int getTitle(boolean forNew) {
        // Die Überschrift der Aktivität hängt von der Aufrufsituation ab
        return forNew ? R.string.product_edit_create : R.string.product_edit_modify;
    }

    @Override
    protected boolean isValidName(String newName) {
        // Der Name eines Produktes darf nicht leer sein
        return !newName.isEmpty();
    }

    @Override
    protected JSONObject queryItem(Database database, Long identifier) throws JSONException {
        // Ermittelt die Daten des zu bearbeitenden Produktes - sofern keines neu angelegt werden soll
        return identifier == null ? null : Products.query(database, identifier);
    }

    @Override
    protected void initializeFromItem(JSONObject item) {
        // Wenn wir ein neues Produkt anlegen, brauchen wir nichts zu tun
        if (item == null)
            return;

        try {
            // Wenn ein Markt zugeordnet ist, so wird dieser vermerket
            String market = Products.getMarket(item);
            if (market != null && !market.isEmpty()) {
                m_market.setText(market);
                m_market.setTag(market);
            }

            // Name und Beschreibung können direkt übernommen werden
            m_description.setText(Products.getDescription(item));
            m_permanent.setChecked(Products.getPermanent(item));
            setName(Products.getName(item));
        } catch (Exception e) {
            // Fehler werden letztlich ignoriert
            setName("### ERROR ###");
        }
    }

    @Override
    protected void updateItem(Database database, Long identifier) {
        // Daten aus der Oberfläche auslesen - man beachte, dass für den Markt der Anzeigename vom gespeicherten Namen abweicht, wenn kein Markt zugeordnet wurde
        String description = m_description.getText().toString();
        String market = (String) m_market.getTag();
        String name = getName();
        Boolean permanent = m_permanent.isChecked();

        // Daten in die lokale Datenbank übertragen
        Products.update(
                database,
                identifier,
                name,
                description.isEmpty() ? null : description,
                market,
                null,
                null,
                null,
                permanent
        );
    }

    @Override
    protected void deleteItem(Database database, Long identifier) {
        Products.delete(database, identifier);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Ohne Antwortdaten können wir gar nichts tun
        if (data == null)
            return;

        switch (requestCode) {
            case RESULT_SELECT_MARKET:
                // Wenn eine Auswahl stattgefunden hat, müssen wir diese respektieren
                if (resultCode == RESULT_OK) {
                    // Den Anzeigenamen auf die Schaltfläche übertragen - der Name, der auch null sein kann, wird im Tag verwaltet
                    String market = data.getStringExtra(MarketList.EXTRA_MARKET_NAME);

                    m_market.setTag(market);

                    // Den Anzeigenamen der Schaltfläche entsprechend anpassen - eventuell abweichend von der tatsächlichen Auswahl
                    if (market == null)
                        m_market.setText(R.string.editSelect_item_nomarket);
                    else
                        m_market.setText(market);
                }
                break;
        }
    }
}
