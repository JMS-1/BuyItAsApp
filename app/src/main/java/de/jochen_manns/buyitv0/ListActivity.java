package de.jochen_manns.buyitv0;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class ListActivity extends android.app.ListActivity implements AdapterView.OnItemLongClickListener {

    protected abstract boolean startEdit(JSONObject item) throws JSONException;

    protected void onCreate(int choiceMode, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            return startEdit(item);
        } catch (JSONException e) {
            finish();
        }

        return true;
    }
}
