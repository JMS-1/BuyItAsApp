package de.jochen_manns.buyitv0;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/*
    Einige kleine Hilsmethoden zum Arbeiten mit JSON Repräsentationen.
 */
class xJsonTools {
    // Die globale Zeitzone.
    private static final TimeZone UniversalTimeZone = TimeZone.getTimeZone("UTC");

    // Das Protokollformat für Datumswerte mit Uhrzeit.
    private static final String IsoDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZZ";

    // Wandelt einen Datumswert in eine universelle Textdarstellung.
    public static String dateToISOString(Date date) {
        // Es wurde kein Datumswert übermittelt
        if (date == null)
            return null;

        // Formatierung vorbereiten
        SimpleDateFormat df = new SimpleDateFormat(IsoDateTimeFormat);

        // Globale Zeitzone verwenden
        df.setTimeZone(UniversalTimeZone);

        // Umwandung durchführen
        return df.format(date);
    }

    // Ermittelt eine Zeichenkette aus der JSON Repräsentation, wobei korrekt null Werte berücksichtigt werden.
    public static String getStringFromJSON(JSONObject json, String name) throws JSONException {
        return json.isNull(name) ? null : json.getString(name);
    }
}
