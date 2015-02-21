package de.jochen_manns.buyitv0;

import android.os.Bundle;
import android.text.Editable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class MarketEdit extends EditActivity<String, JSONObject[]> {
    private final HashSet<String> m_forbiddenNames = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.activity_market_edit, savedInstanceState);
    }

    @Override
    protected boolean isValidName(Editable newName) {
        return ((newName != null) && (newName.length() > 0) && !m_forbiddenNames.contains(newName.toString().toUpperCase()));
    }

    @Override
    protected JSONObject[] queryItem(Database database, String market) throws JSONException {
        return Markets.query(database);
    }

    @Override
    protected void initializeFromItem(JSONObject[] markets) {
        String preSelected = getIdentifier();

        for (JSONObject market : markets)
            try {
                String originalName = Markets.getOriginalName(market);
                String name = Markets.getName(market);

                if ((preSelected != null) && preSelected.equals(originalName))
                    setName(name);
                else
                    m_forbiddenNames.add(name.toUpperCase());
            } catch (JSONException e) {
                continue;
            }
    }

    @Override
    protected void updateItem(Database database, String market) {
        Markets.update(database, market, getName().toString());
    }

    @Override
    protected void deleteItem(Database database, String market) {
        Markets.delete(database, market);
    }
}
