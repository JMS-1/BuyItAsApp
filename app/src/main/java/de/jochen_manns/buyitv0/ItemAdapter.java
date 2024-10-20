package de.jochen_manns.buyitv0;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

/*
    Hilfsklasse zur Implementierung unser Auswahllisten von Produkten
    und Märkten.
 */
abstract class ItemAdapter extends BaseAdapter implements View.OnClickListener, View.OnTouchListener {
    // Erzeugt Views.
    private final LayoutInflater m_inflater;

    // Die verwalteten Elemente.
    protected JSONObject[] m_items = null;

    // Initialisiert eine neue Verwaltung.
    protected ItemAdapter(ListActivity<?, ?, ?> context) {
        m_inflater = LayoutInflater.from(context);
    }

    // Fordert eine Aktualisierung der Liste gemäß dem aktuellen Stand der Datenbank an.
    public JSONObject[] load(String order) {
        return load(order, null, null);
    }

    public abstract JSONObject[] load(String order, String filterField, String filterValue);

    // Übernimmt die neuen Elemente.
    public void refresh(JSONObject[] items) {
        // Liste erneuern
        m_items = items;

        // Alle Interessenten über die neuen Daten informieren
        notifyDataSetChanged();
    }

    // Bereitet ein visuelles Element zur Anzeige vor.
    protected abstract boolean initializeTextView(TextView text, JSONObject item) throws JSONException;

    protected void initializeEditView(ImageView edit, JSONObject item) throws JSONException {
    }

    // Ermittelt einen optionalen Text vor der eigentlichen Bezeichnung.
    protected String getPrefixText(JSONObject item) throws JSONException {
        return "";
    }

    // Meldet die Aktivität, in der die Liste der Elemente tatsächlich angezeigt wird..
    protected ListActivity<?, ?, ?> getContext() {
        return (ListActivity<?, ?, ?>) m_inflater.getContext();
    }

    // Erstellt einen Zugriff auf die lokale Datenablage.
    protected Database createDatabase() {
        return Database.create(getContext());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Wird ein neuer View benötigt, so erzeugen wir diesen hier - bei einer Wiederverwendung müssen wir nur initialisieren
        if (convertView == null)
            convertView = m_inflater.inflate(R.layout.editable_list_item, parent, false);

        // Zielansichten und Quelldaten ermitteln
        TextView textView = convertView.findViewById(R.id.listitem_text);
        TextView fromView = convertView.findViewById(R.id.listitem_from);
        ImageView editView = convertView.findViewById(R.id.listitem_edit);
        JSONObject item = (JSONObject) getItem(position);

        // Jedes visuelle Element weiß, mit welchem Element es verbunden ist
        fromView.setTag(position);
        textView.setTag(position);
        editView.setTag(position);

        // Das Anhängen der Listener könnte man auch nur einmalig machen, aber schaden tut es kaum
        textView.setOnClickListener(this);
        fromView.setOnClickListener(this);
        editView.setOnClickListener(this);
        editView.setOnTouchListener(this);

        // Daten in die Anzeige übertragen und bei Bedarf die Änderungsoption ausblenden
        try {
            int visible = initializeTextView(textView, item) ? View.VISIBLE : View.INVISIBLE;

            editView.setVisibility(visible);

            if (visible == View.VISIBLE) {
                fromView.setText(getPrefixText(item));

                initializeEditView(editView, item);
            }
        } catch (Exception e) {
            // Fehler werden letztlich alle ignoriert
            textView.setText("### ERROR ###");
        }

        return convertView;
    }

    // Verschiebt ein Listenelement.
    public void moveItem(int position, boolean moveLeft) {
        // Was nicht geht
        if (moveLeft && (position <= 0))
            return;
        else if (!moveLeft && (position >= getCount() - 1))
            return;

        // Verschieben ist einfach ein Vertauschen der Ordnung
        getContext().onSwapWithNext(moveLeft ? (position - 1) : position);
    }

    @Override
    public void onClick(View v) {
        Integer position = (Integer) v.getTag();
        JSONObject item = (JSONObject) getItem(position);

        // Anfrage je nach Kontext an die Liste durchreichen
        if (v instanceof TextView)
            getContext().onClick(item);
        else
            getContext().onEdit(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Wir wollen auch ein bisschen visuelles Feedback haben, die Steuerung erfolgt über eine StateListDrawable
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setActivated(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                v.setActivated(false);
                break;
        }

        return false;
    }
}
