package de.jochen_manns.buyitv0;

/*
    Beschreibt den Bearbeitungsstand eines Produktes.
 */
enum ProductStates {
    // Das Produkt wurde lokal neu angelegt und ist im Online Datenbestand noch nicht vorhanden.
    NewlyCreated,

    // Das Produkt ist zum Löschen markiert und wird in der Hauptaktivität nicht mehr angezeigt. Es muss aber eventuell noch im Online Datenbestand gelöscht werden.
    Deleted,

    // Das Produkt existiert auch im Online Datenbestand und wurde lokal verändert.
    Modified,

    // Das Produkt ist identisch zum Online Datenbestand.
    Unchanged
}

