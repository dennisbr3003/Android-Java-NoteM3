package com.example.notematser;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String myString = savedInstanceState.getString("EditorText");
        Log.i("debug", "Restored data: " + myString);
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
}
