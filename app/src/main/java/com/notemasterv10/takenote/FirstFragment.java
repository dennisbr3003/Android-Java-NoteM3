package com.notemasterv10.takenote;

import android.content.DialogInterface;
import android.content.Intent;
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
import java.io.IOException;
import java.io.InputStreamReader;

public class FirstFragment extends Fragment implements Constants {

    SharedResource sr = new SharedResource();
    WebService ws = new WebService();

    private int requestCode;
    private int grantResults[];

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view;
        view = inflater.inflate(R.layout.fragment_first, container, false);

        /* read session */
        if (savedInstanceState == null) {
            getSavedFileOnStartup(view); //only load once on init of fragment (savedInstanceState = null)
        }
        setBackgroundColorUsingPrefs(view);

        Log.d("DB", "check webservice");
        ws.checkForWebService(getContext(), view);
        //ws.createSharedPreferenceObject(getContext());
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

                while (true) {
                    try {
                        if (((line = br.readLine()) != null)) {
                            updateTextObject(v, line);
                            Log.i(getString(R.string.DefaultTag), line);
                        } else {
                            break;
                        }  // avoid endless loop
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

    @Override
    public void onPause() {
        super.onPause();
        // save something here
    }

    private void saveText(View v) {
        EditText et = (EditText) v.getRootView().findViewById(R.id.editText); //use getRootView to get correct view/container because we are in a thread
        sr.saveNoteText(getContext(), et.getText().toString().getBytes());
    }

    public void setBackgroundColorUsingPrefs(View view) {
        EditText et = (EditText) view.getRootView().findViewById(R.id.editText);
        et.setBackgroundColor(sr.getSharedBackgroundColor(getContext()));
    }

    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.imgButtonSave).setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(getContext(), getString(R.string.ToastSaveSucces), Toast.LENGTH_LONG).show();
                } catch (InterruptedException e) {
                    Toast.makeText(getContext(), getString(R.string.ToastSaveFailure), Toast.LENGTH_LONG).show();
                }
            }
        });
        view.findViewById(R.id.imgButtonColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                Log.d("onColorSelected: 0x", Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                sr.saveSharedBackgroundColor(selectedColor, getContext());
                                setBackgroundColorUsingPrefs(view);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("onColorSelectedCancel", "cancel");
                            }
                        })
                        .build()
                        .show();

            }
        });

        view.findViewById(R.id.imgButtonLock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ImageActivity.class); // next step = MainActivity ?
                startActivity(i); // you need an intent to pass to startActivity() so that's why the intent was declared
            }
        });

    }

}
