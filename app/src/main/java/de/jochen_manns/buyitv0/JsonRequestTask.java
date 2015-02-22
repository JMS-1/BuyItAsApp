package de.jochen_manns.buyitv0;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
    Mit Hilfe dieser Basisklasse werden alle Zugriff auf den Web Service der
    App abgewickelt.
 */
abstract class JsonRequestTask extends AsyncTask<String, Void, JSONObject> {
    // Alle Aufrufe müssen den Registrierungsscchlüssel des Anwenders über eine JSON Eigenschaft mit diesem Namen übermitteln.
    protected final static String REQUEST_USER = "userid";

    // Die Aktivität zum Aufruf.
    protected final Context Context;

    // Der Name des zu verwendenden Web Services.
    private final String m_endPoint;

    // Initialisiert einen neuen Web Service Aufruf.
    protected JsonRequestTask(Context context, String webService) {
        m_endPoint = "http://mobile.psimarron.net/BuyIt/" + webService;
        Context = context;
    }

    // Bereitet die Aufrufdaten vor.
    protected abstract void fillRequest(JSONObject postData) throws JSONException;

    // Bearbeitet die Antwortdaten.
    protected abstract void processResponse(JSONObject postReply) throws JSONException;

    // Führt den Aufruf aus.
    public void start() {
        execute(m_endPoint);
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        try {
            // Aufrufdaten vorbereiten
            JSONObject postData = new JSONObject();
            fillRequest(postData);

            // HTTP Aufruf vorbereiten
            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                // Übertragung der Aufrufdaten vorbereiten
                conn.setRequestProperty("content-type", "application/json; charset=utf-8");
                conn.setDoOutput(true);

                // Aufrufdaten übertragen
                OutputStream out = conn.getOutputStream();
                try {
                    out.write(postData.toString().getBytes("UTF-8"));
                } finally {
                    out.close();
                }

                // Aufruf an den Web Service durchführen
                InputStream in = conn.getInputStream();
                try {
                    // Antwortdaten in JSON wandeln
                    String responseString = Tools.responseStreamToString(in);
                    JSONObject response = (JSONObject) new JSONTokener(responseString).nextValue();

                    // Antwortdaten auswerten
                    if (response != null)
                        processResponse(response);

                    return response;
                } finally {
                    in.close();
                }
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            // Alle Fehler werden ignoriert
            return null;
        }
    }
}
