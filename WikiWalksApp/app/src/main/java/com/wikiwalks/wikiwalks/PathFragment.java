package com.wikiwalks.wikiwalks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class PathFragment extends Fragment implements OnMapReadyCallback, EditDialog.EditDialogListener, Path.PathChangeCallback, PathMap.PathMapListener {

    Toolbar toolbar;
    Button selectRouteButton;
    Button recordRouteButton;
    Button exploreButton;
    ImageButton editButton;
    SupportMapFragment mapFragment;
    Path path;
    ConstraintLayout walkPathOptions;
    Button pointOfInterestButton;
    RatingBar ratingBar;
    EditDialog editDialog;
    ArrayList<Polyline> polylines = new ArrayList<>();
    GoogleMap mMap;

    public static PathFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        PathFragment fragment = new PathFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        PathMap.getInstance().addListener(this);
        final View rootView = inflater.inflate(R.layout.path_fragment, container, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_path_preview_frag);
        mapFragment.getMapAsync(this);
        walkPathOptions = rootView.findViewById(R.id.walk_path_option_selector);
        toolbar = rootView.findViewById(R.id.path_frag_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(path.getName());
        selectRouteButton = rootView.findViewById(R.id.select_route_button);
        selectRouteButton.setOnClickListener(view -> {
            RouteListFragment routeListFragment = RouteListFragment.newInstance(path.id);
            routeListFragment.setTargetFragment(this, 0);
            getParentFragmentManager().beginTransaction().add(R.id.main_frame, routeListFragment).addToBackStack(null).commit();
        });
        recordRouteButton = rootView.findViewById(R.id.new_route_button);
        recordRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(path.id)).addToBackStack(null).commit());
        exploreButton = rootView.findViewById(R.id.explore_button);
        exploreButton.setOnClickListener(view -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, WalkFragment.newInstance(path.id, -1)).addToBackStack(null).commit());
        editButton = rootView.findViewById(R.id.edit_title_button);
        editDialog = new EditDialog();
        editDialog.setTargetFragment(this, 0);
        editButton.setOnClickListener(v -> editDialog.show(getActivity().getSupportFragmentManager(), "EditPopup"));
        pointOfInterestButton = rootView.findViewById(R.id.path_frag_pois_button);

        ratingBar = rootView.findViewById(R.id.path_frag_rating_bar);
        ratingBar.setRating((float)path.getRating());
        TextView walkCount = rootView.findViewById(R.id.path_frag_walk_count);
        String walkCountString;
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
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (Route route : path.getRoutes()) polylines.add(route.makePolyline(googleMap));
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10 ));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public void onEdit(String title) {
        if (title.equals("")) {
            title = String.format("Path at %f, %f", path.getMarkerPoint().latitude, path.getMarkerPoint().longitude);
        }
        path.edit(getContext(), title, this);
    }

    @Override
    public void onEditSuccess() {
        editDialog.dismiss();
        toolbar.setTitle(path.getName());
    }

    @Override
    public void onEditFailure() {
        Toast.makeText(getContext(), "Failed to update path...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPathMapChange() {
        path = PathMap.getInstance().getPathList().get(path.id);
        if (path != null) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines = new ArrayList<>();
            for (Route route : path.getRoutes()) {
                polylines.add(route.makePolyline(mMap));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10));
    }

    @Override
    public void OnPathMapUpdateFailure() {

    }

    @Override
    public void onDestroy() {
        PathMap.getInstance().removeListener(this);
        super.onDestroy();
    }
}
