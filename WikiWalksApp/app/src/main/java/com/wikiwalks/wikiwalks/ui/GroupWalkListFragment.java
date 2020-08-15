package com.wikiwalks.wikiwalks.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.GroupWalk;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.EditGroupWalkDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.GroupWalkListRecyclerViewAdapter;

import java.util.ArrayList;

public class GroupWalkListFragment extends Fragment {

    Button scheduleButton;
    Path path;
    RecyclerView recyclerView;
    TextView noWalksIndicator;
    ArrayList<GroupWalk> walks;
    int position;

    public static GroupWalkListFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        GroupWalkListFragment fragment = new GroupWalkListFragment();
        args.putInt("path_id", pathId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        path = PathMap.getInstance().getPathList().get(getArguments().getInt("path_id"));
        walks = path.getGroupWalks();
        final View rootView = inflater.inflate(R.layout.group_walk_list_fragment, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.path_group_walk_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Group Walks - " + path.getName());
        scheduleButton = rootView.findViewById(R.id.submit_group_walk_button);
        scheduleButton.setOnClickListener(v -> launchEditDialog(-1));
        recyclerView = rootView.findViewById(R.id.path_group_walk_list_recyclerview);
        recyclerView.setAdapter(new GroupWalkListRecyclerViewAdapter(this, walks));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        noWalksIndicator = rootView.findViewById(R.id.no_group_walks_indicator);
        if (walks.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noWalksIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noWalksIndicator.setVisibility(View.GONE);
        }
        return rootView;
    }

    public void updateRecyclerView() {
        if (walks.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noWalksIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noWalksIndicator.setVisibility(View.GONE);
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.getAdapter().notifyItemRangeChanged(0, walks.size());
        }
    }

    public void launchEditDialog(int position) {
        EditGroupWalkDialog.newInstance(path.getId(), position).show(getChildFragmentManager(), "GroupWalkPopup");
    }

    public void toggleAttendance(int position, View view) {
        GroupWalk walk = path.getGroupWalks().get(position);
        view.setEnabled(false);
        AlertDialog confirmationDialog = new AlertDialog.Builder(getContext())
                .setTitle("Confirm")
                .setMessage((walk.isAttending()) ? "Cancel attendance?" : "Attend walk?")
                .setPositiveButton("Yes", (dialog, which) -> walk.toggleAttendance(getContext(), new GroupWalk.AttendGroupWalkCallback() {
                    @Override
                    public void toggleAttendanceSuccess() {
                        recyclerView.getAdapter().notifyItemChanged(position);
                    }

                    @Override
                    public void toggleAttendanceFailure() {
                        Toast.makeText(getContext(), "Failed to change attendance...", Toast.LENGTH_SHORT).show();
                        view.setEnabled(true);
                    }
                }))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setOnDismissListener(dialog -> view.setEnabled(true)).create();
        confirmationDialog.show();
    }
}
