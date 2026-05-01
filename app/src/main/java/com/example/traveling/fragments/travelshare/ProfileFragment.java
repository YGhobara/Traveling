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
import com.example.traveling.activities.LandingActivity;
import com.example.traveling.models.UserProfile;
import com.example.traveling.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    private TextView textAvatarInitials;
    private TextView textFullName;
    private TextView textUsername;
    private TextView textEmail;

    private TextView textProfileSectionPlaceholder;

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
        textEmail = view.findViewById(R.id.textEmail);
        textProfileSectionPlaceholder = view.findViewById(R.id.textProfileSectionPlaceholder);

        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonEditProfileLarge = view.findViewById(R.id.buttonEditProfileLarge);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupStats(View view) {
        setStat(view.findViewById(R.id.statTrips), "0", "Voyages");
        setStat(view.findViewById(R.id.statPhotos), "0", "Photos");
        setStat(view.findViewById(R.id.statRoutes), "0", "Trajets");
        setStat(view.findViewById(R.id.statGroups), "0", "Groupes");
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
    }

    private void displayGuestProfile() {
        textAvatarInitials.setText("?");
        textFullName.setText("Mode invité");
        textUsername.setText("Connectez-vous pour personnaliser votre profil");
        textEmail.setText("");

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
        textEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

        userRepository.getUserProfile(currentUser.getUid(), new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (!isAdded()) return;
                displayUserProfile(userProfile, currentUser);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;
                displayFallbackProfile(currentUser);

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
        textEmail.setText(email);

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
        textEmail.setText(email);

        btnLogout.setVisibility(View.VISIBLE);
        buttonEditProfile.setVisibility(View.VISIBLE);
        buttonEditProfileLarge.setText("Modifier le profil");
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