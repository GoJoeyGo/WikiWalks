package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class EditPictureDialog extends DialogFragment implements Picture.EditPictureCallback {

    static final int REQUEST_IMAGE_CAPTURE = 0;
    static final int REQUEST_IMAGE_SELECT = 1;
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
    Uri photoUri;

    public interface EditPictureDialogListener {
        void onEditPicture();
        void onDeletePicture(int position);
    }

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

    @Override
    public void onSubmitPictureSuccess() {
        listener.onEditPicture();
        dismiss();
    }

    @Override
    public void onSubmitPictureFailure() {
        Toast.makeText(getContext(), "Failed to submit picture!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditPictureSuccess() {
        listener.onEditPicture();
        dismiss();
    }

    @Override
    public void onEditPictureFailure() {
        Toast.makeText(getContext(), "Failed to edit picture!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePictureSuccess() {
        listener.onDeletePicture(getArguments().getInt("position"));
        dismiss();
    }

    @Override
    public void onDeletePictureFailure() {
        Toast.makeText(getContext(), "Failed to delete picture!", Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        listener = (EditPictureDialogListener) getParentFragment();
        type = (Picture.PictureType) getArguments().getSerializable("picture_type");
        parentId = getArguments().getInt("parent_id");
        if (getArguments().containsKey("position")) {
            if (type == Picture.PictureType.PATH)
                picture = PathMap.getInstance().getPathList().get(parentId).getPicturesList().get(getArguments().getInt("position"));
            else
                picture = PathMap.getInstance().getPointOfInterestList().get(parentId).getPicturesList().get(getArguments().getInt("position"));
            bitmap = getArguments().getParcelable("image");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_picture_dialog, null);
        imageView = view.findViewById(R.id.edit_picture_popup_selected_image);
        title = view.findViewById(R.id.edit_picture_popup_description);
        cameraButton = view.findViewById(R.id.picture_popup_camera_button);
        cameraButton.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getContext().getPackageManager()) != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + getContext().getString(R.string.app_name));
                    photoUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                } else {
                    File photoDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getContext().getString(R.string.app_name));
                    if (!photoDirectory.exists()) {
                        photoDirectory.mkdir();
                    }
                    photoUri = FileProvider.getUriForFile(getContext(), "com.wikiwalks.wikiwalks.fileprovider", new File(photoDirectory, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg"));
                }
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
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
                if (photoUri != null) {
                    Picture.submit(getContext(), type, parentId, photoUri, title.getEditText().getText().toString(), this);
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
                photoUri = Uri.parse(savedInstanceState.getString("uri"));
                loadIntoImageView();
            }
        }
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (photoUri != null) {
            outState.putString("uri", photoUri.toString());
        }
        outState.putString("description", title.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void loadIntoImageView() {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(photoUri);
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
            Picasso.get().load(photoUri).rotate(degree).into(imageView);
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
                photoUri = data.getData();
            }
            loadIntoImageView();
        }
        submitButton.setEnabled(true);
    }
}
