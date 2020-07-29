package com.wikiwalks.wikiwalks.ui;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.dialogs.EditNameDialog;

import java.util.ArrayList;

public class RecordingFragment extends Fragment implements OnMapReadyCallback, EditNameDialog.EditDialogListener, Route.RouteModifyCallback, PointOfInterest.PointOfInterestSubmitCallback {

    GoogleMap mMap;
    Toolbar toolbar;
    EditNameDialog editNameDialog;
    private boolean recording = true;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Path path;
    private Button stopRecordingButton;
    private Polyline polyline;
    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private ArrayList<String> poiNames = new ArrayList<>();
    private ArrayList<Double> poiLatitudes = new ArrayList<>();
    private ArrayList<Double> poiLongitudes = new ArrayList<>();
    private int submittedPointsOfInterest = 0;
    private Location lastLocation;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    public static RecordingFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        RecordingFragment fragment = new RecordingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        int pathId = getArguments().getInt("pathId");
        if (pathId > -1)
            path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        if (savedInstanceState != null && savedInstanceState.containsKey("latitudes")) {
            double[] latitudeArray = savedInstanceState.getDoubleArray("latitudes");
            double[] longitudeArray = savedInstanceState.getDoubleArray("longitudes");
            double[] altitudeArray = savedInstanceState.getDoubleArray("altitudes");
            for (int i = 0; i < latitudeArray.length; i++) {
                latitudes.add(latitudeArray[i]);
                longitudes.add(longitudeArray[i]);
                altitudes.add(altitudeArray[i]);
                latLngs.add(new LatLng(latitudeArray[i], longitudeArray[i]));
            }
            lastLocation = new Location(LocationManager.GPS_PROVIDER);
            lastLocation.setLatitude(latitudeArray[latitudeArray.length - 1]);
            lastLocation.setLongitude(longitudeArray[longitudeArray.length - 1]);
            lastLocation.setAltitude(altitudeArray[altitudeArray.length - 1]);
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        final View rootView = inflater.inflate(R.layout.recording_fragment, container, false);
        toolbar = rootView.findViewById(R.id.recording_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle((pathId > -1) ? "New Route - " + path.getName() : "New Path");
        stopRecordingButton = rootView.findViewById(R.id.stop_recording_button);
        stopRecordingButton.setOnClickListener(v -> {
            if (recording) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                recording = false;
                stopRecordingButton.setText("RESUME RECORDING");
                if (path != null) {
                    boolean inRange = false;
                    ArrayList<Double> pathLatitudes = path.getAllLatitudes();
                    ArrayList<Double> pathLongitudes = path.getAllLongitudes();
                    for (int i = 0; i < pathLatitudes.size(); i++) {
                        Location pathLocation = new Location(LocationManager.GPS_PROVIDER);
                        pathLocation.setLatitude(pathLatitudes.get(i));
                        pathLocation.setLongitude(pathLongitudes.get(i));
                        for (int j = 0; j < latitudes.size(); j++) {
                            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
                            newLocation.setLatitude(latitudes.get(j));
                            newLocation.setLongitude(longitudes.get(j));
                            if (pathLocation.distanceTo(newLocation) < 10) {
                                inRange = true;
                                break;
                            }
                        }
                        if (inRange) break;
                    }
                    if (!inRange) {
                        new MaterialAlertDialogBuilder(context).setTitle("Make new path?").setMessage("Your route does not connect to the path and cannot be submitted. Make it a new path instead?").setPositiveButton("Yes", (dialog, which) -> {
                            path = null;
                            showSubmissionDialog(true);
                        }).setNegativeButton("No", (dialog, which) -> {

                        }).show();
                    } else showSubmissionDialog(false);
                } else showSubmissionDialog(true);
            } else {
                MainActivity.checkLocationPermission(this.getActivity());
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location.distanceTo(lastLocation) < 15) {
                        stopRecordingButton.setText("STOP RECORDING");
                        startLocationUpdates();
                    } else {
                        Toast.makeText(context, "Too far away from last point to resume!", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        Button markPointButton = rootView.findViewById(R.id.recording_mark_poi_button);
        markPointButton.setOnClickListener(v -> {
            EditNameDialog.newInstance(EditNameDialog.EditNameDialogType.POINT_OF_INTEREST, -1).show(getChildFragmentManager(), "SubmissionPopup");
        });
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.walk_map_frag);
        mapFragment.getMapAsync(this);
        context = getContext();
        return rootView;
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        MainActivity.checkLocationPermission(this.getActivity());
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setCompassEnabled(false);
        if (path != null) {
            for (Route route : path.getRoutes()) {
                route.makePolyline(mMap);
            }
        }
        polyline = mMap.addPolyline(new PolylineOptions().addAll(latLngs));
        polyline.setWidth(20);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20)));
        startLocationUpdates();
    }

