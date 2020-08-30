package com.wikiwalks.wikiwalks.ui.recyclerviewadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.wikiwalks.wikiwalks.Path;
import com.wikiwalks.wikiwalks.R;
import com.wikiwalks.wikiwalks.ui.PathFragment;

public class BookmarkedPathRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkedPathRecyclerViewAdapter.ViewHolder> {
    private Path[] paths;
    private Fragment parentFragment;

    public BookmarkedPathRecyclerViewAdapter(Fragment parentFragment, Path[] paths) {
        this.paths = paths;
        this.parentFragment = parentFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parentFragment.getContext());
        View view = inflater.inflate(R.layout.bookmarks_list_row, parent, false);
        return new BookmarkedPathRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (paths[position] == null || paths[position].getId() == -1) {
            holder.button.setVisibility(View.GONE);
            holder.separator.setVisibility(View.GONE);
        } else {
            holder.button.setVisibility(View.VISIBLE);
            holder.separator.setVisibility(View.VISIBLE);
            if (position == paths.length - 1) {
                holder.separator.setVisibility(View.GONE);
            }
            holder.button.setText(paths[position].getName());
            holder.button.setOnClickListener(v -> parentFragment.getParentFragmentManager().beginTransaction().add(R.id.main_frame, PathFragment.newInstance(paths[position].getId())).addToBackStack(null).commit());
        }
    }

    @Override
    public int getItemCount() {
        return paths.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button button;
        private View separator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.bookmark_button);
            separator = itemView.findViewById(R.id.bookmark_separator);
        }
    }
}
