package de.jochen_manns.buyitv0;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

/*
    Repräsentiert eine Aktivität mit einer Liste von Elementen.
 */
public abstract class ListActivity<TIdentifierType extends Serializable, TEditType extends EditActivity<TIdentifierType, ?>, TAdapterType extends ItemAdapter> extends android.app.ListActivity {
    // Die Kennung für die Antwortdaten zum Ändern oder Anlegen eines Elementes.
    private static final int RESULT_EDIT_ITEM = 1;

    // Die zu verwendende ActionBar.
    private int m_menu;

    // Ermittelt die eindeutige Identifikation eines Elementes.
    protected abstract TIdentifierType getIdentifier(JSONObject item) throws JSONException;

    // Prüft, ob ein Element verändert werden darf.
    protected abstract boolean canEdit(TIdentifierType identifier);

    // Initialisiert eine neue Aktivität.
    protected void onCreate(int menu, Bundle savedInstanceState) {
        m_menu = menu;

        super.onCreate(savedInstanceState);

        // Liste einrichten
        getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);
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

    // Ändert ein existierendes Element oder legt ein neues an.
    protected boolean startEdit(TIdentifierType id) {
        // Die Art der Aktivität zum Ändern, extrahiert aus den generischen Parametern der Basisklasse
        Class editActivity = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];

        // Aufruf vorbereiten
        Intent openForEdit = new Intent(this, editActivity);
        openForEdit.putExtra(EditActivity.EXTRA_IDENTIFIER, id);

        // Aktivität zum Ändern starten
        startActivityForResult(openForEdit, RESULT_EDIT_ITEM);

        return true;
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
        switch (item.getItemId()) {
            case R.id.action_new:
                // Nach Auswahl aus der ActionBar wird das Neuanlegen angestossen
                startEdit(null);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Liest die Elemente der Liste aus der lokalen Datenbank.
    protected void load() {
        new AsyncTask<Void, Void, JSONObject[]>() {
            private TAdapterType getAdapter() {
                return (TAdapterType) ListActivity.this.getListAdapter();
            }

            @Override
            protected JSONObject[] doInBackground(Void... params) {
                // Daten aus der lokalen Datenbank auslesen
                return getAdapter().load();
            }

            @Override
            protected void onPostExecute(JSONObject[] items) {
                super.onPostExecute(items);

                // Daten übernehmen
                getAdapter().refresh(items);
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // ActionBar erzeugen
        getMenuInflater().inflate(m_menu, menu);

        // Standardbeschriftung setzen
        menu.findItem(R.id.action_new).setTitle(R.string.action_new);

        return true;
    }
}
