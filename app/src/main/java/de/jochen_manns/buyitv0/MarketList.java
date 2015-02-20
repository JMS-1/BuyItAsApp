package de.jochen_manns.buyitv0;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

public class MarketList extends ListActivity implements AdapterView.OnItemLongClickListener {

    private static final int RESULT_EDIT_MARKET = 1;

    public final static String ARG_MARKET_NAME = "market";

    public final static int RESULT_SELECTED = 1;

    private String m_market;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_market = null;

        Intent startInfo = getIntent();
        if (startInfo != null)
            if (startInfo.hasExtra(ARG_MARKET_NAME)) {
                m_market = startInfo.getStringExtra(ARG_MARKET_NAME);

                if (m_market == null)
                    m_market = getResources().getString(R.string.editSelect_item_nomarket);
            }

        try {
            setListAdapter(new MarketAdapter(this, m_market != null));
        } catch (JSONException e) {
            finish();
            return;
        }

        ListView view = getListView();
        view.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        view.setOnItemLongClickListener(MarketList.this);

        load();
    }

    private void load() {
        new AsyncTask<Void, Void, MarketAdapter>() {
            @Override
            protected MarketAdapter doInBackground(Void... params) {
                MarketAdapter adapter = (MarketAdapter) getListAdapter();

                adapter.refresh();

                return adapter;
            }

            @Override
            protected void onPostExecute(MarketAdapter adapter) {
                super.onPostExecute(adapter);

                if (adapter != null)
                    adapter.notifyDataSetChanged();

                if (m_market == null)
                    return;

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
        }.execute();
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = getListView();
        if (listView != parent)
            return false;

        try {
            JSONObject market = (JSONObject) listView.getItemAtPosition(position);
            String marketName = Markets.getOriginalName(market);
            if (marketName == null)
                return true;

            onEdit(marketName);
        } catch (JSONException e) {
            finish();
        }

        return true;
    }

    private void onEdit(String originalName) {
        Intent openForEdit = new Intent(this, MarketEdit.class);
        openForEdit.putExtra(MarketEdit.ARG_MARKET_NAME, originalName);
        startActivityForResult(openForEdit, RESULT_EDIT_MARKET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_EDIT_MARKET:
                if (resultCode == MarketEdit.RESULT_SAVED)
                    load();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_market_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_market:
                onEdit(null);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
