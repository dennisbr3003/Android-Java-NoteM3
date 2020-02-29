package com.example.notematser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
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
        showDialog(); // this may change as the build progresses

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

    private void showDialog(){
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

    // This method is overridden from the PointCollectorListener interface and will be called from
    // PointCollector class. In this class an ArrayList with point will be send to this method. I
    // have added code to check the contents and send them to to the log subsequently.
    @Override
    public void pointsCollected(List<Point> points_list) {
        try {
                for (Point p: points_list) {
                    Log.d("Point captured", String.valueOf(p.x) +  " " + String.valueOf(p.y));
                }
                sdb.setPoints(points_list);
        }
        catch (Exception e){
            Log.d("Debug-DB", "No point collected, object = null (" + e.getLocalizedMessage() + ")");
            // do nothing
        }
    }
}
