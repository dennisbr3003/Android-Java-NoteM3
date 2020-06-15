package com.notemasterv10.takenote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.database.PointTable;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listeners.PointCollectorListener;

import java.util.List;

// this implementation launches the interface rendering it not null?
public class ImageActivity extends AppCompatActivity implements PointCollectorListener, NoteMasterConstants {

    SharedResource sr = new SharedResource();

    private PointCollector pointCollector = new PointCollector();

    // Variables for requesting permissions, API 25+
    private int requestCode;
    private int grantResults[]; // used because the variable needs to be final, therefor you
    // cannot update it in code. But you CAN assign a value to an array element

    @Override
    public void onBackPressed() {
        // do nothing in lock screen with back button
    }

    // this method is the call back method for requesting permission(s). If it is granted (or not) this will execute
    // we have to put it here because the request itself is a asynchronous request and the activity may have been
    // loaded with a missing picture which will make it impossible to set valid passpoints -->
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        showPassPointImage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        addTouchListener();

        // This sets the pointCollectorlistener in the PointCollector class with this object
        // rendering the listener not null. This object can be used as a parameter because it
        // implements the PointCollectorListener interface and that is the requirement.
        pointCollector.setPointCollectorListener(this);

        Bundle extras = getIntent().getExtras(); // retrieve any send data

        Log.d("Debug", "Gaan we echt resetten ? " + String.valueOf((extras != null)));

        if (extras != null){
            // to get any sort of value use this:
            Boolean resetPassPoints = extras.getBoolean(getString(R.string.ClearPassPoints), false);
            if (resetPassPoints) {
                // do not do this in a async task; the check will be reached before this is finished. We can't show the dialog
                // from the onPostExecute because the dialog may have to be shown if the bundle = null
                // clear shared prefs to force new passpoint entry
                sr.resetSharedPasspointsSet(this);
            }

        }

        if (sr.getSharedPrefsPasspointImage(this) != SETTING_UNKNOWN) {
            // asynchronous --> if input is required goto callback procedure
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //if you dont have required permissions ask for it (only required for API 23+)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
                // If the permission dialog appears we will load the picture from the callback method (onRequestPermissionsResult).
            } else { // rights are ALREADY granted so go for it -->
                showPassPointImage();
            }
        }
        else{
            Log.d("Test if points are set", String.valueOf(!sr.pointsSetInSharedPrefs(this)));
            if (!sr.pointsSetInSharedPrefs(this)) {
                showSetPassPointsDialog(); // this may change as the build progresses
            }
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

        builder.setPositiveButton(R.string.ButtonCaptionOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing there is only information in this dialog
                dialog.dismiss();
            }
        });
        AlertDialog dlg = builder.create();
        dlg.show();
    }

    // This method is overridden from the PointCollectorListener interface and will be called from
    // PointCollector class. In this class an ArrayList with point will be send to this method. I
    // have added code to check the contents and send them to to the log subsequently.
    @Override
    public void pointsCollected(final List<Point> points_list) {
        if (!sr.pointsSetInSharedPrefs(this)) {
            Log.d("Debug-DB", getString(R.string.SavingPoints));
            savePointsCollected(points_list);
            //give immediate access after storing the 4 points
            Intent i = new Intent(ImageActivity.this, MainActivity.class); // next step = MainActivity ?
            startActivity(i); // you need an intent to pass to startActivity() so that's why the intent was declared
        } else {
            Log.d("Debug-DB", getString(R.string.VerifyPoints));
            // verify points to the ones we save earlier (from the db)
            verifyPassPoints(points_list);
        }
    }

    private void verifyPassPoints(final List<Point> points_list) {

        final PointTable pointTable = new PointTable(this);

        // First show a dialog to be shown during verification
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Do not use setMessage if you want to use an icon; instead use setTitle
        builder.setTitle(R.string.VerifyValues);
        builder.setIcon(R.mipmap.db_storing); // this will be placed besides the title

        final AlertDialog dlg = builder.create();
        dlg.show();

        resizeDialogDimensions(dlg);

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Boolean> asynctask = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {

                List<Point> points_saved = pointTable.getPoints();

                Log.d("Debug-DB","Loaded points from DB " + points_saved.size());

                try { // just to show the dialog
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //do nothing
                }
                int i=0;
                if(points_list.size() == points_saved.size()){
                   for(Point p:points_list){
                       //points_list
                       if((Math.abs(p.x - points_saved.get(i).x) > MAX_DEVIATION) || (Math.abs(p.y - points_saved.get(i).y) > MAX_DEVIATION)) {
                          return false;
                       }
                       i++;
                   }
                } else {
                    return false; // no good to compare different sizes in a loop
                }
                // return value = passed to onPostExecute
                return true;

            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                pointCollector.clearPoints(); // all ways clear for new attempt
                dlg.dismiss();
                if(aBoolean){
                    Intent i = new Intent(ImageActivity.this, MainActivity.class); // next step = MainActivity ?
                    startActivity(i); // you need an intent to pass to startActivity() so that's why the intent was declared
                }
                else {
                    Toast toast = Toast.makeText(ImageActivity.this,"Access denied", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    // Set the background color like this or lose the rounded edges of the toaster message (it becomes square) -->
                    int backgroundColor = ResourcesCompat.getColor(toast.getView().getResources(), R.color.toaster_red, null);
                    toast.getView().getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN);
                    toast.show();
                }
            }
        };

        asynctask.execute();

    }


    private void savePointsCollected(final List<Point> points_list) {
        // this code could hold up the UI thread so we should move this to a asynchronous thread of its own
        final PointTable pointTable = new PointTable(this);
        // First show a dialog to indicate activity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Do not use setMessage if you want to use an icon; instead use setTitle
        builder.setTitle(R.string.StoringValues);
        builder.setIcon(R.mipmap.db_storing); // this will be placed besides the title

        final AlertDialog dlg = builder.create();
        dlg.show();

        // You CAN customize an AlertDialog but only AFTER it is shown. Apparently this can also be done
        // with dialogs that are declared FINAL because they are being referenced to from another thread

        resizeDialogDimensions(dlg);

        // Launch a asynchronous task (that runs in it's own thread)
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    for (Point p : points_list) {
                        Log.d("Point captured", String.valueOf(p.x) + " " + String.valueOf(p.y));
                    }
                    Thread.sleep(1000); // Just so you can see the dialog (test purposes)
                    pointTable.setPoints(points_list);
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
                sr.setSharedPasspointsSet(ImageActivity.this);
                dlg.dismiss();
            }

        };

        asynctask.execute(); // You have to run the task after it's definition
    }

    private void resizeDialogDimensions(AlertDialog dlg) {

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

    }

    private void showPassPointImage(){
        try{
            ImageView im = (ImageView) findViewById(R.id.imageView);
            im.setImageBitmap(sr.getSavedPasspointImageAsBitmap(this));
        } catch(Exception e) {
            // there was an error, reset to default pic and reset passpoints
            Log.d(getString(R.string.ErrorTag), String.format("%s <%s>", getString(R.string.PictureError), e.getMessage()));
            sr.resetSharedPrefsPasspointImage(this);
        }
        if (!sr.pointsSetInSharedPrefs(this)) {
            showSetPassPointsDialog(); // this may change as the build progresses
        }
    }

}
