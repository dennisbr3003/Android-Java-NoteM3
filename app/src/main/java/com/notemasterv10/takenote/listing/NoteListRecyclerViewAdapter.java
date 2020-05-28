package com.notemasterv10.takenote.listing;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.notemasterv10.takenote.R;
import com.notemasterv10.takenote.library.SharedResource;
import com.notemasterv10.takenote.listing.NoteListFragment.OnListFragmentInteractionListener;
import java.util.List;

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.ViewHolder> {

    private final List<Note> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context context;
    SharedResource sr = new SharedResource();
    private ViewHolder holder;
    private int selectedItem = -1;
    private int lastSelected = -1;

    private ClickInterface mClickInterface;

    public interface ClickInterface {
        void itemClickToOpen(Note note);
        void itemClickToDelete(Note note, View v, int position);
    }


    public NoteListRecyclerViewAdapter(List<Note> notes, OnListFragmentInteractionListener listener) {
        mValues = notes;
        mListener = listener;
    }

    public void setClickInterface(ClickInterface clickInterface) {
        mClickInterface = clickInterface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_note, parent, false);
        context = parent.getContext();
        holder = new ViewHolder(view);
        return holder;
    }

    public void resetSelectedItemPositions(){
        lastSelected = -1;
        selectedItem = -1;
        notifyDataSetChanged();
    }

    public void setLastSelectedItem(){
        lastSelected = selectedItem;
    }

    public void setSelectedItem(int position){
        selectedItem = position;
    }

    public int getLastSelectedItem(){
        return lastSelected;
    }

    public int getSelectedItem(){
        return selectedItem;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mContentView.setText(new String(mValues.get(position).getFile()));
        holder.mCreatedView.setText(String.format("%s %s", "Created:", mValues.get(position).getCreated()));

        mValues.get(position).setCurrentNote(sr.getOpenNoteName(context));
        if(mValues.get(position).isCurrentNote()){
            holder.mCurrentNote.setVisibility(View.VISIBLE);
        } else {
            holder.mCurrentNote.setVisibility(View.INVISIBLE);
        }

        if(position == selectedItem) {
           holder.itemView.setBackgroundColor(Color.WHITE);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemClickListener.setPosition(position);
        holder.itemDeleteClickListener.setPosition(position);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mNameView;
        public final TextView mContentView;
        public final TextView mCreatedView;
        public final ImageView mCurrentNote;
        public final ImageView mDeleteNote;
        public Note mItem;

        ItemClickListener itemClickListener;
        ItemDeleteClickListener itemDeleteClickListener;

        public ViewHolder(View view) {

            super(view);

            mView = view;

            mNameView = (TextView) view.findViewById(R.id.txtview_note_name);
            mContentView = (TextView) view.findViewById(R.id.content);
            mCreatedView = (TextView) view.findViewById(R.id.txt_created);
            mCurrentNote = (ImageView) view.findViewById(R.id.img_current);
            mDeleteNote = (ImageView) view.findViewById(R.id.img_deletenote);

            itemClickListener = new ItemClickListener();
            itemDeleteClickListener = new ItemDeleteClickListener();

            mView.setOnClickListener(itemClickListener);
            mDeleteNote.setOnClickListener(itemDeleteClickListener);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public void deleteItemFromList(int position){
        mValues.remove(position);
        notifyDataSetChanged();
    }

    public void loadData(List<Note> notes){
        mValues.clear();
        mValues.addAll(notes);
        notifyDataSetChanged();
    }

    private class ItemClickListener implements View.OnClickListener{

        private int mPosition;

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override
        public void onClick(View v) {
            mClickInterface.itemClickToOpen(mValues.get(mPosition));
        }
    }

    private class ItemDeleteClickListener implements View.OnClickListener{

        private int mPosition;

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override
        public void onClick(View v) {
            mClickInterface.itemClickToDelete(mValues.get(mPosition),v,mPosition);
        }
    }

}
