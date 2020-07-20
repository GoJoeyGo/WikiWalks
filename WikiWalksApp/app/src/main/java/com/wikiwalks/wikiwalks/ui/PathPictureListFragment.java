package com.wikiwalks.wikiwalks.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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

import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PathPicture;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.dialogs.EditPictureDialog;
import com.wikiwalks.wikiwalks.ui.dialogs.SubmitPictureDialog;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.PathPictureListRecyclerViewAdapter;

import java.util.ArrayList;

public class PathPictureListFragment extends Fragment implements Path.GetAdditionalCallback, SubmitPictureDialog.PictureDialogListener, EditPictureDialog.EditPictureDialogListener {

    Button submitPictureButton;
    Path path;
    RecyclerView recyclerView;
    PathPictureListRecyclerViewAdapter recyclerViewAdapter;
    Toolbar toolbar;
    ArrayList<PathPicture> pathPictures;
    TextView noPicturesIndicator;
    int position;


    private static final int REQUEST_CODE_ASK_PERMISSIONS = 0;

    public static PathPictureListFragment newInstance(int pathId) {
        Bundle args = new Bundle();
        args.putInt("pathId", pathId);
        PathPictureListFragment fragment = new PathPictureListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        path = PathMap.getInstance().getPathList().get(getArguments().getInt("pathId"));
        pathPictures = path.getPathPictures();
        final View rootView = inflater.inflate(R.layout.path_picture_list_fragment, container, false);
        toolbar = rootView.findViewById(R.id.path_picture_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Pictures - " + path.getName());
        noPicturesIndicator = rootView.findViewById(R.id.no_pictures_indicator);
        recyclerView = rootView.findViewById(R.id.path_picture_list_recyclerview);
        recyclerViewAdapter = new PathPictureListRecyclerViewAdapter(this, pathPictures);
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
                if (!recyclerView.canScrollVertically(1)) {
                    path.getPictures(getContext(), PathPictureListFragment.this);
                }
            }
        });
        submitPictureButton = rootView.findViewById(R.id.path_submit_picture_button);
        submitPictureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions((new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}), REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                launchSubmitDialog();
            }
        });
        updateRecyclerView();
        path.getPictures(getContext(), this);
        return rootView;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void updateRecyclerView() {
        if (path.getPathPictures().size() == 0) {
            recyclerView.setVisibility(View.GONE);
            noPicturesIndicator.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noPicturesIndicator.setVisibility(View.GONE);
            pathPictures = path.getPathPictures();
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public void launchSubmitDialog() {
        SubmitPictureDialog dialog = new SubmitPictureDialog(path);
        dialog.setTargetFragment(this, 0);
        dialog.show(getActivity().getSupportFragmentManager(), "PicturePopup");
    }

    public void launchEditDialog(int position) {
        this.position = position;
        EditPictureDialog dialog = new EditPictureDialog(pathPictures.get(position));
        dialog.setTargetFragment(this, 0);
        dialog.show(getActivity().getSupportFragmentManager(), "PicturePopup");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchSubmitDialog();
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
    public void onGetAdditionalSuccess() {
        updateRecyclerView();
    }

    @Override
    public void onGetAdditionalFailure() {
        Toast.makeText(getContext(), "Failed to get pictures", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubmit() {
        updateRecyclerView();
    }

    @Override
    public void onEdit() {
        updateRecyclerView();
    }

    @Override
    public void onDelete() {
        recyclerViewAdapter.notifyItemRemoved(position);
        recyclerViewAdapter.notifyItemRangeChanged(position, pathPictures.size());
    }
}
