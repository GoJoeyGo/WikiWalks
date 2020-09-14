package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.DataMap;
import com.wikiwalks.wikiwalks.Photo;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.dialogs.NameDialog;

import java.util.ArrayList;

public class PathFragment extends Fragment implements OnMapReadyCallback, NameDialog.EditDialogListener, Path.PathChangeCallback, DataMap.PathMapListener {

    private MaterialToolbar toolbar;
    private SupportMapFragment mapFragment;
    private Path path;
    private NameDialog nameDialog;
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

        path = DataMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        DataMap.getInstance().addListener(this);

        toolbar = rootView.findViewById(R.id.path_fragment_toolbar);
        toolbar.setTitle(path.getName());
        if (PreferencesManager.getInstance(getContext()).isBookmarked(path.getId())) {
            toolbar.getMenu().getItem(0).setIcon(R.drawable.ic_baseline_bookmark_24);
        }
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.path_menu_edit:
                    NameDialog.newInstance(NameDialog.EditNameDialogType.PATH, path.getId()).show(getChildFragmentManager(), "EditPopup");
                    break;

                case R.id.path_menu_bookmark:
                    menuItem.setIcon((PreferencesManager.getInstance(getContext()).toggleBookmark(path.getId())) ? R.drawable.ic_baseline_bookmark_24 : R.drawable.ic_baseline_bookmark_border_24);
                    break;
            }
            return true;
        });

        Button selectRouteButton = rootView.findViewById(R.id.path_fragment_select_route_button);
        selectRouteButton.setOnClickListener(view -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RouteListFragment.newInstance(path.getId())).addToBackStack(null).commit());

        Button recordRouteButton = rootView.findViewById(R.id.path_fragment_new_route_button);
        recordRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, RecordingFragment.newInstance(path.getId())).addToBackStack(null).commit());

        Button exploreButton = rootView.findViewById(R.id.path_fragment_explore_button);
        exploreButton.setOnClickListener(view -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, WalkFragment.newInstance(path.getId(), -1)).addToBackStack(null).commit());

        Button pointOfInterestButton = rootView.findViewById(R.id.path_fragment_pois_button);
        pointOfInterestButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, PointOfInterestListFragment.newInstance(path.getId())).addToBackStack(null).commit());

        Button groupWalksButton = rootView.findViewById(R.id.path_fragment_group_walks_button);
        groupWalksButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, GroupWalkListFragment.newInstance(path.getId())).addToBackStack(null).commit());

        Button reviewButton = rootView.findViewById(R.id.path_fragment_reviews_button);
        reviewButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, ReviewListFragment.newInstance(Review.ReviewType.PATH, path.getId())).addToBackStack(null).commit());

        Button photosButton = rootView.findViewById(R.id.path_fragment_photos_button);
        photosButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, PhotoListFragment.newInstance(Photo.PhotoType.PATH, path.getId())).addToBackStack(null).commit());

        RatingBar ratingBar = rootView.findViewById(R.id.path_fragment_rating_bar);
        ratingBar.setRating((float) path.getRating());

        getParentFragmentManager().setFragmentResultListener("update_rating", this, (requestKey, result) -> ratingBar.setRating((float) path.getRating()));

        TextView walkCount = rootView.findViewById(R.id.path_fragment_walk_count);
        String walkCountString = path.getWalkCount() == 1 ? getString(R.string.walk_count_once) : String.format(getString(R.string.walk_count_multiple), path.getWalkCount());
        walkCount.setText(walkCountString);

        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.path_fragment_map);

        return rootView;
    }

    @Override
    public void onStart() {
        mapFragment.getMapAsync(this);
        super.onStart();
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
        for (Route route : path.getRoutes()) polylines.add(route.makePolyline(googleMap));
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), 20));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public void setNameDialog(NameDialog nameDialog) {
        this.nameDialog = nameDialog;
    }

    @Override
    public void onEditName(NameDialog.EditNameDialogType type, String name) {
        if (name.equals("")) {
            name = String.format("Path at %f, %f", path.getMarkerPoint().latitude, path.getMarkerPoint().longitude);
        }
        path.edit(getContext(), name, this);
    }

    @Override
    public void onEditSuccess() {
        nameDialog.dismiss();
        toolbar.setTitle(path.getName());
        Toast.makeText(getContext(), R.string.save_path_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditFailure() {
        Toast.makeText(getContext(), R.string.save_path_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPathMapUpdateSuccess() {
        path = DataMap.getInstance().getPathList().get(path.getId());
        if (path != null) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines = new ArrayList<>();
            for (Route route : path.getRoutes()) {
                polylines.add(route.makePolyline(mMap));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), 20));
        }
    }

    @Override
    public void onPathMapUpdateFailure() {

    }
}
