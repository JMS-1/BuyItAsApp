package de.jochen_manns.buyitv0;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class CategoryList extends Activity implements View.OnClickListener {
    private class CategoryAdaptzer extends BaseAdapter {

        private ArrayList<String> m_categories = new ArrayList<>();

        private final LayoutInflater m_inflater;

        private final CategoryList m_list;

        public CategoryAdaptzer(CategoryList list, String preselect) {
            super();

            m_list = list;
            m_inflater = LayoutInflater.from(list);

            // Der Abruf der Daten für den Vorgang erfolgt asynchron
            new Thread(() -> runOnUiThread(() -> {
                try (Database database = Database.create(list)) {
                    m_categories = new ArrayList<>();

                    m_categories.add(getString(R.string.group_no_group));
                    m_categories.addAll(Arrays.asList(Products.queryCategories(database)));

                    notifyDataSetChanged();

                    if (preselect != null && !preselect.isEmpty()) {
                        for (int i = 1; i < m_categories.size(); i++)
                            if (m_categories.get(i).compareTo(preselect) == 0) {
                                m_view.smoothScrollToPosition(i);

                                break;
                            }
                    }
                } catch (Exception e) {
                    // Alle Fehler werden ignoriert
                }
            })).start();
        }

        @Override
        public int getCount() {
            return m_categories.size();
        }

        @Override
        public Object getItem(int position) {
            return m_categories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = m_inflater.inflate(R.layout.editable_list_item, parent, false);

            TextView textView = convertView.findViewById(R.id.listitem_text);
            View editView = convertView.findViewById(R.id.listitem_edit);

            editView.setTag(position);
            editView.setVisibility(View.INVISIBLE);

            textView.setTag(position);
            textView.setText(m_categories.get(position));

            editView.setOnClickListener(m_list);
            textView.setOnClickListener(m_list);

            return convertView;
        }
    }

    public final static String PRESELECTED_CATEGORY = "category";

    private TouchableListView m_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startInfo = getIntent();

        if (startInfo == null) {
            finish();

            return;
        }

        String preSelect = startInfo.getStringExtra(PRESELECTED_CATEGORY);

        m_view = (TouchableListView) getLayoutInflater().inflate(R.layout.list, null);
        m_view.setAdapter(new CategoryAdaptzer(this, preSelect));

        setContentView(m_view);

        setTitle(R.string.group_title);
    }

    @Override
    public void onClick(View v) {
        int index = (int) v.getTag();

        Intent result = new Intent();
        result.putExtra(PRESELECTED_CATEGORY, index < 1 ? null : ((TextView) v).getText().toString());

        setResult(RESULT_OK, result);

        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);

        finish();
    }
}