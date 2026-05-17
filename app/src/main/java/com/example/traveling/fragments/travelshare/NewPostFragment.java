package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.models.Post;
import com.example.traveling.models.UserProfile;
import com.example.traveling.models.LocationSuggestion;
import com.example.traveling.repositories.PhotonRepository;
import com.example.traveling.repositories.PostRepository;
import com.example.traveling.repositories.UserRepository;
import com.example.traveling.repositories.NotificationRepository;
import com.example.traveling.repositories.FollowRepository;
import com.example.traveling.models.Group;
import com.example.traveling.repositories.GroupRepository;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.card.MaterialCardView;
import android.widget.LinearLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.net.Uri;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;


import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.example.traveling.repositories.TravelShareAiRepository;

public class NewPostFragment extends Fragment {

    private TextInputEditText editCaption;
    private MaterialAutoCompleteTextView dropdownLocation;
    private TextInputEditText editImageUrl;
    private SwitchMaterial switchPublic;
    private MaterialButton buttonPublish;

    private PostRepository postRepository;
    private UserRepository userRepository;
    private PhotonRepository photonRepository;
    private NotificationRepository notificationRepository;
    private FollowRepository followRepository;
    private TravelShareAiRepository travelShareAiRepository;
    private MaterialButton buttonSuggestWithAi;
    private ArrayAdapter<LocationSuggestion> locationAdapter;
    private LocationSuggestion selectedLocationSuggestion;
    private String latestLocationQuery = "";

    private final Handler locationSearchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingLocationSearch;
    private boolean isSettingLocationFromSuggestion = false;
    private FirebaseAuth auth;
    private MaterialCardView cardImagePicker;
    private LinearLayout layoutImagePlaceholder;
    private ImageView imagePreview;
    private MaterialButton buttonChooseImage;
    private Uri selectedImageUri;
    private List<String> suggestedTags = new ArrayList<>();
    private TextInputEditText editTags;
    private String uploadedAudioUrl = null;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private static final String CLOUDINARY_UPLOAD_PRESET = "traveling_unsigned";
    private MaterialAutoCompleteTextView dropdownPlaceType;

    private SwitchMaterial switchShareToGroup;
    private TextInputLayout layoutGroupDropdown;
    private MaterialAutoCompleteTextView dropdownGroup;

    private GroupRepository groupRepository;
    private ArrayAdapter<Group> groupAdapter;
    private Group selectedGroup;
    private List<Group> myGroups = new ArrayList<>();

    public NewPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imagePreview.setImageURI(uri);

                        if (layoutImagePlaceholder != null) {
                            layoutImagePlaceholder.setVisibility(View.GONE);
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editCaption = view.findViewById(R.id.editCaption);
        dropdownLocation = view.findViewById(R.id.dropdownLocation);
        editImageUrl = view.findViewById(R.id.editImageUrl);
        switchPublic = view.findViewById(R.id.switchPublic);
        buttonPublish = view.findViewById(R.id.buttonPublish);
        buttonSuggestWithAi = view.findViewById(R.id.buttonSuggestWithAi);
        imagePreview = view.findViewById(R.id.imagePreview);
        cardImagePicker = view.findViewById(R.id.cardImagePicker);
        layoutImagePlaceholder = view.findViewById(R.id.layoutImagePlaceholder);
        buttonChooseImage = view.findViewById(R.id.buttonChooseImage);
        dropdownPlaceType = view.findViewById(R.id.dropdownPlaceType);
        switchShareToGroup = view.findViewById(R.id.switchShareToGroup);
        layoutGroupDropdown = view.findViewById(R.id.layoutGroupDropdown);
        dropdownGroup = view.findViewById(R.id.dropdownGroup);
        editTags = view.findViewById(R.id.editTags);

        postRepository = new PostRepository();
        userRepository = new UserRepository();
        photonRepository = new PhotonRepository();
        groupRepository = new GroupRepository();
        notificationRepository = new NotificationRepository();
        followRepository = new FollowRepository();
        travelShareAiRepository = new TravelShareAiRepository(requireContext());

        setupPlaceTypeDropdown();
        setupLocationAutocomplete();
        setupGroupSharing();
        auth = FirebaseAuth.getInstance();

        buttonPublish.setOnClickListener(v -> publishPost());
        View.OnClickListener chooseImageListener = v ->
                imagePickerLauncher.launch("image/*");

        buttonChooseImage.setOnClickListener(chooseImageListener);
        cardImagePicker.setOnClickListener(chooseImageListener);
        buttonSuggestWithAi.setOnClickListener(v -> suggestWithAi());
    }

