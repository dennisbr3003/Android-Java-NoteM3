package com.example.notematser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FirstFragment extends Fragment {

    MainActivity ma = (MainActivity) getActivity();

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_first, container, false);

        /* read session */
        if(savedInstanceState == null) {
            getSavedFileOnStartup(view); //only load once on init of fragment (savedInstanceState = null)
        }
        setBackgroundColorUsingPrefs(view);
        return view;

    }


    private void updateTextObject(View v, final String msg) {
        final String str = msg;
        final EditText et = (EditText) v.findViewById(R.id.editText);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                et.append(msg);
                et.append("\n");
            }
        });
    }

    private void getSavedFileOnStartup(final View v) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                FileInputStream fis = null;

                try {
                    fis = getContext().openFileInput(getString(R.string.fileName));
                } catch (FileNotFoundException e) {
                    Log.e(getString(R.string.FileNotFoundException), getString(R.string.FileOpsError));
                    return; //do nothing
                }

                Log.i(getString(R.string.DefaultTag), fis.toString());
                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fis)));
                String line;

                while(true){
                    try {
                        if (((line = br.readLine()) != null)) {
                           updateTextObject(v, line);
                           Log.i(getString(R.string.DefaultTag), line);
                        }
                        else {break;}  // avoid endless loop
                    } catch (IOException e) {
                        Log.e(getString(R.string.IOException), getString(R.string.LineReadError));
                    }
                }
                try {
                    fis.close();
                    Log.i(getString(R.string.DefaultTag), getString(R.string.NoErrorOnFileOps));
                } catch (IOException e) {
                    Log.e(getString(R.string.IOException), getString(R.string.FileOpsError));
                    return; // do nothing
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(getString(R.string.InterruptedException), getString(R.string.ThreadNotExceuted));
        }
    }

    private void saveText(View v){
        EditText et = (EditText) v.getRootView().findViewById(R.id.editText); //use getRootView to get correct view/container because we are in a thread
        try {
            FileOutputStream fos = getContext().openFileOutput(getString(R.string.fileName), getContext().MODE_PRIVATE);
            fos.write(et.getText().toString().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(getString(R.string.FileNotFoundException), getString(R.string.FileOpsError));
        } catch (IOException e) {
            Log.e(getString(R.string.IOException), getString(R.string.FileOpsError));
        }
    }

    public void setBackgroundColorUsingPrefs(View view) {

        SharedPreferences prefs = getContext().getSharedPreferences("TakeNote", Context.MODE_PRIVATE);
        Log.d("prefcolor", String.valueOf(prefs.getInt("BackGroundColor", -1)));
        EditText et = (EditText) view.getRootView().findViewById(R.id.editText);
        et.setBackgroundColor(prefs.getInt("BackGroundColor", -1));
    }

    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                Log.d(getString(R.string.DefaultTag), getString(R.string.ClickOnBtnNext));

                ColorPickerDialogBuilder
                        .with(getContext())
                        .setTitle("Choose color")
                        //.initialColor(currentBackgroundColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {
                            @Override
                            public void onColorSelected(int selectedColor) {
                                Log.d("onColorSelected: 0x" ,Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                Log.d("onColorSelectedok" ,Integer.toHexString(selectedColor));
                                SharedPreferences prefs = getContext().getSharedPreferences("TakeNote", Context.MODE_PRIVATE);
                                SharedPreferences.Editor prefsEdit = prefs.edit();
                                prefsEdit.putInt("BackGroundColor", selectedColor);
                                prefsEdit.commit();
                                setBackgroundColorUsingPrefs(view);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("onColorSelectedCancel" , "cancel");
                            }
                        })
                        .build()
                        .show();

            }
        });

        view.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(getString(R.string.DefaultTag), getString(R.string.btnSave_Click));
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        saveText(v);
                    }
                });
                t.start();
                try {
                    t.join();
                    Toast.makeText(getContext(),getString(R.string.ToastSaveSucces), Toast.LENGTH_LONG).show();
                } catch (InterruptedException e) {
                    Toast.makeText(getContext(),getString(R.string.ToastSaveFailure), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
