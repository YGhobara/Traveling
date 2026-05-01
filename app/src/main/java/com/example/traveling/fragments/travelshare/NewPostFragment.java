package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.models.Post;
import com.example.traveling.models.UserProfile;
import com.example.traveling.repositories.PostRepository;
import com.example.traveling.repositories.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class NewPostFragment extends Fragment {

    private TextInputEditText editCaption;
    private TextInputEditText editLocation;
    private TextInputEditText editImageUrl;
    private SwitchMaterial switchPublic;
    private MaterialButton buttonPublish;

    private PostRepository postRepository;
    private UserRepository userRepository;
    private FirebaseAuth auth;

    public NewPostFragment() {
        // Required empty public constructor
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
        editLocation = view.findViewById(R.id.editLocation);
        editImageUrl = view.findViewById(R.id.editImageUrl);
        switchPublic = view.findViewById(R.id.switchPublic);
        buttonPublish = view.findViewById(R.id.buttonPublish);

        postRepository = new PostRepository();
        userRepository = new UserRepository();
        auth = FirebaseAuth.getInstance();

        buttonPublish.setOnClickListener(v -> publishPost());
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
        String location = getText(editLocation);
        String imageUrl = getText(editImageUrl);
        boolean publicPost = switchPublic.isChecked();

        if (TextUtils.isEmpty(caption)) {
            editCaption.setError("Ajoutez une légende.");
            return;
        }

        if (TextUtils.isEmpty(location)) {
            editLocation.setError("Ajoutez un lieu.");
            return;
        }

        if (TextUtils.isEmpty(imageUrl)) {
            editImageUrl.setError("Ajoutez une URL d'image.");
            return;
        }

        buttonPublish.setEnabled(false);

        userRepository.getUserProfile(currentUser.getUid(), new UserRepository.OnUserProfileLoadedListener() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                String authorName = userProfile.getDisplayName();

                if (TextUtils.isEmpty(authorName)) {
                    authorName = currentUser.getEmail();
                }

                createPost(currentUser.getUid(), authorName, caption, location, imageUrl, publicPost);
            }

            @Override
            public void onError(Exception exception) {
                String fallbackName = currentUser.getEmail();
                createPost(currentUser.getUid(), fallbackName, caption, location, imageUrl, publicPost);
            }
        });
    }

    private void createPost(String userId,
                            String authorName,
                            String caption,
                            String location,
                            String imageUrl,
                            boolean publicPost) {
        Post post = new Post(
                null,
                userId,
                authorName,
                caption,
                imageUrl,
                location,
                System.currentTimeMillis(),
                0,
                0,
                publicPost
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