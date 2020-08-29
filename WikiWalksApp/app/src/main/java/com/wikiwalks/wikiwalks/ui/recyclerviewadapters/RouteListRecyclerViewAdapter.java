package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.RouteListFragment;

import java.util.ArrayList;

public class RouteListRecyclerViewAdapter extends RecyclerView.Adapter<RouteListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<Route> routeList;
    private RouteListFragment routeListFragment;

    public RouteListRecyclerViewAdapter(RouteListFragment routeListFragment, ArrayList<Route> routeList) {
        this.routeListFragment = routeListFragment;
        this.routeList = routeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(routeListFragment.getContext());
        View view = inflater.inflate(R.layout.route_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        buttons.add(holder.button);
        holder.button.setText(String.format(routeListFragment.getContext().getString(R.string.route_format), position + 1, routeList.get(position).getDistance()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.indicator.getBackground().setTint(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}));
            holder.button.setOnClickListener(v -> {
                for (Button button : buttons) button.setSelected(false);
                holder.button.setSelected(true);
                routeListFragment.selectRoute(position);
            });
        } else {
            holder.indicator.getBackground().setColorFilter(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}), PorterDuff.Mode.SRC_OVER);
            holder.button.setOnClickListener(v -> {
                for (Button button : buttons) button.setBackgroundColor(0x00000000);
                holder.button.setBackgroundColor(0x1F6200EE);
                routeListFragment.selectRoute(position);
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        buttons.remove(holder.button);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Button button;
        View indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.route_list_frag_button);
            indicator = itemView.findViewById(R.id.route_list_frag_indicator);
        }
    }
}
