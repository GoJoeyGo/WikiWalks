package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;
import com.wikiwalks.wikiwalks.ui.ReviewListFragment;
import com.wikiwalks.wikiwalks.ui.dialogs.ReportDialog;

import java.util.ArrayList;

public class ReviewListRecyclerViewAdapter extends RecyclerView.Adapter<ReviewListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Review> reviewList;
    private ReviewListFragment parentFragment;

    public ReviewListRecyclerViewAdapter(ReviewListFragment parentFragment, ArrayList<Review> reviewList) {
        this.parentFragment = parentFragment;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
        View view = inflater.inflate(R.layout.review_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Review review = reviewList.get(position);
        holder.name.setText(review.getName());
        holder.ratingBar.setRating(review.getRating());
        if (!review.getMessage().isEmpty()) {
            holder.message.setText(review.getMessage());
        } else {
            holder.message.setText(parentFragment.getText(R.string.review_no_text));
            holder.message.setTypeface(holder.message.getTypeface(), Typeface.ITALIC);
        }
        holder.reportButton.setOnClickListener(v -> ReportDialog.newInstance(ReportDialog.ReportType.REVIEW, review.getId()).show(parentFragment.getChildFragmentManager(), "ReportPopup"));
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

        private TextView name;
        private RatingBar ratingBar;
        private TextView message;
        private ImageButton reportButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.review_row_name);
            ratingBar = itemView.findViewById(R.id.review_row_rating);
            message = itemView.findViewById(R.id.review_row_text);
            reportButton = itemView.findViewById(R.id.review_row_report_button);
        }
    }
}
