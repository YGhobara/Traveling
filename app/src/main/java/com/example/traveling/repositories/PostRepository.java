package com.example.traveling.repositories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.traveling.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class PostRepository {

    private final FirebaseFirestore db;

    public PostRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnPostsLoadedListener {
        void onSuccess(List<Post> posts);
        void onError(Exception exception);
    }

    public interface OnPaginatedPostsLoadedListener {
        void onSuccess(List<Post> posts, DocumentSnapshot lastVisibleDocument);
        void onError(Exception exception);
    }

    public void getPublicPosts(final OnPostsLoadedListener listener) {
        db.collection("posts")
                .whereEqualTo("publicPost", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Post> posts = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Post post = document.toObject(Post.class);

                            if (post != null) {
                                post.setId(document.getId());
                                posts.add(post);
                            }
                        }

                        listener.onSuccess(posts);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e);
                    }
                });
    }

    public interface OnPostActionListener {
        void onSuccess();
        void onError(Exception exception);
    }

    public interface OnPostCreatedListener {
        void onSuccess(String postId);
        void onError(Exception exception);
    }

    public void toggleLike(Post post, String userId, final OnPostActionListener listener) {
        if (post == null || post.getId() == null || userId == null) {
            listener.onError(new IllegalArgumentException("Invalid post or user."));
            return;
        }

        DocumentReference postRef = db.collection("posts").document(post.getId());

        db.runTransaction((Transaction.Function<Void>) transaction -> {
                    Post freshPost = transaction.get(postRef).toObject(Post.class);

                    if (freshPost == null) {
                        throw new IllegalStateException("Post not found.");
                    }

                    boolean alreadyLiked = freshPost.getLikedBy() != null
                            && freshPost.getLikedBy().contains(userId);

                    if (alreadyLiked) {
                        transaction.update(postRef,
                                "likedBy", FieldValue.arrayRemove(userId),
                                "likeCount", FieldValue.increment(-1));
                    } else {
                        transaction.update(postRef,
                                "likedBy", FieldValue.arrayUnion(userId),
                                "likeCount", FieldValue.increment(1));
                    }

                    return null;
                }).addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public interface OnPostLoadedListener {
        void onSuccess(Post post);
        void onError(Exception exception);
    }

    public void getPostById(String postId, final OnPostLoadedListener listener) {
        if (postId == null || postId.isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid post id."));
            return;
        }

        db.collection("posts")
                .document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);

                        if (post != null) {
                            post.setId(documentSnapshot.getId());
                            listener.onSuccess(post);
                        } else {
                            listener.onError(new IllegalStateException("Post data is invalid."));
                        }
                    } else {
                        listener.onError(new IllegalStateException("Post not found."));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

    public void getPostsByUser(String userId, OnPostsLoadedListener listener) {
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Post post = document.toObject(Post.class);

                        if (post != null) {
                            post.setId(document.getId());
                            posts.add(post);
                        }
                    }

                    listener.onSuccess(posts);
                })
                .addOnFailureListener(listener::onError);
    }

    public void getPostsByGroup(String groupId, OnPostsLoadedListener listener) {
        if (groupId == null || groupId.trim().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid group id."));
            return;
        }

        db.collection("posts")
                .whereEqualTo("groupId", groupId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Post post = document.toObject(Post.class);

                        if (post != null) {
                            post.setId(document.getId());
                            posts.add(post);
                        }
                    }

                    listener.onSuccess(posts);
                })
                .addOnFailureListener(listener::onError);
    }

    public void createPost(Post post, final OnPostActionListener listener) {
        if (post == null) {
            listener.onError(new IllegalArgumentException("Post cannot be null."));
            return;
        }

        DocumentReference postRef = db.collection("posts").document();

        post.setId(postRef.getId());

        postRef.set(post)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    public void createPostAndReturnId(Post post, final OnPostCreatedListener listener) {
        if (post == null) {
            listener.onError(new IllegalArgumentException("Post cannot be null."));
            return;
        }

        DocumentReference postRef = db.collection("posts").document();

        post.setId(postRef.getId());

        postRef.set(post)
                .addOnSuccessListener(unused -> listener.onSuccess(postRef.getId()))
                .addOnFailureListener(listener::onError);
    }

    public void getPublicPostsPage(@Nullable DocumentSnapshot lastVisibleDocument,
                                   int limit,
                                   @Nullable String placeType,
                                   @Nullable Long startDate,
                                   @Nullable Long endDate,
                                   final OnPaginatedPostsLoadedListener listener) {
        Query query = db.collection("posts")
                .whereEqualTo("publicPost", true);

        if (placeType != null && !placeType.trim().isEmpty() && !"Tous".equals(placeType)) {
            query = query.whereEqualTo("placeType", placeType);
        }

        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("createdAt", startDate);
        }

        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("createdAt", endDate);
        }

        query = query.orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit);

        if (lastVisibleDocument != null) {
            query = query.startAfter(lastVisibleDocument);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        Post post = document.toObject(Post.class);

                        if (post != null) {
                            post.setId(document.getId());
                            posts.add(post);
                        }
                    }

                    DocumentSnapshot newLastVisible = null;

                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        newLastVisible = documents.get(documents.size() - 1);
                    }

                    listener.onSuccess(posts, newLastVisible);
                })
                .addOnFailureListener(listener::onError);
    }
}