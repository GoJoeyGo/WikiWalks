package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import com.google.gson.JsonObject;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GoalsFragment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GoalDialog extends DialogFragment {

    private TextView time;
    private Button submitButton;
    private Calendar calendar;
    private TextInputLayout distance;

    public static GoalDialog newInstance(int position) {
        Bundle args = new Bundle();
        GoalDialog fragment = new GoalDialog();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext(), R.style.DialogTheme);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.goal_dialog, null);
        builder.setTitle(R.string.goal);

        GoalsFragment listener = (GoalsFragment) getParentFragment();
        int position = getArguments().getInt("position");

        distance = view.findViewById(R.id.goal_dialog_distance_input);

        TextView unit = view.findViewById(R.id.goal_dialog_distance_unit);
        String country = Locale.getDefault().getCountry();
        boolean imperial = country.equals("US") || country.equals("LR") || country.equals("MM");
        unit.setText(imperial ? R.string.miles : R.string.kilometres);

        calendar = Calendar.getInstance();
        time = view.findViewById(R.id.goal_dialog_time);

        Button selectTimeButton = view.findViewById(R.id.goal_dialog_end_date_button);
        selectTimeButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (datePicker, year, monthOfYear, dayOfMonth) -> {
                calendar.set(year, monthOfYear, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                time.setText(String.format(getString(R.string.goal_ends), DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime())));
                submitButton.setEnabled(true);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            datePickerDialog.show();
        });

        submitButton = view.findViewById(R.id.goal_dialog_save_button);
        submitButton.setOnClickListener(v -> {
            try {
                if (Double.parseDouble(distance.getEditText().getText().toString()) > 0) {
                    double parsedDistance = imperial ? Double.parseDouble(distance.getEditText().getText().toString()) * 1609.34 : Double.parseDouble(distance.getEditText().getText().toString()) * 1000;
                    if (position > -1) {
                        PreferencesManager.getInstance(getContext()).editGoal(position, calendar.getTimeInMillis(), parsedDistance);
                    } else {
                        PreferencesManager.getInstance(getContext()).addGoal(Calendar.getInstance().getTimeInMillis(), calendar.getTimeInMillis(), parsedDistance);
                    }
                    listener.updateRecyclerView();
                    dismiss();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), R.string.invalid_distance, Toast.LENGTH_SHORT).show();
            }
        });

        Button cancelButton = view.findViewById(R.id.goal_dialog_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());

        Button deleteButton = view.findViewById(R.id.goal_dialog_delete_button);
        deleteButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.delete_goal)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    PreferencesManager.getInstance(getContext()).removeGoal(position);
                    listener.updateRecyclerView();
                    dismiss();
                })
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .create().show());

        if (position > -1) {
            JsonObject goal = PreferencesManager.getInstance(getContext()).getGoals().get(position);
            calendar.setTimeInMillis(goal.get("end_time").getAsLong());
            time.setText(String.format(getString(R.string.goal_ends), DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime())));
            distance.getEditText().setText(String.valueOf(goal.get("distance_goal").getAsDouble() / (imperial ? 1609.34 : 1000)));
            deleteButton.setVisibility(View.VISIBLE);
            submitButton.setEnabled(true);
        }

        if (savedInstanceState != null) {
            distance.getEditText().setText(savedInstanceState.getString("distance"));
            if (savedInstanceState.containsKey("calendar")) {
                calendar = (Calendar) savedInstanceState.getSerializable("calendar");
                time.setText(String.format(getString(R.string.goal_ends), DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime())));
                submitButton.setEnabled(true);
            }
        }

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("distance", distance.getEditText().getText().toString());
        if (submitButton.isEnabled()) {
            outState.putSerializable("calendar", calendar);
        }
    }
}
