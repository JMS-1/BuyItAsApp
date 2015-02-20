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

public abstract class EditActivity extends Activity {

    private Button m_save;

    private Button m_delete;

    private EditText m_name;

    protected abstract boolean initializeFromIntent(Intent intent);

    protected abstract boolean creatingNewItem();

    protected abstract boolean isValidName(Editable newName);

    protected abstract JSONObject queryItem(Database database) throws JSONException;

    protected abstract void initializeFromItem(JSONObject item);

    protected Editable getName() {
        return m_name.getText();
    }

    protected void setName(String name) {
        m_name.setText(name);
    }

    protected void onCreate(int layout, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout);

        m_delete = (Button) findViewById(R.id.button_delete);
        m_save = (Button) findViewById(R.id.button_save);
        m_name = (EditText) findViewById(R.id.edit_name);

        Intent startInfo = getIntent();
        if (startInfo == null) {
            finish();
            return;
        }

        if (!initializeFromIntent(startInfo)) {
            finish();
            return;
        }

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

        if (creatingNewItem())
            m_delete.setVisibility(View.INVISIBLE);
        else
            new AsyncTask<Void, Void, JSONObject>() {
                @Override
                protected JSONObject doInBackground(Void... params) {
                    Database database = Database.create(EditActivity.this);
                    try {
                        return queryItem(database);
                    } catch (JSONException e) {
                        return null;
                    } finally {
                        database.close();
                    }
                }

                @Override
                protected void onPostExecute(JSONObject jsonObject) {
                    super.onPostExecute(jsonObject);

                    if (jsonObject == null)
                        finish();
                    else
                        initializeFromItem(jsonObject);
                }
            }.execute();
    }
}
