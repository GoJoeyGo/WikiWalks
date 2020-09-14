package com.wikiwalks.wikiwalks.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.PreferencesManager;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.BookmarkedPathRecyclerViewAdapter;

import java.util.ArrayList;

public class BookmarksFragment extends Fragment {

    private RecyclerView recyclerView;
    private Path[] paths;
    private int pathsRequested = 0;

    public static BookmarksFragment newInstance() {
        Bundle args = new Bundle();
        BookmarksFragment fragment = new BookmarksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.bookmark_list_fragment, container, false);

        MaterialToolbar toolbar = rootView.findViewById(R.id.bookmark_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
        toolbar.setTitle(R.string.bookmarks);

        recyclerView = rootView.findViewById(R.id.bookmark_list_recyclerview);
        String bookmarks = PreferencesManager.getInstance(getContext()).getBookmarks();
        if (bookmarks.isEmpty()) {
            TextView noBookmarks = rootView.findViewById(R.id.bookmark_list_empty_indicator);
            noBookmarks.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            String[] bookmarksArray = bookmarks.split(",");
            paths = new Path[bookmarksArray.length];
            BookmarkedPathRecyclerViewAdapter adapter = new BookmarkedPathRecyclerViewAdapter(this, paths);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            ArrayList<Integer> requestPaths = new ArrayList<>();
            for (int i = 0; i < bookmarksArray.length; i++) {
                int id = Integer.parseInt(bookmarksArray[i]);
                if (PathMap.getInstance().getPathList().containsKey(id)) {
                    paths[i] = PathMap.getInstance().getPathList().get(id);
                } else {
                    requestPaths.add(i);
                }
            }
            if (requestPaths.size() == 0) {
                initialiseRecyclerView();
            } else {
                for (Integer integer : requestPaths) {
                    Path.getPath(getContext(), Integer.parseInt(bookmarksArray[integer]), new Path.GetPathCallback() {
                        @Override
                        public void onGetPathSuccess(Path path) {
                            paths[integer] = path;
                            initialiseRecyclerView();
                        }

                        @Override
                        public void onGetPathFailure() {
                            initialiseRecyclerView();
                        }
                    });
                }
            }
        }

        return rootView;
    }

    public void initialiseRecyclerView() {
        pathsRequested++;
        if (pathsRequested == paths.length) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }
}
