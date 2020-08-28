package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.R;

public class StatisticsListRecyclerViewAdapter extends RecyclerView.Adapter<StatisticsListRecyclerViewAdapter.ViewHolder> {
    private String[] statisticsList;
    private Context context;

    public StatisticsListRecyclerViewAdapter(Context context, String[] statisticsList) {
        this.context = context;
        this.statisticsList = statisticsList;
    }

    @NonNull
    @Override
    public StatisticsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.statistics_list_row, parent, false);
        return new StatisticsListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.entry.setText(statisticsList[position]);
        if (position == statisticsList.length - 1) holder.separator.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return statisticsList.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView entry;
        View separator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            entry = itemView.findViewById(R.id.statistics_entry);
            separator = itemView.findViewById(R.id.statistics_separator);
        }
    }
}
