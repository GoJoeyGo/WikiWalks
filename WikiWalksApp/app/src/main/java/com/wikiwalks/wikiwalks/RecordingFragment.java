package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class RecordingFragment extends Fragment implements OnMapReadyCallback, SubmissionDialog.SubmissionDialogListener {

    private boolean recording = true;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Path parentPath;
    private LocationRequest locationRequest;
    private Button stopRecordingButton;
    private Polyline polyline;
    private ArrayList<Double> latitudes = new ArrayList<>();
    private ArrayList<Double> longitudes = new ArrayList<>();
    private ArrayList<Double> altitudes = new ArrayList<>();
    private ArrayList<LatLng> latLngs = new ArrayList<>();
    private Location lastLocation;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    public static RecordingFragment newInstance(Path path) {
        Bundle args = new Bundle();
        RecordingFragment fragment = new RecordingFragment();
        fragment.setArguments(args);
        fragment.setParentPath(path);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        final View rootView = inflater.inflate(R.layout.recording_fragment, container, false);
        stopRecordingButton = rootView.findViewById(R.id.stop_recording_button);
        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    recording = false;
                    stopRecordingButton.setText("RESUME RECORDING");
                    if (parentPath != null) {
                        boolean inRange = false;
                        float shortestDistance[] = new float[3];
                        ArrayList<Double> pathLatitudes = parentPath.getAllLatitudes();
                        ArrayList<Double> pathLongitudes = parentPath.getAllLongitudes();
                        ArrayList<Double> pathAltitudes = parentPath.getAllAltitudes();
                        for (int i = 0; i < pathLatitudes.size(); i++) {
                            for (int j = 0; j < latitudes.size(); j++) {
                                if (pathLatitudes.get(i) - 0.00005 < latitudes.get(j) && latitudes.get(j) < pathLatitudes.get(i) + 0.00005 && pathLongitudes.get(i) - 0.00005 < longitudes.get(j) && longitudes.get(j) < pathLongitudes.get(i) + 0.00005) {
                                    latitudes.add(j, pathLatitudes.get(i));
                                    longitudes.add(j, pathLongitudes.get(i));
                                    altitudes.add(j, pathAltitudes.get(i));
                                    inRange = true;
                                    break;
                                }
                            }
                        }
                        if (!inRange) {
                            new MaterialAlertDialogBuilder(context).setTitle("Make new path?").setMessage("Your route does not connect to the path and cannot be submitted. Make it a new path instead?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    parentPath = null;
                                    showSubmissionDialog();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        } else showSubmissionDialog();
                    } else showSubmissionDialog();

                } else {
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location.distanceTo(lastLocation) < 15) {
                                stopRecordingButton.setText("STOP RECORDING");
                                startLocationUpdates();
                            } else {
                                Toast.makeText(context, "Too far away from last point to resume!", Toast.LENGTH_SHORT);
                            }
                        }
                    });

                }
            }
        });
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.walk_map_frag);
        mapFragment.getMapAsync(this);
        context = getContext();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);
        mMap.getUiSettings().setCompassEnabled(false);
        if (parentPath != null) {
            parentPath.makePolyLine(mMap);
            if (parentPath.getParentPath() != null) {
                Path parentParent = parentPath.getParentPath();
                while (parentParent.getParentPath() != null) {
                    parentParent = parentParent.getParentPath();
                }
                parentParent.makeAllPolyLines(mMap);
            } else parentPath.makeAllPolyLines(mMap);
        }
        polyline = mMap.addPolyline(new PolylineOptions());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20));
            }
        });
        startLocationUpdates();
    }

    public void setParentPath(Path parentPath) {
        this.parentPath = parentPath;
    }

    private void startLocationUpdates() {
        recording = true;
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void onLocationChanged(Location location) {

        if (lastLocation == null || location.distanceTo(lastLocation) > 2) {
            latitudes.add(location.getLatitude());
            longitudes.add(location.getLongitude());
            altitudes.add(location.getAltitude());
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
            lastLocation = location;
        }


        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(mMap.getCameraPosition().zoom).bearing(location.getBearing()).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

        polyline.setPoints(latLngs);
    }

    public void showSubmissionDialog() {
        SubmissionDialog dialog = new SubmissionDialog();
        dialog.setTargetFragment(this, 0);
        dialog.show(getActivity().getSupportFragmentManager(), "SubmissionPopup");
    }

    @Override
    public void onPositiveClick(String title) {
        if (title.equals("")) {
            title = String.format("Path at %f, %f", latitudes.get(0), longitudes.get(0));
        }
        Toast.makeText(context, title, Toast.LENGTH_SHORT).show();
        Path newPath = new Path(title, latitudes, longitudes, altitudes, parentPath);
        newPath.submit(context);
    }

    @Override
    public void onNegativeClick() {

    }
}
