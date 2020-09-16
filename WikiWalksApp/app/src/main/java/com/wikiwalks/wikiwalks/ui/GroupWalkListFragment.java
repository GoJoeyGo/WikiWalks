package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wikiwalks.wikiwalks.GroupWalk;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.DataMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.GroupWalkDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.GroupWalkListRecyclerViewAdapter;

import java.util.ArrayList;

public class GroupWalkListFragment extends Fragment implements GroupWalk.GetGroupWalksCallback {

    private Path path;
    private RecyclerView recyclerView;
    private TextView noWalksIndicator;
    private ArrayList<GroupWalk> walks;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        View rootView = inflater.inflate(R.layout.group_walk_list_fragment, container, false);

        path = DataMap.getInstance().getPathList().get(getArguments().getInt("path_id"));
        walks = path.getGroupWalksList();

        MaterialToolbar toolbar = rootView.findViewById(R.id.group_walk_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(String.format(getString(R.string.group_walks_title), path.getName()));

        Button scheduleButton = rootView.findViewById(R.id.group_walk_list_schedule_group_walk_button);
        scheduleButton.setOnClickListener(v -> launchEditDialog(-1));

        swipeRefreshLayout = rootView.findViewById(R.id.group_walk_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::updateGroupWalksList);

        recyclerView = rootView.findViewById(R.id.group_walk_list_recyclerview);
        recyclerView.setAdapter(new GroupWalkListRecyclerViewAdapter(this, walks));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        noWalksIndicator = rootView.findViewById(R.id.group_walk_list_empty_indicator);
        if (walks.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noWalksIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noWalksIndicator.setVisibility(View.GONE);
        }

        updateGroupWalksList();

        return rootView;
    }

    private void updateGroupWalksList() {
        swipeRefreshLayout.setRefreshing(true);
        path.getGroupWalks(getContext(), this);
    }

    public void updateRecyclerView() {
        if (walks.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noWalksIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noWalksIndicator.setVisibility(View.GONE);
        }
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.getAdapter().notifyItemRangeChanged(0, walks.size());
        swipeRefreshLayout.setRefreshing(false);
    }

    public void toggleAttendance(int position, View checkbox) {
        GroupWalk walk = path.getGroupWalksList().get(position);
        checkbox.setEnabled(false);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle((walk.isAttending()) ? R.string.cancel_group_walk_attendance_prompt : R.string.attend_group_walk_prompt)
                .setPositiveButton(R.string.yes, (dialog, which) -> walk.toggleAttendance(getContext(), new GroupWalk.AttendGroupWalkCallback() {
                    @Override
                    public void toggleAttendanceSuccess() {
                        recyclerView.getAdapter().notifyItemChanged(position);
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.toggle_group_walk_attendance_success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void toggleAttendanceFailure() {
                        Toast.makeText(getContext(), R.string.toggle_group_walk_attendance_failure, Toast.LENGTH_SHORT).show();
                        checkbox.setEnabled(true);
                    }
                }))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .setOnDismissListener(dialog -> checkbox.setEnabled(true))
                .create().show();
    }

    public void launchEditDialog(int position) {
        GroupWalkDialog.newInstance(path.getId(), position).show(getChildFragmentManager(), "GroupWalkPopup");
    }

    @Override
    public void onGetGroupWalksSuccess() {
        updateRecyclerView();
    }

    @Override
    public void onGetGroupWalksFailure() {
        Toast.makeText(getContext(), R.string.get_group_walks_failure, Toast.LENGTH_SHORT).show();
    }
}
