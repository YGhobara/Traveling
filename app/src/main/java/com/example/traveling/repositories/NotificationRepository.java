package com.example.traveling.repositories;

import com.example.traveling.models.Notification;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationRepository {

    public interface NotificationsListener {
        void onSuccess(List<Notification> notifications);
        void onError(Exception e);
    }

    public interface CountListener {
        void onSuccess(int count);
        void onError(Exception e);
    }

    public interface ActionListener {
        void onSuccess();
        void onError(Exception e);
    }

    private final FirebaseFirestore db;

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void createNotification(Notification notification, ActionListener listener) {
        String id = db.collection("notifications").document().getId();
        notification.setId(id);

        db.collection("notifications")
                .document(id)
                .set(notification)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void createNotificationsForUsers(List<String> userIds,
                                            String title,
                                            String message,
                                            String type,
                                            String relatedPostId,
                                            String relatedGroupId,
                                            ActionListener listener) {
        if (userIds == null || userIds.isEmpty()) {
            listener.onSuccess();
            return;
        }

        Set<String> uniqueUserIds = new HashSet<>(userIds);
        WriteBatch batch = db.batch();

        for (String userId : uniqueUserIds) {
            if (userId == null || userId.trim().isEmpty()) {
                continue;
            }

            String id = db.collection("notifications").document().getId();

            Notification notification = Notification.createPostNotification(
                    userId,
                    title,
                    message,
                    type,
                    relatedPostId,
                    relatedGroupId
            );
            notification.setId(id);

            batch.set(db.collection("notifications").document(id), notification);
        }

        batch.commit()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void getNotificationsForUser(String userId, NotificationsListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> notifications = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Notification notification = document.toObject(Notification.class);

                        if (notification != null) {
                            notification.setId(document.getId());
                            notifications.add(notification);
                        }
                    }

                    listener.onSuccess(notifications);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getUnreadCount(String userId, CountListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        listener.onSuccess(queryDocumentSnapshots.size())
                )
                .addOnFailureListener(listener::onError);
    }

    public void markAsRead(String notificationId, ActionListener listener) {
        db.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void markAllAsRead(String userId, ActionListener listener) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        batch.update(document.getReference(), "read", true);
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> listener.onSuccess())
                            .addOnFailureListener(listener::onError);
                })
                .addOnFailureListener(listener::onError);
    }
}