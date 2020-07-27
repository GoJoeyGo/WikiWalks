package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.android.gms.maps.model.Polyline;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.PointOfInterestListRecyclerViewAdapter;

import java.util.ArrayList;

public class PointOfInterestListFragment extends Fragment implements OnMapReadyCallback {

    Path path;
    GoogleMap mMap;
    ArrayList<Polyline> polylines = new ArrayList<>();
    ArrayList<Marker> markers = new ArrayList<>();
    ArrayList<PointOfInterest> pointOfInterestList;
    RecyclerView recyclerView;
    PointOfInterestListRecyclerViewAdapter recyclerViewAdapter;

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
        recyclerView = rootView.findViewById(R.id.poi_list_recyclerview);
        recyclerViewAdapter = new PointOfInterestListRecyclerViewAdapter(this, pointOfInterestList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_poi_list_frag);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (Route route : path.getRoutes()) polylines.add(route.makePolyline(mMap));
        for (PointOfInterest pointOfInterest : pointOfInterestList) markers.add(pointOfInterest.makeMarker(googleMap));
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10));
    }
}
