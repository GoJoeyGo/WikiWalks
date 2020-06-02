package com.wikiwalks.wikiwalks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RouteListRecyclerViewAdapter extends RecyclerView.Adapter<RouteListRecyclerViewAdapter.ViewHolder> {
    ArrayList<Button> buttons = new ArrayList<>();
    ArrayList<Path> routeList;
    RouteListFragment context;

    public RouteListRecyclerViewAdapter(RouteListFragment context, ArrayList<Path> pathList) {
        this.context = context;
        this.routeList = pathList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context.getContext());
        View view = inflater.inflate(R.layout.route_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Path path = routeList.get(position);
        holder.button.setText(path.getName());
        holder.button.setOnClickListener(v -> {
            for (Button button : buttons) {
                button.setBackgroundColor(0x00000000);
            }
            holder.button.setBackgroundColor(0xFF777777);
            context.selectRoute(position);
        });
        buttons.add(holder.button);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.route_list_frag_button);
        }
    }
}
