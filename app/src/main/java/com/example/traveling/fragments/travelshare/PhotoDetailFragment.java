package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.traveling.R;

public class PhotoDetailFragment extends Fragment {

    public PhotoDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_detail, container, false);

        ImageView imagePost = view.findViewById(R.id.imageDetailPost);
        TextView textAuthor = view.findViewById(R.id.textDetailAuthorName);
        TextView textLocation = view.findViewById(R.id.textDetailLocationName);
        TextView textCaption = view.findViewById(R.id.textDetailCaption);
        TextView textLikes = view.findViewById(R.id.textDetailLikeCount);

        Bundle args = getArguments();
        if (args != null) {
            String authorName = args.getString("authorName", "");
            String locationName = args.getString("locationName", "");
            String caption = args.getString("caption", "");
            String imageUrl = args.getString("imageUrl", "");
            int likeCount = args.getInt("likeCount", 0);

            textAuthor.setText(authorName);
            textLocation.setText(locationName);
            textCaption.setText(caption);
            textLikes.setText(likeCount + " likes");

            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_post_placeholder)
                        .error(R.drawable.bg_post_placeholder)
                        .into(imagePost);
            } else {
                imagePost.setImageResource(R.drawable.bg_post_placeholder);
            }
        }

        return view;
    }
}