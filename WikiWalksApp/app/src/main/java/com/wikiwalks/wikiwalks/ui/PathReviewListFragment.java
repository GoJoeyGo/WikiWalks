package com.wikiwalks.wikiwalks.ui;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PathReview;
import com.wikiwalks.wikiwalks.ui.dialogs.EditReviewDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.PathReviewListRecyclerViewAdapter;
import com.wikiwalks.wikiwalks.R;

import java.util.ArrayList;

public class PathReviewListFragment extends Fragment implements Path.GetAdditionalCallback, EditReviewDialog.EditReviewCallback {

    Button writeReviewButton;
    Path path;
    RecyclerView recyclerView;
    PathReviewListRecyclerViewAdapter recyclerViewAdapter;
    Toolbar toolbar;
    ArrayList<PathReview> pathReviews;
    ConstraintLayout ownReview;
    TextView ownReviewName;
    TextView ownReviewMessage;
    RatingBar ownReviewRatingBar;
    View separator;
    TextView noReviewsIndicator;

    public static PathReviewListFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        PathReviewListFragment fragment = new PathReviewListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        pathReviews = path.getPathReviews();
        final View rootView = inflater.inflate(R.layout.path_review_list_fragment, container, false);
        ownReview = rootView.findViewById(R.id.path_review_list_own_review);
        ownReviewName = rootView.findViewById(R.id.path_review_own_review_row_name);
        ownReviewMessage = rootView.findViewById(R.id.path_review_own_review_row_text);
        ownReviewRatingBar = rootView.findViewById(R.id.path_review_own_review_row_rating);
        separator = rootView.findViewById(R.id.path_review_separator);
        toolbar = rootView.findViewById(R.id.path_review_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Reviews - " + path.getName());
        noReviewsIndicator = rootView.findViewById(R.id.no_reviews_indicator);
        recyclerView = rootView.findViewById(R.id.path_review_list_recyclerview);
        recyclerViewAdapter = new PathReviewListRecyclerViewAdapter(this, pathReviews);
        recyclerView.setAdapter(recyclerViewAdapter);
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        } else {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    path.getReviews(getContext(), PathReviewListFragment.this);
                }
            }
        });
        writeReviewButton = rootView.findViewById(R.id.path_write_review_button);
        writeReviewButton.setOnClickListener(v -> {
            EditReviewDialog dialog = new EditReviewDialog(path);
            dialog.setTargetFragment(this, 0);
            dialog.show(getActivity().getSupportFragmentManager(), "EditPopup");
        });
        updateRecyclerView();
        path.getReviews(getContext(), this);
        return rootView;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void updateRecyclerView() {
        if (path.getPathReviews().size() == 0 && path.getOwnReview() == null) {
            recyclerView.setVisibility(View.GONE);
            noReviewsIndicator.setVisibility(View.VISIBLE);
            ownReview.setVisibility(View.GONE);
            separator.setVisibility(View.GONE);
            writeReviewButton.setText("WRITE REVIEW");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noReviewsIndicator.setVisibility(View.GONE);
            if (path.getOwnReview() != null) {
                ownReviewRatingBar.setRating(path.getOwnReview().getRating());
                if (!path.getOwnReview().getMessage().isEmpty()) {
                    ownReviewMessage.setTypeface(ownReviewMessage.getTypeface(), Typeface.NORMAL);
                    ownReviewMessage.setText(path.getOwnReview().getMessage());
                } else {
                    ownReviewMessage.setTypeface(ownReviewMessage.getTypeface(), Typeface.ITALIC);
                    ownReviewMessage.setText("no review written");
                }
                ownReview.setVisibility(View.VISIBLE);
                separator.setVisibility(View.VISIBLE);
                writeReviewButton.setText("EDIT REVIEW");
            } else {
                ownReview.setVisibility(View.GONE);
                separator.setVisibility(View.GONE);
                writeReviewButton.setText("WRITE REVIEW");
            }
            pathReviews = path.getPathReviews();
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetAdditionalSuccess() {
        writeReviewButton.setEnabled(true);
        updateRecyclerView();
    }

    @Override
    public void onGetAdditionalFailure() {
        Toast.makeText(getContext(), "Failed to get reviews", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccess() {
        updateRecyclerView();
    }
}
