package com.example.traveling.repositories;

import androidx.annotation.NonNull;

import com.example.traveling.models.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;

public class CommentRepository {

    private final FirebaseFirestore db;

    public CommentRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnCommentsLoadedListener {
        void onSuccess(List<Comment> comments);
        void onError(Exception exception);
    }

    public interface OnCommentActionListener {
        void onSuccess();
        void onError(Exception exception);
    }

    public void getCommentsForPost(String postId, final OnCommentsLoadedListener listener) {
        if (postId == null || postId.isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid post id."));
            return;
        }

        db.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Comment> comments = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Comment comment = document.toObject(Comment.class);

                            if (comment != null) {
                                comment.setId(document.getId());
                                comments.add(comment);
                            }
                        }

                        listener.onSuccess(comments);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onError(e);
                    }
                });
    }

    public void addComment(String postId, Comment comment, final OnCommentActionListener listener) {
        if (postId == null || postId.isEmpty() || comment == null) {
            listener.onError(new IllegalArgumentException("Invalid comment."));
            return;
        }

        db.runTransaction(transaction -> {
                    // Add comment with auto-generated document id
                    var commentRef = db.collection("posts")
                            .document(postId)
                            .collection("comments")
                            .document();

                    transaction.set(commentRef, comment);

                    // Increment commentCount on parent post
                    var postRef = db.collection("posts").document(postId);
                    transaction.update(postRef, "commentCount", FieldValue.increment(1));

                    return null;
                }).addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }
}