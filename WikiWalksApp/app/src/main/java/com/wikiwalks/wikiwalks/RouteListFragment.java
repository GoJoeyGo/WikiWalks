package com.wikiwalks.wikiwalks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class RouteListFragment extends Fragment implements OnMapReadyCallback, Route.RouteSubmitCallback {

    Button selectRouteButton;
    Button deleteButton;
    SupportMapFragment mapFragment;
    private GoogleMap routeListMap;
    Path path;
    ArrayList<Route> routes;
    ArrayList<Polyline> polylines = new ArrayList<>();
    RecyclerView recyclerView;
    int position;
    AlertDialog confirmationDialog;
    RouteListRecyclerViewAdapter recyclerViewAdapter;
    TextView title;

    public static RouteListFragment newInstance(Path path) {
        Bundle args = new Bundle();
        RouteListFragment fragment = new RouteListFragment();
        fragment.setArguments(args);
        fragment.setPath(path);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        routes = path.getRoutes();
        final View rootView = inflater.inflate(R.layout.route_list_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.route_list_recyclerview);
        recyclerViewAdapter = new RouteListRecyclerViewAdapter(this, routes);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        title = rootView.findViewById(R.id.route_list_frag_title);
        selectRouteButton = rootView.findViewById(R.id.select_route_button);
        deleteButton = rootView.findViewById(R.id.edit_route_button);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_route_list_frag);
        mapFragment.getMapAsync(this);
        TextView title = rootView.findViewById(R.id.route_list_frag_title);
        title.setText(path.getName());
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        routeListMap = googleMap;
        for (Route route : routes) {
            polylines.add(route.makePolyline(routeListMap));
        }
        routeListMap.getUiSettings().setAllGesturesEnabled(false);
        routeListMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10 ));
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
        selectRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().replace(R.id.main_frame, WalkFragment.newInstance(path, position)).addToBackStack(null).commit());
        if (routes.get(position).isEditable()) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                confirmationDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this route?")
                        .setPositiveButton("Yes", (dialog, which) ->  path.getRoutes().get(position).delete(getContext(), this))
                        .setNegativeButton("No", (dialog, which) -> confirmationDialog.dismiss()).create();
                confirmationDialog.show();
            });
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSuccess() {
        polylines.get(position).remove();
        polylines.remove(position);
        for (Polyline polyline : polylines) {
            polyline.setVisible(true);
        }
        recyclerViewAdapter.buttons.remove(position);
        recyclerViewAdapter.notifyItemRemoved(position);
        recyclerViewAdapter.notifyItemRangeChanged(position, routes.size());
        deleteButton.setVisibility(View.GONE);
        Toast.makeText(getContext(), "Successfully deleted route!", Toast.LENGTH_SHORT).show();
        if (routes.size() == 0) {
            getParentFragmentManager().popBackStack("Map", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onFailure() {
        Toast.makeText(getContext(), "Failed to delete route...", Toast.LENGTH_SHORT).show();
        confirmationDialog.dismiss();
    }
}
