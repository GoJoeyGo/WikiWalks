package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;
import com.wikiwalks.wikiwalks.ui.ReviewListFragment;

import java.util.ArrayList;

public class ReviewListRecyclerViewAdapter extends RecyclerView.Adapter<ReviewListRecyclerViewAdapter.ViewHolder> {
    ArrayList<Review> reviewList;
    ReviewListFragment context;

    public ReviewListRecyclerViewAdapter(ReviewListFragment context, ArrayList<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context.getContext());
        View view = inflater.inflate(R.layout.review_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.name.setText(reviewList.get(position).getName());
        holder.ratingBar.setRating(reviewList.get(position).getRating());
        if (!reviewList.get(position).getMessage().isEmpty()) {
            holder.message.setText(reviewList.get(position).getMessage());
        } else {
            holder.message.setText("no review written");
            holder.message.setTypeface(holder.message.getTypeface(), Typeface.ITALIC);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        RatingBar ratingBar;
        TextView message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.path_review_row_name);
            ratingBar = itemView.findViewById(R.id.path_review_row_rating);
            message = itemView.findViewById(R.id.path_review_row_text);
        }
    }
}
