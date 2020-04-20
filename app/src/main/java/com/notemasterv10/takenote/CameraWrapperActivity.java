package com.notemasterv10.takenote;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class CameraWrapperActivity extends AppCompatActivity implements Constants {

    SharedResource sr = new SharedResource();

    private File picture_file;

    // Variables for requesting permissions, API 25+
    private int requestCode;
    private int grantResults[]; // used because the variable needs to be final, therefor you
                                // cannot update it in code. But you CAN assign a value to an array element

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_camera);

        addBtnCancelClickListener();
        addBtnPictureClickListener();
        addBtnUseClickListener();

        // Check for permission to access external storage (manifest.xml) and if needed ask for it: API 25+ -->
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {

            // this runs asynchronously so check the callback procedure (onRequestPermissionsResult) on what to do -->
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
            onRequestPermissionsResult(requestCode, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, grantResults);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission denied, Disable the functionality that depends on this permission -->
                    Toast.makeText(this, R.string.access_ext_storage_denied, Toast.LENGTH_SHORT).show();
                    //This part of the app (taking a photo) cannot function without this permission for now so close it -->
                    onDestroy();
                }
                return;
            }
        }
    }

    private void addBtnCancelClickListener(){
        Button btn_cancel = (Button)findViewById(R.id.button_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this will unload this activity and return with a result
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void addBtnUseClickListener() {
        Button btn_use = (Button)findViewById(R.id.button_use_photo);
        btn_use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // returning data from one activity to the calling activity. Make sure the calling activity uses startActivityForResult or it
                // won't work. This intent will be in the 'data' parameter onActivityResult overridden method --> check MainActivity for this
                Intent output = new Intent();
                output.putExtra(CAMERA_ABSOLUTE_FILEPATH, picture_file.getAbsolutePath());
                setResult(RESULT_OK, output);
                finish(); // close the activity
            }
        });
    }


    private void addBtnPictureClickListener() {

        Button btn_picture = (Button) findViewById(R.id.button_picture);

        btn_picture.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onClick(View v) {

                // This is a directory only accessible for the app -->
                File picture_save_directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                // set up to file object to copy the photo to. It will always be overwritten with the new photo -->
                picture_file = new File(picture_save_directory, CAMERA_PHOTO_FILENAME);

                // First check if there is a camera app on the device; it's to be used to take the actual picture
                // We ask the device to check if there is an app available to take a picture through an implicit intent -->
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(i.resolveActivity(getPackageManager())!= null) { // <-- this is to avoid a crash of the app
                    // let the intent know we want to save to photo to that location with that filename -->
                    if (picture_file != null) {
                        Uri photoURI = FileProvider.getUriForFile(CameraWrapperActivity.this,
                                "com.notemasterv10.android.fileprovider",
                                picture_file);
                        i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);  // <-- this relies on the fileprovider in the manifest.xml
                        startActivityForResult(i, REQUEST_ID_CAMERA); // use startActivityForResult to retrieve a photo
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data); // data holds the thumbnail

        switch(requestCode){

            case REQUEST_ID_CAMERA:

                if(resultCode == RESULT_OK) {

                    // The activity of taking and saving a picture is ended so get the bitmap (that should be saved in picture_file) -->
                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>() {

                        @Override
                        protected Void doInBackground(Void... voids) {
                           try{

                               final Bitmap bm = sr.getImageviewBitmapFromAbsolutePath(picture_file.getAbsolutePath());
                               final ImageView im = findViewById(R.id.imageview_photo);

                               // move the (smallest possible) portion of the background task that updates the UI onto the main thread -->
                               runOnUiThread(new Runnable() {
                                   @Override
                                   public void run() {
                                       im.setImageBitmap(bm);
                                   }
                               });
                           } catch(Exception e){
                               Log.e(getString(R.string.takenote_errortag), getString(R.string.picture_error));
                               return null;
                           }
                           return null;
                        }
                    }.execute(); // <-- short notation
                }
            break;

            default:
                // unknown parameter, log error -->
                Log.e(getString(R.string.takenote_errortag), getString(R.string.activity_unknown));
                break;
        }
    }
}
