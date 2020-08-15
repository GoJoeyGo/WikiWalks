package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.PointOfInterestListRecyclerViewAdapter;

import java.util.ArrayList;

public class PointOfInterestListFragment extends Fragment implements OnMapReadyCallback {

    Toolbar toolbar;
    Path path;
    GoogleMap mMap;
    ArrayList<Polyline> polylines = new ArrayList<>();
    ArrayList<Marker> markers = new ArrayList<>();
    ArrayList<PointOfInterest> pointOfInterestList;
    RecyclerView recyclerView;
    PointOfInterestListRecyclerViewAdapter recyclerViewAdapter;
    SupportMapFragment mapFragment;
    TextView noPointsIndicator;

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
        path = PathMap.getInstance().getPathList().get(getArguments().getInt("path_id"));
        pointOfInterestList = path.getPointsOfInterest();
        final View rootView = inflater.inflate(R.layout.poi_list_fragment, container, false);
        toolbar = rootView.findViewById(R.id.poi_list_frag_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Points of Interest - " + path.getName());
        noPointsIndicator = rootView.findViewById(R.id.no_points_indicator);
        recyclerView = rootView.findViewById(R.id.poi_list_recyclerview);
        recyclerViewAdapter = new PointOfInterestListRecyclerViewAdapter(this, pointOfInterestList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        if (pointOfInterestList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noPointsIndicator.setVisibility(View.VISIBLE);
        }
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_poi_list_frag);
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
            recyclerViewAdapter.notifyDataSetChanged();
            recyclerViewAdapter.notifyItemRangeChanged(0, pointOfInterestList.size());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (Route route : path.getRoutes()) polylines.add(route.makePolyline(mMap));
        for (int i = 0; i < pointOfInterestList.size(); i++) {
            markers.add(pointOfInterestList.get(i).makeMarker(googleMap, ((i * 50) % 360)));
        }
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.setOnMarkerClickListener(marker -> true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10));
    }
}