    public void setPath(Path path) {
        this.path = path;
    }

    private void startLocationUpdates() {
        recording = true;
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        MainActivity.checkLocationPermission(this.getActivity());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        addLocation(location);

        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(mMap.getCameraPosition().zoom).bearing(location.getBearing()).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        polyline.setPoints(latLngs);
    }

    public void addLocation(Location location) {
        if (lastLocation == null || location.distanceTo(lastLocation) > 2) {
            latitudes.add(location.getLatitude());
            longitudes.add(location.getLongitude());
            altitudes.add(location.getAltitude());
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
            lastLocation = location;
        }
    }

    public void showSubmissionDialog(boolean newPath) {
        new MaterialAlertDialogBuilder(context).setTitle("Submit?").setPositiveButton("Yes", (dialog, which) -> {
            if (newPath)
                EditNameDialog.newInstance(EditNameDialog.EditNameDialogType.PATH, -1).show(getChildFragmentManager(), "SubmissionPopup");
            else submitRoute("");
        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
    }

    public void submitRoute(String title) {
        if (title.equals("")) {
            title = String.format("Path at %f, %f", latitudes.get(0), longitudes.get(0));
        }
        Route.submit(this.getContext(), path, title, latitudes, longitudes, altitudes, this);
    }

    @Override
    public void onRouteModifySuccess(Path path) {
        if (editNameDialog != null) editNameDialog.dismiss();
        if (poiNames.size() > 0) {
            for (int i = 0; i < poiNames.size(); i++) {
                PointOfInterest.submit(getContext(), poiNames.get(i), poiLatitudes.get(i), poiLongitudes.get(i), path, this);
            }
        } else getParentFragmentManager().popBackStack();
    }

    @Override
    public void onRouteModifyFailure() {
        Toast.makeText(getContext(), "Failed to submit route...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        double[] latitudeArray = new double[latitudes.size()];
        double[] longitudeArray = new double[longitudes.size()];
        double[] altitudeArray = new double[altitudes.size()];
        for (int i = 0; i < latitudes.size(); i++) {
            latitudeArray[i] = latitudes.get(i);
            longitudeArray[i] = longitudes.get(i);
            altitudeArray[i] = altitudes.get(i);
        }
        outState.putDoubleArray("latitudes", latitudeArray);
        outState.putDoubleArray("longitudes", longitudeArray);
        outState.putDoubleArray("altitudes", altitudeArray);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setEditNameDialog(EditNameDialog editNameDialog) {
        this.editNameDialog = editNameDialog;
    }

    @Override
    public void onEditName(EditNameDialog.EditNameDialogType type, String name) {
        if (type == EditNameDialog.EditNameDialogType.PATH) submitRoute(name);
        else {
            if (name.isEmpty())
                name = String.format("Point at %f, %f", lastLocation.getLatitude(), lastLocation.getLongitude());
            poiNames.add(name);
            poiLatitudes.add(lastLocation.getLatitude());
            poiLongitudes.add(lastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
            editNameDialog.dismiss();
        }
    }

    @Override
    public void onSubmitPointOfInterestSuccess(PointOfInterest pointOfInterest) {
        submittedPointsOfInterest++;
        if (submittedPointsOfInterest == poiNames.size()) getParentFragmentManager().popBackStack();
    }

    @Override
    public void onSubmitPointOfInterestFailure() {

    }
}
