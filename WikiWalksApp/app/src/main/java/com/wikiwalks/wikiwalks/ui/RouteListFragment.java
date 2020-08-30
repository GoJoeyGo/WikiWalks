package com.wikiwalks.wikiwalks.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.RouteListRecyclerViewAdapter;

import java.util.ArrayList;

public class RouteListFragment extends Fragment implements OnMapReadyCallback {

    private Button selectRouteButton;
    private Button deleteButton;
    private SupportMapFragment mapFragment;
    private Path path;
    private ArrayList<Route> routes;
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private RecyclerView recyclerView;

    public static RouteListFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        RouteListFragment fragment = new RouteListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.route_list_fragment, container, false);

        path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        routes = path.getRoutes();

        MaterialToolbar toolbar = rootView.findViewById(R.id.route_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(String.format(getString(R.string.routes_title), path.getName()));

        recyclerView = rootView.findViewById(R.id.route_list_recyclerview);
        recyclerView.setAdapter(new RouteListRecyclerViewAdapter(this, routes));
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
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (int i = 0; i < routes.size(); i++) {
            Polyline newPolyline = routes.get(i).makePolyline(googleMap);
            newPolyline.setColor(Color.HSVToColor(new float[]{(i * 50) % 360, 1, 1}));
            polylines.add(newPolyline);
        }
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), 20));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void selectRoute(int position) {
        for (Polyline polyline : polylines) {
            polyline.setVisible(false);
        }
        polylines.get(position).setVisible(true);
        selectRouteButton.setEnabled(true);
        selectRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().replace(R.id.main_frame, WalkFragment.newInstance(path.getId(), position)).addToBackStack(null).commit());
        if (routes.get(position).isEditable()) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle(R.string.delete_route_prompt)
                        .setPositiveButton(R.string.yes, (dialog, which) -> path.getRoutes().get(position).delete(getContext(), new Route.RouteModifyCallback() {
                            @Override
                            public void onRouteEditSuccess(Path path) {
                                polylines.get(position).remove();
                                polylines.remove(position);
                                for (Polyline polyline : polylines) {
                                    polyline.setVisible(true);
                                }
                                recyclerView.getAdapter().notifyItemRemoved(position);
                                recyclerView.getAdapter().notifyItemRangeChanged(position, routes.size());
                                deleteButton.setVisibility(View.GONE);
                                if (routes.size() == 0) {
                                    getParentFragmentManager().popBackStack("Map", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                }
                                Toast.makeText(getContext(), R.string.delete_route_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onRouteEditFailure() {
                                Toast.makeText(getContext(), R.string.delete_route_failure, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }))
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .create().show();
            });
        } else {
            deleteButton.setVisibility(View.GONE);
        }
    }

}
