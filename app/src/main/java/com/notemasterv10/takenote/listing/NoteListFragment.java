package com.notemasterv10.takenote.listing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.notemasterv10.takenote.MainActivity;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.constants.NoteMasterConstants;
import com.notemasterv10.takenote.database.NoteTable;
import com.notemasterv10.takenote.library.SharedResource;

import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends Fragment implements NoteMasterConstants {

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<Note> note_list = new ArrayList<Note>();
    private View v;
    private NoteTable noteTable;
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
            public void itemClickToDelete(final Note note, final View v) {

                markSelectedItem(note);
                sr.askUserConfirmationDialog(v.getContext(), NoteAction.DELETE, note);
                // handling is done through a listener MainActivity.deleteNote, exit here -->

            }

            @Override
            public void itemClickToUpdate(final Note note, final View v) {

                markSelectedItem(note);
                sr.noteNameDialog(getContext(), NoteAction.CHANGE_NAME, note.getName(), note.isCurrentNote(), note.getListPosition());
                // answer is being handled listener MainActivity.renameNote, exit here -->

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

        noteTable = new NoteTable(getContext());

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, List<Note>> asynctask = new AsyncTask<Void, Void, List<Note>>() {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return noteTable.getNoteListing();
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


    public void renameItemInList(int position, String newNoteName){
        nlrv.renameItemFromList(position, newNoteName);
        nlrv.resetSelectedItemPositions(); // clear selection
    }

    public void removeItemFromList(int position){
        nlrv.removeItemFromList(position);
        nlrv.resetSelectedItemPositions(); // clear selection
    }

    public void resetSelection(){
        nlrv.resetSelectedItemPositions(); // clear selection
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Note note);
    }

    private void markSelectedItem(Note note){
        nlrv.setLastSelectedItem();
        nlrv.setSelectedItem(note.getListPosition());
        nlrv.notifyItemChanged(nlrv.getLastSelectedItem());
        nlrv.notifyItemChanged(nlrv.getSelectedItem());
    }

}
