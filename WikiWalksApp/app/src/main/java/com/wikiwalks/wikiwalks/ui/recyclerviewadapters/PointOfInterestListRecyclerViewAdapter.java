package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

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
import com.wikiwalks.wikiwalks.ui.ReviewListFragment;

import java.util.ArrayList;

public class PointOfInterestListRecyclerViewAdapter extends RecyclerView.Adapter<PointOfInterestListRecyclerViewAdapter.ViewHolder> {
    ArrayList<PointOfInterest> pointOfInterestList;
    PointOfInterestListFragment context;

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
    public void onBindViewHolder(@NonNull final PointOfInterestListRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.button.setText(pointOfInterestList.get(position).getName());
        holder.button.setOnClickListener(v -> context.getParentFragmentManager().beginTransaction().add(R.id.main_frame, PointOfInterestFragment.newInstance(pointOfInterestList.get(position).getId())).addToBackStack(null).commit());
    }

    @Override
    public int getItemCount() {
        return pointOfInterestList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.poi_list_frag_button);
        }
    }
}
