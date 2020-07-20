package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathPicture;
import com.wikiwalks.wikiwalks.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class EditPictureDialog extends DialogFragment implements PathPicture.PictureEditCallback {

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
    PathPicture pathPicture;
    AlertDialog confirmationDialog;

    public EditPictureDialog(PathPicture pathPicture) {
        this.pathPicture = pathPicture;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_picture_popup, null);
        imageView = view.findViewById(R.id.edit_picture_popup_selected_image);
        Picasso.get().load(pathPicture.getUrl()).into(imageView);
        title = view.findViewById(R.id.edit_picture_popup_description);
        title.getEditText().setText(pathPicture.getDescription());
        submitButton = view.findViewById(R.id.edit_picture_popup_submit_button);
        submitButton.setOnClickListener(v -> {
            if (!pathPicture.getDescription().equals(title.getEditText().getText().toString())) {
                pathPicture.edit(getContext(), title.getEditText().getText().toString(), this);
            } else {
                dismiss();
            }
        });
        deleteButton = view.findViewById(R.id.edit_picture_popup_delete_button);
        deleteButton.setOnClickListener(v -> {
            confirmationDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this picture?")
                    .setPositiveButton("Yes", (dialog, which) -> pathPicture.delete(getContext(), this))
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
