package com.wikiwalks.wikiwalks.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.RouteListRecyclerViewAdapter;

import java.util.ArrayList;

public class RouteListFragment extends Fragment implements OnMapReadyCallback, Route.RouteModifyCallback {

    Button selectRouteButton;
    Button deleteButton;
    SupportMapFragment mapFragment;
    Path path;
    ArrayList<Route> routes;
    ArrayList<Polyline> polylines = new ArrayList<>();
    RecyclerView recyclerView;
    int position;
    AlertDialog confirmationDialog;
    RouteListRecyclerViewAdapter recyclerViewAdapter;
    Toolbar toolbar;
    private GoogleMap routeListMap;

    public static RouteListFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        RouteListFragment fragment = new RouteListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        routes = path.getRoutes();
        final View rootView = inflater.inflate(R.layout.route_list_fragment, container, false);
        toolbar = rootView.findViewById(R.id.route_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Routes - " + path.getName());
        recyclerView = rootView.findViewById(R.id.route_list_recyclerview);
        recyclerViewAdapter = new RouteListRecyclerViewAdapter(this, routes);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        selectRouteButton = rootView.findViewById(R.id.select_route_button);
        deleteButton = rootView.findViewById(R.id.edit_route_button);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_route_list_frag);
        return rootView;
    }

    @Override
    public void onStart() {
        mapFragment.getMapAsync(this);
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        routeListMap = googleMap;
        routeListMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (int i = 0; i < routes.size(); i++) {
            Polyline newPolyline = routes.get(i).makePolyline(routeListMap);
            newPolyline.setColor(Color.HSVToColor(new float[]{(i * 50) % 360, 1, 1}));
            polylines.add(newPolyline);
        }
        routeListMap.getUiSettings().setAllGesturesEnabled(false);
        routeListMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void selectRoute(final int position) {
        this.position = position;
        for (Polyline polyline : polylines) {
            polyline.setVisible(false);
        }
        polylines.get(position).setVisible(true);
        selectRouteButton.setEnabled(true);
        selectRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().replace(R.id.main_frame, WalkFragment.newInstance(path.getId(), position)).addToBackStack(null).commit());
        if (routes.get(position).isEditable()) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                confirmationDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this route?")
                        .setPositiveButton("Yes", (dialog, which) -> path.getRoutes().get(position).delete(getContext(), this))
                        .setNegativeButton("No", (dialog, which) -> confirmationDialog.dismiss()).create();
                confirmationDialog.show();
            });
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRouteModifySuccess(Path path) {
        polylines.get(position).remove();
        polylines.remove(position);
        for (Polyline polyline : polylines) {
            polyline.setVisible(true);
        }
        recyclerViewAdapter.getButtons().remove(position);
        recyclerViewAdapter.notifyItemRemoved(position);
        recyclerViewAdapter.notifyItemRangeChanged(position, routes.size());
        deleteButton.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Successfully deleted route!", Toast.LENGTH_SHORT).show();
        if (routes.size() == 0) {
            getParentFragmentManager().popBackStack("Map", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onRouteModifyFailure() {
        Toast.makeText(getContext(), "Failed to delete route...", Toast.LENGTH_SHORT).show();
        confirmationDialog.dismiss();
    }
}
