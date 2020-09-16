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
import java.util.Locale;

public class RouteListRecyclerViewAdapter extends RecyclerView.Adapter<RouteListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Button> buttons = new ArrayList<>();
    private ArrayList<Route> routeList;
    private RouteListFragment parentFragment;

    public RouteListRecyclerViewAdapter(RouteListFragment parentFragment, ArrayList<Route> routeList) {
        this.parentFragment = parentFragment;
        this.routeList = routeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
        View view = inflater.inflate(R.layout.route_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        buttons.add(holder.button);

        String country = Locale.getDefault().getCountry();
        if (country.equals("US") || country.equals("LR") || country.equals("MM")) {
            holder.button.setText(String.format(parentFragment.getString(R.string.distance_format), position + 1, routeList.get(position).getDistance() * 0.000621371, parentFragment.getString(R.string.miles)));
        } else {
            holder.button.setText(String.format(parentFragment.getString(R.string.distance_format), position + 1, routeList.get(position).getDistance() * 0.001, parentFragment.getString(R.string.kilometres)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.indicator.getBackground().setTint(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}));
            holder.button.setOnClickListener(v -> {
                for (Button button : buttons) button.setSelected(false);
                holder.button.setSelected(true);
                parentFragment.selectRoute(position);
            });
        } else {
            holder.indicator.getBackground().setColorFilter(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}), PorterDuff.Mode.SRC_OVER);
            holder.button.setOnClickListener(v -> {
                for (Button button : buttons) button.setBackgroundColor(0x00000000);
                holder.button.setBackgroundColor(0x1F6200EE);
                parentFragment.selectRoute(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        buttons.remove(holder.button);
        super.onViewRecycled(holder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button button;
        private View indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.route_row_button);
            indicator = itemView.findViewById(R.id.route_row_colour_indicator);
        }
    }
}
