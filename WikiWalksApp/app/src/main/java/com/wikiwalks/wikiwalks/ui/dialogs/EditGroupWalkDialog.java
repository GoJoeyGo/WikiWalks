package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.AlertDialog;
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

import com.google.android.material.textfield.TextInputLayout;
import com.wikiwalks.wikiwalks.GroupWalk;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GroupWalkListFragment;

import java.text.DateFormat;
import java.util.Calendar;

public class EditGroupWalkDialog extends DialogFragment implements GroupWalk.EditGroupWalkCallback {

    int pathId;
    int groupWalkPosition;
    Path path;
    GroupWalk walk;
    TextView time;
    TextInputLayout title;
    Button selectTimeButton;
    Button submitButton;
    Button cancelButton;
    Button deleteButton;
    Calendar calendar;

    public static EditGroupWalkDialog newInstance(int pathId, int groupWalkPosition) {
        Bundle args = new Bundle();
        EditGroupWalkDialog fragment = new EditGroupWalkDialog();
        args.putInt("path_id", pathId);
        args.putInt("group_walk_position", groupWalkPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        pathId = getArguments().getInt("path_id");
        groupWalkPosition = getArguments().getInt("group_walk_position");
        path = PathMap.getInstance().getPathList().get(pathId);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_group_walk_dialog, null);
        calendar = Calendar.getInstance();
        time = view.findViewById(R.id.edit_group_walk_popup_time);
        title = view.findViewById(R.id.edit_group_walk_title);
        selectTimeButton = view.findViewById(R.id.edit_group_walk_select_time_button);
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
        submitButton = view.findViewById(R.id.edit_group_walk_popup_submit_button);
        submitButton.setOnClickListener(v -> {
            String walkTitle = (title.getEditText().getText().toString().isEmpty()) ? String.format("Walk at %s", path.getName()) : title.getEditText().getText().toString();
            if (walk == null) {
                GroupWalk.submit(getContext(), path, calendar.getTimeInMillis() / 1000, walkTitle, this);
            } else {
                walk.edit(getContext(), calendar.getTimeInMillis() / 1000, walkTitle, this);
            }
        });
        cancelButton = view.findViewById(R.id.edit_group_walk_popup_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());
        deleteButton = view.findViewById(R.id.edit_group_walk_popup_delete_button);
        deleteButton.setOnClickListener(v -> {
            AlertDialog confirmationDialog = new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Deletion")
                    .setMessage("Delete walk?")
                    .setPositiveButton("Yes", (dialog, which) -> walk.delete(getContext(), this))
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).create();
            confirmationDialog.show();
        });
        if (groupWalkPosition > -1) {
            walk = PathMap.getInstance().getPathList().get(pathId).getGroupWalks().get(groupWalkPosition);
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
        if (submitButton.isEnabled()) outState.putSerializable("calendar", calendar);
    }

    @Override
    public void onEditSuccess() {
        ((GroupWalkListFragment) getParentFragment()).updateRecyclerView();
        dismiss();
    }

    @Override
    public void onEditFailure() {
        Toast.makeText(getContext(), "Failed to save walk!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteSuccess() {
        ((GroupWalkListFragment) getParentFragment()).updateRecyclerView();
        dismiss();
    }

    @Override
    public void onDeleteFailure() {
        Toast.makeText(getContext(), "Failed to delete walk!", Toast.LENGTH_SHORT).show();
    }
}
