package de.jochen_manns.buyitv0;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

class ProductAdapter extends ItemAdapter {
    private final LayoutInflater m_inflater;

    private JSONObject[] m_products = null;

    public ProductAdapter(Context context) {
        m_inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return (m_products == null) ? 0 : m_products.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = m_inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        TextView textView = (TextView) convertView;
        JSONObject product = m_products[position];

        try {
            String market = Products.getMarket(product);
            String name = Products.getName(product);

            int res = 0;
            if (Products.isBought(product))
                res = R.string.product_suffix_bought;
            else if ((market != null) && (market.length() > 0))
                res = R.string.product_prefix_market;

            if (res > 0)
                textView.setText(m_inflater.getContext().getResources().getString(res, name, market));
            else
                textView.setText(name);
        } catch (JSONException e) {
            textView.setText("### ERROR ###");
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        try {
            return Products.getIdentifier(m_products[position]);
        } catch (JSONException e) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public Object getItem(int position) {
        return m_products[position];
    }

    @Override
    public void refresh() {
        Database database = Database.create(m_inflater.getContext());
        try {
            m_products = Products.query(database, true);
        } catch (JSONException e) {
            m_products = null;
        } finally {
            database.close();
        }
    }
}
