package de.jochen_manns.buyitv0;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ListView;

// Eine Liste mit der Unterstützung von Gesten.
public class TouchableListView extends ListView implements GestureDetector.OnGestureListener {

    // Wir erlauben auch einige Gesten
    private GestureDetector m_gestures;

    // Gesetzt während einer horizontalen Bewegungsgeste.
    private boolean m_swipe;

    // Der Schwellwert für das Erkennen einer Bewegung.
    private int m_threshold;

    // Die horizontale Position beim Starten einer Geste.
    private float m_downX;

    // Die Nummer des Elementes beim Starten einer Geste.
    private int m_downPosition;

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

    private void initialize(Context context) {
        ViewConfiguration configuration = ViewConfiguration.get(context);

        // Überwachung von Gesten aktivieren
        m_threshold = configuration.getScaledTouchSlop();
        m_gestures = new GestureDetector(context, this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            // Eine Geste beginnt
            case MotionEvent.ACTION_DOWN:
                m_downPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
                m_downX = ev.getX();
                m_swipe = false;
                break;
            // Eine Geste wird beendet
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                m_swipe = false;
                break;
            // Eine Bewegung findet statt
            case MotionEvent.ACTION_MOVE:
                // Wir haben bereits eine Bewegung erkannt
                if (m_swipe)
                    return true;

                // Nur prüfen, wenn wir auf einem Element der Liste angefangen haben
                if (m_downPosition < 0)
                    break;

                // Abstand ermitteln
                float delta = Math.abs(m_downX - ev.getX());

                // Und Überwachung aktivieren
                return m_swipe = (delta >= m_threshold);
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        m_gestures.onTouchEvent(ev);

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
        if (m_downPosition < 0)
            return false;

        return true;
    }
}
