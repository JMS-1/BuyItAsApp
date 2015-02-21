package de.jochen_manns.buyitv0;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

public class MarketList extends ListActivity<String, MarketEdit, MarketAdapter> {

    public final static String ARG_MARKET_NAME = "market";

    public final static int RESULT_SELECTED = 1;

    private String m_market;

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

        m_market = null;

        Intent startInfo = getIntent();
        if (startInfo != null)
            if (startInfo.hasExtra(ARG_MARKET_NAME)) {
                m_market = startInfo.getStringExtra(ARG_MARKET_NAME);

                if (m_market == null)
                    m_market = getResources().getString(R.string.editSelect_item_nomarket);
            }

        setTitle((m_market == null) ? R.string.market_list_forBuy : R.string.market_list_forEdit);

        try {
            setListAdapter(new MarketAdapter(this, m_market != null));
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

        JSONObject market;
        if ((m_market == null) || (position > 0))
            market = (JSONObject) l.getItemAtPosition(position);
        else
            market = null;

        try {
            Intent result = new Intent();
            result.putExtra(ARG_MARKET_NAME, (market == null) ? null : Markets.getName(market));

            setResult(RESULT_SELECTED, result);
        } catch (JSONException e) {
        }

        finish();
    }
}
