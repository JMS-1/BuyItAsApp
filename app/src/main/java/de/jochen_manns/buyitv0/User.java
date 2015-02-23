package de.jochen_manns.buyitv0;

import android.content.Context;
import android.content.SharedPreferences;

/*
    Verwaltet die Anmeldeinformationen eines Benutzers.
 */
class User {
    // Die lokale Ablage für die Benutzerdaten.
    public final static String PREFERENCES_NAME = "UserInformation";

    // Der Name der Benutzerkennung, auch als JSON Eigenschaft verwendet.
    public static final String Name_Identifier = "userid";

    // Der Name des Benutzernamens, auch als JSON Eigenschaft verwendet.
    public static final String Name_Name = "name";

    // Die Benutzerkennung.
    public final String Identifier;

    // Der Name des Benutzers.
    public final String Name;

    // Erstellt eine neue Benutzerinformation.
    private User(String userId, String userName) {
        Identifier = userId;
        Name = userName;
    }

    // Ermittelt die lokal abgelegten Benutzerinformationen.
    public static User load(Context context) {
        // Auslesen aus der lokalen Ablage
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);

        // Prüfen, ob Informationen vorhanden sind
        String userId = preferences.getString(Name_Identifier, null);
        if (userId == null)
            return null;

        // Name des Benutzers auslesen
        String userName = preferences.getString(Name_Name, userId);

        // Information erzeugen und melden
        return new User(userId, userName);
    }

    // Verändert die lokal abgelegten Benutzerinformationen.
    public static void save(String userIdentifier, String userName, Context context) {
        // Lokale Ablage abrufen
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, 0);

        // Änderung eintragen
        SharedPreferences.Editor changes = preferences.edit();
        changes.putString(Name_Identifier, userIdentifier);
        changes.putString(Name_Name, userName);

        // Änderung durchführen
        changes.commit();
    }
}
