package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.RouteListFragment;

import java.util.ArrayList;

public class RouteListRecyclerViewAdapter extends RecyclerView.Adapter<RouteListRecyclerViewAdapter.ViewHolder> {
    ArrayList<AppCompatButton> buttons = new ArrayList<>();
    ArrayList<Route> routeList;
    RouteListFragment routeListFragment;

    public RouteListRecyclerViewAdapter(RouteListFragment routeListFragment, ArrayList<Route> routeList) {
        this.routeListFragment = routeListFragment;
        this.routeList = routeList;
    }

    public ArrayList<AppCompatButton> getButtons() {
        return buttons;
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
        holder.button.setText(String.format("Route %d", position + 1));
        holder.button.setOnClickListener(v -> {
            for (Button button : buttons) {
                button.getBackground().setColorFilter(0x00000000, PorterDuff.Mode.MULTIPLY);
            }
            holder.button.getBackground().setColorFilter(0xFF777777, PorterDuff.Mode.MULTIPLY);
            routeListFragment.selectRoute(position);
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.button.getCompoundDrawablesRelative()[0].setTint(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}));
        } else {
            holder.button.getCompoundDrawables()[0].mutate().setColorFilter(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}), PorterDuff.Mode.SRC_IN);
        }
        buttons.add(holder.button);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatButton button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.route_list_frag_button);
        }
    }
}
