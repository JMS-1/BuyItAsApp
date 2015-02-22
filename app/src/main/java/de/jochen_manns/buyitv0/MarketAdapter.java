package de.jochen_manns.buyitv0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

class MarketAdapter extends ItemAdapter {
    private final LayoutInflater m_inflater;

    private JSONObject[] m_markets = null;

    private JSONObject m_noMarket = new JSONObject();

    public MarketAdapter(Context context, String emptyName) throws JSONException {
        Markets.setName(m_noMarket, emptyName);
        Markets.setOriginalName(m_noMarket, null);

        m_inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return (m_markets == null) ? 0 : (1 + m_markets.length);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = m_inflater.inflate(android.R.layout.simple_list_item_activated_1, parent, false);

        TextView textView = (TextView) convertView;
        JSONObject market = (JSONObject) getItem(position);

        try {
            textView.setText(Markets.getName(market));
        } catch (JSONException e) {
            textView.setText("### ERROR ###");
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        if (position < 1)
            return m_noMarket;
        else
            return m_markets[position - 1];
    }

    @Override
    public void refresh() {
        Database database = Database.create(m_inflater.getContext());
        try {
            m_markets = Markets.query(database);
        } catch (JSONException e) {
            m_markets = null;
        } finally {
            database.close();
        }
    }
}
