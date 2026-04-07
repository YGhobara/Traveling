package com.example.traveling.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    private List<Post> posts = new ArrayList<>();
    private final OnPostClickListener listener;

    public PostAdapter(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.textAuthorName.setText(post.getAuthorName());
        holder.textLocationName.setText(post.getLocationName());
        holder.textCaption.setText(post.getCaption());
        holder.textLikeCount.setText(String.valueOf(post.getLikeCount()));

        if (!TextUtils.isEmpty(post.getImageUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.bg_post_placeholder)
                    .error(R.drawable.bg_post_placeholder)
                    .into(holder.imagePost);
        } else {
            holder.imagePost.setImageResource(R.drawable.bg_post_placeholder);
        }

        holder.buttonLike.setOnClickListener(null);
        holder.buttonComment.setOnClickListener(null);

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

    static class PostViewHolder extends RecyclerView.ViewHolder {

        TextView textAuthorName;
        TextView textLocationName;
        TextView textCaption;
        TextView textLikeCount;
        ImageView imagePost;
        ImageButton buttonLike;
        ImageButton buttonComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            textAuthorName = itemView.findViewById(R.id.textAuthorName);
            textLocationName = itemView.findViewById(R.id.textLocationName);
            textCaption = itemView.findViewById(R.id.textCaption);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            imagePost = itemView.findViewById(R.id.imagePost);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonComment = itemView.findViewById(R.id.buttonComment);
        }
    }
}