    private void setupPlaceTypeDropdown() {
        String[] placeTypes = {
                "Nature",
                "Musée",
                "Monument",
                "Rue",
                "Restaurant",
                "Magasin",
                "Plage",
                "Montagne",
                "Ville",
                "Autre"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                placeTypes
        );

        dropdownPlaceType.setAdapter(adapter);
        dropdownPlaceType.setText(placeTypes[0], false);

        dropdownPlaceType.setOnClickListener(v -> dropdownPlaceType.showDropDown());
    }

    private void setupLocationAutocomplete() {
        locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        dropdownLocation.setAdapter(locationAdapter);
        dropdownLocation.setThreshold(3);

        dropdownLocation.setOnItemClickListener((parent, view, position, id) -> {
            LocationSuggestion suggestion = locationAdapter.getItem(position);

            if (suggestion != null) {
                selectedLocationSuggestion = suggestion;

                isSettingLocationFromSuggestion = true;
                dropdownLocation.setText(suggestion.getDisplayName(), false);
                dropdownLocation.dismissDropDown();
                isSettingLocationFromSuggestion = false;

                dropdownLocation.setError(null);
            }
        });

        dropdownLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSettingLocationFromSuggestion) {
                    return;
                }

                selectedLocationSuggestion = null;

                String query = s == null ? "" : s.toString().trim();
                latestLocationQuery = query;

                if (pendingLocationSearch != null) {
                    locationSearchHandler.removeCallbacks(pendingLocationSearch);
                }

                if (query.length() < 3) {
                    locationAdapter.clear();
                    locationAdapter.notifyDataSetChanged();
                    return;
                }

                pendingLocationSearch = () -> searchLocationSuggestions(query);
                locationSearchHandler.postDelayed(pendingLocationSearch, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void searchLocationSuggestions(String query) {
        photonRepository.searchLocations(query, new PhotonRepository.OnLocationSuggestionsLoadedListener() {

            @Override
            public void onSuccess(List<LocationSuggestion> suggestions) {
                if (!isAdded()) return;

                if (!query.equals(latestLocationQuery)) {
                    return;
                }

                locationAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        suggestions
                );

                dropdownLocation.setAdapter(locationAdapter);

                if (!suggestions.isEmpty()
                        && dropdownLocation.hasFocus()
                        && dropdownLocation.getText() != null
                        && dropdownLocation.getText().toString().trim().length() >= 3) {

                    dropdownLocation.postDelayed(() -> {
                        dropdownLocation.dismissDropDown();
                        dropdownLocation.showDropDown();
                    }, 100);
                }
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                // Keep quiet to avoid annoying the user while typing.
                locationAdapter.clear();
                locationAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setupGroupSharing() {
        groupAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                myGroups
        );

        dropdownGroup.setAdapter(groupAdapter);

        switchShareToGroup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutGroupDropdown.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            if (!isChecked) {
                selectedGroup = null;
                dropdownGroup.setText("", false);
            } else {
                loadMyGroupsForPosting();
            }
        });

        dropdownGroup.setOnClickListener(v -> dropdownGroup.showDropDown());

        dropdownGroup.setOnItemClickListener((parent, view, position, id) -> {
            Group group = groupAdapter.getItem(position);

            if (group != null) {
                selectedGroup = group;
                dropdownGroup.setText(group.getName(), false);
                dropdownGroup.setError(null);
            }
        });
    }

    private void loadMyGroupsForPosting() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour publier dans un groupe.",
                    Toast.LENGTH_SHORT).show();

