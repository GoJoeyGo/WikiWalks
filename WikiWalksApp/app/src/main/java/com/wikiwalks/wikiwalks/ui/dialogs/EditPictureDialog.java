package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.wikiwalks.wikiwalks.Picture;
import com.wikiwalks.wikiwalks.R;

public class EditPictureDialog extends DialogFragment implements Picture.EditPictureCallback {

    @Override
    public void onEditPictureSuccess() {
        listener.onEdit();
        dismiss();
    }

    @Override
    public void onEditPictureFailure() {
        Toast.makeText(getContext(), "Failed to edit picture!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePictureSuccess() {
        listener.onDelete();
        dismiss();
    }

    @Override
    public void onDeletePictureFailure() {
        Toast.makeText(getContext(), "Failed to delete picture!", Toast.LENGTH_SHORT).show();
    }

    public interface EditPictureDialogListener {
        void onEdit();
        void onDelete();
    }

    EditPictureDialogListener listener;
    TextInputLayout title;
    ImageView imageView;
    Button submitButton;
    Button deleteButton;
    Button cancelButton;
    Picture picture;
    AlertDialog confirmationDialog;
    Bitmap bitmap;

    public EditPictureDialog(Picture picture, Bitmap bitmap) {
        this.picture = picture;
        this.bitmap = bitmap;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_picture_popup, null);
        imageView = view.findViewById(R.id.edit_picture_popup_selected_image);
        imageView.setImageBitmap(bitmap);
        title = view.findViewById(R.id.edit_picture_popup_description);
        title.getEditText().setText(picture.getDescription());
        submitButton = view.findViewById(R.id.edit_picture_popup_submit_button);
        submitButton.setOnClickListener(v -> {
            if (!picture.getDescription().equals(title.getEditText().getText().toString())) {
                picture.edit(getContext(), title.getEditText().getText().toString(), this);
            } else {
                dismiss();
            }
        });
        deleteButton = view.findViewById(R.id.edit_picture_popup_delete_button);
        deleteButton.setOnClickListener(v -> {
            confirmationDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this picture?")
                    .setPositiveButton("Yes", (dialog, which) -> picture.delete(getContext(), this))
                    .setNegativeButton("No", (dialog, which) -> confirmationDialog.dismiss()).create();
            confirmationDialog.show();
        });
        cancelButton = view.findViewById(R.id.edit_picture_popup_cancel_button);
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (EditPictureDialogListener) getTargetFragment();
    }
}
