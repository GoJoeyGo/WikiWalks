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
import com.wikiwalks.wikiwalks.Photo;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.PhotoListFragment;

import java.util.ArrayList;

public class PhotoListRecyclerViewAdapter extends RecyclerView.Adapter<PhotoListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Photo> photoList;
    private PhotoListFragment parentFragment;

    public PhotoListRecyclerViewAdapter(PhotoListFragment parentFragment, ArrayList<Photo> photoList) {
        this.parentFragment = parentFragment;
        this.photoList = photoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
        View view = inflater.inflate(R.layout.photo_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Photo photo = photoList.get(position);
        Picasso.get().load(parentFragment.getString(R.string.local_url) + "/pictures/" + photo.getUrl()).resize(photo.getWidth(), photo.getHeight()).onlyScaleDown().placeholder(R.drawable.banner_background).into(holder.imageView);

        if (photo.isEditable()) {
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD_ITALIC);
            holder.name.setText(parentFragment.getString(R.string.you));
            holder.editButton.setVisibility(View.VISIBLE);
            holder.editButton.setOnClickListener(v -> parentFragment.launchEditDialog(position, ((BitmapDrawable) holder.imageView.getDrawable()).getBitmap()));
        } else {
            holder.editButton.setVisibility(View.INVISIBLE);
            holder.name.setTypeface(holder.name.getTypeface(), Typeface.NORMAL);
            holder.name.setText(photo.getSubmitter());
        }

        if (!photo.getDescription().isEmpty()) {
            holder.description.setTypeface(holder.description.getTypeface(), Typeface.NORMAL);
            holder.description.setText(photo.getDescription());
        } else {
            holder.description.setTypeface(holder.description.getTypeface(), Typeface.ITALIC);
            holder.description.setText(parentFragment.getString(R.string.photo_no_caption));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView description;
        private ImageView imageView;
        private ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.path_photo_row_name);
            description = itemView.findViewById(R.id.path_photo_row_text);
            imageView = itemView.findViewById(R.id.path_photo_row_image);
            editButton = itemView.findViewById(R.id.path_photo_edit_button);
        }
    }
}
