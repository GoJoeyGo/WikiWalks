package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.DataMap;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.PointOfInterestListRecyclerViewAdapter;

import java.util.ArrayList;

public class PointOfInterestListFragment extends Fragment implements OnMapReadyCallback {

    private Path path;
    private ArrayList<PointOfInterest> pointOfInterestList;
    private RecyclerView recyclerView;
    private SupportMapFragment mapFragment;
    private TextView noPointsIndicator;
    private ArrayList<Marker> markers = new ArrayList<>();
    private GoogleMap map;

    public static PointOfInterestListFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        PointOfInterestListFragment fragment = new PointOfInterestListFragment();
        args.putInt("path_id", pathId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.poi_list_fragment, container, false);

        path = DataMap.getInstance().getPathList().get(getArguments().getInt("path_id"));
        pointOfInterestList = path.getPointsOfInterest();

        MaterialToolbar toolbar = rootView.findViewById(R.id.poi_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(String.format(getString(R.string.points_of_interest_title), path.getName()));

        noPointsIndicator = rootView.findViewById(R.id.poi_list_empty_indicator);
        recyclerView = rootView.findViewById(R.id.poi_list_recyclerview);
        recyclerView.setAdapter(new PointOfInterestListRecyclerViewAdapter(this, pointOfInterestList));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        if (pointOfInterestList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noPointsIndicator.setVisibility(View.VISIBLE);
        }
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.poi_list_map);

        getParentFragmentManager().setFragmentResultListener("update_poi_list", this, (requestKey, result) -> update());

        return rootView;
    }

    @Override
    public void onStart() {
        mapFragment.getMapAsync(this);
        super.onStart();
    }

    public void update() {
        if (pointOfInterestList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noPointsIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noPointsIndicator.setVisibility(View.GONE);
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.getAdapter().notifyItemRangeChanged(0, pointOfInterestList.size());
        }
        for (Marker marker : markers) marker.remove();
        for (int i = 0; i < pointOfInterestList.size(); i++)
            markers.add(pointOfInterestList.get(i).makeMarker(map, ((i * 50) % 360)));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (Route route : path.getRoutes()) route.makePolyline(googleMap);
        for (int i = 0; i < pointOfInterestList.size(); i++)
            markers.add(pointOfInterestList.get(i).makeMarker(googleMap, ((i * 50) % 360)));
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.setOnMarkerClickListener(marker -> true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), 20));
    }
}
