package com.wikiwalks.wikiwalks;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class RouteListFragment extends Fragment implements OnMapReadyCallback {

    Button selectRouteButton;
    SupportMapFragment mapFragment;
    private GoogleMap routeListMap;
    Path path;
    ArrayList<Path> pathList;
    ArrayList<Polyline> polylines = new ArrayList<>();
    RecyclerView recyclerView;

    public static RouteListFragment newInstance(Path path) {
        Bundle args = new Bundle();
        RouteListFragment fragment = new RouteListFragment();
        fragment.setArguments(args);
        fragment.setPath(path);
        ArrayList<Path> pathList = new ArrayList<>();
        pathList.add(path);
        pathList.addAll(path.getAllChildPaths());
        fragment.setPathList(pathList);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.route_list_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.route_list_recyclerview);
        RouteListRecyclerViewAdapter recyclerViewAdapter = new RouteListRecyclerViewAdapter(this, pathList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        selectRouteButton = rootView.findViewById(R.id.select_route_button);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_route_list_frag);
        mapFragment.getMapAsync(this);
        TextView title = rootView.findViewById(R.id.route_list_frag_title);
        title.setText(path.getName());
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        routeListMap = googleMap;
        for (Path path : pathList) {
            polylines.add(path.makePolyLine(routeListMap));
        }
        routeListMap.getUiSettings().setAllGesturesEnabled(false);
        routeListMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10 ));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setPathList(ArrayList<Path> pathList) {
        this.pathList = pathList;
    }

    public void selectRoute(final int position) {
        for (Polyline polyline : polylines) {
            polyline.setVisible(false);
        }
        polylines.get(position).setVisible(true);
        selectRouteButton.setEnabled(true);
        selectRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, WalkFragment.newInstance(pathList.get(position), true)).addToBackStack(null).commit());
    }
}
