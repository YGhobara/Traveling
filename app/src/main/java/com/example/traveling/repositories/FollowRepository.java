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
}