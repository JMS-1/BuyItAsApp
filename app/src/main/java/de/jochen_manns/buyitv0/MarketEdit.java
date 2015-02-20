package de.jochen_manns.buyitv0;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

public class MarketEdit extends Activity {
    public static final int RESULT_SAVED = 1;

    public static final String ARG_MARKET_NAME = "market";

    private Button m_save;

    private Button m_delete;

    private EditText m_name;

    private String m_market;

    private final HashSet<String> m_forbiddenNames = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market_edit);

        m_save = (Button) findViewById(R.id.button_save);
        m_delete = (Button) findViewById(R.id.button_delete);
        m_name = (EditText) findViewById(R.id.edit_name);

        Intent startInfo = getIntent();
        if (startInfo == null) {
            finish();
            return;
        }

        if (!startInfo.hasExtra(ARG_MARKET_NAME)) {
            finish();
            return;
        }

        m_market = startInfo.getStringExtra(ARG_MARKET_NAME);

        if (m_market == null)
            m_delete.setVisibility(View.INVISIBLE);

        m_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                m_save.setEnabled((s != null) && (s.length() > 0) && !m_forbiddenNames.contains(s.toString().toUpperCase()));
            }
        });

        new AsyncTask<Void, Void, JSONObject[]>() {
            @Override
            protected JSONObject[] doInBackground(Void... params) {
                Database database = Database.create(MarketEdit.this);
                try {
                    return Markets.query(database);
                } catch (JSONException e) {
                    return null;
                } finally {
                    database.close();
                }
            }

            @Override
            protected void onPostExecute(JSONObject[] markets) {
                super.onPostExecute(markets);

                for (JSONObject market : markets)
                    try {
                        String originalName = Markets.getOriginalName(market);
                        String name = Markets.getName(market);

                        if ((m_market != null) && m_market.equals(originalName))
                            m_name.setText(name);
                        else
                            m_forbiddenNames.add(name.toUpperCase());
                    } catch (JSONException e) {
                        continue;
                    }
            }
        }.execute();
    }

    public void onUpdate(View view) {
        Editable name = m_name.getText();
        if ((name == null) || (name.length() < 1))
            return;

        Database database = Database.create(this);
        try {
            Markets.update(database, m_market, name.toString());
        } finally {
            database.close();
        }

        setResult(RESULT_SAVED);

        finish();
    }

    public void onDelete(View view) {
        Database database = Database.create(this);
        try {
            Markets.delete(database, m_market);
        } finally {
            database.close();
        }

        setResult(RESULT_SAVED);

        finish();
    }
}
