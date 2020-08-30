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

import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.PointOfInterestFragment;
import com.wikiwalks.wikiwalks.ui.PointOfInterestListFragment;

import java.util.ArrayList;

public class PointOfInterestListRecyclerViewAdapter extends RecyclerView.Adapter<PointOfInterestListRecyclerViewAdapter.ViewHolder> {
    private ArrayList<PointOfInterest> pointOfInterestList;
    private PointOfInterestListFragment parentFragment;

    public PointOfInterestListRecyclerViewAdapter(PointOfInterestListFragment parentFragment, ArrayList<PointOfInterest> pointOfInterestList) {
        this.parentFragment = parentFragment;
        this.pointOfInterestList = pointOfInterestList;
    }

    @NonNull
    @Override
    public PointOfInterestListRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
        View view = inflater.inflate(R.layout.poi_list_row, parent, false);
        return new PointOfInterestListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.button.setText(pointOfInterestList.get(position).getName());
        holder.button.setOnClickListener(v -> parentFragment.getParentFragmentManager().beginTransaction().add(R.id.main_frame, PointOfInterestFragment.newInstance(pointOfInterestList.get(position).getId())).addToBackStack("point_of_interest_list").commit());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.indicator.getBackground().setTint(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}));
        } else {
            holder.indicator.getBackground().setColorFilter(Color.HSVToColor(new float[]{(position * 50) % 360, 1, 1}), PorterDuff.Mode.SRC_OVER);
        }
    }

    @Override
    public int getItemCount() {
        return pointOfInterestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button button;
        private View indicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.poi_list_frag_button);
            indicator = itemView.findViewById(R.id.poi_list_frag_indicator);
        }
    }
}
