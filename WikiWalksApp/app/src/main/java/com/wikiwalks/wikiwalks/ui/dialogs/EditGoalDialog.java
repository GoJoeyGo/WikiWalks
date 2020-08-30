package com.wikiwalks.wikiwalks.ui.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
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
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GoalsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Arrays;
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_goal_dialog, null);
        builder.setTitle(R.string.goal);

        GoalsFragment listener = (GoalsFragment) getParentFragment();
        int position = getArguments().getInt("position");

        distance = view.findViewById(R.id.edit_goal_distance);

        TextView unit = view.findViewById(R.id.edit_goal_distance_unit);
        String country = Locale.getDefault().getCountry();
        boolean imperial = country.equals("US") || country.equals("LR") || country.equals("MM");
        unit.setText(imperial ? R.string.miles : R.string.kilometres);

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

        Button cancelButton = view.findViewById(R.id.edit_goal_popup_cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());

        Button deleteButton = view.findViewById(R.id.edit_goal_popup_delete_button);
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
            try {
                JSONObject goal = PreferencesManager.getInstance(getContext()).getGoals().get(position);
                calendar.setTimeInMillis(goal.getLong("end_time"));
                time.setText(String.format(getString(R.string.goal_ends), DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime())));
                distance.getEditText().setText(String.valueOf(goal.getDouble("distance_goal") / (imperial ? 1609.34 : 1000)));
                deleteButton.setVisibility(View.VISIBLE);
                submitButton.setEnabled(true);
            } catch (JSONException e) {
                Log.e("EditGoalDialog", "Getting goal attributes", e);
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
        if (submitButton.isEnabled()) {
            outState.putSerializable("calendar", calendar);
        }
    }
}
