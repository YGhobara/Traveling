package com.example.traveling.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostGridAdapter extends RecyclerView.Adapter<PostGridAdapter.PostGridViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    private final List<Post> posts = new ArrayList<>();
    private final OnPostClickListener listener;

    public PostGridAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Post> newPosts) {
        posts.clear();

        if (newPosts != null) {
            posts.addAll(newPosts);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_grid, parent, false);
        return new PostGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostGridViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.textGridLocation.setText(
                !TextUtils.isEmpty(post.getLocationName())
                        ? post.getLocationName()
                        : "Lieu inconnu"
        );

        holder.textGridAuthor.setText(
                !TextUtils.isEmpty(post.getAuthorName())
                        ? "par " + post.getAuthorName()
                        : "par Voyageur"
        );

        if (!TextUtils.isEmpty(post.getPlaceType())) {
            holder.textGridPlaceType.setVisibility(View.VISIBLE);
            holder.textGridPlaceType.setText(post.getPlaceType());
        } else {
            holder.textGridPlaceType.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(post.getImageUrl())
                .placeholder(R.drawable.bg_post_placeholder)
                .error(R.drawable.bg_post_placeholder)
                .centerCrop()
                .into(holder.imageGridPost);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostGridViewHolder extends RecyclerView.ViewHolder {

        ImageView imageGridPost;
        TextView textGridLocation;
        TextView textGridAuthor;
        TextView textGridPlaceType;

        public PostGridViewHolder(@NonNull View itemView) {
            super(itemView);

            imageGridPost = itemView.findViewById(R.id.imageGridPost);
            textGridLocation = itemView.findViewById(R.id.textGridLocation);
            textGridAuthor = itemView.findViewById(R.id.textGridAuthor);
            textGridPlaceType = itemView.findViewById(R.id.textGridPlaceType);
        }
    }
}