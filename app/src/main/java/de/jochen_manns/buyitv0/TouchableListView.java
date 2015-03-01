package de.jochen_manns.buyitv0;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListView;

/*
    Eine Liste mit der optionalen Unterstützung von Gesten.
 */
public class TouchableListView extends ListView implements GestureDetector.OnGestureListener {

    // Wir erlauben auch einige Gesten und diese Hilfsklasse wertet Elementargesten aus.
    private GestureDetector m_gestures;

    // Gesetzt während einer horizontalen Bewegungsgeste.
    private boolean m_swipe;

    // Der Schwellwert für das Erkennen einer horizontalen Bewegung.
    private int m_threshold;

    // Die horizontale Position beim Starten einer Geste.
    private float m_downX;

    // Die Nummer des Elementes beim Starten einer Geste.
    private int m_downPosition;

    // Gesetzt, wenn Gesten überwacht werden sollen - das ist die Voreinstellung.
    private boolean m_inspectTouch = true;

    // Erstellt eine neue Liste.
    public TouchableListView(Context context) {
        super(context);
        initialize(context);
    }

    // Erstellt eine neue Liste.
    public TouchableListView(Context context, AttributeSet attributes) {
        super(context, attributes);
        initialize(context);
    }

    // Erstellt eine neue Liste.
    public TouchableListView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        initialize(context);
    }

    // Initialisiert die Verwaltung der Gesten.
    private void initialize(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);

        // Überwachung von Gesten vorbereiten
        m_threshold = configuration.getScaledTouchSlop();
        m_gestures = new GestureDetector(context, this);
    }

    // Deaktiviert die Auswertung von Gesten.
    public void disableGestures() {
        // Diese Liste wird nun keine eigene Auswertung von Gesten vornehmen
        m_inspectTouch = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Wenn wir Gesten auswerten dürfen, dann schauen wir uns an, was der Anwender von uns will
        if (m_inspectTouch)
            switch (ev.getAction()) {
                // Eine interessante Geste beginnt
                case MotionEvent.ACTION_DOWN:
                    m_downX = ev.getX();
                    m_downPosition = pointToPosition((int) m_downX, (int) ev.getY());
                    m_swipe = false;
                    break;
                // Eine Geste wird beendet
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    m_downPosition = -1;
                    m_swipe = false;
                    break;
                // Eine Bewegung findet statt
                case MotionEvent.ACTION_MOVE:
                    // Sind wird nicht aktiv müssen wir prüfen, ob eine interessante Geste vorliegt
                    if (!m_swipe) {
                        // Nur prüfen, wenn wir auf einem Element der Liste angefangen haben
                        if (m_downPosition < 0)
                            break;

                        // Den horizontalen Abstand ermitteln
                        float delta = Math.abs(m_downX - ev.getX());

                        // Und bei Bedarf die eigene Auswertung der Geste aktivieren
                        m_swipe = (delta >= m_threshold);
                    }

                    // Wir übernehmen die Auswertung der Geste
                    if (m_swipe)
                        return true;
                    break;
            }

        // Das darf dann die Basisklasse machen
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Eigene Auswertung der Geste.
        if (m_swipe)
            m_gestures.onTouchEvent(ev);

        // Wieder muss die Basisklasse ran
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // Erst einmal schauen, ob wir auf einem Listenelement angefangen haben - tatsächlich sollte die Implementierung keine andere Nutzung zulassen.
        if (m_downPosition < 0)
            return true;

        // Aber nur, wenn wir auch auf dem letzten Element gelandet sind
        int position = pointToPosition((int) e2.getX(), (int) e2.getY());
        if (position != m_downPosition)
            return true;

        // Dann teilen wir das einfach unserer Listenverwaltung mit.
        ItemAdapter adapter = (ItemAdapter) getAdapter();
        adapter.moveItem(m_downPosition, velocityX < 0);

        // Eine weitere Auswertung ist nicht notwendig
        return true;
    }
}
