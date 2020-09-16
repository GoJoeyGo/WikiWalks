package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GoalsFragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GoalsListRecyclerViewAdapter extends RecyclerView.Adapter<GoalsListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<JsonObject> goalsList;
    private GoalsFragment parentFragment;

    public GoalsListRecyclerViewAdapter(GoalsFragment parentFragment, ArrayList<JsonObject> goalsList) {
        this.parentFragment = parentFragment;
        this.goalsList = goalsList;
    }

    @NonNull
    @Override
    public GoalsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
        View view = inflater.inflate(R.layout.goal_list_row, parent, false);
        return new GoalsListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        JsonObject goal = goalsList.get(position);
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(goal.get("start_time").getAsLong());

        Calendar endDate = Calendar.getInstance();
        endDate.setTimeInMillis(goal.get("end_time").getAsLong());

        holder.dateRange.setText(String.format(parentFragment.getString(R.string.date_range), DateFormat.getDateInstance(DateFormat.SHORT).format(startDate.getTime()), DateFormat.getDateInstance(DateFormat.SHORT).format(endDate.getTime())));

        String country = Locale.getDefault().getCountry();
        String progressText;
        double progress = goal.get("progress").getAsDouble();
        double distanceGoal = goal.get("distance_goal").getAsDouble();
        if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
            progressText = String.format(parentFragment.getString(R.string.progress), progress * 0.000621371, distanceGoal * 0.000621371, parentFragment.getString(R.string.miles));
        } else {
            progressText = String.format(parentFragment.getString(R.string.progress), progress * 0.001, distanceGoal * 0.001, parentFragment.getString(R.string.kilometres));
        }
        holder.progress.setText(progressText);

        if (progress > distanceGoal) {
            holder.progress.setTextColor(Color.GREEN);
        } else if (endDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            holder.progress.setTextColor(Color.RED);
        }

        holder.editButton.setOnClickListener(v -> parentFragment.launchEditDialog(position));
        if (position == goalsList.size() - 1) {
            holder.separator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return goalsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView dateRange;
        TextView progress;
        ImageButton editButton;
        View separator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateRange = itemView.findViewById(R.id.goal_row_date_range);
            progress = itemView.findViewById(R.id.goal_row_progress_indicator);
            editButton = itemView.findViewById(R.id.goal_row_edit_button);
            separator = itemView.findViewById(R.id.goal_row_separator);
        }
    }
}
