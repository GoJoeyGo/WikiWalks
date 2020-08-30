package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.EditGoalDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.GoalsListRecyclerViewAdapter;

import org.json.JSONObject;

import java.util.ArrayList;

public class GoalsFragment extends Fragment {

    private ArrayList<JSONObject> goalsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noGoalsIndicator;

    public static GoalsFragment newInstance() {
        Bundle args = new Bundle();
        GoalsFragment fragment = new GoalsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.goals_list_fragment, container, false);

        MaterialToolbar toolbar = rootView.findViewById(R.id.goals_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(R.string.goals);

        Button setGoalButton = rootView.findViewById(R.id.set_goal_button);
        setGoalButton.setOnClickListener(v -> launchEditDialog(-1));

        noGoalsIndicator = rootView.findViewById(R.id.no_goals_indicator);

        populateList();
        recyclerView = rootView.findViewById(R.id.goals_list_recyclerview);
        recyclerView.setAdapter(new GoalsListRecyclerViewAdapter(this, goalsList));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return rootView;
    }

    public void updateRecyclerView() {
        populateList();
        if (goalsList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noGoalsIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noGoalsIndicator.setVisibility(View.GONE);
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.getAdapter().notifyItemRangeChanged(0, goalsList.size());
        }
    }

    public void launchEditDialog(int position) {
        EditGoalDialog.newInstance(position).show(getChildFragmentManager(), "GoalEditPopup");
    }

    private void populateList() {
        goalsList.clear();
        ArrayList<JSONObject> goals = PreferencesManager.getInstance(getContext()).getGoals();
        goalsList.addAll(goals);
    }
}