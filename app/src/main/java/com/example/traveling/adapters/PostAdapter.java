package com.example.traveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts = new ArrayList<>();

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
        holder.textLikeCount.setText(post.getLikeCount() + " likes");
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

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            textAuthorName = itemView.findViewById(R.id.textAuthorName);
            textLocationName = itemView.findViewById(R.id.textLocationName);
            textCaption = itemView.findViewById(R.id.textCaption);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
        }
    }
}