package de.jochen_manns.buyitv0;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductEdit extends EditActivity {

    public static final int RESULT_SAVED = 1;

    private static final int RESULT_SELECT_MARKET = 1;

    public static final String ARG_ITEM_ID = "id";

    private EditText m_description;

    private Button m_market;

    private long m_identifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.activity_product_edit, savedInstanceState);
    }

    @Override
    protected boolean initializeFromIntent(Intent intent) {
        m_market = (Button) findViewById(R.id.edit_item_market);
        m_description = (EditText) findViewById(R.id.edit_item_description);

        m_identifier = intent.getLongExtra(ARG_ITEM_ID, Long.MAX_VALUE);

        return (m_identifier != Long.MAX_VALUE);
    }

    @Override
    protected boolean creatingNewItem() {
        return (m_identifier == Long.MIN_VALUE);
    }

    @Override
    protected boolean isValidName(Editable newName) {
        return ((newName != null) && (newName.length() > 0));
    }

    @Override
    protected JSONObject queryItem(Database database) throws JSONException {
        return Products.query(database, m_identifier);
    }

    @Override
    protected void initializeFromItem(JSONObject item) {
        try {
            String market = Products.getMarket(item);
            if ((market != null) && (market.length() > 0)) {
                m_market.setText(market);
                m_market.setTag(market);
            }

            m_description.setText(Products.getDescription(item));
            setName(Products.getName(item));

        } catch (JSONException e) {
            setName("### ERROR ###");
            m_description.setText("### ERROR ###");
        }
    }

    public void onSelectMarket(View view) {
        Intent showSelector = new Intent(this, MarketList.class);
        showSelector.putExtra(MarketList.ARG_MARKET_NAME, (String) m_market.getTag());
        startActivityForResult(showSelector, RESULT_SELECT_MARKET);
    }

    public void onUpdate(View view) {
        Editable name = getName();
        if ((name == null) || (name.length() < 1))
            return;

        Editable description = m_description.getText();
        if ((description != null) && (description.length() < 1))
            description = null;

        String market = (String) m_market.getTag();

        Database database = Database.create(this);
        try {
            Products.update(database, m_identifier, name.toString(), (description == null) ? null : description.toString(), market);
        } finally {
            database.close();
        }

        setResult(RESULT_SAVED);

        finish();
    }

    public void onDelete(View view) {
        Database database = Database.create(this);
        try {
            Products.delete(database, m_identifier);
        } finally {
            database.close();
        }

        setResult(RESULT_SAVED);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null)
            switch (requestCode) {
                case RESULT_SELECT_MARKET:
                    if (resultCode == MarketList.RESULT_SELECTED) {
                        String market = data.getStringExtra(MarketList.ARG_MARKET_NAME);

                        m_market.setTag(market);

                        if (market == null)
                            m_market.setText(R.string.editSelect_item_nomarket);
                        else
                            m_market.setText(market);
                    }
                    break;
            }
    }
}
