package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.wikiwalks.wikiwalks.GroupWalk;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.DataMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GroupWalkListFragment;

import java.text.DateFormat;
import java.util.Calendar;

public class GroupWalkDialog extends DialogFragment implements GroupWalk.EditGroupWalkCallback {

    private Path path;
    private GroupWalk walk;
    private TextView time;
    private TextInputLayout title;
    private Button submitButton;
    private Calendar calendar;

    public static GroupWalkDialog newInstance(int pathId, int groupWalkPosition) {
        Bundle args = new Bundle();
        GroupWalkDialog fragment = new GroupWalkDialog();
        args.putInt("path_id", pathId);
        args.putInt("group_walk_position", groupWalkPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.DialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.group_walk_dialog, null);
        builder.setTitle(R.string.group_walk);

        int pathId = getArguments().getInt("path_id");
        int groupWalkPosition = getArguments().getInt("group_walk_position");
        path = DataMap.getInstance().getPathList().get(pathId);
        calendar = Calendar.getInstance();

        title = view.findViewById(R.id.group_walk_dialog_title_input);

        time = view.findViewById(R.id.group_walk_dialog_time);
        Button selectTimeButton = view.findViewById(R.id.group_walk_dialog_select_time_button);
        selectTimeButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
                calendar.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(getContext(), (timePicker, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    time.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(calendar.getTime()));
                    submitButton.setEnabled(true);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            datePickerDialog.show();
        });

        submitButton = view.findViewById(R.id.group_walk_dialog_save_button);
        submitButton.setOnClickListener(v -> {
            submitButton.setEnabled(false);
            String walkTitle = (title.getEditText().getText().toString().isEmpty()) ? String.format("Walk at %s", path.getName()) : title.getEditText().getText().toString();
            if (walk == null) {
                GroupWalk.submit(getContext(), path, calendar.getTimeInMillis() / 1000, walkTitle, this);
            } else {
                walk.edit(getContext(), calendar.getTimeInMillis() / 1000, walkTitle, this);
            }
        });

        Button cancelButton = view.findViewById(R.id.group_walk_dialog_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());

        Button deleteButton = view.findViewById(R.id.group_walk_dialog_delete_button);
        deleteButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.cancel_group_walk_prompt)
                .setPositiveButton(R.string.yes, (dialog, which) -> walk.delete(getContext(), this))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .create().show());

        if (groupWalkPosition > -1) {
            walk = DataMap.getInstance().getPathList().get(pathId).getGroupWalksList().get(groupWalkPosition);
            title.getEditText().setText(walk.getTitle());
            calendar.setTimeInMillis(walk.getTime() * 1000);
            time.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(calendar.getTime()));
            submitButton.setEnabled(true);
            deleteButton.setVisibility(View.VISIBLE);
        }

        if (savedInstanceState != null) {
            title.getEditText().setText(savedInstanceState.getString("title"));
            if (savedInstanceState.containsKey("calendar")) {
                calendar = (Calendar) savedInstanceState.getSerializable("calendar");
                time.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(calendar.getTime()));
                submitButton.setEnabled(true);
            }
        }

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title.getEditText().getText().toString());
        if (submitButton.isEnabled()) {
            outState.putSerializable("calendar", calendar);
        }
    }

    @Override
    public void onEditSuccess() {
        ((GroupWalkListFragment) getParentFragment()).updateRecyclerView();
        dismiss();
        Toast.makeText(getContext(), R.string.save_group_walk_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditFailure() {
        submitButton.setEnabled(true);
        Toast.makeText(getContext(), R.string.save_group_walk_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteSuccess() {
        ((GroupWalkListFragment) getParentFragment()).updateRecyclerView();
        dismiss();
        Toast.makeText(getContext(), R.string.delete_group_walk_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteFailure() {
        Toast.makeText(getContext(), R.string.delete_group_walk_failure, Toast.LENGTH_SHORT).show();
    }
}
