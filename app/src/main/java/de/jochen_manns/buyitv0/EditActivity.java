package de.jochen_manns.buyitv0;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import org.json.JSONException;

import java.io.Serializable;

public abstract class EditActivity<TIdentifierType extends Serializable, TProtocolType> extends Activity {

    public static final String ARG_EXTRA_ID = "id";

    public static final int RESULT_SAVED = 1;

    private EditText m_name;

    private TIdentifierType m_identifier;

    private int m_menu;

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

    protected void onCreate(int layout, int menu, Bundle savedInstanceState) {
        m_menu = menu;

        super.onCreate(savedInstanceState);

        setContentView(layout);

        getActionBar().setIcon(android.R.color.transparent);

        m_name = (EditText) findViewById(R.id.edit_name);

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

        m_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                invalidateOptionsMenu();
            }
        });

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Database database = Database.create(EditActivity.this);
        try {
            switch (item.getItemId()) {
                case R.id.button_save:
                    updateItem(database, m_identifier);
                    break;
                case R.id.button_delete:
                    deleteItem(database, m_identifier);
                    break;
                default:
                    return super.onOptionsItemSelected(item);
            }
        } finally {
            database.close();
        }

        setResult(RESULT_SAVED);

        finish();

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(m_menu, menu);

        MenuItem saveItem = menu.findItem(R.id.button_save);
        MenuItem deleteItem = menu.findItem(R.id.button_delete);

        saveItem.setTitle(R.string.button_save);
        saveItem.setEnabled(isValidName(m_name.getText()));

        if (m_identifier == null)
            deleteItem.setVisible(false);
        else
            deleteItem.setTitle(R.string.button_delete);

        return true;
    }
}
