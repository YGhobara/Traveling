package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.activities.MainActivity;
import com.example.traveling.adapters.PostAdapter;
import com.example.traveling.models.Post;
import com.example.traveling.repositories.PostRepository;

import java.util.List;
import com.example.traveling.R;
import com.example.traveling.models.Group;
import com.example.traveling.repositories.GroupRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GroupDetailFragment extends Fragment {

    private static final String ARG_GROUP_ID = "groupId";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private GroupRepository groupRepository;
    private RecyclerView recyclerGroupPosts;
    private PostAdapter postAdapter;
    private PostRepository postRepository;

    private ImageButton buttonBack;
    private TextView textHeaderTitle;
    private TextView textGroupName;
    private TextView textGroupDescription;
    private TextView textGroupMeta;
    private TextView textGroupFeedStatus;
    private MaterialButton buttonJoinLeave;

    private String groupId;
    private Group currentGroup;

    public GroupDetailFragment() {
        // Required empty public constructor
    }

    public static GroupDetailFragment newInstance(String groupId) {
        GroupDetailFragment fragment = new GroupDetailFragment();

        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_detail, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        groupRepository = new GroupRepository();
        postRepository = new PostRepository();


        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }

        bindViews(view);
        setupActions();
        setupPostRecycler();

        if (TextUtils.isEmpty(groupId)) {
            textGroupFeedStatus.setText("Groupe introuvable.");
            buttonJoinLeave.setVisibility(View.GONE);
        } else {
            loadGroup();
        }

        return view;
    }

    private void bindViews(View view) {
        buttonBack = view.findViewById(R.id.buttonBack);
        textHeaderTitle = view.findViewById(R.id.textHeaderTitle);
        textGroupName = view.findViewById(R.id.textGroupName);
        textGroupDescription = view.findViewById(R.id.textGroupDescription);
        textGroupMeta = view.findViewById(R.id.textGroupMeta);
        textGroupFeedStatus = view.findViewById(R.id.textGroupFeedStatus);
        buttonJoinLeave = view.findViewById(R.id.buttonJoinLeave);
        recyclerGroupPosts = view.findViewById(R.id.recyclerGroupPosts);
    }

    private void setupPostRecycler() {
        postAdapter = new PostAdapter(
                post -> openPostDetail(post),
                post -> toggleLike(post)
        );

        if (currentUser != null) {
            postAdapter.setCurrentUserId(currentUser.getUid());
        }

        recyclerGroupPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerGroupPosts.setAdapter(postAdapter);
    }

    private void loadGroupPosts() {
        if (TextUtils.isEmpty(groupId)) return;

        if (postAdapter.getItemCount() == 0) {
            textGroupFeedStatus.setVisibility(View.VISIBLE);
            recyclerGroupPosts.setVisibility(View.GONE);
            textGroupFeedStatus.setText("Chargement des publications...");
        }

        postRepository.getPostsByGroup(groupId, new PostRepository.OnPostsLoadedListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (!isAdded()) return;

                postAdapter.setPosts(posts);

                if (posts.isEmpty()) {
                    recyclerGroupPosts.setVisibility(View.GONE);
                    textGroupFeedStatus.setVisibility(View.VISIBLE);
                    textGroupFeedStatus.setText("Aucune publication dans ce groupe pour le moment.");
                } else {
                    textGroupFeedStatus.setVisibility(View.GONE);
                    recyclerGroupPosts.setVisibility(View.VISIBLE);
                }
            }

            private void toggleLike(Post post) {
                if (currentUser == null) {
                    Toast.makeText(requireContext(),
                            "Connectez-vous pour aimer une photo.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                postRepository.toggleLike(post, currentUser.getUid(), new PostRepository.OnPostActionListener() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;
                        loadGroupPosts();
                    }

                    @Override
                    public void onError(Exception exception) {
                        if (!isAdded()) return;

                        Toast.makeText(requireContext(),
                                "Erreur like: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                recyclerGroupPosts.setVisibility(View.GONE);
                textGroupFeedStatus.setVisibility(View.VISIBLE);
                textGroupFeedStatus.setText("Impossible de charger les publications du groupe.");

                Toast.makeText(requireContext(),
                        "Erreur publications: " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleLike(Post post) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour aimer une publication.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        postRepository.toggleLike(post, currentUser.getUid(), new PostRepository.OnPostActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                loadGroupPosts();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur lors du like : " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupActions() {
        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        buttonJoinLeave.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(requireContext(),
                        "Connectez-vous pour rejoindre un groupe.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentGroup == null || currentGroup.getId() == null) {
                return;
            }

            if (currentGroup.isMember(currentUser.getUid())) {
                leaveGroup();
            } else {
                joinGroup();
            }
        });
    }

    private void loadGroup() {
        textGroupFeedStatus.setText("Chargement du groupe...");

        groupRepository.getGroupById(groupId, new GroupRepository.GroupListener() {
            @Override
            public void onSuccess(Group group) {
                if (!isAdded()) return;

                currentGroup = group;
                displayGroup(group);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                textGroupFeedStatus.setText("Impossible de charger le groupe.");
                Toast.makeText(requireContext(),
                        "Erreur: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openPostDetail(Post post) {
        PhotoDetailFragment fragment = new PhotoDetailFragment();

        Bundle args = new Bundle();
        args.putString("postId", post.getId());
        args.putString("authorName", post.getAuthorName());
        args.putString("locationName", post.getLocationName());
        args.putString("placeType", post.getPlaceType());
        args.putString("caption", post.getCaption());
        args.putInt("commentCount", post.getCommentCount());
        args.putDouble("latitude", post.getLatitude());
        args.putDouble("longitude", post.getLongitude());
        args.putString("photonPlaceId", post.getPhotonPlaceId());
        args.putString("imageUrl", post.getImageUrl());
        args.putInt("likeCount", post.getLikeCount());
        args.putLong("createdAt", post.getCreatedAt());
        fragment.setArguments(args);

        ((MainActivity) requireActivity()).openFragmentWithBackStack(fragment);
    }

    private void displayGroup(Group group) {
        textHeaderTitle.setText(group.getName());
        textGroupName.setText(group.getName());
        textGroupDescription.setText(group.getDisplayDescription());

        String visibility = group.isPublicGroup() ? "Public" : "Privé";
        textGroupMeta.setText(group.getMemberCountText() + " · " + visibility);

        textGroupFeedStatus.setText("Les publications du groupe apparaîtront ici.");

        if (currentUser == null) {
            buttonJoinLeave.setText("Connexion requise");
            buttonJoinLeave.setEnabled(false);
            return;
        }

        boolean isMember = group.isMember(currentUser.getUid());

        if (isMember) {
            buttonJoinLeave.setText("Quitter le groupe");
            buttonJoinLeave.setEnabled(true);
        } else {
            buttonJoinLeave.setText("Rejoindre le groupe");
            buttonJoinLeave.setEnabled(true);
        }
        loadGroupPosts();
    }

    private void joinGroup() {
        groupRepository.joinGroup(currentGroup.getId(), currentUser.getUid(), new GroupRepository.ActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Groupe rejoint.",
                        Toast.LENGTH_SHORT).show();

                loadGroup();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void leaveGroup() {
        groupRepository.leaveGroup(currentGroup.getId(), currentUser.getUid(), new GroupRepository.ActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Vous avez quitté le groupe.",
                        Toast.LENGTH_SHORT).show();

                loadGroup();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}