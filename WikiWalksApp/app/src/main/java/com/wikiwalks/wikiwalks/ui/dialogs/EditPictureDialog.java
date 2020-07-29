package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.Picture;
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

public class EditPictureDialog extends DialogFragment implements Picture.EditPictureCallback {

    @Override
    public void onSubmitPictureSuccess() {
        listener.onEdit();
        dismiss();
    }

    @Override
    public void onSubmitPictureFailure() {
        Toast.makeText(getContext(), "Failed to submit picture!", Toast.LENGTH_SHORT).show();
    }

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
        void setPictureDialog(EditPictureDialog editPictureDialog);
        void onEdit();
        void onDelete();
    }


    Picture.PictureType type;
    EditPictureDialogListener listener;
    TextInputLayout title;
    int parentId;
    ImageView imageView;
    Button cameraButton;
    Button galleryButton;
    Button submitButton;
    Button deleteButton;
    Button cancelButton;
    Picture picture;
    AlertDialog confirmationDialog;
    Bitmap bitmap;
    Uri photoURI;
    String filename;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_SELECT = 1;

    public static EditPictureDialog newInstance(Picture.PictureType type, int parentId, int position, Bitmap bitmap) {
        Bundle args = new Bundle();
        EditPictureDialog fragment = new EditPictureDialog();
        args.putSerializable("picture_type", type);
        args.putInt("parent_id", parentId);
        if (position > -1) args.putInt("position", position);
        if (bitmap != null) args.putParcelable("image", bitmap);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        listener = (EditPictureDialogListener) getParentFragment();
        listener.setPictureDialog(this);
        type = (Picture.PictureType) getArguments().getSerializable("picture_type");
        parentId = getArguments().getInt("parent_id");
        if (getArguments().containsKey("position")) {
            if (type == Picture.PictureType.PATH) picture = PathMap.getInstance().getPathList().get(parentId).getPicturesList().get(getArguments().getInt("position"));
            else picture = PathMap.getInstance().getPointOfInterestList().get(parentId).getPicturesList().get(getArguments().getInt("position"));
            bitmap = getArguments().getParcelable("image");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_picture_popup, null);
        imageView = view.findViewById(R.id.edit_picture_popup_selected_image);
        title = view.findViewById(R.id.edit_picture_popup_description);
        cameraButton = view.findViewById(R.id.picture_popup_camera_button);
        cameraButton.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getContext().getPackageManager()) != null) {
                try {
                    File photoFile = File.createTempFile(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", ".jpg", getContext().getExternalFilesDir(Environment.DIRECTORY_DCIM));
                    photoURI = FileProvider.getUriForFile(getContext(), "com.wikiwalks.wikiwalks.fileprovider", photoFile);
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
                } catch (IOException ex) {
                    Log.e("PATH_PHOTO", ex.toString());
                }
            }
        });
        galleryButton = view.findViewById(R.id.picture_popup_gallery_button);
        galleryButton.setOnClickListener(v -> {
            Intent selectPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(selectPicture, REQUEST_IMAGE_SELECT);
        });
        submitButton = view.findViewById(R.id.edit_picture_popup_submit_button);
        submitButton.setOnClickListener(v -> {
            if (picture != null) {
                if (!picture.getDescription().equals(title.getEditText().getText().toString())) {
                    picture.edit(getContext(), title.getEditText().getText().toString(), this);
                } else {
                    dismiss();
                }
            } else {
                if (photoURI != null) {
                    Picture.submit(getContext(), type, parentId, filename, photoURI, title.getEditText().getText().toString(), this);
                }
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
        if (picture != null) {
            title.getEditText().setText(picture.getDescription());
            imageView.setImageBitmap(bitmap);
            cameraButton.setVisibility(View.GONE);
            galleryButton.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }
        if (savedInstanceState != null) {
            title.getEditText().setText(savedInstanceState.getString("description"));
            if (savedInstanceState.containsKey("filename")) {
                filename = savedInstanceState.getString("filename");
                photoURI = Uri.parse(savedInstanceState.getString("uri"));
                loadIntoImageView();
            }
        }
        builder.setView(view);
        return builder.create();
    }

    private void loadIntoImageView() {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(photoURI);
            ExifInterface ei = new ExifInterface(inputStream);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degree = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
            Picasso.get().load(photoURI).rotate(degree).into(imageView);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        submitButton.setEnabled(false);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_SELECT && data != null) {
                photoURI = data.getData();
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(photoURI);
                    byte[] buffer = new byte[inputStream.available()];
                    inputStream.read(buffer);
                    File targetFile = File.createTempFile(photoURI.getLastPathSegment(), ".jpg", getContext().getExternalCacheDir());
                    filename = targetFile.getAbsolutePath();
                    OutputStream outputStream = new FileOutputStream(targetFile);
                    outputStream.write(buffer);
                    outputStream.close();
                    inputStream.close();
                    targetFile.deleteOnExit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                filename = getContext().getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + photoURI.getLastPathSegment();
            }
            loadIntoImageView();
        }
        submitButton.setEnabled(true);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (photoURI != null) {
            outState.putString("uri", photoURI.toString());
            outState.putString("filename", filename);
        };
        outState.putString("description", title.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }
}
