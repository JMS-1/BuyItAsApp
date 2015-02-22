package de.jochen_manns.buyitv0;

import android.content.Context;
import android.content.SharedPreferences;

class User {
    public final static String PREFERENCES_NAME = "UserInformationXXX";

    public static final String Name_Identifier = "userid";

    public static final String Name_Name = "name";

    public final String Identifier;

    public final String Name;

    private User(String userId, String userName) {
        Identifier = userId;
        Name = userName;
    }

    public static User load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);

        String userId = preferences.getString(Name_Identifier, null);
        if (userId == null)
            return null;

        String userName = preferences.getString(Name_Name, userId);

        return new User(userId, userName);
    }

    public static void save(String userIdentifier, String userName, Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);

        SharedPreferences.Editor changes = preferences.edit();
        changes.putString(Name_Identifier, userIdentifier);
        changes.putString(Name_Name, userName);
        changes.commit();
    }
}
