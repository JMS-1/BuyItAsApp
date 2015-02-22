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

/*
    Die Basisklasse für die Aktivitäten zum Ändern der Daten der Entitäten
    der App - im Moment sind das Produkte und Märkte.
 */
public abstract class EditActivity<TIdentifierType extends Serializable, TProtocolType> extends Activity {

    // Die Intentdaten mit der eindeutigen Identifikation der betroffenen Entität.
    public static final String EXTRA_IDENTIFIER = "id";

    // Das Eingabefeld mit dem Namen der Entität.
    private EditText m_name;

    // Die eindeutige Identifikation der Entität - oder null, wenn eine neue Entität angelegt wird.
    private TIdentifierType m_identifier;

    // Die zu verwendende ActionBar.
    private int m_menu;

    // Meldet, ob der aktuelle Name gültig ist.
    protected abstract boolean isValidName(Editable newName);

    // Ermittelt Informationen zum aktuellen Änderungsvorgang, in einfachsten Fall die aktuelle Entität.
    protected abstract TProtocolType queryItem(Database database, TIdentifierType item) throws JSONException;

    // Beendet die Initialisierung der Aktivität auf Basis der via queryItem ermittelten Informationen.
    protected abstract void initializeFromItem(TProtocolType item);

    // Entfernt eine existierende Entität.
    protected abstract void deleteItem(Database database, TIdentifierType item);

    // Ändert eine existierende Entität oder legt eine neue an.
    protected abstract void updateItem(Database database, TIdentifierType item);

    // Meldet die Überschrift der Aktivität.
    protected abstract int getTitle(boolean forNew);

    // Meldet den aktuellen Namen der Entität, so wie er in der Oberfläche angezeigt wird.
    protected Editable getName() {
        return m_name.getText();
    }

    // Überträgt einen Namen in die Oberfläche.
    protected void setName(String name) {
        m_name.setText(name);
    }

    // Meldet die aktuelle eindeutige Identifikation der Entität.
    protected TIdentifierType getIdentifier() {
        return m_identifier;
    }

    // Initialisiert eine neue Aktivität.
    protected void onCreate(int layout, int menu, Bundle savedInstanceState) {
        // ActionBar vermerken
        m_menu = menu;

        // Framework initialisieren
        super.onCreate(savedInstanceState);

        // Oberfläche laden
        setContentView(layout);

        // Wir wollen soviel Platz in der ActionBar wie möglich und verzichten daher auf das Icon der App
        getActionBar().setIcon(android.R.color.transparent);

        // Das Eingabefeld mit dem Namen der Enttität
        m_name = (EditText) findViewById(R.id.edit_name);

        // Ein Aufruf der Aktivität ist nur (intern) über einen Intent möglich
        Intent startInfo = getIntent();
        if (startInfo == null) {
            finish();
            return;
        }

        // Bei dem Aufruf muss immer eine eindeutige Identifikation mitgegeben werden
        if (!startInfo.hasExtra(EXTRA_IDENTIFIER)) {
            finish();
            return;
        }

        // Die Art der eindeutigen Identifikation ist über eine Typparameter der Hilfsklasse abstrahiert
        m_identifier = (TIdentifierType) startInfo.getSerializableExtra(EXTRA_IDENTIFIER);

        // Abhängig von der eindeutigen Identifikation wird die Überschrift der Aktivität festgelegt
        setTitle(getTitle(m_identifier == null));

        // Änderungen des Namens schlagen sich in den Menüpunkten der ActionBar nieder
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

        // Der Abruf der Daten für den Vorgang erfolg asynchron
        new AsyncTask<Void, Void, TProtocolType>() {
            @Override
            protected TProtocolType doInBackground(Void... params) {
                // Zugriff auf die lokale Datenbank vorbereiten
                Database database = Database.create(EditActivity.this);
                try {
                    // Initialisierunginformationen abrufen
                    return queryItem(database, m_identifier);
                } catch (Exception e) {
                    // Alle Fehler werden ignoriert
                    return null;
                } finally {
                    database.close();
                }
            }

            @Override
            protected void onPostExecute(TProtocolType jsonObject) {
                super.onPostExecute(jsonObject);

                // Initialisierung abschliessen
                initializeFromItem(jsonObject);
            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Das Erzeugen der Datenbankhilfsklasse ist unkritisch, solange wir keine Operationen ausführen
        Database database = Database.create(EditActivity.this);
        try {
            switch (item.getItemId()) {
                case R.id.button_save:
                    // Anlegen oder ändern
                    updateItem(database, m_identifier);
                    break;
                case R.id.button_delete:
                    // Entfernen
                    deleteItem(database, m_identifier);
                    break;
                default:
                    // Alles was wir nicht kennen
                    return super.onOptionsItemSelected(item);
            }
        } finally {
            database.close();
        }

        // Vermutlich wurde eine Änderung durchgeführt
        setResult(RESULT_OK);

        // Diese Aktivität kann nun beendet werden
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Gewünschte ActionBar laden
        getMenuInflater().inflate(m_menu, menu);

        MenuItem saveItem = menu.findItem(R.id.button_save);
        MenuItem deleteItem = menu.findItem(R.id.button_delete);

        // Menüpunkt zum Ändern respektive Anlegen
        saveItem.setTitle((m_identifier == null) ? R.string.button_new : R.string.button_save);
        saveItem.setEnabled(isValidName(getName()));

        // Menüpunkt zum Löschen
        if (m_identifier == null)
            deleteItem.setVisible(false);
        else
            deleteItem.setTitle(R.string.button_delete);

        return true;
    }
}
