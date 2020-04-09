package com.example.notematser;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements DialogAnswerListener {

    SharedResource sr = new SharedResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sr.setDialogAnswerListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_reset_passpoints:
                Intent i = new Intent(this, ImageActivity.class);
                i.putExtra(getString(R.string.ClearPassPoints), true); // = received in the activity as a bundle in onCreate of the target activity
                startActivity(i);
                return true;
            case R.id.action_reset_backgroundcolor:
                // No dialog needed here, can be easily reset
                EditText et = (EditText) findViewById(R.id.editText);
                et.setBackgroundColor(-1);
                sr.saveSharedBackgroundColor(-1, this);
                return true;
            case R.id.action_clear_notetext:
                sr.askUserConfirmationDialog(this);
                return true;
            case R.id.action_change_passpoint_picture:
                sr.selectPassPointImageCustomDialog(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String myString = savedInstanceState.getString("EditorText");
        Log.i(getString(R.string.DefaultTag), "Restored data: " + myString);
        // assign texteditor value here -->
        EditText et = (EditText) findViewById(R.id.editText);
        et.setText(myString);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        EditText et = (EditText) findViewById(R.id.editText);
        Log.d(getString(R.string.DefaultTag), getString(R.string.FoundValue) + et.getText().toString());
        String x = et.getText().toString();
        savedInstanceState.putString("EditorText", x);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public void booleanAnswerConfirmed(Boolean answer) {
        EditText et = (EditText) findViewById(R.id.editText);
        if(answer){
            Log.d(getString(R.string.DefaultTag), "Confirmed");
            et.setText("");
            sr.saveNoteText(this, et.getText().toString().getBytes());
        } else {
            Log.d(getString(R.string.DefaultTag), "Not confirmed");
        }

    }
}