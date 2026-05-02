package com.example.traveling.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.models.Post;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class PostMapInfoWindow extends InfoWindow {

    public interface OnPopupClickListener {
        void onPopupClick(Post post);
    }

    private final Post post;
    private final OnPopupClickListener listener;

    public PostMapInfoWindow(MapView mapView, Post post, OnPopupClickListener listener) {
        super(R.layout.item_map_post_popup, mapView);
        this.post = post;
        this.listener = listener;
    }

    @Override
    public void onOpen(Object item) {
        View view = mView;

        ImageView imagePopupPost = view.findViewById(R.id.imagePopupPost);
        TextView textPopupLocation = view.findViewById(R.id.textPopupLocation);
        TextView textPopupAuthor = view.findViewById(R.id.textPopupAuthor);

        textPopupLocation.setText(safe(post.getLocationName()));
        textPopupAuthor.setText("par " + safe(post.getAuthorName()));

        Glide.with(view.getContext())
                .load(post.getImageUrl())
                .placeholder(R.drawable.bg_post_placeholder)
                .error(R.drawable.bg_post_placeholder)
                .centerCrop()
                .into(imagePopupPost);

        view.setOnClickListener(v -> {
            close();

            if (listener != null) {
                listener.onPopupClick(post);
            }
        });
    }

    @Override
    public void onClose() {
        // Nothing needed
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty()
                ? "Voyageur"
                : value.trim();
    }
}