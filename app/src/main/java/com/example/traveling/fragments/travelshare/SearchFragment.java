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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.adapters.PostGridAdapter;
import com.example.traveling.models.Post;
import com.example.traveling.repositories.PostRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerSearchPosts;
    private TextView textSearchStatus;
    private TextView tabPublications;
    private TextView tabMap;

    private PostRepository postRepository;
    private PostGridAdapter postGridAdapter;

    private final List<Post> allPosts = new ArrayList<>();

    public SearchFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        postRepository = new PostRepository();

        bindViews(view);
        setupRecycler();
        setupTabs();
        loadPublicPosts();

        return view;
    }

    private void bindViews(View view) {
        recyclerSearchPosts = view.findViewById(R.id.recyclerSearchPosts);
        textSearchStatus = view.findViewById(R.id.textSearchStatus);
        tabPublications = view.findViewById(R.id.tabPublications);
        tabMap = view.findViewById(R.id.tabMap);
    }

    private void setupRecycler() {
        postGridAdapter = new PostGridAdapter(this::openPostDetail);

        recyclerSearchPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerSearchPosts.setAdapter(postGridAdapter);
    }

    private void setupTabs() {
        tabPublications.setOnClickListener(v -> {
            selectPublicationsTab();
        });

        tabMap.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Vue carte à venir.",
                    Toast.LENGTH_SHORT).show();
        });

        selectPublicationsTab();
    }

    private void selectPublicationsTab() {
        tabPublications.setTextColor(getResources().getColor(R.color.travel_primary, null));
        tabPublications.setTypeface(null, android.graphics.Typeface.BOLD);

        tabMap.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);

        recyclerSearchPosts.setVisibility(View.VISIBLE);
    }

    private void loadPublicPosts() {
        textSearchStatus.setText("Chargement des publications...");

        postRepository.getPublicPosts(new PostRepository.OnPostsLoadedListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (!isAdded()) return;

                allPosts.clear();
                allPosts.addAll(posts);

                postGridAdapter.submitList(allPosts);

                if (allPosts.isEmpty()) {
                    textSearchStatus.setText("Aucune publication publique pour le moment.");
                } else {
                    textSearchStatus.setText(allPosts.size() + " publication(s) publique(s)");
                }
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                textSearchStatus.setText("Impossible de charger les publications.");

                Toast.makeText(requireContext(),
                        "Erreur de chargement.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPostDetail(Post post) {
        PhotoDetailFragment fragment = new PhotoDetailFragment();

        Bundle args = new Bundle();
        args.putString("postId", post.getId());
        args.putString("imageUrl", post.getImageUrl());
        args.putString("locationName", post.getLocationName());
        args.putString("authorName", post.getAuthorName());
        args.putString("caption", post.getCaption());
        args.putInt("likeCount", post.getLikeCount());
        args.putInt("commentCount", post.getCommentCount());
        args.putString("placeType", post.getPlaceType());
        args.putLong("createdAt", post.getCreatedAt());

        fragment.setArguments(args);

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).openFragmentWithBackStack(fragment);
        }
    }
}