package de.jochen_manns.buyitv0;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

class Tools {
    public static String responseStreamToString(InputStream stream) throws IOException {
        if (stream == null)
            throw new NullPointerException("stream");

        ArrayList<byte[]> response = new ArrayList<byte[]>();
        int total = 0;

        for (; ; ) {
            byte[] part = new byte[10000];
            int partLen = stream.read(part);
            if (partLen < 1)
                break;

            if (partLen < part.length) {
                byte[] scratch = new byte[partLen];
                System.arraycopy(part, 0, scratch, 0, scratch.length);

                part = scratch;
            }

            response.add(part);

            total += part.length;
        }

        byte[] all = new byte[total];
        int pos = 0;

        for (byte[] part : response) {
            System.arraycopy(part, 0, all, pos, part.length);

            pos += part.length;
        }

        return new String(all, "UTF-8");
    }

    private static final TimeZone UniversalTimeZone = TimeZone.getTimeZone("UTC");

    private static final String IsoDateTimeFormat = "yyyy-MM-dd'T'HH:mm:ssZZ";

    private static final String[] IsoDateTimeFormatArray = {IsoDateTimeFormat};


    public static String dateToISOString(Date date) {
        if (date == null)
            return null;

        SimpleDateFormat df = new SimpleDateFormat(IsoDateTimeFormat);

        df.setTimeZone(UniversalTimeZone);

        return df.format(date);
    }

    public static Date dateFromISOString(String date) throws DateParseException
    {
        if (date == null)
            return null;

        return DateUtils.parseDate(date, IsoDateTimeFormatArray);
    }

    public static String getStringFromJSON(JSONObject json, String name) throws JSONException {
        return json.isNull(name) ? null : json.getString(name);
    }
}
