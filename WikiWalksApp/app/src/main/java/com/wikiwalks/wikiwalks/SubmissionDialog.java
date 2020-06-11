package com.wikiwalks.wikiwalks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public class SubmissionDialog extends DialogFragment {

    public interface SubmissionDialogListener {
        void onPositiveClick(String title);
        void onNegativeClick();
    }

    SubmissionDialogListener listener;
    TextInputLayout title;
    Button submitButton;
    Button cancelButton;
    boolean isNew;

    public SubmissionDialog(boolean isNew) {
        this.isNew = isNew;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.submission_popup, null);
        title = view.findViewById(R.id.submit_path_name);
        if (!isNew) title.setVisibility(View.GONE);
        submitButton = view.findViewById(R.id.submit_popup_submit_button);
        submitButton.setOnClickListener(v -> {
            SubmissionDialog.this.getDialog().dismiss();
            listener.onPositiveClick(title.getEditText().getText().toString());
        });
        cancelButton = view.findViewById(R.id.submit_popup_cancel_button);
        cancelButton.setOnClickListener(v -> {
            SubmissionDialog.this.getDialog().cancel();
            listener.onNegativeClick();
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (SubmissionDialogListener) getTargetFragment();
    }
}
