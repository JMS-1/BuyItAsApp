package de.jochen_manns.buyitv0;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

class LogonTask extends JsonRequestTask {
    private final String m_userIdentifier;

    public LogonTask(Context request, String requestIdentifier) {
        super(request, "user.php");

        m_userIdentifier = requestIdentifier;
    }

    protected void fillRequest(JSONObject postData) throws JSONException {
        postData.put(REQUEST_USER, m_userIdentifier);
    }

    protected void processResponse(JSONObject postReply) throws JSONException {
        User.save(m_userIdentifier, postReply, Context);
    }
}
