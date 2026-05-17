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
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.example.traveling.R;
import com.example.traveling.activities.LandingActivity;
import com.example.traveling.models.UserProfile;
import com.example.traveling.models.Group;
import com.example.traveling.repositories.GroupRepository;
import com.example.traveling.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.traveling.models.Post;
import com.example.traveling.repositories.PostRepository;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.traveling.adapters.PostGridAdapter;

import com.example.traveling.activities.MainActivity;
import com.example.traveling.adapters.GroupAdapter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.net.Uri;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;
import com.example.traveling.adapters.SavedRouteAdapter;
import com.example.traveling.local.SavedRouteEntity;
import com.example.traveling.models.RouteOption;
import com.example.traveling.repositories.SavedRouteRepository;
import com.example.traveling.fragments.travelpath.RouteDetailFragment;
import com.example.traveling.utils.RouteJsonMapper;

import org.json.JSONException;

import java.util.ArrayList;
public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private GroupRepository groupRepository;
    private SavedRouteRepository savedRouteRepository;

    private TextView textAvatarInitials;
    private TextView textFullName;
    private TextView textUsername;
    private ImageView imageProfileAvatar;
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

    private View layoutProfileGroupsSection;
    private TextView textProfileGroupsStatus;
    private RecyclerView recyclerProfileGroups;
    private RecyclerView recyclerProfilePhotos;
    private PostGridAdapter profilePhotosAdapter;
    private RecyclerView recyclerProfileRoutes;
    private SavedRouteAdapter savedRouteAdapter;
    private int currentRoutesCount = 0;
    private MaterialButton buttonManageGroups;

    private List<Post> currentUserPosts;

    private GroupAdapter profileGroupsAdapter;
    private UserProfile currentUserProfile;
    private int currentPhotosCount = 0;
    private int currentGroupsCount = 0;

    private ActivityResultLauncher<String> avatarPickerLauncher;
    private Uri selectedAvatarUri;
    private ImageView currentDialogAvatarPreview;
    private String currentAvatarUrl = "";
    private static final String CLOUDINARY_UPLOAD_PRESET = "traveling_unsigned";

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
        groupRepository = new GroupRepository();
        savedRouteRepository = new SavedRouteRepository(requireContext());

        bindViews(view);
        setupStats(view);
        setupProfileGroupsRecycler();
        setupProfilePhotosRecycler();
        setupProfileRoutesRecycler();
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
        imageProfileAvatar = view.findViewById(R.id.imageProfileAvatar);
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

        layoutProfileGroupsSection = view.findViewById(R.id.layoutProfileGroupsSection);
        textProfileGroupsStatus = view.findViewById(R.id.textProfileGroupsStatus);
        recyclerProfileGroups = view.findViewById(R.id.recyclerProfileGroups);
        buttonManageGroups = view.findViewById(R.id.buttonManageGroups);
        recyclerProfilePhotos = view.findViewById(R.id.recyclerProfilePhotos);
        recyclerProfileRoutes = view.findViewById(R.id.recyclerProfileRoutes);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedAvatarUri = uri;

                        if (currentDialogAvatarPreview != null) {
                            currentDialogAvatarPreview.setImageURI(uri);
                        }
                    }
                }
        );
    }

    private void setupProfileGroupsRecycler() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : null;

        profileGroupsAdapter = new GroupAdapter(
                currentUserId,
                group -> openGroupDetail(group),
                group -> {
                    // User is already member in profile groups list
                }
        );

        recyclerProfileGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerProfileGroups.setAdapter(profileGroupsAdapter);
    }

    private void setupProfilePhotosRecycler() {
        profilePhotosAdapter = new PostGridAdapter(post -> openPostDetail(post));

        recyclerProfilePhotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerProfilePhotos.setAdapter(profilePhotosAdapter);
    }

    private void setupProfileRoutesRecycler() {
        savedRouteAdapter = new SavedRouteAdapter(savedRoute -> openSavedRouteDetail(savedRoute));

        recyclerProfileRoutes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerProfileRoutes.setAdapter(savedRouteAdapter);
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

        View.OnClickListener editListener = v -> showEditProfileDialog();

        buttonEditProfile.setOnClickListener(editListener);
        buttonEditProfileLarge.setOnClickListener(editListener);

        tabPhotos.setOnClickListener(v -> showPhotosSection());
        tabRoutes.setOnClickListener(v -> showRoutesSection());
        tabGroups.setOnClickListener(v -> showGroupsSection());

        buttonManageGroups.setOnClickListener(v -> {
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity())
                        .openFragmentWithBackStack(new GroupsFragment());
            }
        });
    }

    private void showPhotosSection() {
        setTab(tabPhotos, "Photos", currentPhotosCount, R.drawable.ic_bookmark_outline, true);
        setTab(tabRoutes, "Trajets", currentRoutesCount, R.drawable.ic_directions_outline, false);
        setTab(tabGroups, "Groupes", currentGroupsCount, R.drawable.ic_person_outline, false);

        layoutProfileGroupsSection.setVisibility(View.GONE);
        recyclerProfileRoutes.setVisibility(View.GONE);

        if (currentUserPosts == null || currentUserPosts.isEmpty()) {
            recyclerProfilePhotos.setVisibility(View.GONE);
            textProfileSectionPlaceholder.setVisibility(View.VISIBLE);
            textProfileSectionPlaceholder.setText("Les photos publiées apparaîtront ici.");
        } else {
            textProfileSectionPlaceholder.setVisibility(View.GONE);
            recyclerProfilePhotos.setVisibility(View.VISIBLE);
        }
    }

    private void showRoutesSection() {
        setTab(tabPhotos, "Photos", currentPhotosCount, R.drawable.ic_bookmark_outline, false);
        setTab(tabRoutes, "Trajets", currentRoutesCount, R.drawable.ic_directions_outline, true);
        setTab(tabGroups, "Groupes", currentGroupsCount, R.drawable.ic_person_outline, false);

        layoutProfileGroupsSection.setVisibility(View.GONE);
        recyclerProfilePhotos.setVisibility(View.GONE);

        if (currentRoutesCount == 0) {
            recyclerProfileRoutes.setVisibility(View.GONE);
            textProfileSectionPlaceholder.setVisibility(View.VISIBLE);
            textProfileSectionPlaceholder.setText("Les trajets sauvegardés apparaîtront ici.");
        } else {
            textProfileSectionPlaceholder.setVisibility(View.GONE);
            recyclerProfileRoutes.setVisibility(View.VISIBLE);
        }
    }

    private void showGroupsSection() {
        setTab(tabPhotos, "Photos", currentPhotosCount, R.drawable.ic_bookmark_outline, false);
        setTab(tabRoutes, "Trajets", currentRoutesCount, R.drawable.ic_directions_outline, false);
        setTab(tabGroups, "Groupes", currentGroupsCount, R.drawable.ic_person_outline, true);

        textProfileSectionPlaceholder.setVisibility(View.GONE);
        recyclerProfilePhotos.setVisibility(View.GONE);
        recyclerProfileRoutes.setVisibility(View.GONE);
        layoutProfileGroupsSection.setVisibility(View.VISIBLE);
    }

    private void openGroupDetail(Group group) {
        if (group == null || group.getId() == null) return;

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity())
                    .openFragmentWithBackStack(GroupDetailFragment.newInstance(group.getId()));
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

    private void openSavedRouteDetail(SavedRouteEntity savedRoute) {
        if (savedRoute == null) return;

        try {
            RouteOption routeOption = RouteJsonMapper.fromEntity(savedRoute);

            RouteDetailFragment fragment = new RouteDetailFragment();

            Bundle args = new Bundle();
            args.putSerializable("routeOption", routeOption);
            fragment.setArguments(args);

            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).openFragmentWithBackStack(fragment);
            }

        } catch (JSONException e) {
            Toast.makeText(requireContext(),
                    "Impossible d'ouvrir ce trajet.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void displayGuestProfile() {
        textAvatarInitials.setText("?");
        textFullName.setText("Mode invité");
        textUsername.setText("Connectez-vous pour personnaliser votre profil");

        btnLogout.setVisibility(View.GONE);
        buttonEditProfile.setVisibility(View.GONE);
        imageProfileAvatar.setVisibility(View.GONE);
        textAvatarInitials.setVisibility(View.VISIBLE);
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
        currentUserProfile = userProfile;
        currentAvatarUrl = safe(userProfile.getAvatarUrl());
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

        displayAvatarOrInitials(userProfile.getAvatarUrl(), firstName, lastName, username, email);

        btnLogout.setVisibility(View.VISIBLE);
        buttonEditProfile.setVisibility(View.VISIBLE);
        buttonEditProfileLarge.setText("Modifier le profil");
        textProfileSectionPlaceholder.setText("Les photos publiées apparaîtront ici.");
        setStat(statFollowers, String.valueOf(userProfile.getFollowersCount()), "Abonnés");
        setStat(statFollowing, String.valueOf(userProfile.getFollowingCount()), "Abonnements");
    }

    private void displayAvatarOrInitials(String avatarUrl,
                                         String firstName,
                                         String lastName,
                                         String username,
                                         String email) {
        if (!TextUtils.isEmpty(avatarUrl)) {
            textAvatarInitials.setVisibility(View.GONE);
            imageProfileAvatar.setVisibility(View.VISIBLE);

            Glide.with(requireContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.bg_avatar_circle)
                    .error(R.drawable.bg_avatar_circle)
                    .circleCrop()
                    .into(imageProfileAvatar);
        } else {
            imageProfileAvatar.setVisibility(View.GONE);
            textAvatarInitials.setVisibility(View.VISIBLE);
            textAvatarInitials.setText(makeInitials(firstName, lastName, username, email));
        }
    }

    private void showEditProfileDialog() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour modifier votre profil.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);

        TextInputEditText editFirstName = dialogView.findViewById(R.id.editFirstName);
        TextInputEditText editLastName = dialogView.findViewById(R.id.editLastName);
        TextInputEditText editUsername = dialogView.findViewById(R.id.editUsername);

        ImageView imageEditAvatar = dialogView.findViewById(R.id.imageEditAvatar);
        MaterialButton buttonChooseAvatar = dialogView.findViewById(R.id.buttonChooseAvatar);

        currentDialogAvatarPreview = imageEditAvatar;
        selectedAvatarUri = null;

        if (!TextUtils.isEmpty(currentAvatarUrl)) {
            Glide.with(requireContext())
                    .load(currentAvatarUrl)
                    .placeholder(R.drawable.bg_avatar_circle)
                    .error(R.drawable.bg_avatar_circle)
                    .circleCrop()
                    .into(imageEditAvatar);
        } else {
            imageEditAvatar.setImageResource(R.drawable.bg_avatar_circle);
        }

        buttonChooseAvatar.setOnClickListener(v ->
                avatarPickerLauncher.launch("image/*")
        );

        if (currentUserProfile != null) {
            editFirstName.setText(currentUserProfile.getFirstName());
            editLastName.setText(currentUserProfile.getLastName());
            editUsername.setText(currentUserProfile.getUsername());
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Modifier le profil")
                .setView(dialogView)
                .setNegativeButton("Annuler", null)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    String firstName = getDialogText(editFirstName);
                    String lastName = getDialogText(editLastName);
                    String username = getDialogText(editUsername);

                    if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && TextUtils.isEmpty(username)) {
                        Toast.makeText(requireContext(),
                                "Ajoutez au moins une information.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    uploadAvatarIfNeededAndUpdateProfile(currentUser, firstName, lastName, username);
                })
                .show();
    }

    private void uploadAvatarIfNeededAndUpdateProfile(FirebaseUser currentUser,
                                                      String firstName,
                                                      String lastName,
                                                      String username) {
        if (selectedAvatarUri == null) {
            updateProfile(currentUser, firstName, lastName, username, currentAvatarUrl);
            return;
        }

        MediaManager.get()
                .upload(selectedAvatarUri)
                .unsigned(CLOUDINARY_UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        if (!isAdded()) return;

                        Object secureUrlObject = resultData.get("secure_url");

                        if (secureUrlObject == null) {
                            Toast.makeText(requireContext(),
                                    "Erreur : URL avatar introuvable.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uploadedAvatarUrl = secureUrlObject.toString();
                        updateProfile(currentUser, firstName, lastName, username, uploadedAvatarUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (!isAdded()) return;

                        Toast.makeText(requireContext(),
                                "Erreur téléversement avatar : " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                })
                .dispatch();
    }

    private void updateProfile(FirebaseUser currentUser,
                               String firstName,
                               String lastName,
                               String username,
                               String avatarUrl) {
        userRepository.updateUserProfile(
                currentUser.getUid(),
                firstName,
                lastName,
                username,
                avatarUrl,
                new UserRepository.OnUserProfileActionListener() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;

                        Toast.makeText(requireContext(),
                                "Profil mis à jour.",
                                Toast.LENGTH_SHORT).show();

                        loadUserProfile(currentUser);
                    }

                    @Override
                    public void onError(Exception exception) {
                        if (!isAdded()) return;

                        Toast.makeText(requireContext(),
                                "Erreur modification profil : " + exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private String getDialogText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private void displayFallbackProfile(FirebaseUser currentUser) {
        String email = currentUser.getEmail() != null ? currentUser.getEmail() : "Utilisateur";

        displayAvatarOrInitials("", "", "", "", email);
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
                currentUserPosts = posts;
                profilePhotosAdapter.submitList(posts);

                int photosCount = posts.size();
                currentPhotosCount = photosCount;
                int voyagesCount = countDistinctLocations(posts);

                setStat(statTrips, String.valueOf(voyagesCount), "Voyages");

                loadSavedRoutesCount(userId, photosCount);
                showPhotosSection();
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

    private void loadSavedRoutesCount(String userId, int photosCount) {
        savedRouteRepository.getAllSavedRoutes(new SavedRouteRepository.LoadRoutesCallback() {
            @Override
            public void onSuccess(List<SavedRouteEntity> routes) {
                if (!isAdded()) return;

                currentRoutesCount = routes == null ? 0 : routes.size();
                savedRouteAdapter.submitList(routes);

                loadGroupCount(userId, photosCount);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                currentRoutesCount = 0;
                savedRouteAdapter.submitList(new ArrayList<>());

                loadGroupCount(userId, photosCount);
            }
        });
    }

    private void loadGroupCount(String userId, int photosCount) {
        groupRepository.getMyGroups(userId, new GroupRepository.GroupListListener() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (!isAdded()) return;

                int groupsCount = groups == null ? 0 : groups.size();
                currentGroupsCount = groupsCount;
                profileGroupsAdapter.submitList(groups);

                if (groupsCount == 0) {
                    textProfileGroupsStatus.setText("Vous n'avez rejoint aucun groupe pour le moment.");
                } else {
                    textProfileGroupsStatus.setText("Vos groupes de voyage");
                }
                updateTabCounts(photosCount, currentRoutesCount, groupsCount);
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                updateTabCounts(photosCount, currentRoutesCount, 0);
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