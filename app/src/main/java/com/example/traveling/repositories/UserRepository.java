package com.example.traveling.repositories;

import com.example.traveling.models.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private final FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnUserProfileLoadedListener {
        void onSuccess(UserProfile userProfile);
        void onError(Exception exception);
    }

    public interface OnUserProfileActionListener {
        void onSuccess();
        void onError(Exception exception);
    }

    public void createUserProfile(UserProfile userProfile, final OnUserProfileActionListener listener) {
        if (userProfile == null || userProfile.getUid() == null || userProfile.getUid().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid user profile."));
            return;
        }

        db.collection("users")
                .document(userProfile.getUid())
                .set(userProfile)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void getUserProfile(String uid, final OnUserProfileLoadedListener listener) {
        if (uid == null || uid.isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid user id."));
            return;
        }

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);

                        if (userProfile != null) {
                            listener.onSuccess(userProfile);
                        } else {
                            listener.onError(new IllegalStateException("User profile is invalid."));
                        }
                    } else {
                        listener.onError(new IllegalStateException("User profile not found."));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public void updateUserProfile(String uid,
                                  String firstName,
                                  String lastName,
                                  String username,
                                  final OnUserProfileActionListener listener) {
        if (uid == null || uid.isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid user id."));
            return;
        }

        db.collection("users")
                .document(uid)
                .update(
                        "firstName", firstName,
                        "lastName", lastName,
                        "username", username
                )
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }
}