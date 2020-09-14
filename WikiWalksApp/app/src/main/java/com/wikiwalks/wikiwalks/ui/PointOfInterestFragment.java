package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.Photo;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;
import com.wikiwalks.wikiwalks.Route;
import com.wikiwalks.wikiwalks.ui.dialogs.NameDialog;

public class PointOfInterestFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, NameDialog.EditDialogListener, PointOfInterest.PointOfInterestEditCallback {

    private MaterialToolbar toolbar;
    private PointOfInterest pointOfInterest;
    private SupportMapFragment mapFragment;
    private NameDialog nameDialog;

    public static PointOfInterestFragment newInstance(int pointOfInterestId) {
        Bundle args = new Bundle();
        PointOfInterestFragment fragment = new PointOfInterestFragment();
        args.putInt("point_of_interest_id", pointOfInterestId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.poi_fragment, container, false);

        pointOfInterest = PathMap.getInstance().getPointOfInterestList().get(getArguments().getInt("point_of_interest_id"));

        toolbar = rootView.findViewById(R.id.poi_fragment_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        if (pointOfInterest.isEditable()) {
            toolbar.getMenu().getItem(0).setVisible(true);
        }
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.point_of_interest_menu_edit:
                    NameDialog.newInstance(NameDialog.EditNameDialogType.POINT_OF_INTEREST, pointOfInterest.getId()).show(getChildFragmentManager(), "EditPopup");
                    break;

                case R.id.point_of_interest_menu_delete:
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(R.string.delete_point_of_interest_prompt)
                            .setPositiveButton(R.string.yes, (dialog, which) -> pointOfInterest.delete(getContext(), this))
                            .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                            .create().show();
                    break;
            }
            return false;
        });
        toolbar.setTitle(pointOfInterest.getName());

        TextView description = rootView.findViewById(R.id.poi_fragment_description);
        description.setText(String.format(getString(R.string.point_of_interest_description), pointOfInterest.getPath().getName()));

        Button reviewsButton = rootView.findViewById(R.id.poi_fragment_reviews_button);
        reviewsButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, ReviewListFragment.newInstance(Review.ReviewType.POINT_OF_INTEREST, pointOfInterest.getId())).addToBackStack(null).commit());

        Button photosButton = rootView.findViewById(R.id.poi_fragment_photos_button);
        photosButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, PhotoListFragment.newInstance(Photo.PhotoType.POINT_OF_INTEREST, pointOfInterest.getId())).addToBackStack(null).commit());

        RatingBar ratingBar = rootView.findViewById(R.id.poi_fragment_rating_bar);
        ratingBar.setRating((float) pointOfInterest.getRating());

        getParentFragmentManager().setFragmentResultListener("update_point_rating", this, (requestKey, result) -> ratingBar.setRating((float) pointOfInterest.getRating()));

        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.poi_fragment_map);

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
        for (Route route : pointOfInterest.getPath().getRoutes()) route.makePolyline(googleMap);
        pointOfInterest.makeMarker(googleMap, BitmapDescriptorFactory.HUE_RED);
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.setOnMarkerClickListener(this);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(pointOfInterest.getPath().getBounds(), 20));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    public void setNameDialog(NameDialog nameDialog) {
        this.nameDialog = nameDialog;
    }

    @Override
    public void onEditName(NameDialog.EditNameDialogType type, String name) {
        pointOfInterest.edit(getContext(), name, this);
    }

    @Override
    public void onEditPointOfInterestSuccess() {
        toolbar.setTitle(pointOfInterest.getName());
        getParentFragmentManager().setFragmentResult("update_poi_list", new Bundle());
        nameDialog.dismiss();
        Toast.makeText(getContext(), R.string.save_point_of_interest_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditPointOfInterestFailure() {
        Toast.makeText(getContext(), R.string.save_point_of_interest_failure, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePointOfInterestSuccess() {
        getParentFragmentManager().setFragmentResult("update_poi_list", new Bundle());
        getParentFragmentManager().popBackStack();
        Toast.makeText(getContext(), R.string.delete_point_of_interest_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePointOfInterestFailure() {
        Toast.makeText(getContext(), R.string.delete_point_of_interest_failure, Toast.LENGTH_SHORT).show();
    }
}
