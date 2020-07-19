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

public class PictureDialog extends DialogFragment implements PathPicture.PictureUploadCallback {

    @Override
    public void onSubmitPictureSuccess() {
        listener.onEdit();
        getDialog().dismiss();
    }

    @Override
    public void onSubmitPictureFailure() {

    }

    public interface PictureDialogListener {
        void onEdit();
    }

    PictureDialogListener listener;
    TextInputLayout title;
    ImageView imageView;
    Button cameraButton;
    Button galleryButton;
    Button editButton;
    Button cancelButton;
    Path path;
    Uri photoURI;
    String filename;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_SELECT = 1;

    public PictureDialog(Path path) {
        this.path = path;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.picture_popup, null);
        imageView = view.findViewById(R.id.picture_popup_selected_image);
        cameraButton = view.findViewById(R.id.picture_popup_camera_button);
        cameraButton.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePicture.resolveActivity(getContext().getPackageManager()) != null) {
                try {
                    File photoFile = File.createTempFile(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_", ".jpg", getContext().getExternalFilesDir(Environment.DIRECTORY_DCIM));
                    photoURI = FileProvider.getUriForFile(getContext(),
                            "com.wikiwalks.wikiwalks.fileprovider",
                            photoFile);
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
        title = view.findViewById(R.id.picture_popup_description);
        editButton = view.findViewById(R.id.picture_popup_submit_button);
        editButton.setOnClickListener(v -> {
            if (photoURI != null) {
                PathPicture.upload(getContext(), filename, photoURI, title.getEditText().getText().toString(), path, this);
            }
        });
        cancelButton = view.findViewById(R.id.picture_popup_cancel_button);
        cancelButton.setOnClickListener(v -> {
            this.getDialog().cancel();
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (PictureDialogListener) getTargetFragment();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        editButton.setEnabled(false);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_SELECT && data != null) {
                photoURI = data.getData();
                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(photoURI);
                    byte[] buffer = new byte[inputStream.available()];
                    inputStream.read(buffer);
                    File targetFile = File.createTempFile(photoURI.getLastPathSegment(), ".jpg", getContext().getExternalCacheDir());
                    filename = targetFile.getAbsolutePath();
                    OutputStream outStream = new FileOutputStream(targetFile);
                    outStream.write(buffer);
                    outStream.close();
                    inputStream.close();
                    targetFile.deleteOnExit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                filename = getContext().getExternalFilesDir(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + photoURI.getLastPathSegment();
            }
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
                imageView.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        editButton.setEnabled(true);
    }
}
