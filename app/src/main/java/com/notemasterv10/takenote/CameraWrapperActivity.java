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
import android.net.Uri;
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

    private static final int TAKE_PICTURE = 263; //This constant dedicated to this class
    private static final String PHOTO_FILENAME = "passpoint_picture.jpg"; //This constant dedicated to this class

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
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            //if you dont have required permissions ask for it (only required for API 23+)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
            onRequestPermissionsResult(requestCode, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, grantResults);
        }

    }

    @Override // android recommended class to handle permissions
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("permission", "granted");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.uujm
                    Toast.makeText(this, "Permission denied to read or write your External storage", Toast.LENGTH_SHORT).show();

                    //app cannot function without this permission for now so close it...
                    onDestroy();
                }
                return;
            }

            // other 'case' line to check for other
            // permissions this app might request
        }
    }

    private void addBtnCancelClickListener(){
        Button btn_cancel = (Button)findViewById(R.id.button_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this will unload this activity and return with a result
                Log.d(getString(R.string.DefaultTag), getString(R.string.cancel_button_clicked));
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
                Log.d(getString(R.string.DefaultTag), getString(R.string.using_phote) + String.valueOf(picture_file.getAbsolutePath()));
                // returning data from one activity to the calling activity. Make sure the calling activity uses startActivityForResult or it
                // won't work. This intent will be in the 'data' parameter onActivityResult overridden method --> check MainActivity for this
                Intent output = new Intent();
                output.putExtra(CAMERA_ABSOLUTE_FILEPATH, String.valueOf(picture_file.getAbsolutePath()));
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

                // File picture_save_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                // This is a directory only accessible for the app -->
                File picture_save_directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                // set up to file object to copy the photo to. It will always be overwritten with the new photo -->

                picture_file = new File(picture_save_directory, PHOTO_FILENAME);
                Log.d(getString(R.string.DefaultTag), getString(R.string.tempfile_successfully_created) + " " + String.valueOf(picture_file.getAbsolutePath()));

                //First check if there is a camera app on the device; it's to be used to take the actual picture
                // We ask the device to check if there is an app available to take a picture through an implicit intent -->
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(i.resolveActivity(getPackageManager())!= null) { // <-- this is to avoid a crash of the app
                    // let the intent know we want to save to photo to that location with that filename -->
                    if (picture_file != null) {
                        Uri photoURI = FileProvider.getUriForFile(CameraWrapperActivity.this,
                                "com.notemasterv10.android.fileprovider",
                                picture_file);
                        i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);  // <-- dit ging fout, nu niet meer door gebruik van de fileprovider
                        startActivityForResult(i, TAKE_PICTURE); // use startActivityForResult to retrieve a photo
                    }
                }
            }
        });
    }
    /* to be deployed
        private void galleryAddPic() {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);
        }
        */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // data holds the thumbnail
        switch(requestCode){
            case TAKE_PICTURE:
                // The activity of taking and saving a picture is ended so get the bitmap (that should be saved in picture_file)
                // TODO needs refactoring -->
                try{
                    Log.d("Test", "Ok nieuwe versie");
                    ImageView im = findViewById(R.id.imageview_photo);
                    sr.setImageviewBitmapFromAbsolutePath(im, picture_file.getAbsolutePath());

                }catch (Exception e) {
                    try {
                        ImageView im = findViewById(R.id.imageview_photo);
                        im.setImageBitmap(sr.createBitmapFromOSFile(picture_file.getAbsolutePath()));
                    } catch (Exception ex) {
                        Log.d(getString(R.string.DefaultTag), getString(R.string.picture_error));
                        // do nothing
                    }
                }
                return;
            default:
                Log.d(getString(R.string.DefaultTag), getString(R.string.activity_unknown));
        }
    }
}
