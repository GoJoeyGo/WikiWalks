package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathReview;
import com.wikiwalks.wikiwalks.R;

public class EditReviewDialog extends DialogFragment implements PathReview.SubmitReviewCallback {

    public interface EditReviewCallback {
        void onSuccess();
    }

    @Override
    public void onSubmitReviewSuccess() {
        listener.onSuccess();
        getDialog().dismiss();
    }

    @Override
    public void onSubmitReviewFailure() {
    }

    EditReviewCallback listener;
    TextInputLayout message;
    RatingBar rating;
    Button saveButton;
    Button deleteButton;
    Button cancelButton;
    Path path;
    PathReview review;
    AlertDialog confirmationDialog;

    public EditReviewDialog(Path path) {
        this.path = path;
        review = path.getOwnReview();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.review_popup, null);
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
                PathReview newReview = new PathReview(-1, path, "", (int) rating.getRating(), message.getEditText().getText().toString(), true);
                newReview.submit(getContext(), this);
            } else {
                review.edit(getContext(), message.getEditText().getText().toString(), (int) rating.getRating(), this);
            }
        });
        deleteButton = view.findViewById(R.id.edit_review_popup_delete_button);
        deleteButton.setOnClickListener(v -> {
                    confirmationDialog = new AlertDialog.Builder(getContext())
                            .setTitle("Confirm Deletion")
                            .setMessage("Are you sure you want to delete this review?")
                            .setPositiveButton("Yes", (dialog, which) -> review.delete(getContext(), this))
                            .setNegativeButton("No", (dialog, which) -> confirmationDialog.dismiss()).create();
                    confirmationDialog.show();
                });
        cancelButton = view.findViewById(R.id.edit_review_popup_cancel_button);
        cancelButton.setOnClickListener(v -> {
            getDialog().cancel();
        });
        if (review != null) {
            deleteButton.setVisibility(View.VISIBLE);
            message.getEditText().setText(review.getMessage());
            rating.setRating(review.getRating());
        }
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (EditReviewCallback) getTargetFragment();
    }
}
