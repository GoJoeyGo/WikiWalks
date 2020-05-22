package com.wikiwalks.wikiwalks;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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

import java.util.ArrayList;

public class RecordingFragment extends Fragment implements OnMapReadyCallback, SubmissionDialog.SubmissionDialogListener {

    private Double[] startingPoint;
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

    public static RecordingFragment newInstance(Path path, Double[] startingPoint) {
        Bundle args = new Bundle();
        RecordingFragment fragment = new RecordingFragment();
        fragment.setArguments(args);
        fragment.setParentPath(path);
        fragment.setStartingPoint(startingPoint);
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
                    fusedLocationProviderClient.removeLocationUpdates(new LocationCallback());
                    recording = false;
                    stopRecordingButton.setText("RESUME RECORDING");
                    showSubmissionDialog();
                } else {
                    stopRecordingButton.setText("STOP RECORDING");
                    startLocationUpdates();
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
        if (parentPath != null) {
            latitudes.add(startingPoint[0]);
            longitudes.add(startingPoint[1]);
            altitudes.add(startingPoint[2]);
            latLngs.add(new LatLng(startingPoint[0], startingPoint[1]));
            parentPath.makePolyLine(mMap);
            Path parentParent = parentPath.getParentPath();
            while (parentParent != null) {
                parentParent.makePolyLine(mMap);
                parentParent = parentParent.getParentPath();
            }
            for (Path child : parentPath.getChildPaths()) {
                child.makePolyLine(mMap);
            }
        }
        polyline = mMap.addPolyline(new PolylineOptions());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
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
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private void onLocationChanged(Location location) {

        if (lastLocation == null || location.distanceTo(lastLocation) > 2) {
            latitudes.add(location.getLatitude());
            longitudes.add(location.getLongitude());
            altitudes.add(location.getAltitude());
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
            lastLocation = location;
        }


        CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).bearing(location.getBearing()).zoom(20).build();
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

    }

    @Override
    public void onNegativeClick() {

    }

    public void setStartingPoint(Double[] startingPoint) {
        this.startingPoint = startingPoint;
    }
}
