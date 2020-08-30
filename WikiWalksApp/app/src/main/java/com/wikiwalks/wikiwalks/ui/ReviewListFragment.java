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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;
import com.wikiwalks.wikiwalks.ui.dialogs.EditReviewDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.ReviewListRecyclerViewAdapter;

import java.util.ArrayList;

public class ReviewListFragment extends Fragment implements Review.GetReviewCallback, EditReviewDialog.EditReviewDialogListener {

    private Button writeReviewButton;
    private int parentId;
    private RecyclerView recyclerView;
    private ArrayList<Review> reviews;
    private ConstraintLayout ownReviewLayout;
    private TextView ownReviewMessage;
    private RatingBar ownReviewRatingBar;
    private View separator;
    private TextView noReviewsIndicator;
    private Review.ReviewType type;
    private Path path;
    private PointOfInterest pointOfInterest;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static ReviewListFragment newInstance(Review.ReviewType type, int parentId) {
        Bundle args = new Bundle();
        args.putInt("parentId", parentId);
        args.putSerializable("type", type);
        ReviewListFragment fragment = new ReviewListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        parentId = getArguments().getInt("parentId");
        type = (Review.ReviewType) getArguments().getSerializable("type");
        String title;
        if (type == Review.ReviewType.PATH) {
            path = PathMap.getInstance().getPathList().get(parentId);
            reviews = path.getReviewsList();
            title = path.getName();
        } else {
            pointOfInterest = PathMap.getInstance().getPointOfInterestList().get(parentId);
            reviews = pointOfInterest.getReviewsList();
            title = pointOfInterest.getName();
        }
        final View rootView = inflater.inflate(R.layout.review_list_fragment, container, false);
        ownReviewLayout = rootView.findViewById(R.id.path_review_list_own_review);
        ownReviewMessage = rootView.findViewById(R.id.path_review_own_review_row_text);
        ownReviewRatingBar = rootView.findViewById(R.id.path_review_own_review_row_rating);
        separator = rootView.findViewById(R.id.path_review_separator);
        MaterialToolbar toolbar = rootView.findViewById(R.id.path_review_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(String.format(getString(R.string.reviews_title), title));
        noReviewsIndicator = rootView.findViewById(R.id.no_reviews_indicator);
        recyclerView = rootView.findViewById(R.id.path_review_list_recyclerview);
        recyclerView.setAdapter(new ReviewListRecyclerViewAdapter(this, reviews));
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        } else {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing()) {
                    updateReviewsList(false);
                }
            }
        });
        swipeRefreshLayout = rootView.findViewById(R.id.review_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> updateReviewsList(true));
        writeReviewButton = rootView.findViewById(R.id.path_write_review_button);
        writeReviewButton.setOnClickListener(v -> EditReviewDialog.newInstance(type, parentId).show(getChildFragmentManager(), "EditPopup"));
        updateReviewsList(true);
        return rootView;
    }

    public void updateReviewsList(boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (type == Review.ReviewType.PATH) {
            path.getReviews(getContext(), refresh, ReviewListFragment.this);
        } else {
            pointOfInterest.getReviews(getContext(), refresh, ReviewListFragment.this);
        }
    }

    public void updateRecyclerView() {
        Review ownReview = (type == Review.ReviewType.PATH) ? path.getOwnReview() : pointOfInterest.getOwnReview();
        if (reviews.size() == 0 && ownReview == null) {
            recyclerView.setVisibility(View.GONE);
            noReviewsIndicator.setVisibility(View.VISIBLE);
            ownReviewLayout.setVisibility(View.GONE);
            separator.setVisibility(View.GONE);
            writeReviewButton.setText(R.string.write_review);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noReviewsIndicator.setVisibility(View.GONE);
            if (ownReview != null) {
                ownReviewRatingBar.setRating(ownReview.getRating());
                if (!ownReview.getMessage().isEmpty()) {
                    ownReviewMessage.setTypeface(ownReviewMessage.getTypeface(), Typeface.NORMAL);
                    ownReviewMessage.setText(ownReview.getMessage());
                } else {
                    ownReviewMessage.setTypeface(ownReviewMessage.getTypeface(), Typeface.ITALIC);
                    ownReviewMessage.setText(R.string.review_no_text);
                }
                ownReviewLayout.setVisibility(View.VISIBLE);
                separator.setVisibility(View.VISIBLE);
                writeReviewButton.setText(R.string.edit_review);
            } else {
                ownReviewLayout.setVisibility(View.GONE);
                separator.setVisibility(View.GONE);
                writeReviewButton.setText(R.string.write_review);
            }
            recyclerView.getAdapter().notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onGetReviewSuccess() {
        writeReviewButton.setEnabled(true);
        updateRecyclerView();
    }

    @Override
    public void onGetReviewFailure() {
        Toast.makeText(getContext(), R.string.get_reviews_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditReview() {
        updateRecyclerView();
    }
}
