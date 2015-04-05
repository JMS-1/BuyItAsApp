package de.jochen_manns.buyitv0;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Führt einen Web Service Aufruf zur Verifikation der Benutzeridentifikation
    durch. Im Erfolgsfall wird der Online gespeicherte Name des Benutzers
    gemeldet.
 */
class LogonTask extends JsonRequestTask {
    // Die Benutzerregistrierung, die verifiziert werden soll.
    private final String m_userIdentifier;

    // Erstellt einen neuen Aufruf.
    public LogonTask(ProductList request, String requestIdentifier) {
        super(request, "user.php");

        m_userIdentifier = requestIdentifier;
    }

    // Als Eingangsparameter wird die zu überprüfende Registrierung verwendet.
    protected void fillRequest(JSONObject postData) throws JSONException {
        postData.put(REQUEST_USER, m_userIdentifier);
    }

    // Die Antwort enthält dann nur noch den Namen des Benutzers, wenn dieser bekannt ist.
    protected void processResponse(JSONObject postReply) throws JSONException {
        // Wird kein Name gemeldet, so ist kein Benutzer zur Registrierung bekannt
        String userName = JsonTools.getStringFromJSON(postReply, User.Name_Name);
        if ((userName == null) || (userName.length() < 1))
            return;

        // Der Name eines bekannten Anwenders wird peristent gespeichert
        User.save(m_userIdentifier, userName, Context);

        // Synchronisation anfordern
        ((ProductList) Context).synchronize(true);
    }
}
