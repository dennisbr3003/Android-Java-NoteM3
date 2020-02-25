package com.example.notematser;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        addTouchListener();
        showDialog();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addTouchListener(){
       ImageView iv = (ImageView) findViewById(R.id.imageView);
       iv.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
               float x = event.getX();
               float y = event.getY();
               Log.d(getString(R.string.DebugTCLabel), "X " + String.valueOf(x) + " Y " + String.valueOf(y));
               return false;
           }
       });
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.DialogTitle);
        builder.setMessage(R.string.DialogMessage);
        builder.setIcon(R.mipmap.dialog_orange_warning);
        builder.setPositiveButton(R.string.btnPosCaption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        AlertDialog dlg = builder.create();
        dlg.show();
    }

}
