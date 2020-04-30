package com.notemasterv10.takenote;

import android.graphics.Point;
import android.media.Image;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;

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

        ImageView image = (ImageView) v.getRootView().findViewById(R.id.imageview_counter);
        ImageView image_background = (ImageView) v.getRootView().findViewById(R.id.imageView);

        switch(points_array.size()){
            case 0:
                // show picture number 1
                image.setImageResource(R.mipmap.no_1a);
                break;
            case 1:
                // show picture number 2
                image.setImageResource(R.mipmap.no_2a);
                break;
            case 2:
                // show picture number 3
                image.setImageResource(R.mipmap.no_3a);
                break;
            case 3:
                // show picture number 4
                image.setImageResource(R.mipmap.no_4a);
                break;
            default:
                // do nothing
                break;
        }

        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            // touch release, set return value to true to be able to manipulate actions -->
            try { //delay disappearance of image a little bit so it lingers
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            image.setVisibility(View.INVISIBLE);
            return true;
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) image.getLayoutParams();
        //Initial location relative to the touch point coordinates (left lower corner) -->
        int iLeft = (int) ((int) event.getX() - ((int) image.getWidth() * 1.2));
        // If the touch point coordinates are close to the left border switch location (to lower right corner) -->
        if (iLeft <= 0) {
            iLeft = (int) ((int) event.getX() + ((int) image.getWidth() * 0.2));
        }
        // If the touch point coordinates are (too) close to the upper border, adjust location and prevent partial visibility -->
        int iTop = ((int) event.getY());
        if (iTop <= (int) image.getHeight() * 0.2) {
            iTop = (int) (image.getHeight() * 0.5);
        }
        // If the touch point coordinates are (too) close to the lower border, adjust location and prevent shrinking of image -->
        if (((int) event.getY() + (int) (image.getHeight()) >= (int) image_background.getHeight())) {
            iTop = (int) event.getY() - (int) (image.getHeight());
        }
        // set image location coordinates -->
        params.setMargins(iLeft, iTop, 0, 0); //left, top, right, bottom
        image.setLayoutParams(params);
        image.setVisibility(VISIBLE);

        float x = event.getX();
        float y = event.getY();
        Log.d("TakeNote_Info", "X " + String.valueOf(x) + " Y " + String.valueOf(y));
        points_array.add(new Point((int) x, (int) y));
        Log.d("TakeNote_Info", "Array size = " + String.valueOf(points_array.size()));
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
        // set return value to true to be able to manipulate actions -->
        return true;
    }

    public void clearPoints() {
        points_array.clear();
    }
}
