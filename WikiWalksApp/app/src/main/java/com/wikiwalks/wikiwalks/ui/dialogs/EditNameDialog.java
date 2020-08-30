package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;

public class EditNameDialog extends DialogFragment {

    private EditDialogListener listener;
    private TextInputLayout title;

    public interface EditDialogListener {
        void setEditNameDialog(EditNameDialog editNameDialog);
        void onEditName(EditNameDialogType type, String name);
    }

    public static EditNameDialog newInstance(EditNameDialogType type, int parentId) {
        Bundle args = new Bundle();
        EditNameDialog fragment = new EditNameDialog();
        args.putSerializable("type", type);
        args.putInt("parent_id", parentId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_name_dialog, null);
        builder.setTitle(R.string.set_name);

        listener = (EditDialogListener) getParentFragment();
        listener.setEditNameDialog(this);

        EditNameDialogType type = (EditNameDialogType) getArguments().getSerializable("type");
        title = view.findViewById(R.id.edit_name);

        if (savedInstanceState != null && savedInstanceState.containsKey("name")) {
            title.getEditText().setText(savedInstanceState.getString("name"));
        } else if (getArguments().getInt("parent_id") > -1) {
            if (type == EditNameDialogType.PATH) {
                title.getEditText().setText(PathMap.getInstance().getPathList().get(getArguments().getInt("parent_id")).getName());
            } else if (type == EditNameDialogType.POINT_OF_INTEREST) {
                title.getEditText().setText(PathMap.getInstance().getPointOfInterestList().get(getArguments().getInt("parent_id")).getName());
            }
        }

        if (type == EditNameDialogType.USERNAME) {
            title.getEditText().setText(PreferencesManager.getInstance(getContext()).getName());
        }
        Button editButton = view.findViewById(R.id.edit_popup_save_button);
        editButton.setOnClickListener(v -> {
            listener.onEditName(type, title.getEditText().getText().toString());
        });
        Button cancelButton = view.findViewById(R.id.edit_popup_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("name", title.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }

    public enum EditNameDialogType {PATH, POINT_OF_INTEREST, USERNAME}
}
