package com.example.notematser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.List;

// this implementation launches the interface rendering it not null?
public class ImageActivity extends AppCompatActivity implements PointCollectorListener {

    private PointCollector pointCollector = new PointCollector();
    private Database sdb = new Database(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        addTouchListener();
        // This sets the pointCollectorlistener in the PointCollector class with this object
        // rendering the listener not null. This object can be used as a parameter because it
        // implements the PointCollectorListener interface and that is the requirement.
        pointCollector.setPointCollectorListener(this);

        if (!pointsSetInPrefs()) {
            showSetPassPointsDialog(); // this may change as the build progresses
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addTouchListener() {
        ImageView iv = (ImageView) findViewById(R.id.imageView);
        // The pointCollector class is send here which implements the
        // View.OnTouchListener normally used here. This way we can keep complicated
        // in one place  (this new PointCollector class) and avoid lengthy code here.
        // It will still automatically fire the OnTouch method but it will communicate
        // with this class through the interface and a reference to this object
        iv.setOnTouchListener(pointCollector);
    }

    private void showSetPassPointsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.DialogTitle);
        builder.setMessage(R.string.DialogMessage);
        builder.setIcon(R.mipmap.dialog_orange_warning);

        builder.setPositiveButton(R.string.btnPosCaption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing there is only information in this dialog
            }
        });
        AlertDialog dlg = builder.create();
        dlg.show();
    }

    private boolean pointsSetInPrefs() {

        SharedPreferences prefs = getSharedPreferences("TakeNote", Context.MODE_PRIVATE);
        return prefs.getBoolean("PointsSet", false);

    }

    // This method is overridden from the PointCollectorListener interface and will be called from
    // PointCollector class. In this class an ArrayList with point will be send to this method. I
    // have added code to check the contents and send them to to the log subsequently.
    @Override
    public void pointsCollected(final List<Point> points_list) {
        if (!pointsSetInPrefs()) {
            Log.d("Debug-DB", getString(R.string.points_saving));
            savePointsCollected(points_list);
        } else {
            Log.d("Debug-DB", getString(R.string.points_verify));
            // verify points to the ones we save earlier (from the db)
        }
    }

    private void savePointsCollected(final List<Point> points_list) {
        // this code could hold up the UI thread so we should move this to a asynchronous thread of its own

        // First show a dialog to indicate activity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Do not use setMessage if you want to use an icon; instead use setTitle
        builder.setTitle(R.string.storing_progress);
        builder.setIcon(R.mipmap.db_storing); // this will be placed besides the title

        final AlertDialog dlg = builder.create();
        dlg.show();

        // You CAN customize an AlertDialog but only AFTER it is shown. Apparently this can also be done
        // with dialogs that are declared FINAL because they are being referenced to from another thread

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;
        // The absolute height of the available display size in pixels.
        int displayHeight = displayMetrics.heightPixels;

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dlg.getWindow().getAttributes());

        // Set alert dialog width equal to screen width 70%. Use relative values for different screens
        int dialogWindowWidth = (int) (displayWidth * 0.7f);
        // Set alert dialog height equal to screen height 17%, the dialog will be sort-of-centered
        int dialogWindowHeight = (int) (displayHeight * 0.17f);

        // Set the width and height for the layout parameters
        // This will be the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dlg.getWindow().setAttributes(layoutParams);

        // Launch a asynchronous task (that runs in it's own thread)
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    for (Point p : points_list) {
                        Log.d("Point captured", String.valueOf(p.x) + " " + String.valueOf(p.y));
                    }
                    Thread.sleep(1000); // Just so you can see the dialog (test purposes)
                    sdb.setPoints(points_list);
                    Log.d("Debug-DB", "Point saved to MySQL DB");
                } catch (Exception e) {
                    Log.d("Debug-DB", "No point collected, object = null (" + e.getLocalizedMessage() + ")");
                    // do nothing
                }
                return null;
            }

            // this runs AFTER the code of 'doInBackground' has finished
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pointCollector.clearPoints(); //empty after save in "on completion" event of async task.
                //save the action to a startup preference we can check on startup

                SharedPreferences prefs = getSharedPreferences("TakeNote", Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEdit = prefs.edit();
                prefsEdit.putBoolean("PointsSet", true);
                prefsEdit.apply(); // apply does it's work in th ebackground, commit does not.

                dlg.dismiss();
            }

        };

        asynctask.execute(); // You have to run the task after it's definition
    }

}
