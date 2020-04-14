package com.notemasterv10.takenote;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PointCollector implements View.OnTouchListener {

    private List<Point> points_array = new ArrayList<Point>();
    private PointCollectorListener pointCollectorListener;

    public PointCollectorListener getPointCollectorListener() {
        return pointCollectorListener;
    }
    // The PointCollectorListener is set from outside this class (by another class). If it is
    // not set, it will be a null reference. This is checked when running the onTouchListener
    // in this class. If the refrence is not null it will execute the method and pass the
    // collected points. Actually ImageActivity is passed to here (it implements the interface!).
    public void setPointCollectorListener(PointCollectorListener pointCollectorListener) {
        this.pointCollectorListener = pointCollectorListener;
    }

    public boolean onTouch(View v, MotionEvent event) {

        // TODU get some graphic acknowledgement of the point being pressed

        float x = event.getX();
        float y = event.getY();
        Log.d("Dubug-DB", "X " + String.valueOf(x) + " Y " + String.valueOf(y));
        points_array.add(new Point((int) x, (int) y));
        Log.d("Debug-DB", "Array size = " + String.valueOf(points_array.size()));
        if(points_array.size() == 4) {
            if (pointCollectorListener != null) {
                pointCollectorListener.pointsCollected(points_array); // this method is actually an interface method overridden in ImageActivity
                                                                      // Also check ImageActivity.java and PointCollectorListener.java (interface)
            }
            //points_array.clear(); better practise to do this after the points are saved to the db. The save is asynchronous
            // so the array if passed by reference may be emptied before it is saved (?). Anyway the array is passed as a final
            // because it is used in a separate thread and in order to use it like that it ahs t obe passed as a final; so it
            // cannot be altered. We have ti facilitate a public method here where it is not final. See below:
        }
        return false;
    }

    public void clearPoints() {
        points_array.clear();
    }
}
