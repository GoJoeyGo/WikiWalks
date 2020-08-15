package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.AlertDialog;
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

import com.google.android.material.textfield.TextInputLayout;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;

public class EditReviewDialog extends DialogFragment implements Review.EditReviewCallback {

    EditReviewDialogListener listener;
    TextInputLayout message;
    RatingBar rating;
    Button saveButton;
    Button deleteButton;
    Button cancelButton;
    Review review;
    AlertDialog confirmationDialog;
    int parentId;
    Review.ReviewType type;

    public interface EditReviewDialogListener {
        void setEditReviewDialog(EditReviewDialog editReviewDialog);
        void onEditReview();
    }

    public static EditReviewDialog newInstance(Review.ReviewType type, int parentId) {
        Bundle args = new Bundle();
        args.putInt("parentId", parentId);
        args.putSerializable("type", type);
        EditReviewDialog dialog = new EditReviewDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onSubmitReviewSuccess() {
        listener.onEditReview();
        dismiss();
    }

    @Override
    public void onSubmitReviewFailure() {
        Toast.makeText(getContext(), "Failed to submit review!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditReviewSuccess() {
        listener.onEditReview();
        dismiss();
    }

    @Override
    public void onEditReviewFailure() {
        Toast.makeText(getContext(), "Failed to edit review!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteReviewSuccess() {
        listener.onEditReview();
        dismiss();
    }

    @Override
    public void onDeleteReviewFailure() {
        Toast.makeText(getContext(), "Failed to delete review!", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        listener = (EditReviewDialogListener) getParentFragment();
        listener.setEditReviewDialog(this);
        parentId = getArguments().getInt("parentId");
        type = (Review.ReviewType) getArguments().getSerializable("type");
        if (type == Review.ReviewType.PATH) {
            review = PathMap.getInstance().getPathList().get(parentId).getOwnReview();
        } else {
            review = PathMap.getInstance().getPointOfInterestList().get(parentId).getOwnReview();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_review_dialog, null);
        rating = view.findViewById(R.id.edit_review_rating_bar);
        rating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if ((int) rating > 0) {
                saveButton.setEnabled(true);
            } else {
                saveButton.setEnabled(false);
            }
        });
        message = view.findViewById(R.id.edit_review_path_name);
        saveButton = view.findViewById(R.id.edit_review_popup_save_button);
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
        deleteButton = view.findViewById(R.id.edit_review_popup_delete_button);
        cancelButton = view.findViewById(R.id.edit_review_popup_cancel_button);
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });
        if (review != null) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                confirmationDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this review?")
                        .setPositiveButton("Yes", (dialog, which) -> review.delete(getContext(), this))
                        .setNegativeButton("No", (dialog, which) -> confirmationDialog.dismiss()).create();
                confirmationDialog.show();
            });
            message.getEditText().setText(review.getMessage());
            rating.setRating(review.getRating());
        }
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
