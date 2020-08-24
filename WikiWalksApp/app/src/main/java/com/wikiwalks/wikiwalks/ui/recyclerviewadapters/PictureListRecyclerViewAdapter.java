package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.wikiwalks.wikiwalks.Picture;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.PictureListFragment;

import java.util.ArrayList;

public class PictureListRecyclerViewAdapter extends RecyclerView.Adapter<PictureListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Picture> pictureList;
    private PictureListFragment context;

    public PictureListRecyclerViewAdapter(PictureListFragment context, ArrayList<Picture> pictureList) {
        this.context = context;
        this.pictureList = pictureList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context.getContext());
        View view = inflater.inflate(R.layout.picture_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Picture picture = pictureList.get(position);
        Picasso.get().load(context.getString(R.string.local_url) + "/pictures/" + picture.getUrl()).placeholder(R.drawable.banner_background).into(holder.imageView);
        if (picture.isEditable()) {
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD_ITALIC);
            holder.name.setText("You");
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> context.launchEditDialog(position, ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap()));
        } else {
            holder.editButton.setVisibility(View.INVISIBLE);
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.NORMAL);
            holder.name.setText(picture.getSubmitter());
        }
        if (!picture.getDescription().isEmpty()) {
            holder.description.setTypeface(holder.description.getTypeface(), Typeface.NORMAL);
            holder.description.setText(picture.getDescription());
        } else {
            holder.description.setTypeface(holder.description.getTypeface(), Typeface.ITALIC);
            holder.description.setText("no description written");
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return pictureList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView description;
        ImageView imageView;
        ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.path_picture_row_name);
            description = itemView.findViewById(R.id.path_picture_row_text);
            imageView = itemView.findViewById(R.id.path_picture_row_image);
            editButton = itemView.findViewById(R.id.path_picture_edit_button);
        }
    }
}
