package com.notemasterv10.takenote.listing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.notemasterv10.takenote.Database;
import com.notemasterv10.takenote.MainActivity;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.library.SharedResource;

import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends Fragment implements NoteMasterConstants {

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<Note> note_list = new ArrayList<Note>();
    private View v;
    private Database sdb;
    private NoteListRecyclerViewAdapter nlrv;
    SharedResource sr = new SharedResource();
    private boolean deleteConfirmed;

    public NoteListFragment() {
    }

    public static NoteListFragment newInstance(int columnCount) {
        NoteListFragment fragment = new NoteListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // to gain access to options menu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        if (view instanceof RecyclerView) {
            Context context = view.getContext();

            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            nlrv = new NoteListRecyclerViewAdapter(note_list, mListener);
            // set the adapter click listeners
            setItemClickListeners(nlrv);
            // Set the adapter
            recyclerView.setAdapter(nlrv);

        }
        v = view;
        return view;
    }

    private void setItemClickListeners(final NoteListRecyclerViewAdapter nlrv){

        nlrv.setClickInterface(new NoteListRecyclerViewAdapter.ClickInterface() {

            @Override
            public void itemClickToOpen(Note note) {
                if (mListener != null) {
                    mListener.onListFragmentInteraction(note);
                }
            }

            @Override
            public void itemClickToDelete(final Note note, final View v, final int position) {

                nlrv.setLastSelectedItem();
                nlrv.setSelectedItem(position);
                nlrv.notifyItemChanged(nlrv.getLastSelectedItem());
                nlrv.notifyItemChanged(nlrv.getSelectedItem());

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(v.getContext());

                builder.setTitle(R.string.ConfirmDialogTitle);
                builder.setMessage(String.format("Are you sure you want to delete '%s' ? This cannot be undone.", note.getName()));
                builder.setIcon(R.mipmap.dialog_orange_warning);
                builder.setCancelable(false); // block back-button

                builder.setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteConfirmed = true;
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteConfirmed = false;
                        dialog.dismiss();
                    }
                });

                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if(deleteConfirmed) {
                            nlrv.deleteItemFromList(position);
                            if (null != mListener) {
                                mListener.onListFragmentInteractionDelete(note);
                            }
                        }
                        nlrv.resetSelectedItemPositions(); // clear selection
                    }
                });
                AlertDialog dlg = builder.create();
                dlg.show();

            }

            @Override
            public void itemClickToUpdate(final Note note, final View v) {

                nlrv.setLastSelectedItem();
                nlrv.setSelectedItem(note.getListPosition());
                nlrv.notifyItemChanged(nlrv.getLastSelectedItem());
                nlrv.notifyItemChanged(nlrv.getSelectedItem());

                sr.noteNameDialog(getContext(), null, NoteAction.SET_NAME, "",null, note.getName(), note.isCurrentNote(), note.getListPosition());
                // answer is being handled listener MainActivity.renameAnswerConfirmed, exit here -->

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    public void loadData(){

        sdb = new Database(getContext());

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, List<Note>> asynctask = new AsyncTask<Void, Void, List<Note>>() {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return sdb.getNoteListing();
            }

            @Override
            protected void onPostExecute(List<Note> note_list) {
                Log.d("DB", "Aantal elementen " + String.valueOf(note_list.size()));
                super.onPostExecute(note_list);
                nlrv.loadData(note_list);
            }
        };
        asynctask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
        sr.setDialogAnswerListener((MainActivity) getActivity());
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        note_list.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    public void deleteItemFromList(int position, String newNoteName){
        nlrv.renameItemFromList(position, newNoteName);
        nlrv.resetSelectedItemPositions(); // clear selection
    }


    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Note note);
        void onListFragmentInteractionDelete(Note note);
    }
}
