package com.notemasterv10.takenote.listing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.notemasterv10.takenote.Database;
import com.notemasterv10.takenote.R;

import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<Note> note_list = new ArrayList<Note>();
    private View v;

    public NoteListFragment() {
    }

    public static NoteListFragment newInstance(int columnCount) {
        NoteListFragment fragment = new NoteListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();

            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new NoteListRecyclerViewAdapter(note_list, mListener));

        }
        v = view;
        return view;
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
    public String runAnotherTest(){
        return "test2";
    }

    @Override
    public void onResume() {
        super.onResume();
        final Database sdb = new Database(getContext());

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
                RecyclerView recyclerView = (RecyclerView) v;
                if (mColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
                }
                recyclerView.setAdapter(new NoteListRecyclerViewAdapter(note_list, mListener));
            }
        };
        asynctask.execute();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        final Database sdb = new Database(getContext());

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
                RecyclerView recyclerView = (RecyclerView) view;
                if (mColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
                }
                recyclerView.setAdapter(new NoteListRecyclerViewAdapter(note_list, mListener));
            }
        };
        asynctask.execute();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        note_list.clear();
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Note note);
    }
}
