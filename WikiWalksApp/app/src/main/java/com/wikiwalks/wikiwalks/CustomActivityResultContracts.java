package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

public abstract class CustomActivityResultContracts {
    public static class ExportSettings extends ActivityResultContracts.CreateDocument {
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull String input) {
            super.createIntent(context, input);
            return new Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .setType("application/json")
                    .putExtra(Intent.EXTRA_TITLE, input);
        }
    }

    public static class SelectPicture extends ActivityResultContracts.GetContent {
        @Override
        public Intent createIntent(@NonNull Context context, @NonNull String input) {
            super.createIntent(context, input);
            return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
    }
}
