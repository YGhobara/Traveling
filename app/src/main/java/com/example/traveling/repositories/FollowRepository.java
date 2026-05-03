package com.example.traveling.repositories;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class FollowRepository {

    public interface FollowCheckListener {
        void onResult(boolean isFollowing);
        void onError(Exception e);
    }

    public interface FollowActionListener {
        void onSuccess();
        void onError(Exception e);
    }

    public interface PlaceTypesListener {
        void onSuccess(java.util.List<String> placeTypes);
        void onError(Exception e);
    }

    public interface UserIdsListener {
        void onSuccess(java.util.List<String> userIds);
        void onError(Exception e);
    }

    private final FirebaseFirestore db;

    public FollowRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void isFollowing(String currentUserId, String targetUserId, FollowCheckListener listener) {
        db.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                        listener.onResult(documentSnapshot.exists())
                )
                .addOnFailureListener(listener::onError);
    }

    public void followUser(String currentUserId,
                           String targetUserId,
                           String currentUserName,
                           String targetUserName,
                           FollowActionListener listener) {

        if (currentUserId == null || targetUserId == null || currentUserId.equals(targetUserId)) {
            listener.onError(new IllegalArgumentException("Invalid follow target."));
            return;
        }

        long now = System.currentTimeMillis();

        Map<String, Object> followingData = new HashMap<>();
        followingData.put("userId", targetUserId);
        followingData.put("displayName", targetUserName);
        followingData.put("createdAt", now);

        Map<String, Object> followerData = new HashMap<>();
        followerData.put("userId", currentUserId);
        followerData.put("displayName", currentUserName);
        followerData.put("createdAt", now);

        WriteBatch batch = db.batch();

        batch.set(
                db.collection("users")
                        .document(currentUserId)
                        .collection("following")
                        .document(targetUserId),
                followingData
        );

        batch.set(
                db.collection("users")
                        .document(targetUserId)
                        .collection("followers")
                        .document(currentUserId),
                followerData
        );

        batch.update(
                db.collection("users").document(currentUserId),
                "followingCount", FieldValue.increment(1)
        );

        batch.update(
                db.collection("users").document(targetUserId),
                "followersCount", FieldValue.increment(1)
        );

        batch.commit()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void unfollowUser(String currentUserId,
                             String targetUserId,
                             FollowActionListener listener) {

        if (currentUserId == null || targetUserId == null || currentUserId.equals(targetUserId)) {
            listener.onError(new IllegalArgumentException("Invalid unfollow target."));
            return;
        }

        WriteBatch batch = db.batch();

        batch.delete(
                db.collection("users")
                        .document(currentUserId)
                        .collection("following")
                        .document(targetUserId)
        );

        batch.delete(
                db.collection("users")
                        .document(targetUserId)
                        .collection("followers")
                        .document(currentUserId)
        );

        batch.update(
                db.collection("users").document(currentUserId),
                "followingCount", FieldValue.increment(-1)
        );

        batch.update(
                db.collection("users").document(targetUserId),
                "followersCount", FieldValue.increment(-1)
        );

        batch.commit()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void isFollowingPlaceType(String userId, String placeType, FollowCheckListener listener) {
        if (userId == null || placeType == null || placeType.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid place type."));
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("followedPlaceTypes")
                .document(placeType)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                        listener.onResult(documentSnapshot.exists())
                )
                .addOnFailureListener(listener::onError);
    }

    public void followPlaceType(String userId, String placeType, FollowActionListener listener) {
        if (userId == null || placeType == null || placeType.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid place type."));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("placeType", placeType);
        data.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("followedPlaceTypes")
                .document(placeType)
                .set(data)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void unfollowPlaceType(String userId, String placeType, FollowActionListener listener) {
        if (userId == null || placeType == null || placeType.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid place type."));
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("followedPlaceTypes")
                .document(placeType)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void getFollowedPlaceTypes(String userId, PlaceTypesListener listener) {
        if (userId == null) {
            listener.onError(new IllegalArgumentException("Invalid user id."));
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("followedPlaceTypes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<String> placeTypes = new java.util.ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String placeType = document.getString("placeType");

                        if (placeType != null && !placeType.trim().isEmpty()) {
                            placeTypes.add(placeType);
                        }
                    }

                    listener.onSuccess(placeTypes);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getFollowerIds(String userId, UserIdsListener listener) {
        if (userId == null || userId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid user id."));
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("followers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<String> ids = new java.util.ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ids.add(document.getId());
                    }

                    listener.onSuccess(ids);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getUsersFollowingPlaceType(String placeType, UserIdsListener listener) {
        if (placeType == null || placeType.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid place type."));
            return;
        }

        db.collectionGroup("followedPlaceTypes")
                .whereEqualTo("placeType", placeType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<String> ids = new java.util.ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        if (document.getReference().getParent().getParent() != null) {
                            ids.add(document.getReference().getParent().getParent().getId());
                        }
                    }

                    listener.onSuccess(ids);
                })
                .addOnFailureListener(listener::onError);
    }
}