package com.notemasterv10.takenote;

import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.notemasterv10.takenote.listeners.PointCollectorListener;

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
    // in this class. If the reference is not null it will execute the method and pass the
    // collected points. Actually ImageActivity is passed to here (it implements the interface!).
    public void setPointCollectorListener(PointCollectorListener pointCollectorListener) {
        this.pointCollectorListener = pointCollectorListener;
    }

    public boolean onTouch(View v, MotionEvent event) {

        /*
        I have tried to use one ImageView object and update the image on runtime. I tried to do this
        with setImageResource. It did show the correct image but with the wrong dimensions. At this
        point the image had to be repositioned to the center. The images had different sizes and at runtime
        the sizes of the image where those of the previous image and the counter did reposition correctly.
        I could not find a way to force a refresh (invalidate() did not work) So I had to create 4 images,
        one for each number. This way I could successfully update the layout parameters and reposition
        the numbers correctly, hence this construction :(
        */
        ImageView image1 = (ImageView) v.getRootView().findViewById(R.id.imageview_counter1);
        ImageView image2 = (ImageView) v.getRootView().findViewById(R.id.imageview_counter2);
        ImageView image3 = (ImageView) v.getRootView().findViewById(R.id.imageview_counter3);
        ImageView image4 = (ImageView) v.getRootView().findViewById(R.id.imageview_counter4);

        ImageView image_background = (ImageView) v.getRootView().findViewById(R.id.imageView);

        if (event.getAction() == android.view.MotionEvent.ACTION_DOWN){

            switch(points_array.size()){
                case 0:
                    image1 = updateLayoutParams(event, image1, image_background);
                    image1.setVisibility(VISIBLE);
                    break;
                case 1:
                    image2 = updateLayoutParams(event, image2, image_background);
                    image2.setVisibility(VISIBLE);
                    break;
                case 2:
                    image3 = updateLayoutParams(event, image3, image_background);
                    image3.setVisibility(VISIBLE);
                    break;
                case 3:
                    image4 = updateLayoutParams(event, image4, image_background);
                    image4.setVisibility(VISIBLE);
                    break;
            }

            float x = event.getX();
            float y = event.getY();
            Log.d("DENNIS_BRINK", "X " + x + " Y " + y);
            points_array.add(new Point((int) x, (int) y));
            Log.d("DENNIS_BRINK", "touch action down, added touched coordinate = " + String.valueOf(points_array.size()));
        }

        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

            try { // delay disappearance of image a little bit so it lingers
                Thread.sleep(250);
                image1.setVisibility(View.INVISIBLE);
                image2.setVisibility(View.INVISIBLE);
                image3.setVisibility(View.INVISIBLE);
                image4.setVisibility(View.INVISIBLE);
            } catch (InterruptedException e) {
                // ignore
            }
            catch (Exception e){
                Log.d("DENNIS_BRINK", e.getMessage());
            }

            if(points_array.size() == 4) {

                if (pointCollectorListener != null) {
                    pointCollectorListener.pointsCollected(points_array); // this method is actually an interface method overridden in ImageActivity
                    // Also check ImageActivity.java and PointCollectorListener.java (interface)
                }
                // points_array.clear(); better practise to do this after the points are saved to the db. The save is asynchronous
                // so the array if passed by reference may be emptied before it is saved (?). Anyway the array is passed as a final
                // because it is used in a separate thread and in order to use it like that it has to be passed as a final; so it
                // cannot be altered. We have to facilitate a public method here where it is not final. See below:
            }
        }

        // set return value to true to be able to manipulate actions -->
        return true;
    }

    private ImageView updateLayoutParams(MotionEvent event, ImageView child, ImageView parent){

        Coordinate coordinate = calculateNumberPosition(event, child, parent);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) child.getLayoutParams();
        params.setMargins(coordinate.getLeft(), coordinate.getTop(), 0, 0);
        child.setLayoutParams(params);
        return child;

    }

    private Coordinate calculateNumberPosition(MotionEvent event, ImageView child, ImageView parent){

        // constants -->
        double x_correction = 1.3;
        double y_correction = 1.65;
        int y_upperlimit = 245;

        // which coordinates where touched -->
        int x = (int)event.getX();
        int y = (int)event.getY();

        // what are the dimension of the image you want to show -->
        int childH = 0;
        int childW = 0;

        Log.d("DENNIS_BRINK", "Original child (h,w) " + child.getMeasuredHeight() + "," + child.getMeasuredWidth());

        // we create a calculation box (same width and height) -->
        if(child.getWidth() > child.getHeight()){
            childH = child.getWidth();
            childW = child.getWidth();
        } else {
            childH = child.getHeight();
            childW = child.getHeight();
        }

        // the (absolute) difference between the actual image width and the calculation box width (144) -->
        int totalDeviationSquareDimensions = Math.abs(child.getWidth() - child.getHeight());

        // what are the dimension of the underlying parent -->
        int parentH = parent.getHeight();
        int parentW = parent.getWidth();

        Coordinate c = new Coordinate();

        Log.d("DENNIS_BRINK", "touch point (x,y) " + x + "," + y);
        Log.d("DENNIS_BRINK", "child (h,w) " + childH + "," + childW);
        Log.d("DENNIS_BRINK", "parent (h,w) " + parentH + "," + parentW);
        Log.d("DENNIS_BRINK", "totalDeviationSquareDimensions " + totalDeviationSquareDimensions);

        // first we want it at our fingertip (slightly above it)
        if(((y - (int)(childH * y_correction)) <= parentH) && y >= y_upperlimit){
            c.setTop(y - (int)(childH * y_correction));
            c.setLeft(x - (int) ((childW) / 2) + (totalDeviationSquareDimensions / 2));
            if((c.getLeft() <= 0)){
                c.setLeft(0);
            }
            if((parentW - c.getLeft()) < childW) {
                c.setLeft(parentW - childW);
            }
            return c;
        } else { // not enough room at the top
            // Do we have enough room on the left? --> (like when we touch somewhere in the center)
            if((x) >= (int)(x_correction * childW)){
                c.setLeft(x - (int)(x_correction * childW));
                c.setTop(0);
                return c;
            }
            // we do not have enough space on the left so we go to the right -->
            if((x) < (int)(x_correction * childW)){
                c.setLeft(x + childW);
                c.setTop(0);
                return c;
            }
        }

        return null; // this should never happen

    }

    class Coordinate{

        private int left;
        private int top;

        public Coordinate() {
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }
    }

    public void clearPoints() {
        points_array.clear();
    }
}
