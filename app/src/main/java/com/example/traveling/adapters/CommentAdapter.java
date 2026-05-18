package com.example.traveling.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.models.Comment;

import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.traveling.models.UserProfile;
import com.example.traveling.repositories.UserRepository;

import java.util.HashMap;
import java.util.Map;
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnCommentAuthorClickListener {
        void onCommentAuthorClick(Comment comment);
    }

    private OnCommentAuthorClickListener authorClickListener;
    private List<Comment> comments = new ArrayList<>();
    private final UserRepository userRepository = new UserRepository();
    private final Map<String, String> avatarCache = new HashMap<>();

    public CommentAdapter() {
    }

    public CommentAdapter(OnCommentAuthorClickListener authorClickListener) {
        this.authorClickListener = authorClickListener;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.textCommentDate.setText("· " + formatRelativeTime(comment.getCreatedAt()));
        holder.textCommentAuthor.setText(comment.getAuthorName());
        holder.textCommentAuthor.setTextColor(Color.parseColor("#1565C0"));

        holder.textCommentAuthor.setOnClickListener(v -> {
            if (authorClickListener != null) {
                authorClickListener.onCommentAuthorClick(comment);
            }
        });
        holder.textCommentBody.setText(comment.getText());
        displayCommentAvatar(holder, comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView textCommentDate;
        TextView textCommentAuthor;
        TextView textCommentBody;
        TextView textCommentAvatarInitials;
        ImageView imageCommentAvatar;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textCommentDate = itemView.findViewById(R.id.textCommentDate);
            textCommentAuthor = itemView.findViewById(R.id.textCommentAuthor);
            textCommentBody = itemView.findViewById(R.id.textCommentBody);
            textCommentAvatarInitials = itemView.findViewById(R.id.textCommentAvatarInitials);
            imageCommentAvatar = itemView.findViewById(R.id.imageCommentAvatar);
        }
    }

    private void displayCommentAvatar(CommentViewHolder holder, Comment comment) {
        String userId = comment.getUserId();

        holder.textCommentAvatarInitials.setText(makeInitial(comment.getAuthorName()));
        holder.textCommentAvatarInitials.setVisibility(View.VISIBLE);
        holder.imageCommentAvatar.setVisibility(View.GONE);

        if (TextUtils.isEmpty(userId)) {
            return;
        }

        if (avatarCache.containsKey(userId)) {
            String cachedAvatarUrl = avatarCache.get(userId);
            loadAvatarIntoHolder(holder, cachedAvatarUrl);
            return;
        }

        userRepository.getUserProfile(userId, new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                String avatarUrl = userProfile.getAvatarUrl();
                avatarCache.put(userId, avatarUrl != null ? avatarUrl : "");
                loadAvatarIntoHolder(holder, avatarUrl);
            }

            @Override
            public void onError(Exception exception) {
                avatarCache.put(userId, "");
            }
        });
    }

    private void loadAvatarIntoHolder(CommentViewHolder holder, String avatarUrl) {
        if (TextUtils.isEmpty(avatarUrl)) {
            holder.imageCommentAvatar.setVisibility(View.GONE);
            holder.textCommentAvatarInitials.setVisibility(View.VISIBLE);
            return;
        }

        holder.textCommentAvatarInitials.setVisibility(View.GONE);
        holder.imageCommentAvatar.setVisibility(View.VISIBLE);

        Glide.with(holder.itemView.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.bg_avatar_circle)
                .error(R.drawable.bg_avatar_circle)
                .circleCrop()
                .into(holder.imageCommentAvatar);
    }

    private String makeInitial(String name) {
        if (TextUtils.isEmpty(name)) {
            return "?";
        }

        return name.trim().substring(0, 1).toUpperCase();
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
            long minutes = diff / minute;
            return "il y a " + minutes + " min";
        } else if (diff < day) {
            long hours = diff / hour;
            return "il y a " + hours + " h";
        } else if (diff < month) {
            long days = diff / day;
            return "il y a " + days + " j";
        } else if (diff < year) {
            long months = diff / month;
            return "il y a " + months + " mois";
        } else {
            long years = diff / year;
            long remainingMonths = (diff % year) / month;

            if (remainingMonths == 0) {
                return "il y a " + years + " an" + (years > 1 ? "s" : "");
            }

            return "il y a " + years + " an" + (years > 1 ? "s" : "")
                    + " et " + remainingMonths + " mois";
        }
    }
}