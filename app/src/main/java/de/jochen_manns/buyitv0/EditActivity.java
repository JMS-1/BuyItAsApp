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

import java.io.Serializable;

public abstract class EditActivity<TIdentifierType extends Serializable, TProtocolType> extends Activity {

    public static final String ARG_EXTRA_ID = "id";

    public static final int RESULT_SAVED = 1;

    private Button m_save;

    private Button m_delete;

    private EditText m_name;

    private TIdentifierType m_identifier;

    protected abstract boolean isValidName(Editable newName);

    protected abstract TProtocolType queryItem(Database database, TIdentifierType item) throws JSONException;

    protected abstract void initializeFromItem(TProtocolType item);

    protected abstract void deleteItem(Database database, TIdentifierType item);

    protected abstract void updateItem(Database database, TIdentifierType item);

    protected abstract int getTitle(boolean forNew);

    protected Editable getName() {
        return m_name.getText();
    }

    protected void setName(String name) {
        m_name.setText(name);
    }

    protected TIdentifierType getIdentifier() {
        return m_identifier;
    }

    protected void onCreate(int layout, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout);

        getActionBar().setIcon(android.R.color.transparent);

        m_delete = (Button) findViewById(R.id.button_delete);
        m_save = (Button) findViewById(R.id.button_save);
        m_name = (EditText) findViewById(R.id.edit_name);

        m_save.setText(R.string.button_save);
        m_delete.setText(R.string.button_delete);

        Intent startInfo = getIntent();
        if (startInfo == null) {
            finish();
            return;
        }

        if (!startInfo.hasExtra(ARG_EXTRA_ID)) {
            finish();
            return;
        }

        m_identifier = (TIdentifierType) startInfo.getSerializableExtra(ARG_EXTRA_ID);

        setTitle(getTitle(m_identifier == null));

        m_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidName(getName()))
                    return;

                Database database = Database.create(EditActivity.this);
                try {
                    updateItem(database, m_identifier);
                } finally {
                    database.close();
                }

                setResult(RESULT_SAVED);

                finish();
            }
        });

        m_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Database database = Database.create(EditActivity.this);
                try {
                    deleteItem(database, m_identifier);
                } finally {
                    database.close();
                }

                setResult(RESULT_SAVED);

                finish();
            }
        });

        m_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                m_save.setEnabled(isValidName(s));
            }
        });

        if (m_identifier == null)
            m_delete.setVisibility(View.INVISIBLE);

        new AsyncTask<Void, Void, TProtocolType>() {
            @Override
            protected TProtocolType doInBackground(Void... params) {
                Database database = Database.create(EditActivity.this);
                try {
                    return queryItem(database, m_identifier);
                } catch (JSONException e) {
                    return null;
                } finally {
                    database.close();
                }
            }

            @Override
            protected void onPostExecute(TProtocolType jsonObject) {
                super.onPostExecute(jsonObject);

                initializeFromItem(jsonObject);
            }
        }.execute();
    }
}
