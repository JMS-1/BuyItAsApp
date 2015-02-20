package de.jochen_manns.buyitv0;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class MarketEdit extends EditActivity<JSONObject[]> {
    public static final String ARG_MARKET_NAME = "market";

    private String m_market;

    private final HashSet<String> m_forbiddenNames = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.activity_market_edit, savedInstanceState);
    }

    @Override
    protected boolean initializeFromIntent(Intent intent) {
        if (!intent.hasExtra(ARG_MARKET_NAME))
            return false;

        m_market = intent.getStringExtra(ARG_MARKET_NAME);

        return true;
    }

    @Override
    protected boolean creatingNewItem() {
        return (m_market == null);
    }

    @Override
    protected boolean isValidName(Editable newName) {
        return ((newName != null) && (newName.length() > 0) && !m_forbiddenNames.contains(newName.toString().toUpperCase()));
    }

    @Override
    protected JSONObject[] queryItem(Database database) throws JSONException {
        return Markets.query(database);
    }

    @Override
    protected void initializeFromItem(JSONObject[] markets) {
        for (JSONObject market : markets)
            try {
                String originalName = Markets.getOriginalName(market);
                String name = Markets.getName(market);

                if ((m_market != null) && m_market.equals(originalName))
                    setName(name);
                else
                    m_forbiddenNames.add(name.toUpperCase());
            } catch (JSONException e) {
                continue;
            }
    }

    @Override
    protected boolean updateItem(Database database) {
        Markets.update(database, m_market, getName().toString());

        return true;
    }

    @Override
    protected boolean deleteItem(Database database) {
        Markets.delete(database, m_market);

        return true;
    }
}
