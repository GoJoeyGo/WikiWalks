package com.wikiwalks.wikiwalks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

public class PathReviewListFragment extends Fragment implements Path.GetReviewsCallback {

    Button writeReviewButton;
    Button deleteButton;
    SupportMapFragment mapFragment;
    Path path;
    RecyclerView recyclerView;
    AlertDialog confirmationDialog;
    PathReviewListRecyclerViewAdapter recyclerViewAdapter;
    Toolbar toolbar;
    ArrayList<PathReview> pathReviews;
    ConstraintLayout ownReview;
    TextView ownReviewName;
    TextView ownReviewMessage;
    RatingBar ownReviewRatingBar;
    View separator;

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
        pathReviews = new ArrayList<>();
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
        recyclerView = rootView.findViewById(R.id.path_review_list_recyclerview);
        recyclerViewAdapter = new PathReviewListRecyclerViewAdapter(this, pathReviews);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        updateRecyclerView();
        writeReviewButton = rootView.findViewById(R.id.path_write_review_button);
        path.getReviews(getContext(), this);
        return rootView;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void updateRecyclerView() {
        for (PathReview pathReview : path.getPathReviews()) {
            if (pathReview.isEditable()) {
                ownReviewRatingBar.setRating(pathReview.getRating());
                ownReviewMessage.setText(pathReview.getMessage());
                ownReview.setVisibility(View.VISIBLE);
                separator.setVisibility(View.VISIBLE);
            } else if (!pathReviews.contains(pathReview)) {
                pathReviews.add(pathReview);
            }
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetReviewsSuccess() {
        updateRecyclerView();
    }

    @Override
    public void onGetReviewsFailure() {
        Toast.makeText(getContext(), "Failed to get reviews", Toast.LENGTH_SHORT).show();
    }
}
