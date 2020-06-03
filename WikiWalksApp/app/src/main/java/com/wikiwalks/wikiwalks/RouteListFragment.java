package com.wikiwalks.wikiwalks;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Polyline;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RouteListFragment extends Fragment implements OnMapReadyCallback, EditDialog.EditDialogListener, Path.PathChangeCallback {

    Button selectRouteButton;
    Button editButton;
    SupportMapFragment mapFragment;
    private GoogleMap routeListMap;
    Path path;
    ArrayList<Path> pathList;
    ArrayList<Polyline> polylines = new ArrayList<>();
    RecyclerView recyclerView;
    int position;
    EditDialog editDialog;
    AlertDialog confirmationDialog;
    RouteListRecyclerViewAdapter recyclerViewAdapter;
    TextView title;

    public static RouteListFragment newInstance(Path path) {
        Bundle args = new Bundle();
        RouteListFragment fragment = new RouteListFragment();
        fragment.setArguments(args);
        fragment.setPath(path);
        ArrayList<Path> pathList = new ArrayList<>();
        pathList.add(path);
        pathList.addAll(path.getAllChildPaths());
        fragment.setPathList(pathList);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.route_list_fragment, container, false);
        recyclerView = rootView.findViewById(R.id.route_list_recyclerview);
        recyclerViewAdapter = new RouteListRecyclerViewAdapter(this, pathList);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        title = rootView.findViewById(R.id.route_list_frag_title);
        selectRouteButton = rootView.findViewById(R.id.select_route_button);
        editButton = rootView.findViewById(R.id.edit_route_button);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_route_list_frag);
        mapFragment.getMapAsync(this);
        TextView title = rootView.findViewById(R.id.route_list_frag_title);
        title.setText(path.getName());
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        routeListMap = googleMap;
        for (Path path : pathList) {
            polylines.add(path.makePolyLine(routeListMap));
        }
        routeListMap.getUiSettings().setAllGesturesEnabled(false);
        routeListMap.moveCamera(CameraUpdateFactory.newLatLngBounds(path.getBounds(), getResources().getDisplayMetrics().widthPixels, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()), 10 ));
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setPathList(ArrayList<Path> pathList) {
        this.pathList = pathList;
    }

    public void selectRoute(final int position) {
        this.position = position;
        for (Polyline polyline : polylines) {
            polyline.setVisible(false);
        }
        polylines.get(position).setVisible(true);
        selectRouteButton.setEnabled(true);
        selectRouteButton.setOnClickListener(v -> getParentFragmentManager().beginTransaction().add(R.id.main_frame, WalkFragment.newInstance(pathList.get(position), true)).addToBackStack(null).commit());
        if (pathList.get(position).isEditable()) {
            editButton.setVisibility(View.VISIBLE);
            editDialog = new EditDialog();
            editDialog.setTargetFragment(this, 0);
            editButton.setOnClickListener(v -> editDialog.show(getParentFragmentManager(), "EditPopup"));
        } else {
            editButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEdit(String title) {
        if (title.equals("")) {
            title = String.format("Route at %f, %f", path.getLatitudes().get(0), path.getLongitudes().get(0));
        }
        pathList.get(position).edit(getContext(), title, this);
    }

    @Override
    public void onDelete() {
        editDialog.editButton.setEnabled(false);
        confirmationDialog = new AlertDialog.Builder(getContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this route?" + ((pathList.get(position).getParentPath() == null) ? " This will delete the entire path!" : ""))
                .setPositiveButton("Yes", (dialog, which) ->  pathList.get(position).delete(getContext(), this))
                .setNegativeButton("No", (dialog, which) -> confirmationDialog.dismiss()).create();
        confirmationDialog.show();
    }

    @Override
    public void onEditSuccess() {
        editDialog.dismiss();
        recyclerViewAdapter.buttons.get(position).setText(pathList.get(position).getName());
        title.setText(path.getName());
        PathFragment parent = (PathFragment)getTargetFragment();
        parent.title.setText(path.getName());
    }

    @Override
    public void onEditFailure() {
        Toast.makeText(getContext(), "Failed to update path...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteSuccess() {
        if (pathList.get(position).getParentPath() == null) {
            getParentFragmentManager().popBackStack();
            getParentFragmentManager().popBackStack();
        } else {
            recyclerViewAdapter.buttons.remove(position);
            recyclerViewAdapter.notifyItemRemoved(position);
            PathFragment parent = (PathFragment) getTargetFragment();
            parent.polylines.get(pathList.get(position).id).remove();
            parent.polylines.remove(pathList.get(position).id);
            polylines.remove(position);
        }
        Toast.makeText(getContext(), "Successfully deleted route!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteFailure() {
        Toast.makeText(getContext(), "Failed to delete route...", Toast.LENGTH_SHORT).show();
        confirmationDialog.dismiss();
    }
}
