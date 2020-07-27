package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.Picture;
import com.wikiwalks.wikiwalks.PointOfInterest;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.Review;
import com.wikiwalks.wikiwalks.Route;

public class PointOfInterestFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mMap;
    PointOfInterest pointOfInterest;
    Button reviewsButton;
    Button picturesButton;
    SupportMapFragment mapFragment;

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
        pointOfInterest = PathMap.getInstance().getPointOfInterestList().get(getArguments().getInt("point_of_interest_id"));
        final View rootView = inflater.inflate(R.layout.poi_fragment, container, false);
        reviewsButton = rootView.findViewById(R.id.poi_frag_reviews_button);
        reviewsButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, ReviewListFragment.newInstance(Review.ReviewType.POINT_OF_INTEREST, pointOfInterest.getId())).addToBackStack(null).commit());
        picturesButton = rootView.findViewById(R.id.poi_frag_pictures_button);
        picturesButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, PictureListFragment.newInstance(Picture.PictureType.POINT_OF_INTEREST, pointOfInterest.getId())).addToBackStack(null).commit());
        TextView title = rootView.findViewById(R.id.poi_frag_title);
        title.setText(pointOfInterest.getName());
        RatingBar ratingBar = rootView.findViewById(R.id.poi_frag_rating_bar);
        ratingBar.setRating((float) pointOfInterest.getRating());
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_poi_preview_frag);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        for (Route route : pointOfInterest.getPath().getRoutes()) route.makePolyline(mMap);
        pointOfInterest.makeMarker(mMap);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(pointOfInterest.getPath().getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10));
    }
}
