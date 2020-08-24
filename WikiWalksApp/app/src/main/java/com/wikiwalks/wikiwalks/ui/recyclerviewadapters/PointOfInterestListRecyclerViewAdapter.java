package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.PointOfInterestFragment;
import com.wikiwalks.wikiwalks.ui.PointOfInterestListFragment;

import java.util.ArrayList;

public class PointOfInterestListRecyclerViewAdapter extends RecyclerView.Adapter<PointOfInterestListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<PointOfInterest> pointOfInterestList;
    private PointOfInterestListFragment context;

    public PointOfInterestListRecyclerViewAdapter(PointOfInterestListFragment context, ArrayList<PointOfInterest> pointOfInterestList) {
        this.context = context;
        this.pointOfInterestList = pointOfInterestList;
    }

    @NonNull
    @Override
    public PointOfInterestListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context.getContext());
        View view = inflater.inflate(R.layout.poi_list_row, parent, false);
        return new PointOfInterestListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.button.setText(pointOfInterestList.get(position).getName());
        holder.button.setOnClickListener(v -> {
            PointOfInterestFragment newFragment = PointOfInterestFragment.newInstance(pointOfInterestList.get(position).getId());
            newFragment.setTargetFragment(context, 0);
            context.getParentFragmentManager().beginTransaction().add(R.id.main_frame, newFragment).addToBackStack("point_of_interest_list").commit();
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.button.getCompoundDrawablesRelative()[0].setTint(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}));
        } else {
            holder.button.getCompoundDrawables()[0].mutate().setColorFilter(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}), PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        return pointOfInterestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatButton button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.poi_list_frag_button);
        }
    }
}
