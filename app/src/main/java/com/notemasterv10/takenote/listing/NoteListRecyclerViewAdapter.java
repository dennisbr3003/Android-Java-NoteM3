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

    private int selectedPos = RecyclerView.NO_POSITION;
    private final List<Note> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context context;
    SharedResource sr = new SharedResource();
    // Database sdb = new Database(context);

    public NoteListRecyclerViewAdapter(List<Note> notes, OnListFragmentInteractionListener listener) {
        mValues = notes;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_note, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mContentView.setText(new String(mValues.get(position).getFile()));
        holder.mCreatedView.setText(String.format("%s %s", "Created:", mValues.get(position).getCreated()));
        if(mValues.get(position).getName().equals(sr.getOpenNoteName(context))){
            holder.mCurrentNote.setVisibility(View.VISIBLE);
        } else {
            holder.mCurrentNote.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setSelected(selectedPos == position);

        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        if (holder.itemView.isSelected()) {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                notifyItemChanged(selectedPos);

                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
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
        public Note mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.txtview_note_name);
            mContentView = (TextView) view.findViewById(R.id.content);
            mCreatedView = (TextView) view.findViewById(R.id.txt_created);
            mCurrentNote = (ImageView) view.findViewById(R.id.img_current);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
