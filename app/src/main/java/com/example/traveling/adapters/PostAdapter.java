package com.example.traveling.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
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

    public interface OnLikeClickListener {
        void onLikeClick(Post post);
    }

    public interface OnAuthorClickListener {
        void onAuthorClick(Post post);
    }

    private List<Post> posts = new ArrayList<>();
    private final OnPostClickListener postClickListener;
    private final OnLikeClickListener likeClickListener;
    private final OnAuthorClickListener authorClickListener;
    private String currentUserId;

    public PostAdapter(OnPostClickListener postClickListener,
                       OnLikeClickListener likeClickListener) {
        this(postClickListener, likeClickListener, null);
    }

    public PostAdapter(OnPostClickListener postClickListener,
                       OnLikeClickListener likeClickListener,
                       OnAuthorClickListener authorClickListener) {
        this.postClickListener = postClickListener;
        this.likeClickListener = likeClickListener;
        this.authorClickListener = authorClickListener;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged();
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
        boolean likedByCurrentUser = currentUserId != null && post.isLikedByUser(currentUserId);


        holder.textAuthorName.setText(post.getAuthorName());
        holder.textAuthorName.setOnClickListener(v -> {
            if (authorClickListener != null) {
                authorClickListener.onAuthorClick(post);
            }
        });
        holder.textAuthorName.setTextColor(Color.parseColor("#1565C0"));
        holder.textLocationName.setText(
                !TextUtils.isEmpty(post.getLocationName())
                        ? post.getLocationName()
                        : "Lieu inconnu"
        );

        holder.textPostDate.setText(formatRelativeTime(post.getCreatedAt()));
        holder.textCaption.setText(post.getCaption());
        holder.textLikeCount.setText(String.valueOf(post.getLikeCount()));
        holder.textCommentCount.setText(String.valueOf(post.getCommentCount()));

        if (!TextUtils.isEmpty(post.getPlaceType())) {
            holder.textPlaceType.setText(post.getPlaceType());
            holder.textPlaceType.setVisibility(View.VISIBLE);
        } else {
            holder.textPlaceType.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(post.getImageUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.bg_post_placeholder)
                    .error(R.drawable.bg_post_placeholder)
                    .into(holder.imagePost);
        } else {
            holder.imagePost.setImageResource(R.drawable.bg_post_placeholder);
        }

        GestureDetector gestureDetector = new GestureDetector(
                holder.itemView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (postClickListener != null) {
                            postClickListener.onPostClick(post);
                        }
                        return true;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (likeClickListener != null) {
                            likeClickListener.onLikeClick(post);
                        }
                        return true;
                    }
                }
        );

        holder.imagePost.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        holder.buttonLike.setColorFilter(
                likedByCurrentUser ? Color.parseColor("#E53935") : Color.parseColor("#6B7280")
        );

        holder.buttonLike.setOnClickListener(v -> {
            if (likeClickListener != null) {
                likeClickListener.onLikeClick(post);
            }
        });

        holder.buttonComment.setOnClickListener(null);

        holder.itemView.setOnClickListener(v -> {
            if (postClickListener != null) {
                postClickListener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textLocationName;
        TextView textPostDate;
        TextView textAuthorName;
        TextView textPlaceType;
        TextView textCaption;
        TextView textLikeCount;
        TextView textCommentCount;
        ImageView imagePost;
        ImageButton buttonLike;
        ImageButton buttonComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            textAuthorName = itemView.findViewById(R.id.textAuthorName);
            textPlaceType = itemView.findViewById(R.id.textPlaceType);
            textCaption = itemView.findViewById(R.id.textCaption);
            textLikeCount = itemView.findViewById(R.id.textLikeCount);
            textCommentCount = itemView.findViewById(R.id.textCommentCount);
            imagePost = itemView.findViewById(R.id.imagePost);
            buttonLike = itemView.findViewById(R.id.buttonLike);
            buttonComment = itemView.findViewById(R.id.buttonComment);
            textLocationName = itemView.findViewById(R.id.textLocationName);
            textPostDate = itemView.findViewById(R.id.textPostDate);
        }
    }

    private String formatRelativeTime(long timestamp) {
        if (timestamp <= 0) {
            return "date inconnue";
        }

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;
        long month = 30 * day;
        long year = 365 * day;

        if (diff < minute) {
            return "à l’instant";
        } else if (diff < hour) {
            return "il y a " + (diff / minute) + " min";
        } else if (diff < day) {
            return "il y a " + (diff / hour) + " h";
        } else if (diff < month) {
            return "il y a " + (diff / day) + " j";
        } else if (diff < year) {
            return "il y a " + (diff / month) + " mois";
        } else {
            return "il y a " + (diff / year) + " an(s)";
        }
    }
}