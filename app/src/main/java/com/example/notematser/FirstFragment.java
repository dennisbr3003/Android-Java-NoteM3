package com.example.notematser;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import static android.content.Context.MODE_PRIVATE;

public class FirstFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_first, container, false);
        getSavedFileOnStartup(view);
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
                    fis = getContext().openFileInput("sometextfile");
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

    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getString(R.string.DefaultTag), getString(R.string.btnSave_Click));
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        view.findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(getString(R.string.DefaultTag), getString(R.string.btnSave_Click));
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // first get the text from editText
                        EditText et = (EditText) v.getRootView().findViewById(R.id.editText); //use getRootView to get correct view/container
                        Log.d(getString(R.string.DefaultTag), getString(R.string.FoundValue) + et.getText().toString());
                        try {
                            FileOutputStream fos = getContext().openFileOutput("sometextfile", getContext().MODE_PRIVATE);
                            fos.write(et.getText().toString().getBytes());
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
        });
    }
}
