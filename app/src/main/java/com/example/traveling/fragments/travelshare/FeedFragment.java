package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.adapters.PostAdapter;
import com.example.traveling.models.Post;
import com.example.traveling.repositories.PostRepository;

import java.util.List;

public class FeedFragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private TextView textEmptyFeed;
    private PostAdapter postAdapter;
    private PostRepository postRepository;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts);
        textEmptyFeed = view.findViewById(R.id.textEmptyFeed);

        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        postAdapter = new PostAdapter();
        recyclerViewPosts.setAdapter(postAdapter);

        postRepository = new PostRepository();

        loadPosts();

        return view;
    }

    private void loadPosts() {
        postRepository.getPublicPosts(new PostRepository.OnPostsLoadedListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (!isAdded()) {
                    return;
                }

                if (posts == null || posts.isEmpty()) {
                    recyclerViewPosts.setVisibility(View.GONE);
                    textEmptyFeed.setVisibility(View.VISIBLE);
                } else {
                    textEmptyFeed.setVisibility(View.GONE);
                    recyclerViewPosts.setVisibility(View.VISIBLE);
                    postAdapter.setPosts(posts);
                }
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }

                recyclerViewPosts.setVisibility(View.GONE);
                textEmptyFeed.setVisibility(View.VISIBLE);
                textEmptyFeed.setText("Failed to load posts.");

                Toast.makeText(requireContext(),
                        "Error: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}