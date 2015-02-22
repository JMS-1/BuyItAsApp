package de.jochen_manns.buyitv0;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

public class MarketList extends ListActivity<String, MarketEdit, MarketAdapter> {

    public final static String EXTRA_MARKET_NAME = "market";

    public final static String EXTRA_PRODUCT_IDENTIFIER = "forBuy";

    public final static int RESULT_SELECTED = 1;

    private String m_market;

    private Long m_product;

    @Override
    protected String getIdentifier(JSONObject item) throws JSONException {
        return Markets.getOriginalName(item);
    }

    @Override
    protected boolean canEdit(String identifier) {
        return identifier != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(ListView.CHOICE_MODE_SINGLE, R.menu.menu_market_list, savedInstanceState);

        Intent startInfo = getIntent();
        if (startInfo == null) {
            finish();
            return;
        }

        m_product = (Long) startInfo.getSerializableExtra(EXTRA_PRODUCT_IDENTIFIER);
        m_market = startInfo.getStringExtra(EXTRA_MARKET_NAME);

        setTitle((m_market == null) ? R.string.market_list_forBuy : R.string.market_list_forEdit);

        String emptyName;
        if (m_product == null)
            emptyName = getResources().getString(R.string.editSelect_item_nomarket);
        else
            emptyName = getResources().getString(R.string.editSelect_item_nobuy);

        try {
            setListAdapter(new MarketAdapter(this, emptyName));
        } catch (JSONException e) {
            finish();
            return;
        }

        if (m_market != null)
            getListAdapter().registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();

                    ListView view = getListView();
                    for (int i = 0; i < view.getCount(); i++)
                        try {
                            JSONObject market = (JSONObject) view.getItemAtPosition(i);
                            String name = Markets.getName(market);

                            if (m_market.equals(name)) {
                                view.setItemChecked(i, true);
                                view.smoothScrollToPosition(i);
                                break;
                            }
                        } catch (JSONException e) {
                            continue;
                        }
                }
            });

        load();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        JSONObject market = (position < 1) ? null : (JSONObject) l.getItemAtPosition(position);

        try {
            Intent result = new Intent();
            result.putExtra(EXTRA_MARKET_NAME, (market == null) ? null : Markets.getName(market));
            result.putExtra(EXTRA_PRODUCT_IDENTIFIER, m_product);

            setResult(RESULT_SELECTED, result);
        } catch (JSONException e) {
        }

        finish();
    }
}
