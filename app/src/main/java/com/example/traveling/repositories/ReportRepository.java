package com.example.traveling.repositories;

import com.example.traveling.models.Report;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportRepository {

    private final FirebaseFirestore db;

    public ReportRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnReportActionListener {
        void onSuccess();
        void onError(Exception exception);
    }

    public interface OnReportCheckListener {
        void onResult(boolean alreadyReported);
        void onError(Exception exception);
    }

    public void hasUserReported(String postId, String userId, final OnReportCheckListener listener) {
        if (postId == null || postId.isEmpty() || userId == null || userId.isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid report check."));
            return;
        }

        String reportId = buildReportId(postId, userId);

        db.collection("reports")
                .document(reportId)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                        listener.onResult(documentSnapshot.exists())
                )
                .addOnFailureListener(listener::onError);
    }

    public void createReport(Report report, final OnReportActionListener listener) {
        if (report == null
                || report.getPostId() == null || report.getPostId().isEmpty()
                || report.getReportedByUserId() == null || report.getReportedByUserId().isEmpty()) {
            listener.onError(new IllegalArgumentException("Invalid report."));
            return;
        }

        String reportId = buildReportId(report.getPostId(), report.getReportedByUserId());
        report.setId(reportId);

        DocumentReference reportRef = db.collection("reports").document(reportId);

        reportRef.set(report)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onError);
    }

    private String buildReportId(String postId, String userId) {
        return postId + "_" + userId;
    }
}