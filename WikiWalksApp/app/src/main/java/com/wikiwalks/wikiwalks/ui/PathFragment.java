package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.Picture;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.dialogs.EditNameDialog;

import java.util.ArrayList;

public class PathFragment extends Fragment implements OnMapReadyCallback, EditNameDialog.EditDialogListener, Path.PathChangeCallback, PathMap.PathMapListener {

    private Toolbar toolbar;
    private SupportMapFragment mapFragment;
    private Path path;
    private EditNameDialog editNameDialog;
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private GoogleMap mMap;

    public static PathFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        PathFragment fragment = new PathFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.path_fragment, container, false);

        path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        PathMap.getInstance().addListener(this);

        toolbar = rootView.findViewById(R.id.path_frag_toolbar);
        toolbar.setTitle(path.getName());
        if (PreferencesManager.getInstance(getContext()).isBookmarked(path.getId())) toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_bookmark_24);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.path_menu_edit:
                    EditNameDialog.newInstance(EditNameDialog.EditNameDialogType.PATH, path.getId()).show(getChildFragmentManager(), "EditPopup");
                    break;

                case R.id.path_menu_bookmark:
                    menuItem.setIcon((PreferencesManager.getInstance(getContext()).toggleBookmark(path.getId())) ? R.drawable.ic_baseline_bookmark_24 : R.drawable.ic_baseline_bookmark_border_24);
                    break;
            }
            return true;
        });

        Button selectRouteButton = rootView.findViewById(R.id.select_route_button);
        selectRouteButton.setOnClickListener(view -> {
            RouteListFragment routeListFragment = RouteListFragment.newInstance(path.getId());
            routeListFragment.setTargetFragment(this, 0);
            getParentFragmentManager().beginTransaction().add(R.id.main_frame, routeListFragment).addToBackStack(null).commit();
        });

        Button recordRouteButton = rootView.findViewById(R.id.new_route_button);
        recordRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(path.getId())).addToBackStack(null).commit());

        Button exploreButton = rootView.findViewById(R.id.explore_button);
        exploreButton.setOnClickListener(view -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, WalkFragment.newInstance(path.getId(), -1)).addToBackStack(null).commit());

        Button pointOfInterestButton = rootView.findViewById(R.id.path_frag_pois_button);
        pointOfInterestButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, PointOfInterestListFragment.newInstance(path.getId())).addToBackStack(null).commit());

        Button groupWalksButton = rootView.findViewById(R.id.path_frag_group_walks_button);
        groupWalksButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, GroupWalkListFragment.newInstance(path.getId())).addToBackStack(null).commit());

        Button reviewButton = rootView.findViewById(R.id.path_frag_reviews_button);
        reviewButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, ReviewListFragment.newInstance(Review.ReviewType.PATH, path.getId())).addToBackStack(null).commit());

        Button picturesButton = rootView.findViewById(R.id.path_frag_pictures_button);
        picturesButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, PictureListFragment.newInstance(Picture.PictureType.PATH, path.getId())).addToBackStack(null).commit());

        RatingBar ratingBar = rootView.findViewById(R.id.path_frag_rating_bar);
        ratingBar.setRating((float) path.getRating());

        TextView walkCount = rootView.findViewById(R.id.path_frag_walk_count);
        String walkCountString = (path.getWalkCount() == 1) ? "Path has been walked once." : String.format("Path has been walked %s times.", path.getWalkCount());
        walkCount.setText(walkCountString);

        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_path_preview_frag);

        return rootView;
    }

    @Override
    public void onStart() {
        mapFragment.getMapAsync(this);
        super.onStart();
    }

    @Override
    public void onDestroy() {
        PathMap.getInstance().removeListener(this);
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (Route route : path.getRoutes()) polylines.add(route.makePolyline(googleMap));
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), 20));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public void setEditNameDialog(EditNameDialog editNameDialog) {
        this.editNameDialog = editNameDialog;
    }

    @Override
    public void onEditName(EditNameDialog.EditNameDialogType type, String name) {
        if (name.equals("")) {
            name = String.format("Path at %f, %f", path.getMarkerPoint().latitude, path.getMarkerPoint().longitude);
        }
        path.edit(getContext(), name, this);
    }

    @Override
    public void onEditSuccess() {
        editNameDialog.dismiss();
        toolbar.setTitle(path.getName());
    }

    @Override
    public void onEditFailure() {
        Toast.makeText(getContext(), "Failed to update path...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPathMapUpdateSuccess() {
        path = PathMap.getInstance().getPathList().get(path.getId());
        if (path != null) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines = new ArrayList<>();
            for (Route route : path.getRoutes()) {
                polylines.add(route.makePolyline(mMap));
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), 20));
    }

    @Override
    public void onPathMapUpdateFailure() {

    }
}
