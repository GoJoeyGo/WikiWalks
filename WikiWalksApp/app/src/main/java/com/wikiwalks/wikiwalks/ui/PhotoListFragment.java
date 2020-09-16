package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.MainActivity;
import com.wikiwalks.wikiwalks.DataMap;
import com.wikiwalks.wikiwalks.Photo;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.PhotoDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.PhotoListRecyclerViewAdapter;

import java.util.ArrayList;

public class PhotoListFragment extends Fragment implements Photo.GetPhotosCallback, PhotoDialog.EditPhotoDialogListener {

    private int parentId;
    private RecyclerView recyclerView;
    private ArrayList<Photo> photos;
    private TextView noPhotosIndicator;
    private Photo.PhotoType type;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static PhotoListFragment newInstance(Photo.PhotoType type, int parentId) {
        Bundle args = new Bundle();
        args.putInt("parentId", parentId);
        args.putSerializable("type", type);
        PhotoListFragment fragment = new PhotoListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void updatePhotosList(boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (type == Photo.PhotoType.PATH) {
            DataMap.getInstance().getPathList().get(parentId).getPhotos(getContext(), refresh, PhotoListFragment.this);
        } else {
            DataMap.getInstance().getPointOfInterestList().get(parentId).getPhotos(getContext(), refresh, PhotoListFragment.this);
        }
    }

    public void updateRecyclerView() {
        if (photos.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noPhotosIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noPhotosIndicator.setVisibility(View.GONE);
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.getAdapter().notifyItemRangeChanged(0, photos.size());
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    public void launchEditDialog(int position, Bitmap bitmap) {
        PhotoDialog.newInstance(type, parentId, position, bitmap).show(getChildFragmentManager(), "PhotoPopup");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.photo_list_fragment, container, false);

        parentId = getArguments().getInt("parentId");
        type = (Photo.PhotoType) getArguments().getSerializable("type");

        String title;
        if (type == Photo.PhotoType.PATH) {
            photos = DataMap.getInstance().getPathList().get(parentId).getPhotosList();
            title = DataMap.getInstance().getPathList().get(parentId).getName();
        } else {
            photos = DataMap.getInstance().getPointOfInterestList().get(parentId).getPhotosList();
            title = DataMap.getInstance().getPointOfInterestList().get(parentId).getName();
        }

        MaterialToolbar toolbar = rootView.findViewById(R.id.photo_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(String.format(getString(R.string.photos_title), title));

        noPhotosIndicator = rootView.findViewById(R.id.photo_list_empty_indicator);

        recyclerView = rootView.findViewById(R.id.path_photo_list_recyclerview);
        recyclerView.setAdapter(new PhotoListRecyclerViewAdapter(this, photos));
        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        } else {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing()) {
                    updatePhotosList(false);
                }
            }
        });

        swipeRefreshLayout = rootView.findViewById(R.id.photo_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> updatePhotosList(true));

        Button addPhotoButton = rootView.findViewById(R.id.photo_list_add_photo_button);
        addPhotoButton.setOnClickListener(v -> MainActivity.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, granted -> {
            if (granted) {
                launchEditDialog(-1, null);
            } else {
                getParentFragmentManager().beginTransaction().add(R.id.main_frame, PermissionsFragment.newInstance(Manifest.permission.WRITE_EXTERNAL_STORAGE)).addToBackStack(null).commit();
            }
        }));
        if (savedInstanceState == null) {
            updatePhotosList(true);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("not_new", true);
    }

    @Override
    public void onGetPhotosSuccess() {
        updateRecyclerView();
    }

    @Override
    public void onGetPhotosFailure() {
        Toast.makeText(getContext(), R.string.get_photos_failure, Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onEditPhoto() {
        updateRecyclerView();
    }

    @Override
    public void onDeletePhoto(int position) {
        recyclerView.getAdapter().notifyItemRemoved(position);
        recyclerView.getAdapter().notifyItemRangeChanged(position, photos.size());
        updateRecyclerView();
    }
}
