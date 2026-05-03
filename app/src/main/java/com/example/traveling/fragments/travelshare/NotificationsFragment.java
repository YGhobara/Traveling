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

import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.adapters.NotificationAdapter;
import com.example.traveling.models.Notification;
import com.example.traveling.repositories.NotificationRepository;
import com.example.traveling.repositories.PostRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private ImageButton buttonBack;
    private MaterialButton buttonMarkAllRead;
    private TextView textNotificationsStatus;
    private RecyclerView recyclerNotifications;

    private FirebaseAuth firebaseAuth;
    private NotificationRepository notificationRepository;
    private PostRepository postRepository;
    private NotificationAdapter notificationAdapter;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        notificationRepository = new NotificationRepository();
        postRepository = new PostRepository();

        bindViews(view);
        setupRecycler();
        setupActions();
        loadNotifications();

        return view;
    }

    private void bindViews(View view) {
        buttonBack = view.findViewById(R.id.buttonBack);
        buttonMarkAllRead = view.findViewById(R.id.buttonMarkAllRead);
        textNotificationsStatus = view.findViewById(R.id.textNotificationsStatus);
        recyclerNotifications = view.findViewById(R.id.recyclerNotifications);
    }

    private void setupRecycler() {
        notificationAdapter = new NotificationAdapter(notification -> handleNotificationClick(notification));

        recyclerNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerNotifications.setAdapter(notificationAdapter);
    }

    private void setupActions() {
        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        buttonMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }

    private void loadNotifications() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            textNotificationsStatus.setText("Connectez-vous pour voir vos notifications.");
            recyclerNotifications.setVisibility(View.GONE);
            buttonMarkAllRead.setVisibility(View.GONE);
            return;
        }

        textNotificationsStatus.setText("Chargement des notifications...");

        notificationRepository.getNotificationsForUser(currentUser.getUid(), new NotificationRepository.NotificationsListener() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (!isAdded()) return;

                notificationAdapter.submitList(notifications);

                if (notifications == null || notifications.isEmpty()) {
                    textNotificationsStatus.setText("Aucune notification pour le moment.");
                    textNotificationsStatus.setVisibility(View.VISIBLE);
                    recyclerNotifications.setVisibility(View.GONE);
                } else {
                    textNotificationsStatus.setVisibility(View.GONE);
                    recyclerNotifications.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                textNotificationsStatus.setText("Impossible de charger les notifications.");
                recyclerNotifications.setVisibility(View.GONE);

                Toast.makeText(requireContext(),
                        "Erreur notifications : " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleNotificationClick(Notification notification) {
        if (notification == null) return;

        if (!notification.isRead()) {
            notificationRepository.markAsRead(notification.getId(), new NotificationRepository.ActionListener() {
                @Override
                public void onSuccess() {
                    openRelatedPost(notification);
                }

                @Override
                public void onError(Exception e) {
                    openRelatedPost(notification);
                }
            });
        } else {
            openRelatedPost(notification);
        }
    }

    private void openRelatedPost(Notification notification) {
        if (notification == null || TextUtils.isEmpty(notification.getRelatedPostId())) {
            Toast.makeText(requireContext(),
                    "Publication associée introuvable.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        postRepository.getPostById(notification.getRelatedPostId(), new PostRepository.OnPostLoadedListener() {
            @Override
            public void onSuccess(com.example.traveling.models.Post post) {
                if (!isAdded()) return;

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

                if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).openFragmentWithBackStack(fragment);
                }
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Impossible d'ouvrir la publication.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markAllAsRead() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) return;

        buttonMarkAllRead.setEnabled(false);

        notificationRepository.markAllAsRead(currentUser.getUid(), new NotificationRepository.ActionListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                buttonMarkAllRead.setEnabled(true);
                loadNotifications();

                Toast.makeText(requireContext(),
                        "Notifications marquées comme lues.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                buttonMarkAllRead.setEnabled(true);

                Toast.makeText(requireContext(),
                        "Erreur : " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}