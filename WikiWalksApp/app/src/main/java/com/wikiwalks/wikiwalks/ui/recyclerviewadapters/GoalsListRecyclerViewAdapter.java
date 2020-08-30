package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GoalsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class GoalsListRecyclerViewAdapter extends RecyclerView.Adapter<GoalsListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<JSONObject> goalsList;
    private GoalsFragment parentFragment;

    public GoalsListRecyclerViewAdapter(GoalsFragment parentFragment, ArrayList<JSONObject> goalsList) {
        this.parentFragment = parentFragment;
        this.goalsList = goalsList;
    }

    @NonNull
    @Override
    public GoalsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
        View view = inflater.inflate(R.layout.goals_list_row, parent, false);
        return new GoalsListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        JSONObject goal = goalsList.get(position);
        try {
            Calendar startDate = Calendar.getInstance();
            startDate.setTimeInMillis(goal.getLong("start_time"));

            Calendar endDate = Calendar.getInstance();
            endDate.setTimeInMillis(goal.getLong("end_time"));

            holder.dateRange.setText(String.format(parentFragment.getString(R.string.date_range), DateFormat.getDateInstance(DateFormat.SHORT).format(startDate.getTime()), DateFormat.getDateInstance(DateFormat.SHORT).format(endDate.getTime())));

            String country = Locale.getDefault().getCountry();
            String progressText;
            double progress = goal.getDouble("progress");
            double distanceGoal = goal.getDouble("distance_goal");
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
        } catch (JSONException e) {
            Log.e("GoalsListRVA", "Getting goal attributes", e);
        }
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
            dateRange = itemView.findViewById(R.id.goal_date_range);
            progress = itemView.findViewById(R.id.goal_progress);
            editButton = itemView.findViewById(R.id.goal_edit_button);
            separator = itemView.findViewById(R.id.goal_separator);
        }
    }
}
