package com.example.traveling.fragments.travelshare;

import android.content.Intent;
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

import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.activities.LandingActivity;
import com.example.traveling.models.UserProfile;
import com.example.traveling.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.traveling.models.Post;
import com.example.traveling.repositories.PostRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private PostRepository postRepository;

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

    private MaterialButton buttonEditProfileLarge;
    private MaterialButton btnLogout;
    private ImageButton buttonEditProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
        postRepository = new PostRepository();

        bindViews(view);
        setupStats(view);
        setupActions();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            displayGuestProfile();
        } else {
            loadUserProfile(currentUser);
        }

        return view;
    }

    private void bindViews(View view) {
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

        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonEditProfileLarge = view.findViewById(R.id.buttonEditProfileLarge);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupStats(View view) {
        setStat(statTrips, "0", "Voyages");
        setStat(statFollowers, "0", "Abonnés");
        setStat(statFollowing, "0", "Abonnements");

        setupTabs();
    }

    private void setupTabs() {
        setTab(tabPhotos, "Photos", 0, R.drawable.ic_bookmark_outline, true);
        setTab(tabRoutes, "Trajets", 0, R.drawable.ic_directions_outline, false);
        setTab(tabGroups, "Groupes", 0, R.drawable.ic_person_outline, false);
    }

    private void updateTabCounts(int photosCount, int routesCount, int groupsCount) {
        setTab(tabPhotos, "Photos", photosCount, R.drawable.ic_bookmark_outline, true);
        setTab(tabRoutes, "Trajets", routesCount, R.drawable.ic_directions_outline, false);
        setTab(tabGroups, "Groupes", groupsCount, R.drawable.ic_person_outline, false);
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

    private void setupActions() {
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Intent intent = new Intent(requireActivity(), LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        View.OnClickListener editListener = v ->
                Toast.makeText(requireContext(),
                        "Modification du profil à venir.",
                        Toast.LENGTH_SHORT).show();

        buttonEditProfile.setOnClickListener(editListener);
        buttonEditProfileLarge.setOnClickListener(editListener);

        tabGroups.setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity())
                        .openFragmentWithBackStack(new GroupsFragment());
            }
        });
    }

    private void displayGuestProfile() {
        textAvatarInitials.setText("?");
        textFullName.setText("Mode invité");
        textUsername.setText("Connectez-vous pour personnaliser votre profil");

        btnLogout.setVisibility(View.GONE);
        buttonEditProfile.setVisibility(View.GONE);
        buttonEditProfileLarge.setText("Se connecter");
        textProfileSectionPlaceholder.setText("Connectez-vous pour voir vos photos, trajets et groupes.");

        buttonEditProfileLarge.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), LandingActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserProfile(FirebaseUser currentUser) {
        textFullName.setText("Chargement...");
        textUsername.setText("");

        userRepository.getUserProfile(currentUser.getUid(), new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (!isAdded()) return;
                displayUserProfile(userProfile, currentUser);
                loadProfileStats(currentUser.getUid());
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;
                displayFallbackProfile(currentUser);
                loadProfileStats(currentUser.getUid());

                Toast.makeText(requireContext(),
                        "Profil utilisateur incomplet.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserProfile(UserProfile userProfile, FirebaseUser currentUser) {
        String firstName = safe(userProfile.getFirstName());
        String lastName = safe(userProfile.getLastName());
        String username = safe(userProfile.getUsername());
        String email = safe(userProfile.getEmail());

        String fullName = (firstName + " " + lastName).trim();

        if (TextUtils.isEmpty(fullName)) {
            fullName = userProfile.getDisplayName();
        }

        if (TextUtils.isEmpty(email) && currentUser.getEmail() != null) {
            email = currentUser.getEmail();
        }

        textFullName.setText(fullName);
        textUsername.setText(!TextUtils.isEmpty(username) ? "@" + username : "@voyageur");

        textAvatarInitials.setText(makeInitials(firstName, lastName, username, email));

        btnLogout.setVisibility(View.VISIBLE);
        buttonEditProfile.setVisibility(View.VISIBLE);
        buttonEditProfileLarge.setText("Modifier le profil");
        textProfileSectionPlaceholder.setText("Les photos publiées apparaîtront ici.");
    }

    private void displayFallbackProfile(FirebaseUser currentUser) {
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "Utilisateur";

        textAvatarInitials.setText(makeInitials("", "", "", email));
        textFullName.setText("Utilisateur");
        textUsername.setText("@profil");

        btnLogout.setVisibility(View.VISIBLE);
        buttonEditProfile.setVisibility(View.VISIBLE);
        buttonEditProfileLarge.setText("Modifier le profil");
    }

    private void loadProfileStats(String userId) {
        postRepository.getPostsByUser(userId, new PostRepository.OnPostsLoadedListener() {
            @Override
            public void onSuccess(List<Post> posts) {
                if (!isAdded()) return;

                int photosCount = posts.size();
                int voyagesCount = countDistinctLocations(posts);

                setStat(statTrips, String.valueOf(voyagesCount), "Voyages");

                // Temporary placeholders until follow system exists
                setStat(statFollowers, "0", "Abonnés");
                setStat(statFollowing, "0", "Abonnements");

                updateTabCounts(photosCount, 0, 0);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Impossible de charger les statistiques.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int countDistinctLocations(List<Post> posts) {
        Set<String> locations = new HashSet<>();

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