package com.example.notematser;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
        Log.d("Test", String.valueOf(savedInstanceState == null));
        if(savedInstanceState == null) {
            getSavedFileOnStartup(view); //only load once on init of fragment (savedInstanceState = null)
        }
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
        Log.d(getString(R.string.DefaultTag), getString(R.string.FoundValue) + et.getText().toString());
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

/*
    private String getSessionValue(final View v, final int value) {
        final String[] returnValue = {null};
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                FileInputStream fis = null;

                try {
                    fis = getContext().openFileInput("session");
                } catch (FileNotFoundException e) {
                    Log.e(getString(R.string.FileNotFoundException), getString(R.string.FileOpsError));
                    return; //do nothing file needs to be created
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fis)));
                String line;
                int i = 0;
                while(true){
                    try {
                        if (((line = br.readLine()) != null)) {
                            i++;
                            if(i==value) {
                                returnValue[0] = line;
                                Log.i(getString(R.string.DefaultTag), line);
                                break;
                            }
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
        return returnValue[0];
    }

 */

/*
    private void setSessionValue(final View v, final String svalue){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = getContext().openFileOutput("session", getContext().MODE_PRIVATE);
                    fos.write(svalue.toString().getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.e(getString(R.string.FileNotFoundException), getString(R.string.FileOpsError));
                } catch (IOException e) {
                    Log.e(getString(R.string.IOException), getString(R.string.FileOpsError));
                }
            }
        });
        t.start();
        try {
            t.join();
            Log.i(getString(R.string.DefaultTag), getString(R.string.NoErrorOnFileOps));
        } catch (InterruptedException e) {
            Log.e(getString(R.string.InterruptedException), getString(R.string.ThreadNotExceuted));
        }
    }
 */

    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getString(R.string.DefaultTag), "Click detected on btnNext");
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
                    Log.i(getString(R.string.DefaultTag), getString(R.string.NoErrorOnFileOps));
                } catch (InterruptedException e) {
                    Log.e(getString(R.string.InterruptedException), getString(R.string.ThreadNotExceuted));
                }
            }
        });
    }

}
