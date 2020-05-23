package com.wikiwalks.wikiwalks;

import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PathFragment extends Fragment implements OnMapReadyCallback {

    Button selectRouteButton;
    Button recordRouteButton;
    Button exploreButton;
    SupportMapFragment mapFragment;
    private GoogleMap pathPreviewMap;
    Path path;
    ConstraintLayout walkPathOptions;
    Button pointOfInterestButton;
    RatingBar ratingBar;

    public static PathFragment newInstance(Path path) {
        Bundle args = new Bundle();
        PathFragment fragment = new PathFragment();
        fragment.setArguments(args);
        fragment.setPath(path);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.path_fragment, container, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_path_preview_frag);
        mapFragment.getMapAsync(this);
        walkPathOptions = rootView.findViewById(R.id.walk_path_option_selector);
        selectRouteButton = rootView.findViewById(R.id.select_route_button);
        if (path.getChildPaths().size() == 0) selectRouteButton.setText("WALK PATH");
        selectRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (path.getChildPaths().size() == 0) {
                    getFragmentManager().beginTransaction().add(R.id.main_frame, WalkFragment.newInstance(path, true)).addToBackStack(null).commit();
                } else getFragmentManager().beginTransaction().add(R.id.main_frame, RouteListFragment.newInstance(path)).addToBackStack(null).commit();
            }
        });
        recordRouteButton = rootView.findViewById(R.id.new_route_button);
        recordRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(path)).addToBackStack(null).commit();
            }
        });
        exploreButton = rootView.findViewById(R.id.explore_button);
        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().add(R.id.main_frame, WalkFragment.newInstance(path, false)).addToBackStack(null).commit();
            }
        });
        pointOfInterestButton = rootView.findViewById(R.id.path_frag_pois_button);
        if (path.getAllPointsOfInterest().size() == 0) {
            pointOfInterestButton.setVisibility(View.GONE);
        }
        ratingBar = rootView.findViewById(R.id.path_frag_rating_bar);
        ratingBar.setRating((float)path.getRating());
        TextView title = rootView.findViewById(R.id.path_frag_title);
        title.setText(path.getName());
        TextView walkCount = rootView.findViewById(R.id.path_frag_walk_count);
        String walkCountString = "";
        if (path.getWalkCount() == 1) {
            walkCountString = "Path has been walked once.";
        }
        else {
            walkCountString = String.format("Path has been walked %s times.", path.getWalkCount());
        }
        walkCount.setText(walkCountString);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        pathPreviewMap = googleMap;
        LatLng startingPoint = new LatLng(path.getLatitudes().get(0), path.getLongitudes().get(0));
        path.makePolyLine(pathPreviewMap);
        for (Path child : path.getChildPaths()) {
            child.makePolyLine(pathPreviewMap);
        }
        pathPreviewMap.addMarker(new MarkerOptions().position(startingPoint));
        pathPreviewMap.getUiSettings().setAllGesturesEnabled(false);
        pathPreviewMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10 ));
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
