package de.jochen_manns.buyitv0;

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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

public abstract class ListActivity<TIdentifierType extends Serializable, TEditType extends EditActivity<TIdentifierType, ?>, TAdapterType extends ItemAdapter> extends android.app.ListActivity implements AdapterView.OnItemLongClickListener {
    private static final int RESULT_EDIT_ITEM = 1;

    private int m_menu;

    protected abstract TIdentifierType getIdentifier(JSONObject item) throws JSONException;

    protected abstract boolean canEdit(TIdentifierType identifier);

    protected void onCreate(int choiceMode, int menu, Bundle savedInstanceState) {
        m_menu = menu;

        super.onCreate(savedInstanceState);

        getActionBar().setIcon(android.R.color.transparent);

        ListView view = getListView();
        view.setChoiceMode(choiceMode);
        view.setOnItemLongClickListener(this);
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = getListView();
        if (listView != parent)
            return false;

        try {
            JSONObject item = (JSONObject) listView.getItemAtPosition(position);
            TIdentifierType identifier = getIdentifier(item);

            if (canEdit(identifier))
                return startEdit(identifier);
        } catch (JSONException e) {
            finish();
        }

        return true;
    }

    protected boolean startEdit(TIdentifierType id) {
        Class editActivity = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

        Intent openForEdit = new Intent(this, editActivity);
        openForEdit.putExtra(EditActivity.EXTRA_IDENTIFIER, id);

        startActivityForResult(openForEdit, RESULT_EDIT_ITEM);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_EDIT_ITEM:
                if (resultCode == RESULT_OK)
                    load();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                startEdit(null);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void load() {
        new AsyncTask<TAdapterType, Void, TAdapterType>() {
            @Override
            protected TAdapterType doInBackground(TAdapterType... params) {
                params[0].refresh();
                return params[0];
            }

            @Override
            protected void onPostExecute(TAdapterType adapter) {
                super.onPostExecute(adapter);

                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        }.execute((TAdapterType) ListActivity.this.getListAdapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(m_menu, menu);

        MenuItem newItem = menu.findItem(R.id.action_new);
        newItem.setTitle(R.string.action_new);

        return true;
    }
}
