package com.wikiwalks.wikiwalks.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.PathMap;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.recyclerviewadapters.BookmarkedPathRecyclerViewAdapter;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class BookmarksFragment extends Fragment {

    RecyclerView recyclerView;
    BookmarkedPathRecyclerViewAdapter adapter;
    Path[] paths;

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
        final View rootView = inflater.inflate(R.layout.bookmarks_list_fragment, container, false);
        Toolbar toolbar = rootView.findViewById(R.id.bookmarks_list_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener((View v) -> getParentFragmentManager().popBackStack());
        toolbar.setTitle("Bookmarks");
        recyclerView = rootView.findViewById(R.id.bookmarks_list_recyclerview);
        SharedPreferences preferences = getContext().getSharedPreferences("preferences", MODE_PRIVATE);
        String bookmarks = preferences.getString("bookmarks", "");
        Log.e("test", "test");
        if (bookmarks.equals("")) {
            TextView noBookmarks = rootView.findViewById(R.id.no_bookmarks_indicator);
            noBookmarks.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            String[] bookmarksArray = bookmarks.split(",");
            paths = new Path[bookmarksArray.length];
            adapter = new BookmarkedPathRecyclerViewAdapter(this, paths);
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
                            paths[integer] = new Path();
                            initialiseRecyclerView();
                        }
                    });
                }
            }
        }
        return rootView;
    }

    public void initialiseRecyclerView() {
        boolean notNull = true;
        for (Path path : paths) {
            if (path == null) {
                notNull = false;
                break;
            }
        }
        if (notNull) {
            adapter.notifyDataSetChanged();
        }
    }
}
