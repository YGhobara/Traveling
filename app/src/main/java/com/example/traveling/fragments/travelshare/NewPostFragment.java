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
import com.example.traveling.models.Group;
import com.example.traveling.repositories.GroupRepository;
import com.google.android.material.textfield.TextInputLayout;

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

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class NewPostFragment extends Fragment {

    private TextInputEditText editCaption;
    private MaterialAutoCompleteTextView dropdownLocation;
    private TextInputEditText editImageUrl;
    private SwitchMaterial switchPublic;
    private MaterialButton buttonPublish;

    private PostRepository postRepository;
    private UserRepository userRepository;
    private PhotonRepository photonRepository;
    private ArrayAdapter<LocationSuggestion> locationAdapter;
    private LocationSuggestion selectedLocationSuggestion;
    private String latestLocationQuery = "";

    private final Handler locationSearchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingLocationSearch;
    private boolean isSettingLocationFromSuggestion = false;
    private FirebaseAuth auth;

    private ImageView imagePreview;
    private MaterialButton buttonChooseImage;
    private Uri selectedImageUri;

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
        imagePreview = view.findViewById(R.id.imagePreview);
        buttonChooseImage = view.findViewById(R.id.buttonChooseImage);
        dropdownPlaceType = view.findViewById(R.id.dropdownPlaceType);
        switchShareToGroup = view.findViewById(R.id.switchShareToGroup);
        layoutGroupDropdown = view.findViewById(R.id.layoutGroupDropdown);
        dropdownGroup = view.findViewById(R.id.dropdownGroup);

        postRepository = new PostRepository();
        userRepository = new UserRepository();
        photonRepository = new PhotonRepository();
        groupRepository = new GroupRepository();

        setupPlaceTypeDropdown();
        setupLocationAutocomplete();
        setupGroupSharing();
        auth = FirebaseAuth.getInstance();

        buttonPublish.setOnClickListener(v -> publishPost());
        buttonChooseImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );
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

        postRepository.createPost(post, new PostRepository.OnPostActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(),
                        "Publication ajoutée.",
                        Toast.LENGTH_SHORT).show();

                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                        requireActivity().findViewById(R.id.bottom_navigation);

                bottomNav.setSelectedItemId(R.id.nav_feed);
            }

            @Override
            public void onError(Exception exception) {
                buttonPublish.setEnabled(true);
                buttonPublish.setText("Publier");

                Toast.makeText(requireContext(),
                        "Erreur : " + exception.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }
}