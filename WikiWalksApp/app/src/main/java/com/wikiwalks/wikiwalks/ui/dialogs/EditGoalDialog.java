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
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GoalsFragment;
import com.wikiwalks.wikiwalks.ui.GroupWalkListFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditGoalDialog extends DialogFragment {

    private TextView time;
    private Button submitButton;
    private Calendar calendar;
    private TextInputLayout distance;

    public static EditGoalDialog newInstance(int position) {
        Bundle args = new Bundle();
        EditGoalDialog fragment = new EditGoalDialog();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        GoalsFragment listener = (GoalsFragment) getParentFragment();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_goal_dialog, null);
        int position = getArguments().getInt("position");

        distance = view.findViewById(R.id.edit_goal_distance);

        TextView unit = view.findViewById(R.id.edit_goal_distance_unit);
        String country = Locale.getDefault().getCountry();
        if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
            unit.setText(getString(R.string.miles));
        } else {
            unit.setText(getString(R.string.kilometres));
        }

        calendar = Calendar.getInstance();
        time = view.findViewById(R.id.edit_goal_popup_time);

        Button selectTimeButton = view.findViewById(R.id.edit_goal_select_time_button);
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

        submitButton = view.findViewById(R.id.edit_goal_popup_submit_button);
        submitButton.setOnClickListener(v -> {
            try {
                if (Double.parseDouble(distance.getEditText().getText().toString()) > 0) {
                    double parsedDistance;
                    if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
                        parsedDistance = Double.parseDouble(distance.getEditText().getText().toString()) * 1609.34;
                    } else {
                        parsedDistance = Double.parseDouble(distance.getEditText().getText().toString()) * 1000;
                    }
                    if (position > -1) {
                        PreferencesManager.getInstance(getContext()).editGoal(position, calendar.getTimeInMillis(), parsedDistance);
                    } else {
                        PreferencesManager.getInstance(getContext()).addGoal(Calendar.getInstance().getTimeInMillis(), calendar.getTimeInMillis(), parsedDistance);
                    }
                    listener.updateRecyclerView();
                    dismiss();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getString(R.string.invalid_distance), Toast.LENGTH_SHORT).show();
            }
        });

        Button cancelButton = view.findViewById(R.id.edit_goal_popup_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());

        Button deleteButton = view.findViewById(R.id.edit_goal_popup_delete_button);
        deleteButton.setOnClickListener(v -> {
            AlertDialog confirmationDialog = new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.confirm_deletion))
                    .setMessage(getString(R.string.delete_goal))
                    .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                        PreferencesManager.getInstance(getContext()).removeGoal(position);
                        listener.updateRecyclerView();
                        dismiss();
                    })
                    .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss()).create();
            confirmationDialog.show();
        });

        if (position > -1) {
            try {
                JSONObject goal = PreferencesManager.getInstance(getContext()).getGoals().get(position);
                calendar.setTimeInMillis(goal.getLong("end_time"));
                time.setText(String.format(getString(R.string.goal_ends), DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime())));
                double importedDistance = goal.getDouble("distance_goal");
                if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
                    importedDistance = importedDistance / 1609.34;
                } else {
                    importedDistance = importedDistance / 1000;
                }
                distance.getEditText().setText(String.valueOf(importedDistance));
                deleteButton.setVisibility(View.VISIBLE);
                submitButton.setEnabled(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        if (submitButton.isEnabled()) outState.putSerializable("calendar", calendar);
    }
}
