package com.example.traveling.fragments.travelshare;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.traveling.R;
import com.example.traveling.adapters.CommentAdapter;
import com.example.traveling.models.Comment;
import com.example.traveling.models.Post;
import com.example.traveling.models.UserProfile;
import com.example.traveling.repositories.UserRepository;
import com.example.traveling.repositories.CommentRepository;
import com.example.traveling.repositories.PostRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class PhotoDetailFragment extends Fragment {

    private ImageView imagePost;
    private TextView textAuthor;
    private TextView textLocation;
    private TextView textCaption;
    private TextView textLikes;
    private ImageButton buttonLike;

    private RecyclerView recyclerViewComments;
    private TextView textNoComments;
    private TextInputEditText editTextComment;
    private MaterialButton buttonSendComment;
    private CommentAdapter commentAdapter;

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private FirebaseAuth firebaseAuth;

    private String postId;
    private Post currentPost;

    public PhotoDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_detail, container, false);

        imagePost = view.findViewById(R.id.imageDetailPost);
        textAuthor = view.findViewById(R.id.textDetailAuthorName);
        textLocation = view.findViewById(R.id.textDetailLocationName);
        textCaption = view.findViewById(R.id.textDetailCaption);
        textLikes = view.findViewById(R.id.textDetailLikeCount);
        buttonLike = view.findViewById(R.id.buttonDetailLike);

        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        textNoComments = view.findViewById(R.id.textNoComments);
        editTextComment = view.findViewById(R.id.editTextComment);
        buttonSendComment = view.findViewById(R.id.buttonSendComment);

        postRepository = new PostRepository();
        commentRepository = new CommentRepository();
        userRepository = new UserRepository();
        firebaseAuth = FirebaseAuth.getInstance();

        commentAdapter = new CommentAdapter();
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewComments.setAdapter(commentAdapter);

        Bundle args = getArguments();
        if (args != null) {
            postId = args.getString("postId", null);
            displayInitialData(args);
        }

        buttonLike.setOnClickListener(v -> toggleLike());
        buttonSendComment.setOnClickListener(v -> sendComment());

        loadFreshPost();
        loadComments();

        return view;
    }

    private void displayInitialData(Bundle args) {
        String authorName = args.getString("authorName", "");
        String locationName = args.getString("locationName", "");
        String caption = args.getString("caption", "");
        String imageUrl = args.getString("imageUrl", "");
        int likeCount = args.getInt("likeCount", 0);

        textAuthor.setText(authorName);
        textLocation.setText(locationName);
        textCaption.setText(caption);
        textLikes.setText(likeCount + " J'aime");

        loadImage(imageUrl);
    }

    private void loadFreshPost() {
        if (postId == null) return;

        postRepository.getPostById(postId, new PostRepository.OnPostLoadedListener() {
            @Override
            public void onSuccess(Post post) {
                if (!isAdded()) return;

                currentPost = post;
                displayPost(post);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur chargement post : " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayPost(Post post) {
        textAuthor.setText(post.getAuthorName());
        textLocation.setText(post.getLocationName());
        textCaption.setText(post.getCaption());
        textLikes.setText(post.getLikeCount() + " J'aime");

        loadImage(post.getImageUrl());
        updateLikeIcon(post);
    }

    private void loadImage(String imageUrl) {
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

    private void updateLikeIcon(Post post) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        boolean likedByCurrentUser = currentUserId != null && post.isLikedByUser(currentUserId);

        buttonLike.setColorFilter(
                likedByCurrentUser ? Color.parseColor("#E53935") : Color.parseColor("#6B7280")
        );
    }

    private void toggleLike() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour aimer une publication.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPost == null) {
            Toast.makeText(requireContext(),
                    "Publication non chargée.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        postRepository.toggleLike(currentPost, currentUser.getUid(), new PostRepository.OnPostActionListener() {
            @Override
            public void onSuccess() {
                loadFreshPost();
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

    private void loadComments() {
        if (postId == null) return;

        commentRepository.getCommentsForPost(postId, new CommentRepository.OnCommentsLoadedListener() {
            @Override
            public void onSuccess(List<Comment> comments) {
                if (!isAdded()) return;

                if (comments == null || comments.isEmpty()) {
                    recyclerViewComments.setVisibility(View.GONE);
                    textNoComments.setVisibility(View.VISIBLE);
                } else {
                    textNoComments.setVisibility(View.GONE);
                    recyclerViewComments.setVisibility(View.VISIBLE);
                    commentAdapter.setComments(comments);
                }
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur chargement commentaires : " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendComment() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour commenter.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (postId == null) {
            Toast.makeText(requireContext(),
                    "Publication introuvable.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String text = editTextComment.getText() != null
                ? editTextComment.getText().toString().trim()
                : "";

        if (text.isEmpty()) {
            editTextComment.setError("Commentaire vide");
            return;
        }

        buttonSendComment.setEnabled(false);

        userRepository.getUserProfile(currentUser.getUid(), new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (!isAdded()) return;

                String authorName = userProfile.getDisplayName();

                Comment comment = new Comment(
                        null,
                        postId,
                        currentUser.getUid(),
                        authorName,
                        text,
                        System.currentTimeMillis()
                );

                addCommentToFirestore(comment);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                // Fallback for older accounts that do not yet have a Firestore profile
                String fallbackAuthorName = currentUser.getEmail() != null
                        ? currentUser.getEmail()
                        : "Utilisateur";

                Comment comment = new Comment(
                        null,
                        postId,
                        currentUser.getUid(),
                        fallbackAuthorName,
                        text,
                        System.currentTimeMillis()
                );

                addCommentToFirestore(comment);
            }
        });
    }

    private void addCommentToFirestore(Comment comment) {
        commentRepository.addComment(postId, comment, new CommentRepository.OnCommentActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                editTextComment.setText("");
                buttonSendComment.setEnabled(true);
                loadComments();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                buttonSendComment.setEnabled(true);

                Toast.makeText(requireContext(),
                        "Erreur ajout commentaire : " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}