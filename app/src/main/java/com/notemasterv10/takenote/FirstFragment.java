package com.notemasterv10.takenote;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.library.FragmentControlMethods;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listing.NoteListFragment;
import com.notemasterv10.takenote.webservice.WebService;

public class FirstFragment extends Fragment implements NoteMasterConstants, FragmentControlMethods {

    SharedResource sr = new SharedResource();
    WebService ws = new WebService();
    private View view;

    private int requestCode;
    private int grantResults[];

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_first, container, false);

        /* read session */
        if (savedInstanceState == null) {
            getSavedFileOnStartup(view); //only load once on init of fragment (savedInstanceState = null)
        }

        setBackgroundColorUsingPrefs(view);
        return view;
    }

    private void loadNote(final View v, final byte[] note) {

        final EditText et = v.findViewById(R.id.editText);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // convert byte array directly to string -->
                if (note != null) {
                    et.setText(new String(note));
                }
                displayFileName(v);
            }
        });
    }

    private void displayFileName(View v){

        // this method may also be executed from threads, hence the v.getRootView()
        final TextView tv = (TextView) v.getRootView().findViewById(R.id.text_view_currentnote);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv.setText(sr.getOpenNoteName(getContext()));
            }
        });

    }

    private void getSavedFileOnStartup(final View v) {

        final String noteName = sr.getOpenNoteName(getContext());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                loadNote(v, sr.getNote(getContext(), noteName));
            }
        });
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            Log.e(getString(R.string.InterruptedException), getString(R.string.ThreadNotExceuted));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // save something here
    }

    @Override
    public void onResume() {

        super.onResume();
        displayFileName(view);

    }

    @SuppressLint("StaticFieldLeak")
    private void saveNote(final View v, final NoteAction noteAction) {

        final EditText et = (EditText) v.getRootView().findViewById(R.id.editText); //use getRootView to get correct view/container because we are in a thread
        final byte[] note = et.getText().toString().getBytes();

        if (!(sr.getOpenNoteName(getContext()).equals(NO_FILENAME))) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    sr.saveNote(getContext(), note, sr.getOpenNoteName(getContext()));
                    return null;
                }
                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if(noteAction.equals(NoteAction.SAVE_NEW)) {
                        et.setText("");
                        sr.setOpenNoteName(getContext(), NO_FILENAME);
                        displayFileName(v);
                        Toast.makeText(getContext(), R.string.new_note, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), getString(R.string.ToastSaveSucces), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
        else { // show dialog and get a filename
            sr.getNoteNameDialog(getContext(), NO_FILENAME, note, noteAction);
        }


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
                Log.d("DB", "Actie SAVE_RETURN");
                saveNote(v, NoteAction.SAVE_RETURN);
            }
        });

        view.findViewById(R.id.imgButtonNew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DB", "Actie SAVE_NEW");
                saveNote(v, NoteAction.SAVE_NEW);
            }
        });

        view.findViewById(R.id.imgButtonColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getString(R.string.DefaultTag), getString(R.string.ClickOnBtnNext));

                ColorPickerDialogBuilder
                        .with(getContext())
                        .setTitle("Choose color")
                        .initialColor(sr.getSharedBackgroundColor(getContext()))
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

        sr.setDialogAnswerListener((MainActivity) getActivity());
    }



    @Override
    public void showChildFragment(String fragment_tag){

        switch(fragment_tag){
            case NOTELIST_FRAGMENT_TAG:
                NoteListFragment nlf = new NoteListFragment();
                FragmentTransaction fm = getChildFragmentManager().beginTransaction();
                fm.replace(R.id.child_fragment_container, nlf, NOTELIST_FRAGMENT_TAG).addToBackStack(null);
                fm.commit();
        }

    }

}
