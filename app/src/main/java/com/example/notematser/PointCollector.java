package com.example.notematser;

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
    // collected points.
    public void setPointCollectorListener(PointCollectorListener pointCollectorListener) {
        this.pointCollectorListener = pointCollectorListener;
    }

    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Log.d("Dubug-DB", "X " + String.valueOf(x) + " Y " + String.valueOf(y));
        points_array.add(new Point((int) x, (int) y));
        Log.d("Debug-DB", "Array size = " + String.valueOf(points_array.size()));
        if(points_array.size() == 4) {
            if (pointCollectorListener != null) {
                pointCollectorListener.pointsCollected(points_array);
            }
            points_array.clear();
        }
        return false;
    }
}
