package de.jochen_manns.buyitv0;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

abstract class JsonRequestTask extends AsyncTask<String, Void, JSONObject> {
    protected final static String REQUEST_USER = "userid";

    protected final Context Context;

    private final String m_endPoint;

    protected JsonRequestTask(Context context, String webService) {
        m_endPoint = "http://mobile.psimarron.net/BuyIt/" + webService;
        Context = context;
    }

    protected abstract void fillRequest(JSONObject postData) throws JSONException;

    protected abstract void processResponse(JSONObject postReply) throws JSONException;

    public void start() {
        execute(m_endPoint);
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        try {
            JSONObject postData = new JSONObject();
            fillRequest(postData);

            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestProperty("content-type", "application/json; charset=utf-8");
                conn.setDoOutput(true);

                OutputStream out = conn.getOutputStream();
                try {
                    out.write(postData.toString().getBytes("UTF-8"));
                } finally {
                    out.close();
                }

                InputStream in = conn.getInputStream();
                try {
                    String responseString = Tools.responseStreamToString(in);
                    JSONObject response = (JSONObject) new JSONTokener(responseString).nextValue();
                    if (response != null)
                        processResponse(response);

                    return response;
                } finally {
                    in.close();
                }
            } finally {
                conn.disconnect();
            }
        } catch (IOException e) {
            return null;
        } catch (JSONException e) {
            return null;
        }
    }
}
