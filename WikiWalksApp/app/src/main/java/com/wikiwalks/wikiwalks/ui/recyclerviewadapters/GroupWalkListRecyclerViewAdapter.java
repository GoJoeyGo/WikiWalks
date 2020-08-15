package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.GroupWalk;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.GroupWalkListFragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GroupWalkListRecyclerViewAdapter extends RecyclerView.Adapter<GroupWalkListRecyclerViewAdapter.ViewHolder> {
    ArrayList<GroupWalk> groupWalkList;
    GroupWalkListFragment context;

    public GroupWalkListRecyclerViewAdapter(GroupWalkListFragment context, ArrayList<GroupWalk> groupWalkList) {
        this.context = context;
        this.groupWalkList = groupWalkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context.getContext());
        View view = inflater.inflate(R.layout.group_walk_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        GroupWalk walk = groupWalkList.get(position);
        holder.title.setText(walk.getTitle());
        holder.time.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(walk.getTime() * 1000)));
        holder.host.setText(String.format("Host: %s", walk.getHostName()));
        if (walk.getAttendees().size() == 0) {
            holder.attendees.setText(R.string.no_attendees);
            holder.attendees.setTypeface(holder.attendees.getTypeface(), Typeface.ITALIC);
        } else {
            holder.attendees.setText(String.format("Attendees: %s", TextUtils.join(", ", walk.getAttendees())));
            holder.attendees.setTypeface(holder.attendees.getTypeface(), Typeface.NORMAL);
        }
        if (walk.isEditable()) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.attendingButton.setVisibility(View.GONE);
            holder.editButton.setOnClickListener(v -> context.launchEditDialog(position));
        } else holder.editButton.setVisibility(View.GONE);
        if (walk.isAttending())
            holder.attendingButton.setImageResource(R.drawable.ic_baseline_check_box_24);
        else
            holder.attendingButton.setImageResource(R.drawable.ic_baseline_check_box_outline_blank_24);
        holder.attendingButton.setOnClickListener(v -> context.toggleAttendance(position, v));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return groupWalkList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView time;
        TextView host;
        TextView attendees;
        ImageButton attendingButton;
        ImageButton editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.group_walk_row_title);
            time = itemView.findViewById(R.id.group_walk_row_time);
            host = itemView.findViewById(R.id.group_walk_row_host);
            attendees = itemView.findViewById(R.id.group_walk_row_attendees);
            attendingButton = itemView.findViewById(R.id.group_walk_row_attend_button);
            editButton = itemView.findViewById(R.id.group_walk_row_edit_button);
        }
    }
}
