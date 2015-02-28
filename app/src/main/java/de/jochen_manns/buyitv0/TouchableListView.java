package de.jochen_manns.buyitv0;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;

// Eine Liste mit der Unterstützung von Gesten.
public class TouchableListView extends ListView implements GestureDetector.OnGestureListener {

    // Wir erlauben auch einige Gesten
    private GestureDetector m_gestures;

    // Erstellt eine neue Liste.
    public TouchableListView(Context context) {
        super(context);

        // Überwachung der Gesten vorbereiten
        m_gestures = new GestureDetector(context, this);
    }

    // Erstellt eine neue Liste.
    public TouchableListView(Context context, AttributeSet attributes) {
        super(context, attributes);

        // Überwachung der Gesten vorbereiten
        m_gestures = new GestureDetector(context, this);
    }

    // Erstellt eine neue Liste.
    public TouchableListView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);

        // Überwachung der Gesten vorbereiten
        m_gestures = new GestureDetector(context, this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        m_gestures.onTouchEvent(ev);

        return super.onInterceptTouchEvent(ev);
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
        return false;
    }
}
