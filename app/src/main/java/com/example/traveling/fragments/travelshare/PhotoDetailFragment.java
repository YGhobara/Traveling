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
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.traveling.models.Report;
import com.example.traveling.repositories.ReportRepository;

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
    private TextView textPostDate;
    private TextView textPlaceType;
    private TextView textCaption;
    private TextView textCommentsTitle;
    private TextView textLikes;
    private ImageButton buttonLike;
    private ImageButton buttonBack;
    private MaterialButton buttonReport;
    private MaterialButton buttonOpenMaps;

    private RecyclerView recyclerViewComments;
    private TextView textNoComments;
    private TextInputEditText editTextComment;
    private MaterialButton buttonSendComment;
    private CommentAdapter commentAdapter;

    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private ReportRepository reportRepository;
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
        textPostDate = view.findViewById(R.id.textDetailPostDate);
        textPlaceType = view.findViewById(R.id.textDetailPlaceType);
        textCaption = view.findViewById(R.id.textDetailCaption);
        textLikes = view.findViewById(R.id.textDetailLikeCount);
        textCommentsTitle = view.findViewById(R.id.textCommentsTitle);
        buttonLike = view.findViewById(R.id.buttonDetailLike);
        buttonBack = view.findViewById(R.id.buttonBack);
        buttonReport = view.findViewById(R.id.buttonReport);
        buttonOpenMaps = view.findViewById(R.id.buttonOpenMaps);

        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        textNoComments = view.findViewById(R.id.textNoComments);
        editTextComment = view.findViewById(R.id.editTextComment);
        buttonSendComment = view.findViewById(R.id.buttonSendComment);

        postRepository = new PostRepository();
        commentRepository = new CommentRepository();
        userRepository = new UserRepository();
        reportRepository = new ReportRepository();
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
        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
        buttonReport.setOnClickListener(v -> handleReportClick());
        buttonOpenMaps.setOnClickListener(v -> openLocationInMaps());

        loadFreshPost();
        loadComments();

        return view;
    }

    private void displayInitialData(Bundle args) {
        String authorName = args.getString("authorName", "");
        String locationName = args.getString("locationName", "");
        String placeType = args.getString("placeType", "");
        String caption = args.getString("caption", "");
        String imageUrl = args.getString("imageUrl", "");
        int likeCount = args.getInt("likeCount", 0);
        long createdAt = args.getLong("createdAt", 0);

        textAuthor.setText(authorName);
        textLocation.setText(locationName);
        textPostDate.setText(formatRelativeTime(createdAt));
        displayPlaceType(placeType);
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
        textPostDate.setText(formatRelativeTime(post.getCreatedAt()));
        displayPlaceType(post.getPlaceType());
        textCaption.setText(post.getCaption());
        textLikes.setText(post.getLikeCount() + " J'aime");
        textCommentsTitle.setText("Commentaires (" + post.getCommentCount() + ")");

        loadImage(post.getImageUrl());
        updateLikeIcon(post);
    }

    private void displayPlaceType(String placeType) {
        if (!TextUtils.isEmpty(placeType)) {
            textPlaceType.setText(placeType);
            textPlaceType.setVisibility(View.VISIBLE);
        } else {
            textPlaceType.setVisibility(View.GONE);
        }
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

    private void handleReportClick() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour signaler une publication.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (postId == null || postId.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Publication introuvable.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        reportRepository.hasUserReported(postId, currentUser.getUid(), new ReportRepository.OnReportCheckListener() {
            @Override
            public void onResult(boolean alreadyReported) {
                if (!isAdded()) return;

                if (alreadyReported) {
                    Toast.makeText(requireContext(),
                            "Vous avez déjà signalé cette publication.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showReportReasonDialog(currentUser.getUid());
                }
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Erreur vérification signalement : " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showReportReasonDialog(String userId) {
        String[] reasons = {
                "Contenu inapproprié",
                "Spam",
                "Fausse information",
                "Contenu offensant",
                "Autre"
        };

        final String[] selectedReason = {reasons[0]};

        new AlertDialog.Builder(requireContext())
                .setTitle("Signaler la publication")
                .setSingleChoiceItems(reasons, 0, (dialog, which) -> {
                    selectedReason[0] = reasons[which];
                })
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Envoyer", (dialog, which) -> submitReport(userId, selectedReason[0]))
                .show();
    }

    private void submitReport(String userId, String reason) {
        Report report = new Report(
                null,
                postId,
                userId,
                reason,
                System.currentTimeMillis()
        );

        buttonReport.setEnabled(false);

        reportRepository.createReport(report, new ReportRepository.OnReportActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                buttonReport.setEnabled(true);

                Toast.makeText(requireContext(),
                        "Signalement envoyé.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                buttonReport.setEnabled(true);

                Toast.makeText(requireContext(),
                        "Erreur signalement : " + exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openLocationInMaps() {
        String locationName = textLocation.getText() != null
                ? textLocation.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(locationName)) {
            Toast.makeText(requireContext(),
                    "Lieu non disponible.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(locationName));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(locationName)));
            startActivity(browserIntent);
        }
    }

    private String formatRelativeTime(long timestamp) {
        if (timestamp <= 0) {
            return "Date inconnue";
        }

        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;
        long month = 30 * day;
        long year = 365 * day;

        if (diff < minute) {
            return "Publié à l’instant";
        } else if (diff < hour) {
            long minutes = diff / minute;
            return "Publié il y a " + minutes + " min";
        } else if (diff < day) {
            long hours = diff / hour;
            return "Publié il y a " + hours + " h";
        } else if (diff < month) {
            long days = diff / day;
            return "Publié il y a " + days + " j";
        } else if (diff < year) {
            long months = diff / month;
            return "Publié il y a " + months + " mois";
        } else {
            long years = diff / year;
            long remainingMonths = (diff % year) / month;

            if (remainingMonths == 0) {
                return "Publié il y a " + years + " an" + (years > 1 ? "s" : "");
            }

            return "Publié il y a " + years + " an" + (years > 1 ? "s" : "")
                    + " et " + remainingMonths + " mois";
        }
    }
}