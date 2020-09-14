package com.wikiwalks.wikiwalks.ui.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;
import com.wikiwalks.wikiwalks.CustomActivityResultContracts;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.Photo;
import com.wikiwalks.wikiwalks.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoDialog extends DialogFragment implements Photo.EditPhotoCallback {

    private Photo.PhotoType type;
    private EditPhotoDialogListener listener;
    private TextInputLayout title;
    private int parentId;
    private ImageView imageView;
    private Button submitButton;
    private Photo photo;
    private Uri photoUri;

    private ActivityResultLauncher<Uri> takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), isSuccess -> {
        if (isSuccess) {
            submitButton.setEnabled(false);
            loadIntoImageView();
            submitButton.setEnabled(true);
        } else {
            photoUri = null;
        }
    });
    private ActivityResultLauncher<String> selectPhoto = registerForActivityResult(new CustomActivityResultContracts.SelectPhoto(), uri -> {
        if (uri != null) {
            submitButton.setEnabled(false);
            photoUri = uri;
            loadIntoImageView();
            submitButton.setEnabled(true);
        }
    });

    public interface EditPhotoDialogListener {
        void onEditPhoto();
        void onDeletePhoto(int position);
    }

    public static PhotoDialog newInstance(Photo.PhotoType type, int parentId, int position, Bitmap bitmap) {
        Bundle args = new Bundle();
        PhotoDialog fragment = new PhotoDialog();
        args.putSerializable("photo_type", type);
        args.putInt("parent_id", parentId);
        args.putInt("position", position);
        if (bitmap != null) {
            args.putParcelable("image", bitmap);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onEditPhotoSuccess() {
        listener.onEditPhoto();
        dismiss();
        Toast.makeText(getContext(), R.string.save_photo_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditPhotoFailure() {
        Toast.makeText(getContext(), R.string.save_photo_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePhotoSuccess() {
        listener.onDeletePhoto(getArguments().getInt("position"));
        dismiss();
        Toast.makeText(getContext(), R.string.delete_group_walk_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePhotoFailure() {
        Toast.makeText(getContext(), R.string.delete_photo_failure, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.DialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.photo_dialog, null);
        builder.setTitle(R.string.photo);

        listener = (EditPhotoDialogListener) getParentFragment();
        type = (Photo.PhotoType) getArguments().getSerializable("photo_type");
        parentId = getArguments().getInt("parent_id");
        if (getArguments().getInt("position") > -1) {
            photo = type == Photo.PhotoType.PATH ? PathMap.getInstance().getPathList().get(parentId).getPhotosList().get(getArguments().getInt("position")) : PathMap.getInstance().getPointOfInterestList().get(parentId).getPhotosList().get(getArguments().getInt("position"));
        }

        imageView = view.findViewById(R.id.photo_dialog_selected_image);
        title = view.findViewById(R.id.photo_dialog_caption_input);

        Button cameraButton = view.findViewById(R.id.photo_dialog_camera_button);
        cameraButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + R.string.app_name);
                photoUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                takePhoto.launch(photoUri);
            } else {
                MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, granted -> {
                    if (granted) {
                        File photoDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_name));
                        if (!photoDirectory.exists()) {
                            photoDirectory.mkdir();
                        }
                        photoUri = FileProvider.getUriForFile(getContext(), "com.wikiwalks.wikiwalks.fileprovider", new File(photoDirectory, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg"));
                        takePhoto.launch(photoUri);
                    }
                });
            }
        });

        Button galleryButton = view.findViewById(R.id.photo_dialog_gallery_button);
        galleryButton.setOnClickListener(v -> MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, granted -> {
            if (granted) {
                selectPhoto.launch(null);
            }
        }));

        submitButton = view.findViewById(R.id.photo_dialog_save_button);
        submitButton.setOnClickListener(v -> {
            if (photo != null) {
                if (!photo.getDescription().equals(title.getEditText().getText().toString())) {
                    photo.edit(getContext(), title.getEditText().getText().toString(), this);
                } else {
                    dismiss();
                }
            } else {
                if (photoUri != null) {
                    Photo.submit(getContext(), type, parentId, photoUri, title.getEditText().getText().toString(), this);
                }
            }
        });

        Button deleteButton = view.findViewById(R.id.photo_dialog_delete_button);
        deleteButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.delete_photo_prompt)
                .setPositiveButton(R.string.yes, (dialog, which) -> photo.delete(getContext(), this))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .create().show());

        Button cancelButton = view.findViewById(R.id.photo_dialog_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());

        if (photo != null) {
            title.getEditText().setText(photo.getDescription());
            imageView.setImageBitmap(getArguments().getParcelable("image"));
            cameraButton.setVisibility(View.GONE);
            galleryButton.setVisibility(View.GONE);
            submitButton.setEnabled(true);
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            title.getEditText().setText(savedInstanceState.getString("description"));
            if (savedInstanceState.containsKey("uri")) {
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
        } catch (IOException e) {
            Log.e("EditPhotoDialog", "Previewing image", e);
        }
    }
}
