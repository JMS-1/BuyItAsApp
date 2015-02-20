package de.jochen_manns.buyitv0;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

public class ProductList extends ListActivity implements AdapterView.OnItemLongClickListener {
    private static final int RESULT_EDIT_PRODUCT = 1;

    private static final int RESULT_START_BUY = 2;

    private class ItemSynchronize extends SynchronizeTask {
        public ItemSynchronize(boolean clear) {
            super(ProductList.this, clear);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            // Es ist nun an der Zeit die Anzeige zu aktualisieren
            if (jsonObject != null)
                load();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wenn der Anwender sich neu anmeldet, dann müssen wir alle Daten neu anfordern
        getSharedPreferences(User.PREFERENCES_NAME, 0)
                .registerOnSharedPreferenceChangeListener(
                        new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                                updateUser(User.Name_Identifier.equals(key));
                            }
                        });

        setListAdapter(new ProductAdapter(this));

        ListView view = getListView();
        view.setChoiceMode(ListView.CHOICE_MODE_NONE);
        view.setOnItemLongClickListener(this);

        updateUser(false);
        load();
    }

    private void updateUser(boolean synchronize) {
        // Benutzeranmeldung ermitteln
        User user = User.load(this);

        // Beim ersten Aufruf haben wir keine Anmeldung
        String userName = (user == null) ? getResources().getString(R.string.app_name) : user.Name;

        // Name der Anwendung anpassen
        setTitle(userName);

        // Wird der Anwender gewechselt so müssen wir neu Laden
        if (synchronize)
            synchronize(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_product_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_synchronize:
                onSynchronize();
                return true;

            case R.id.action_logon:
                onLogon();
                return true;

            case R.id.action_new_product:
                onEdit(Long.MIN_VALUE);
                return true;

            case R.id.action_start_buy:
                onBuy();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void synchronize(boolean clearDatabase) {
        new ItemSynchronize(clearDatabase).start();
    }

    private void onBuy() {
        Intent selectMarket = new Intent(this, MarketList.class);
        startActivityForResult(selectMarket, RESULT_START_BUY);
    }

    private void onStartBuy(String market) {
    }

    private void load() {
        new AsyncTask<ProductAdapter, Void, ProductAdapter>() {
            @Override
            protected ProductAdapter doInBackground(ProductAdapter... params) {
                params[0].refresh();
                return params[0];
            }

            @Override
            protected void onPostExecute(ProductAdapter adapter) {
                super.onPostExecute(adapter);

                if (adapter != null)
                    adapter.notifyDataSetChanged();
            }
        }.execute((ProductAdapter) ProductList.this.getListAdapter());
    }

    public void onSynchronize() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null)
                return;
            if (!networkInfo.isConnected())
                return;

            synchronize(false);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    public void onLogon() {
        User user = User.load(this);
        String userId = (user == null) ? "8E34D0C5" : user.Identifier;

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null)
                return;
            if (!networkInfo.isConnected())
                return;

            new LogonTask(this, userId).start();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    private void onEdit(long id) {
        Intent openForEdit = new Intent(this, ProductEdit.class);
        openForEdit.putExtra(ProductEdit.ARG_ITEM_ID, id);
        startActivityForResult(openForEdit, RESULT_EDIT_PRODUCT);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = getListView();
        if (listView != parent)
            return false;

        onEdit(id);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_EDIT_PRODUCT:
                if (resultCode == EditActivity.RESULT_SAVED)
                    load();
                break;
            case RESULT_START_BUY:
                if (data != null)
                    onStartBuy(data.getStringExtra(MarketList.ARG_MARKET_NAME));
                break;
        }
    }
}