            switchShareToGroup.setChecked(false);
            return;
        }

        groupRepository.getMyGroups(currentUser.getUid(), new GroupRepository.GroupListListener() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (!isAdded()) return;

                myGroups.clear();

                if (groups != null) {
                    myGroups.addAll(groups);
                }

                groupAdapter.notifyDataSetChanged();

                if (myGroups.isEmpty()) {
                    Toast.makeText(requireContext(),
                            "Vous n'avez rejoint aucun groupe.",
                            Toast.LENGTH_SHORT).show();

                    switchShareToGroup.setChecked(false);
                }
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;

                Toast.makeText(requireContext(),
                        "Impossible de charger vos groupes.",
                        Toast.LENGTH_SHORT).show();

                switchShareToGroup.setChecked(false);
            }
        });
    }

    private void publishPost() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour publier une photo.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String caption = getText(editCaption);
        String location = dropdownLocation.getText() == null
                ? ""
                : dropdownLocation.getText().toString().trim();

        String placeType = dropdownPlaceType.getText() != null
                ? dropdownPlaceType.getText().toString().trim()
                : "";
        String imageUrl = getText(editImageUrl);
        boolean publicPost = switchPublic.isChecked();

        if (TextUtils.isEmpty(caption)) {
            editCaption.setError("Ajoutez une légende.");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            dropdownLocation.setError("Ajoutez un lieu.");
            return;
        }

        if (selectedLocationSuggestion == null) {
            dropdownLocation.setError("Sélectionnez un lieu dans les suggestions.");
            return;
        }

        if (selectedImageUri == null && TextUtils.isEmpty(imageUrl)) {
            editImageUrl.setError("Choisissez une image ou ajoutez une URL d'image.");
            return;
        }

        final String selectedGroupId;
        final String selectedGroupName;

        if (switchShareToGroup.isChecked()) {
            if (selectedGroup == null) {
                dropdownGroup.setError("Choisissez un groupe.");
                return;
            }

            selectedGroupId = selectedGroup.getId();
            selectedGroupName = selectedGroup.getName();
        } else {
            selectedGroupId = null;
            selectedGroupName = null;
        }
        suggestedTags.clear();
        suggestedTags.addAll(parseTagsFromInput());
        buttonPublish.setEnabled(false);

        userRepository.getUserProfile(currentUser.getUid(), new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                String authorName = userProfile.getDisplayName();

                if (TextUtils.isEmpty(authorName)) {
                    authorName = currentUser.getEmail();
                }

                uploadImageIfNeededAndCreatePost(
                        currentUser.getUid(),
                        authorName,
                        caption,
                        location,
                        selectedLocationSuggestion.getLatitude(),
                        selectedLocationSuggestion.getLongitude(),
                        selectedLocationSuggestion.getPhotonPlaceId(),
                        placeType,
                        imageUrl,
                        publicPost,
                        selectedGroupId,
                        selectedGroupName
                );
            }

            @Override
            public void onError(Exception exception) {
                String fallbackName = currentUser.getEmail();
                uploadImageIfNeededAndCreatePost(
                        currentUser.getUid(),
                        fallbackName,
                        caption,
                        location,
                        selectedLocationSuggestion.getLatitude(),
                        selectedLocationSuggestion.getLongitude(),
                        selectedLocationSuggestion.getPhotonPlaceId(),
                        placeType,
                        imageUrl,
                        publicPost,
                        selectedGroupId,
                        selectedGroupName
                );
            }
        });
    }

    private void suggestWithAi() {
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(),
                    "Choisissez d'abord une image.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        buttonSuggestWithAi.setEnabled(false);
        buttonSuggestWithAi.setText("Analyse en cours...");

        travelShareAiRepository.suggestPostMetadata(
                selectedImageUri,
                new TravelShareAiRepository.SuggestPostMetadataCallback() {
                    @Override
                    public void onSuccess(TravelShareAiRepository.PostMetadataSuggestion suggestion) {
                        if (!isAdded()) return;

                        buttonSuggestWithAi.setEnabled(true);
                        buttonSuggestWithAi.setText("Suggérer avec IA");

                        if (suggestion.getTags() != null) {
                            suggestedTags.clear();
                            suggestedTags.addAll(suggestion.getTags());

                            if (suggestion.getTags() != null && !suggestion.getTags().isEmpty()) {
                                editTags.setText(TextUtils.join(", ", suggestion.getTags()));
                            }
                        }

                        if (!TextUtils.isEmpty(suggestion.getPlaceType())) {
                            dropdownPlaceType.setText(suggestion.getPlaceType(), false);
                        }

                        if (TextUtils.isEmpty(getText(editCaption))
                                && !TextUtils.isEmpty(suggestion.getSuggestedCaption())) {
                            editCaption.setText(suggestion.getSuggestedCaption());
                        }

                        Toast.makeText(requireContext(),
                                "Suggestions IA ajoutées.",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Exception exception) {
                        if (!isAdded()) return;

                        buttonSuggestWithAi.setEnabled(true);
                        buttonSuggestWithAi.setText("Suggérer avec IA");

                        Toast.makeText(requireContext(),
                                "Erreur IA : " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void uploadImageIfNeededAndCreatePost(String userId,
                                                  String authorName,
                                                  String caption,
                                                  String location,
                                                  double latitude,
                                                  double longitude,
                                                  String photonPlaceId,
                                                  String placeType,
                                                  String fallbackImageUrl,
                                                  boolean publicPost,
                                                  String groupId,
                                                  String groupName) {
        if (selectedImageUri == null) {
            createPost(userId, authorName, caption, location, latitude, longitude, photonPlaceId, placeType, fallbackImageUrl, publicPost, groupId, groupName);
            return;
        }

        buttonPublish.setText("Téléversement...");

        MediaManager.get()
                .upload(selectedImageUri)
                .unsigned(CLOUDINARY_UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Upload started
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Optional progress handling later
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        if (!isAdded()) return;

                        Object secureUrlObject = resultData.get("secure_url");

                        if (secureUrlObject == null) {
                            buttonPublish.setEnabled(true);
                            buttonPublish.setText("Publier");

                            Toast.makeText(requireContext(),
                                    "Erreur : URL Cloudinary introuvable.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uploadedImageUrl = secureUrlObject.toString();

                        createPost(userId, authorName, caption, location, latitude, longitude, photonPlaceId, placeType, uploadedImageUrl, publicPost, groupId, groupName);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        if (!isAdded()) return;

                        buttonPublish.setEnabled(true);
                        buttonPublish.setText("Publier");

                        Toast.makeText(requireContext(),
                                "Erreur téléversement : " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Upload rescheduled
                    }
                })
                .dispatch();
    }

    private void createPost(String userId,
                            String authorName,
                            String caption,
                            String location,
                            double latitude,
                            double longitude,
                            String photonPlaceId,
                            String placeType,
                            String imageUrl,
                            boolean publicPost,
                            String groupId,
                            String groupName) {
        buttonPublish.setText("Publication...");
        Post post = new Post(
                null,
                userId,
                authorName,
                caption,
                imageUrl,
                location,
                latitude,
                longitude,
                photonPlaceId,
                placeType,
                System.currentTimeMillis(),
                0,
                0,
                publicPost,
                groupId,
                groupName
        );

        post.setLikedBy(new ArrayList<>());
        post.setTags(new ArrayList<>(suggestedTags));
        post.setAudioUrl(uploadedAudioUrl);

        postRepository.createPostAndReturnId(post, new PostRepository.OnPostCreatedListener() {
            @Override
            public void onSuccess(String createdPostId) {
                if (!isAdded()) return;

                createNotificationsForPost(post, createdPostId);

                Toast.makeText(requireContext(),
                        "Publication ajoutée.",
                        Toast.LENGTH_SHORT).show();

                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        requireActivity().findViewById(R.id.bottom_navigation);

                bottomNav.setSelectedItemId(R.id.nav_feed);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) return;

                buttonPublish.setEnabled(true);
                buttonPublish.setText("Publier");

                Toast.makeText(requireContext(),
                        "Erreur : " + exception.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationsForPost(Post post, String createdPostId) {
        if (post == null || createdPostId == null) return;

        Set<String> recipientIds = new HashSet<>();
        String authorId = post.getUserId();

        // 1) Notify followers of the author, only if the post is public
        if (post.isPublicPost()) {
            followRepository.getFollowerIds(authorId, new FollowRepository.UserIdsListener() {
                @Override
                public void onSuccess(List<String> userIds) {
                    if (userIds != null) {
                        recipientIds.addAll(userIds);
                    }

                    loadPlaceTypeNotificationRecipients(post, createdPostId, recipientIds);
                }

                @Override
                public void onError(Exception e) {
                    loadPlaceTypeNotificationRecipients(post, createdPostId, recipientIds);
                }
            });
        } else {
            loadGroupNotificationRecipients(post, createdPostId, recipientIds);
        }
    }

    private void loadPlaceTypeNotificationRecipients(Post post,
                                                     String createdPostId,
                                                     Set<String> recipientIds) {
        if (post.isPublicPost() && !TextUtils.isEmpty(post.getPlaceType())) {
            followRepository.getUsersFollowingPlaceType(post.getPlaceType(), new FollowRepository.UserIdsListener() {
                @Override
                public void onSuccess(List<String> userIds) {
                    if (userIds != null) {
                        recipientIds.addAll(userIds);
                    }

                    loadGroupNotificationRecipients(post, createdPostId, recipientIds);
                }

                @Override
                public void onError(Exception e) {
                    loadGroupNotificationRecipients(post, createdPostId, recipientIds);
                }
            });
        } else {
            loadGroupNotificationRecipients(post, createdPostId, recipientIds);
        }
    }

    private void loadGroupNotificationRecipients(Post post,
                                                 String createdPostId,
                                                 Set<String> recipientIds) {
        if (!TextUtils.isEmpty(post.getGroupId()) && selectedGroup != null && selectedGroup.getMemberIds() != null) {
            recipientIds.addAll(selectedGroup.getMemberIds());
        }

        sendPostNotifications(post, createdPostId, recipientIds);
    }

    private void sendPostNotifications(Post post, String createdPostId, Set<String> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return;

        // Do not notify the author about their own publication.
        recipientIds.remove(post.getUserId());

        if (recipientIds.isEmpty()) return;

        String title = "Nouvelle publication";
        String message;

        if (!TextUtils.isEmpty(post.getGroupName())) {
            message = post.getAuthorName() + " a publié une photo dans " + post.getGroupName();
        } else if (!TextUtils.isEmpty(post.getPlaceType())) {
            message = post.getAuthorName() + " a publié une photo : " + post.getPlaceType();
        } else {
            message = post.getAuthorName() + " a publié une nouvelle photo";
        }

        String type = !TextUtils.isEmpty(post.getGroupId())
                ? "group_post"
                : "new_post";

        notificationRepository.createNotificationsForUsers(
                new ArrayList<>(recipientIds),
                title,
                message,
                type,
                createdPostId,
                post.getGroupId(),
                new NotificationRepository.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Notifications created silently.
                    }

                    @Override
                    public void onError(Exception e) {
                        // Do not block publication if notifications fail.
                    }
                }
        );
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private List<String> parseTagsFromInput() {
        List<String> tags = new ArrayList<>();

        String rawTags = getText(editTags);

        if (TextUtils.isEmpty(rawTags)) {
            return tags;
        }

        String[] parts = rawTags.split(",");

        for (String part : parts) {
            String tag = part.trim();

            if (tag.startsWith("#")) {
                tag = tag.substring(1).trim();
            }

            tag = tag.replaceAll("\\s+", " ");

            if (!TextUtils.isEmpty(tag) && !tags.contains(tag)) {
                tags.add(tag);
            }
        }

        return tags;
    }
}