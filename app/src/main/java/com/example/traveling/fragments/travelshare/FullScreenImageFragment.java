package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.traveling.R;

public class FullScreenImageFragment extends Fragment {

    private static final String ARG_IMAGE_URL = "imageUrl";

    private ImageView imageFullscreen;
    private ImageButton buttonCloseFullscreen;

    public FullScreenImageFragment() {
        // Required empty public constructor
    }

    public static FullScreenImageFragment newInstance(String imageUrl) {
        FullScreenImageFragment fragment = new FullScreenImageFragment();

        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);

        imageFullscreen = view.findViewById(R.id.imageFullscreen);
        buttonCloseFullscreen = view.findViewById(R.id.buttonCloseFullscreen);

        String imageUrl = "";

        if (getArguments() != null) {
            imageUrl = getArguments().getString(ARG_IMAGE_URL, "");
        }

        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_post_placeholder)
                    .error(R.drawable.bg_post_placeholder)
                    .fitCenter()
                    .into(imageFullscreen);
        } else {
            imageFullscreen.setImageResource(R.drawable.bg_post_placeholder);
        }

        buttonCloseFullscreen.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }
}