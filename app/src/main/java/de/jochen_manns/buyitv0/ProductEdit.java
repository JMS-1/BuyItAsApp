package de.jochen_manns.buyitv0;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
    Die Aktivität zum Ändern der Daten eines existierenden Produktes respektive
    zum Anlegen eines neuen Produktes.
 */
public class ProductEdit extends EditActivity<Long, JSONObject> {

    private static final Pattern m_dateReg = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$");

    // Die Ergebniskennung für die Auswahl eines Marktes.
    private static final int RESULT_SELECT_MARKET = 1;

    // Das Eingabefeld mit der Beschreibung des Produktes.
    private EditText m_description;

    // Die Schaltfläche zur Auswahl eines Marktes.
    private TextView m_market;

    // Umschalter für dauerhafte Einträge.
    private Switch m_permanent;

    // Datum (einschließlich) von dem an der Eintrag relevant ist.
    private EditText m_from;

    // Datum (einschließlich) bis zu dem der Eintrag relevant ist.
    private EditText m_to;

    // Optionale Gruppe zu dem der Eintrag gehört.
    private EditText m_category;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.activity_product_edit, R.menu.menu_product_edit, savedInstanceState);

        // Der Markt wird ausgewählt, nicht eingegeben
        m_market = findViewById(R.id.edit_product_market);
        m_description = findViewById(R.id.edit_product_description);
        m_permanent = findViewById(R.id.edit_product_permanent);
        m_from = findViewById(R.id.edit_product_from);
        m_to = findViewById(R.id.edit_product_to);
        m_category = findViewById(R.id.edit_product_category);

        m_market.setText(R.string.editSelect_item_nomarket);
        m_market.setTag(null);
        m_market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ruft die Aktivität zur Auswahl eines Marktes aus - die aktuelle Auswahl wird dabei mit übergeben
                Intent showSelector = new Intent(ProductEdit.this, MarketList.class);
                showSelector.putExtra(MarketList.EXTRA_MARKET_NAME, (String) m_market.getTag());
                startActivityForResult(showSelector, RESULT_SELECT_MARKET);
            }
        });

        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(Locale.GERMANY);
        configuration.setLayoutDirection(Locale.GERMANY);

        createConfigurationContext(configuration);

        AddDatePicker(R.id.edit_product_from_label, m_from);
        AddDatePicker(R.id.edit_product_to_label, m_to);
    }

    private void AddDatePicker(int labelId, EditText edit) {
        Context me = this;

        TextView label = findViewById(labelId);

        label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(me, R.style.DatePickerStyle);

                datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString( R.string.datepicker_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatePicker picker = datePickerDialog.getDatePicker();

                        edit.setText(MessageFormat.format(
                                "{0,number,0000}-{1,number,00}-{2,number,00}",
                                picker.getYear(),
                                1 + picker.getMonth(),
                                picker.getDayOfMonth()));
                    }
                });

                datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString( R.string.datepicker_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edit.setText(null);
                    }
                });

                try {
                    String date = edit.getText().toString();
                    Matcher matcher = m_dateReg.matcher(date);

                    if (matcher.find()) {
                        int year = Integer.parseInt(matcher.group(1), 10);
                        int month = Integer.parseInt(matcher.group(2), 10);
                        int day = Integer.parseInt(matcher.group(3), 10);

                        datePickerDialog.updateDate(year, month - 1, day);
                    }
                } catch (Exception e) {
                    // Alle Fehler einfach ignorieren.
                }

                datePickerDialog.show();
            }
        });
    }

    @Override
    protected int getTitle(boolean forNew) {
        // Die Überschrift der Aktivität hängt von der Aufrufsituation ab
        return forNew ? R.string.product_edit_create : R.string.product_edit_modify;
    }

    @Override
    protected boolean isValidName(String newName) {
        // Der Name eines Produktes darf nicht leer sein
        return !newName.isEmpty();
    }

    @Override
    protected JSONObject queryItem(Database database, Long identifier) throws JSONException {
        // Ermittelt die Daten des zu bearbeitenden Produktes - sofern keines neu angelegt werden soll
        return identifier == null ? null : Products.query(database, identifier);
    }

    @Override
    protected void initializeFromItem(JSONObject item) {
        // Wenn wir ein neues Produkt anlegen, brauchen wir nichts zu tun
        if (item == null)
            return;

        try {
            // Wenn ein Markt zugeordnet ist, so wird dieser vermerket
            String market = Products.getMarket(item);
            if (market != null && !market.isEmpty()) {
                m_market.setText(market);
                m_market.setTag(market);
            }

            // Name und Beschreibung können direkt übernommen werden
            m_description.setText(Products.getDescription(item));
            m_from.setText(Products.getFrom(item));
            m_to.setText(Products.getTo(item));
            m_permanent.setChecked(Products.getPermanent(item));
            m_category.setText(Products.getCategory(item));

            setName(Products.getName(item));
        } catch (Exception e) {
            // Fehler werden letztlich ignoriert
            setName("### ERROR ###");
        }
    }

    @Override
    protected void updateItem(Database database, Long identifier) {
        // Daten aus der Oberfläche auslesen - man beachte, dass für den Markt der Anzeigename vom gespeicherten Namen abweicht, wenn kein Markt zugeordnet wurde
        String description = m_description.getText().toString();

        // Daten in die lokale Datenbank übertragen
        Products.update(
                database,
                identifier,
                getName(),
                description.isEmpty() ? null : description,
                (String) m_market.getTag(),
                m_from.getText().toString(),
                m_to.getText().toString(),
                m_category.getText().toString(),
                m_permanent.isChecked()
        );
    }

    @Override
    protected void deleteItem(Database database, Long identifier) {
        Products.delete(database, identifier);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Ohne Antwortdaten können wir gar nichts tun
        if (data == null)
            return;

        switch (requestCode) {
            case RESULT_SELECT_MARKET:
                // Wenn eine Auswahl stattgefunden hat, müssen wir diese respektieren
                if (resultCode == RESULT_OK) {
                    // Den Anzeigenamen auf die Schaltfläche übertragen - der Name, der auch null sein kann, wird im Tag verwaltet
                    String market = data.getStringExtra(MarketList.EXTRA_MARKET_NAME);

                    m_market.setTag(market);

                    // Den Anzeigenamen der Schaltfläche entsprechend anpassen - eventuell abweichend von der tatsächlichen Auswahl
                    if (market == null)
                        m_market.setText(R.string.editSelect_item_nomarket);
                    else
                        m_market.setText(market);
                }
                break;
        }
    }
}
