package com.example.traveling.repositories;

import androidx.annotation.Nullable;

import com.example.traveling.models.Group;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class GroupRepository {

    public interface GroupListListener {
        void onSuccess(List<Group> groups);
        void onError(Exception e);
    }

    public interface GroupListener {
        void onSuccess(Group group);
        void onError(Exception e);
    }

    public interface ActionListener {
        void onSuccess();
        void onError(Exception e);
    }

    private final FirebaseFirestore db;

    public GroupRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void createGroup(Group group, ActionListener listener) {
        String groupId = db.collection("groups").document().getId();
        group.setId(groupId);

        db.collection("groups")
                .document(groupId)
                .set(group)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void getPublicGroups(GroupListListener listener) {
        db.collection("groups")
                .whereEqualTo("publicGroup", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Group group = doc.toObject(Group.class);
                        if (group != null) {
                            group.setId(doc.getId());
                            groups.add(group);
                        }
                    }

                    listener.onSuccess(groups);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getMyGroups(String userId, GroupListListener listener) {
        db.collection("groups")
                .whereArrayContains("memberIds", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Group group = doc.toObject(Group.class);
                        if (group != null) {
                            group.setId(doc.getId());
                            groups.add(group);
                        }
                    }

                    listener.onSuccess(groups);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getPublicGroupsByUser(String userId, GroupListListener listener) {
        if (userId == null || userId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid user id."));
            return;
        }

        db.collection("groups")
                .whereArrayContains("memberIds", userId)
                .whereEqualTo("publicGroup", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Group group = doc.toObject(Group.class);

                        if (group != null) {
                            group.setId(doc.getId());
                            groups.add(group);
                        }
                    }

                    listener.onSuccess(groups);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getGroupById(String groupId, GroupListener listener) {
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Group group = documentSnapshot.toObject(Group.class);

                    if (group != null) {
                        group.setId(documentSnapshot.getId());
                        listener.onSuccess(group);
                    } else {
                        listener.onError(new Exception("Groupe introuvable"));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public void joinGroup(String groupId, String userId, ActionListener listener) {
        db.collection("groups")
                .document(groupId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Group group = documentSnapshot.toObject(Group.class);

                    if (group != null && group.isMember(userId)) {
                        listener.onSuccess();
                        return;
                    }

                    db.collection("groups")
                            .document(groupId)
                            .update(
                                    "memberIds", FieldValue.arrayUnion(userId),
                                    "memberCount", FieldValue.increment(1)
                            )
                            .addOnSuccessListener(unused -> listener.onSuccess())
                            .addOnFailureListener(listener::onError);
                })
                .addOnFailureListener(listener::onError);
    }

    public void leaveGroup(String groupId, String userId, ActionListener listener) {
        db.collection("groups")
                .document(groupId)
                .update(
                        "memberIds", FieldValue.arrayRemove(userId),
                        "memberCount", FieldValue.increment(-1)
                )
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void deleteGroup(String groupId, ActionListener listener) {
        if (groupId == null || groupId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid group id."));
            return;
        }

        db.collection("groups")
                .document(groupId)
                .delete()
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public boolean isCurrentUserMember(@Nullable Group group, String userId) {
        return group != null && group.isMember(userId);
    }
}