package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.content.pm.PackageManager;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.Picture;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.EditPictureDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.PictureListRecyclerViewAdapter;

import java.util.ArrayList;

public class PictureListFragment extends Fragment implements Picture.GetPicturesCallback, EditPictureDialog.EditPictureDialogListener {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 0;
    Button submitPictureButton;
    int parentId;
    RecyclerView recyclerView;
    PictureListRecyclerViewAdapter recyclerViewAdapter;
    Toolbar toolbar;
    ArrayList<Picture> pictures;
    TextView noPicturesIndicator;
    int position;
    EditPictureDialog editPictureDialog;
    Picture.PictureType type;
    SwipeRefreshLayout swipeRefreshLayout;

    public static PictureListFragment newInstance(Picture.PictureType type, int parentId) {
        Bundle args = new Bundle();
        args.putInt("parentId", parentId);
        args.putSerializable("type", type);
        PictureListFragment fragment = new PictureListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        parentId = getArguments().getInt("parentId");
        type = (Picture.PictureType) getArguments().getSerializable("type");
        String title;
        if (type == Picture.PictureType.PATH) {
            pictures = PathMap.getInstance().getPathList().get(parentId).getPicturesList();
            title = PathMap.getInstance().getPathList().get(parentId).getName();
        } else {
            pictures = PathMap.getInstance().getPointOfInterestList().get(parentId).getPicturesList();
            title = PathMap.getInstance().getPointOfInterestList().get(parentId).getName();
        }
        final View rootView = inflater.inflate(R.layout.picture_list_fragment, container, false);
        toolbar = rootView.findViewById(R.id.path_picture_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Pictures - " + title);
        noPicturesIndicator = rootView.findViewById(R.id.no_pictures_indicator);
        recyclerView = rootView.findViewById(R.id.path_picture_list_recyclerview);
        recyclerViewAdapter = new PictureListRecyclerViewAdapter(this, pictures);
        recyclerView.setAdapter(recyclerViewAdapter);
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
                    updatePicturesList(false);
                }
            }
        });
        swipeRefreshLayout = rootView.findViewById(R.id.picture_list_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> updatePicturesList(true));
        submitPictureButton = rootView.findViewById(R.id.path_submit_picture_button);
        submitPictureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions((new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}), REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                launchEditDialog(-1, null);
            }
        });
        updatePicturesList(true);
        return rootView;
    }

    public void updatePicturesList(boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (type == Picture.PictureType.PATH) {
            PathMap.getInstance().getPathList().get(parentId).getPictures(getContext(), refresh, PictureListFragment.this);
        } else {
            PathMap.getInstance().getPointOfInterestList().get(parentId).getPictures(getContext(), refresh, PictureListFragment.this);
        }
    }

    public void updateRecyclerView() {
        if (pictures.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noPicturesIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noPicturesIndicator.setVisibility(View.GONE);
            recyclerViewAdapter.notifyDataSetChanged();
            recyclerViewAdapter.notifyItemRangeChanged(0, pictures.size());
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    public void launchEditDialog(int position, Bitmap bitmap) {
        this.position = position;
        EditPictureDialog.newInstance(type, parentId, position, bitmap).show(getChildFragmentManager(), "PicturePopup");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchEditDialog(-1, null);
            } else {
                getParentFragmentManager().beginTransaction()
                        .add(R.id.main_frame, PermissionsFragment.newInstance(PermissionsFragment.PermissionType.STORAGE)).addToBackStack(null)
                        .commit();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onGetPicturesSuccess() {
        updateRecyclerView();
    }

    @Override
    public void onGetPicturesFailure() {
        Toast.makeText(getContext(), "Failed to get pictures...", Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void setPictureDialog(EditPictureDialog editPictureDialog) {
        this.editPictureDialog = editPictureDialog;
    }

    @Override
    public void onEditPicture() {
        updateRecyclerView();
    }

    @Override
    public void onDeletePicture() {
        recyclerViewAdapter.notifyItemRemoved(position);
        recyclerViewAdapter.notifyItemRangeChanged(position, pictures.size());
        updateRecyclerView();
    }
}
