package com.wikiwalks.wikiwalks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;

public class EditDialog extends DialogFragment {

    public interface EditDialogListener {
        void onEdit(String title);
        void onDelete();
    }

    EditDialogListener listener;
    TextInputLayout title;
    Button editButton;
    Button deleteButton;
    Button cancelButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_popup, null);
        title = view.findViewById(R.id.edit_path_name);
        editButton = view.findViewById(R.id.edit_popup_edit_button);
        editButton.setOnClickListener(v -> {
            listener.onEdit(title.getEditText().getText().toString());
        });
        deleteButton = view.findViewById(R.id.edit_popup_delete_button);
        deleteButton.setOnClickListener(v -> {
            this.dismiss();
            listener.onDelete();
        });
        cancelButton = view.findViewById(R.id.edit_popup_cancel_button);
        cancelButton.setOnClickListener(v -> {
            this.getDialog().cancel();
        });
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (EditDialogListener) getTargetFragment();
    }
}
