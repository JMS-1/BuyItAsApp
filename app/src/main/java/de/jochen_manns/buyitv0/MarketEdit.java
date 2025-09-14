package de.jochen_manns.buyitv0;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

/*
    Die Aktivität zur Bearbeitung der Informationen eines Marktes - tatsächlich
    ist das nur der Name des Marktes.
 */
public class MarketEdit extends EditActivity<String, JSONObject[]> {
    // Die Namen aller anderen Märkte - hiermit wird sichergestellt, dass der Name eines Marktes eindeutig ist
    private final HashSet<String> m_forbiddenNames = new HashSet<String>();

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.activity_market_edit, R.id.market_root, R.menu.menu_market_edit, savedInstanceState);
    }

    @Override
    protected int getTitle(boolean forNew) {
        // Je nach Aufrufsituation anders
        return forNew ? R.string.market_edit_create : R.string.market_edit_modify;
    }

    @Override
    protected boolean isValidName(String newName) {
        // Der Name eines Marktes darf nicht leer sein UND es darf keinen anderen Markt mit demselben Namen geben
        return !newName.isEmpty() && !m_forbiddenNames.contains(newName.toUpperCase());
    }

    @Override
    protected JSONObject[] queryItem(Database database, String market) throws JSONException {
        // Einfach alle Märkte aus der lokalen Datenbank auslesen
        return Markets.query(database, null);
    }

    @Override
    protected void initializeFromItem(JSONObject[] markets) {
        // Beim Ändern müssen wird sicherstellen, dass der ursprüngliche Namen bei Aufruf der Aktivität nicht in der Verbotsliste landet
        String preSelected = getIdentifier();

        // Im Wesentlichen bauen wir die Liste der verbotenen Namen auf
        for (JSONObject market : markets)
            try {
                String originalName = Markets.getOriginalName(market);
                String name = Markets.getName(market);

                // Wir allerdings ein existierender Markt verändert, so wird dessen aktueller Name in das Eingabefeld übernommen
                if ((preSelected != null) && preSelected.equals(originalName))
                    setName(name);
                else
                    m_forbiddenNames.add(name.toUpperCase());
            } catch (Exception e) {
                // Alle Fehler werden ignoriert
            }
    }

    @Override
    protected void updateItem(Database database, String market) {
        Markets.update(database, market, getName());
    }

    @Override
    protected void deleteItem(Database database, String market) {
        Markets.delete(database, market);
    }
}
