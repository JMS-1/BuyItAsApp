package de.jochen_manns.buyitv0;

import android.widget.BaseAdapter;

/*
    Eigentliche eine Schnittstelle, hier als abstrakte Basisklasse umgesetzt.
    Die angebotenen Methoden unterstützen die Basisklasse der Auswahllisten
    bei dem Zugriff auf die Daten.
 */
abstract class ItemAdapter extends BaseAdapter {
    // Fordert eine Aktualisierung der Liste gemäß dem aktuellen Stand der Datenbank an
    public abstract void refresh();
}
