package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;

public class ReviewDialog extends DialogFragment implements Review.EditReviewCallback {

    private int parentId;
    private EditReviewDialogListener listener;
    private TextInputLayout message;
    private RatingBar rating;
    private Button saveButton;
    private Review review;
    private Review.ReviewType type;

    public interface EditReviewDialogListener {
        void onEditReview();
    }

    public static ReviewDialog newInstance(Review.ReviewType type, int parentId) {
        Bundle args = new Bundle();
        args.putInt("parentId", parentId);
        args.putSerializable("type", type);
        ReviewDialog dialog = new ReviewDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onEditReviewSuccess() {
        listener.onEditReview();
        dismiss();
        Toast.makeText(getContext(), getString(R.string.save_review_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditReviewFailure() {
        Toast.makeText(getContext(), getString(R.string.save_review_failure), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteReviewSuccess() {
        listener.onEditReview();
        dismiss();
        Toast.makeText(getContext(), getString(R.string.delete_review_success), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteReviewFailure() {
        Toast.makeText(getContext(), getString(R.string.delete_review_failure), Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.DialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.review_dialog, null);
        builder.setTitle(R.string.review);

        listener = (EditReviewDialogListener) getParentFragment();
        parentId = getArguments().getInt("parentId");
        type = (Review.ReviewType) getArguments().getSerializable("type");
        review = type == Review.ReviewType.PATH ? PathMap.getInstance().getPathList().get(parentId).getOwnReview() : PathMap.getInstance().getPointOfInterestList().get(parentId).getOwnReview();

        rating = view.findViewById(R.id.review_dialog_rating_bar);
        rating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if ((int) rating > 0) {
                saveButton.setEnabled(true);
            } else {
                saveButton.setEnabled(false);
            }
        });

        message = view.findViewById(R.id.review_dialog_text);

        saveButton = view.findViewById(R.id.review_dialog_save_button);
        saveButton.setOnClickListener(v -> {
            if (review == null) {
                Review.submit(getContext(), type, parentId, message.getEditText().getText().toString(), (int) rating.getRating(), this);
            } else {
                if (!review.getMessage().equals(message.getEditText().getText().toString()) || (int) rating.getRating() != review.getRating()) {
                    review.edit(getContext(), message.getEditText().getText().toString(), (int) rating.getRating(), this);
                } else {
                    dismiss();
                }
            }
        });

        Button deleteButton = view.findViewById(R.id.review_dialog_delete_button);
        if (review != null) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(getContext())
                    .setTitle(R.string.delete_review_prompt)
                    .setPositiveButton(R.string.yes, (dialog, which) -> review.delete(getContext(), this))
                    .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                    .create().show());
            message.getEditText().setText(review.getMessage());
            rating.setRating(review.getRating());
        }

        Button cancelButton = view.findViewById(R.id.review_dialog_cancel_button);
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        if (savedInstanceState != null && savedInstanceState.containsKey("review_text")) {
            message.getEditText().setText(savedInstanceState.getString("review_text"));
            rating.setRating(savedInstanceState.getInt("review_stars"));
        }

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("review_text", message.getEditText().getText().toString());
        outState.putInt("review_stars", (int) rating.getRating());
        super.onSaveInstanceState(outState);
    }
}
