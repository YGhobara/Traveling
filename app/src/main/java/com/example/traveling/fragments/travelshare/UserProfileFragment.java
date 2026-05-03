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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.adapters.PostGridAdapter;
import com.example.traveling.models.Post;
import com.example.traveling.models.UserProfile;
import com.example.traveling.repositories.PostRepository;
import com.example.traveling.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "userId";

    private UserRepository userRepository;
    private PostRepository postRepository;

    private String targetUserId;

    private ImageButton buttonBack;
    private TextView textAvatarInitials;
    private TextView textFullName;
    private TextView textUsername;
    private TextView textProfileSectionPlaceholder;

    private View statTrips;
    private View statFollowers;
    private View statFollowing;

    private TextView tabPhotos;
    private TextView tabRoutes;
    private TextView tabGroups;

    private MaterialButton buttonFollowUser;

    private RecyclerView recyclerProfilePhotos;
    private PostGridAdapter profilePhotosAdapter;

    private int currentPhotosCount = 0;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance(String userId) {
        UserProfileFragment fragment = new UserProfileFragment();

        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        userRepository = new UserRepository();
        postRepository = new PostRepository();

        if (getArguments() != null) {
            targetUserId = getArguments().getString(ARG_USER_ID);
        }

        bindViews(view);
        setupStats();
        setupPhotosRecycler();
        setupActions();

        if (TextUtils.isEmpty(targetUserId)) {
            textFullName.setText("Profil introuvable");
            textUsername.setText("");
            buttonFollowUser.setEnabled(false);
        } else {
            loadUserProfile();
            loadUserPosts();
        }

        return view;
    }

    private void bindViews(View view) {
        buttonBack = view.findViewById(R.id.buttonBack);

        textAvatarInitials = view.findViewById(R.id.textAvatarInitials);
        textFullName = view.findViewById(R.id.textFullName);
        textUsername = view.findViewById(R.id.textUsername);
        textProfileSectionPlaceholder = view.findViewById(R.id.textProfileSectionPlaceholder);

        statTrips = view.findViewById(R.id.statTrips);
        statFollowers = view.findViewById(R.id.statFollowers);
        statFollowing = view.findViewById(R.id.statFollowing);

        tabPhotos = view.findViewById(R.id.tabPhotos);
        tabRoutes = view.findViewById(R.id.tabRoutes);
        tabGroups = view.findViewById(R.id.tabGroups);

        buttonFollowUser = view.findViewById(R.id.buttonFollowUser);
        recyclerProfilePhotos = view.findViewById(R.id.recyclerProfilePhotos);

        View layoutProfileGroupsSection = view.findViewById(R.id.layoutProfileGroupsSection);
        if (layoutProfileGroupsSection != null) {
            layoutProfileGroupsSection.setVisibility(View.GONE);
        }
    }

    private void setupStats() {
        setStat(statTrips, "0", "Voyages");
        setStat(statFollowers, "0", "Abonnés");
        setStat(statFollowing, "0", "Abonnements");

        setTab(tabPhotos, "Photos", 0, R.drawable.ic_bookmark_outline, true);
        setTab(tabRoutes, "Trajets", 0, R.drawable.ic_directions_outline, false);
        setTab(tabGroups, "Groupes", 0, R.drawable.ic_person_outline, false);
    }

    private void setupPhotosRecycler() {
        profilePhotosAdapter = new PostGridAdapter(post -> openPostDetail(post));

        recyclerProfilePhotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerProfilePhotos.setAdapter(profilePhotosAdapter);
    }

    private void setupActions() {
        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        buttonFollowUser.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Suivi d'utilisateur à venir.",
                        Toast.LENGTH_SHORT).show()
        );

        tabPhotos.setOnClickListener(v -> showPhotosSection());

        tabRoutes.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Trajets publics à venir.",
                        Toast.LENGTH_SHORT).show()
        );

        tabGroups.setOnClickListener(v ->
                Toast.makeText(requireContext(),
                        "Groupes publics à venir.",
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserProfile() {
        textFullName.setText("Chargement...");
        textUsername.setText("");

        userRepository.getUserProfile(targetUserId, new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (!isAdded()) return;
                displayUserProfile(userProfile);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                textFullName.setText("Utilisateur");
                textUsername.setText("@profil");
                textAvatarInitials.setText("?");

                Toast.makeText(requireContext(),
                        "Impossible de charger ce profil.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserProfile(UserProfile userProfile) {
        String firstName = safe(userProfile.getFirstName());
        String lastName = safe(userProfile.getLastName());
        String username = safe(userProfile.getUsername());
        String email = safe(userProfile.getEmail());

        String fullName = (firstName + " " + lastName).trim();

        if (TextUtils.isEmpty(fullName)) {
            fullName = userProfile.getDisplayName();
        }

        textFullName.setText(!TextUtils.isEmpty(fullName) ? fullName : "Voyageur");
        textUsername.setText(!TextUtils.isEmpty(username) ? "@" + username : "@voyageur");
        textAvatarInitials.setText(makeInitials(firstName, lastName, username, email));
    }

    private void loadUserPosts() {
        postRepository.getPostsByUser(targetUserId, new PostRepository.OnPostsLoadedListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (!isAdded()) return;

                currentPhotosCount = posts == null ? 0 : posts.size();
                int voyagesCount = countDistinctLocations(posts);

                profilePhotosAdapter.submitList(posts);

                setStat(statTrips, String.valueOf(voyagesCount), "Voyages");
                setStat(statFollowers, "0", "Abonnés");
                setStat(statFollowing, "0", "Abonnements");

                setTab(tabPhotos, "Photos", currentPhotosCount, R.drawable.ic_bookmark_outline, true);
                setTab(tabRoutes, "Trajets", 0, R.drawable.ic_directions_outline, false);
                setTab(tabGroups, "Groupes", 0, R.drawable.ic_person_outline, false);

                showPhotosSection();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                textProfileSectionPlaceholder.setText("Impossible de charger les photos.");
                textProfileSectionPlaceholder.setVisibility(View.VISIBLE);
                recyclerProfilePhotos.setVisibility(View.GONE);
            }
        });
    }

    private void showPhotosSection() {
        setTab(tabPhotos, "Photos", currentPhotosCount, R.drawable.ic_bookmark_outline, true);
        setTab(tabRoutes, "Trajets", 0, R.drawable.ic_directions_outline, false);
        setTab(tabGroups, "Groupes", 0, R.drawable.ic_person_outline, false);

        if (currentPhotosCount == 0) {
            recyclerProfilePhotos.setVisibility(View.GONE);
            textProfileSectionPlaceholder.setVisibility(View.VISIBLE);
            textProfileSectionPlaceholder.setText("Aucune photo publiée pour le moment.");
        } else {
            textProfileSectionPlaceholder.setVisibility(View.GONE);
            recyclerProfilePhotos.setVisibility(View.VISIBLE);
        }
    }

    private void openPostDetail(Post post) {
        if (post == null || post.getId() == null) return;

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

    private void setTab(TextView tab, String label, int count, int iconRes, boolean selected) {
        tab.setText(label + " (" + count + ")");
        tab.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
        tab.setCompoundDrawablePadding(6);

        tab.setTextColor(selected
                ? getResources().getColor(R.color.travel_primary, null)
                : android.graphics.Color.parseColor("#6B7280"));

        tab.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void setStat(View statView, String value, String label) {
        TextView textValue = statView.findViewById(R.id.textStatValue);
        TextView textLabel = statView.findViewById(R.id.textStatLabel);

        textValue.setText(value);
        textLabel.setText(label);
    }

    private int countDistinctLocations(List<Post> posts) {
        Set<String> locations = new HashSet<>();

        if (posts == null) return 0;

        for (Post post : posts) {
            String location = safe(post.getLocationName()).toLowerCase();

            if (!TextUtils.isEmpty(location)) {
                locations.add(location);
            }
        }

        return locations.size();
    }

    private String makeInitials(String firstName, String lastName, String username, String email) {
        String first = safe(firstName);
        String last = safe(lastName);

        if (!TextUtils.isEmpty(first) && !TextUtils.isEmpty(last)) {
            return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
        }

        if (!TextUtils.isEmpty(first)) {
            return first.substring(0, 1).toUpperCase();
        }

        String user = safe(username);
        if (!TextUtils.isEmpty(user)) {
            return user.substring(0, 1).toUpperCase();
        }

        String mail = safe(email);
        if (!TextUtils.isEmpty(mail)) {
            return mail.substring(0, 1).toUpperCase();
        }

        return "?";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}