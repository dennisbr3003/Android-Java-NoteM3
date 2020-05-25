package com.notemasterv10.takenote.listing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
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
    private boolean deleteConfirmed;
    SharedResource sr = new SharedResource();

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
        mValues.get(position).setCurrentNote(sr.getOpenNoteName(context));
        if(mValues.get(position).isCurrentNote()){
            holder.mCurrentNote.setVisibility(View.VISIBLE);
        } else {
            holder.mCurrentNote.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setSelected(selectedPos == position);

        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        if (holder.itemView.isSelected()) {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        // open file action -->
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                notifyItemChanged(selectedPos);

                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        // delete file action -->
        holder.mDeleteNote.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                notifyItemChanged(selectedPos);
                selectedPos = holder.getLayoutPosition();
                notifyItemChanged(selectedPos);

                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                builder.setTitle(R.string.ConfirmDialogTitle);
                builder.setMessage(R.string.AreYouSure);
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
                            mValues.remove(selectedPos);
                            // this statement I believe forces me to build the dialog in the adapter (to good not to use)
                            notifyDataSetChanged();
                            if (null != mListener) {
                                mListener.onListFragmentInteractionDelete(holder.mItem);
                            }
                        }
                    }
                });
                AlertDialog dlg = builder.create();
                dlg.show();

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
        public final ImageView mDeleteNote;
        public Note mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.txtview_note_name);
            mContentView = (TextView) view.findViewById(R.id.content);
            mCreatedView = (TextView) view.findViewById(R.id.txt_created);
            mCurrentNote = (ImageView) view.findViewById(R.id.img_current);
            mDeleteNote = (ImageView) view.findViewById(R.id.img_deletenote);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }




}
