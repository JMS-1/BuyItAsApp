package de.jochen_manns.buyitv0;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

/*
    Repräsentiert eine Aktivität mit einer Liste von Elementen.
 */
public abstract class ListActivity<TIdentifierType extends Serializable, TEditType extends EditActivity<TIdentifierType, ?>, TAdapterType extends ItemAdapter> extends Activity {
    // Die Kennung für die Antwortdaten zum Ändern oder Anlegen eines Elementes.
    private static final int RESULT_EDIT_ITEM = 1;

    // Die zu verwendende ActionBar.
    private int m_menu;

    // Das wäre dann unsere Liste.
    private TouchableListView m_view;

    // Ermittelt die eindeutige Identifikation eines Elementes.
    protected abstract TIdentifierType getIdentifier(JSONObject item) throws JSONException;

    // Prüft, ob ein Element verändert werden darf.
    protected abstract boolean canEdit(TIdentifierType identifier);

    // Initialisiert eine neue Aktivität.
    protected void onCreate(int menu, Bundle savedInstanceState) {
        m_menu = menu;

        super.onCreate(savedInstanceState);

        // Liste erzeugen und verwenden
        setContentView(m_view = (TouchableListView) getLayoutInflater().inflate(R.layout.list, null));

        // Titelzeile berücksichtigen.
        EditActivity.RespectTitleBar(this, R.id.list_root);
    }

    // Meldet die Anzeige der Liste.
    public TouchableListView getListView() {
        return m_view;
    }

    // Meldet die Verwaltung der Listenelemente.
    public TAdapterType getListAdapter() {
        return (TAdapterType) getListView().getAdapter();
    }

    // Legt die Verwaltung der Listenelemente fest.
    public void setListAdapter(TAdapterType adapter) {
        getListView().setAdapter(adapter);
    }

    // Aktiviert die Veränderung der Informationen des ausgewählten Listeneintrags.
    public abstract void onClick(JSONObject item);

    // Vertauscht ein Element mit dem Folgenden.
    public void onSwapWithNext(int leftPosition) {
    }

    // Wird aufgerufen, nachdem die Liste aktualisiert wurde.
    protected void afterLoad() {
    }

    // Aktiviert die Veränderung der Informationen des ausgewählten Listeneintrags.
    public void onEdit(JSONObject item) {
        try {
            // Eindeutige Identifikation ermitteln
            TIdentifierType identifier = getIdentifier(item);

            // Und den Änderungsvorgang starten, sofern dies möglich ist
            if (canEdit(identifier))
                startEdit(identifier);
        } catch (JSONException e) {
        }
    }

    // Deaktiviert die Auswertung von Gesten.
    public void disableGestures() {
        getListView().disableGestures();
    }

    // Ändert ein existierendes Element oder legt ein neues an.
    protected void startEdit(TIdentifierType id) {
        // Die Art der Aktivität zum Ändern, extrahiert aus den generischen Parametern der Basisklasse
        Class editActivity = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

        // Aufruf vorbereiten
        Intent openForEdit = new Intent(this, editActivity);
        openForEdit.putExtra(EditActivity.EXTRA_IDENTIFIER, id);

        // Aktivität zum Ändern starten
        startActivityForResult(openForEdit, RESULT_EDIT_ITEM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_EDIT_ITEM:
                // Nach der Aktualisierung der lokalen Datenbank sollte die Liste aktualisiert werden - TODO: hier ist noch Optimierungspotential
                if (resultCode == RESULT_OK)
                    load();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            // Nach Auswahl aus der ActionBar wird das Neuanlegen angestossen
            startEdit(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String activeCategory = "";

    // Liest die Liste der Elemente aus der lokalen Datenbank.
    protected void load() {
        load(activeCategory);
    }

    protected void load(String category) {
        activeCategory = category;

        new BackgroundTask<JSONObject[]>(this) {
            @Override
            protected JSONObject[] doInBackground() {
                // Daten aus der lokalen Datenbank auslesen
                return getListAdapter().load(
                        null,
                        category != null ? Products.Category : null,
                        category != null && category.isEmpty() ? null : category
                );
            }

            @Override
            protected void onPostExecute(JSONObject[] items) {
                // Daten in die Oberfläche übernehmen
                getListAdapter().refresh(items);

                // Benachrichtigung versenden
                afterLoad();
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ActionBar erzeugen
        getMenuInflater().inflate(m_menu, menu);

        return true;
    }
}
