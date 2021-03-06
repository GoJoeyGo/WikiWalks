package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.DataMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;

import java.util.HashMap;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, DataMap.DataMapListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<Integer, Polyline> polylines = new HashMap<>();
    private HashMap<Integer, Marker> markers = new HashMap<>();
    private boolean hasFailed = false;

    public static MapsFragment newInstance() {
        return new MapsFragment();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.map_fragment, container, false);

        DataMap.getInstance().addListener(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        MaterialToolbar toolbar = rootView.findViewById(R.id.map_fragment_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.maps_menu_settings:
                    getParentFragmentManager().beginTransaction().add(R.id.main_frame, SettingsFragment.newInstance()).addToBackStack(null).commit();
                    break;
                case R.id.maps_menu_bookmarks:
                    getParentFragmentManager().beginTransaction().add(R.id.main_frame, BookmarksFragment.newInstance()).addToBackStack(null).commit();
                    break;
            }
            return true;
        });

        Button createPath = rootView.findViewById(R.id.map_fragment_add_button);
        createPath.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(-1)).addToBackStack(null).commit());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment_map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onDestroy() {
        DataMap.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        setMapLocation();
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraIdleListener(() -> {
            LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            if (Math.abs(bounds.northeast.latitude - bounds.southwest.latitude) < 3) {
                DataMap.getInstance().updatePaths(mMap.getProjection().getVisibleRegion().latLngBounds, getContext());
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        getParentFragmentManager().beginTransaction().add(R.id.main_frame, PathFragment.newInstance((Integer) marker.getTag())).addToBackStack("Map").commit();
        return true;
    }

    public void setMapLocation() {
        MainActivity.checkPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION, granted -> {
            if (granted) {
                mMap.setMyLocationEnabled(true);
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    } else {
                        setMapLocation();
                    }
                });
            } else {
                getParentFragmentManager().beginTransaction().add(R.id.main_frame, PermissionsFragment.newInstance(Manifest.permission.ACCESS_FINE_LOCATION)).addToBackStack(null).commit();
                setMapLocation();
            }
        });
    }

    @Override
    public void onDataMapUpdateSuccess() {
        DataMap dataMap = DataMap.getInstance();
        HashMap<Integer, Path> paths = dataMap.getPathList();
        for (Map.Entry<Integer, Polyline> polylineEntry : polylines.entrySet()) {
            polylineEntry.getValue().remove();
            polylines.remove(polylineEntry);
        }
        for (Map.Entry<Integer, Marker> markerEntry : markers.entrySet()) {
            markerEntry.getValue().remove();
            markers.remove(markerEntry);
        }
        for (Map.Entry<Integer, Path> pathEntry : paths.entrySet()) {
            Path path = pathEntry.getValue();
            for (Route route : path.getRoutes()) {
                Polyline polyline = route.makePolyline(mMap);
                polylines.put(route.getId(), polyline);
            }
            Marker marker = path.makeMarker(mMap);
            markers.put(pathEntry.getKey(), marker);
        }
    }

    @Override
    public void onDataMapUpdateFailure() {
        if (!hasFailed) {
            hasFailed = true;
            Toast.makeText(getContext(), R.string.get_paths_failure, Toast.LENGTH_SHORT).show();
        }
    }
}
