package com.example.traveling.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.example.traveling.models.RouteOption;
import com.example.traveling.models.RouteStep;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.net.Uri;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;

public class RoutePdfExporter {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;

    public static File exportRouteToPdf(Context context, RouteOption routeOption) throws Exception {
        PdfDocument pdfDocument = new PdfDocument();

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(20);
        titlePaint.setFakeBoldText(true);

        Paint sectionPaint = new Paint();
        sectionPaint.setTextSize(15);
        sectionPaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint();
        bodyPaint.setTextSize(12);

        Paint smallPaint = new Paint();
        smallPaint.setTextSize(10);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT,
                1
        ).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = MARGIN;

        canvas.drawText(safe(routeOption.getTitle(), "Parcours Traveling"), MARGIN, y, titlePaint);
        y += 28;

        canvas.drawText(safe(routeOption.getSummary(), ""), MARGIN, y, bodyPaint);
        y += 24;

        canvas.drawText(
                "Budget : " + String.format(Locale.FRANCE, "%.0f €", routeOption.getEstimatedBudget())
                        + "   Durée : " + formatDuration(routeOption.getEstimatedDurationMinutes())
                        + "   Effort : " + safe(routeOption.getEffortLevel(), "-"),
                MARGIN,
                y,
                bodyPaint
        );
        y += 34;

        canvas.drawText("Étapes du parcours", MARGIN, y, sectionPaint);
        y += 24;

        int currentDay = -1;

        if (routeOption.getSteps() != null) {
            for (int i = 0; i < routeOption.getSteps().size(); i++) {
                RouteStep step = routeOption.getSteps().get(i);

                if (y > PAGE_HEIGHT - 90) {
                    pdfDocument.finishPage(page);

                    pageInfo = new PdfDocument.PageInfo.Builder(
                            PAGE_WIDTH,
                            PAGE_HEIGHT,
                            pdfDocument.getPages().size() + 1
                    ).create();

                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = MARGIN;
                }

                int day = step.getDayNumber() <= 0 ? 1 : step.getDayNumber();

                if (day != currentDay) {
                    currentDay = day;
                    canvas.drawText("Jour " + currentDay, MARGIN, y, sectionPaint);
                    y += 22;
                }

                String stepTitle = (i + 1) + ". " + safe(step.getName(), "Étape")
                        + " — " + safe(step.getPeriod(), "Moment")
                        + " / " + safe(step.getCategory(), "Activité");

                canvas.drawText(stepTitle, MARGIN, y, bodyPaint);
                y += 18;

                y = drawMultilineText(canvas, safe(step.getDescription(), ""), MARGIN + 12, y, smallPaint, 80);
                y += 4;

                canvas.drawText(
                        "Durée : " + step.getEstimatedDurationMinutes()
                                + " min   Coût : "
                                + String.format(Locale.FRANCE, "%.0f €", step.getEstimatedCost()),
                        MARGIN + 12,
                        y,
                        smallPaint
                );
                y += 22;

                if (step.getTravelToNextMinutes() > 0) {
                    canvas.drawText(
                            "Trajet suivant : " + step.getTravelToNextMinutes()
                                    + " min • " + safe(step.getTravelToNextMode(), "déplacement"),
                            MARGIN + 12,
                            y,
                            smallPaint
                    );
                    y += 22;
                }

                y += 8;
            }
        }

        pdfDocument.finishPage(page);

        File file = new File(context.getCacheDir(), makeSafeFileName(routeOption.getTitle()) + ".pdf");

        FileOutputStream outputStream = new FileOutputStream(file);
        pdfDocument.writeTo(outputStream);
        outputStream.close();

        pdfDocument.close();

        return file;
    }

    public static void writeRoutePdfToUri(Context context,
                                          RouteOption routeOption,
                                          Uri destinationUri) throws Exception {
        File tempFile = exportRouteToPdf(context, routeOption);

        try (FileInputStream inputStream = new FileInputStream(tempFile);
             OutputStream outputStream = context.getContentResolver().openOutputStream(destinationUri)) {

            if (outputStream == null) {
                throw new IllegalStateException("Impossible d'ouvrir le fichier de destination.");
            }

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
        }
    }

    public static Uri saveRoutePdfToDownloads(Context context, RouteOption routeOption) throws Exception {
        File tempFile = exportRouteToPdf(context, routeOption);
        String fileName = makeSafeFileName(routeOption.getTitle()) + ".pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Traveling");

            Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

            if (uri == null) {
                throw new IllegalStateException("Impossible de créer le fichier PDF.");
            }

            try (OutputStream outputStream = resolver.openOutputStream(uri);
                 FileInputStream inputStream = new FileInputStream(tempFile)) {

                if (outputStream == null) {
                    throw new IllegalStateException("Impossible d'ouvrir le fichier PDF.");
                }

                copyStream(inputStream, outputStream);
            }

            return uri;
        }

        File downloadsDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "Traveling"
        );

        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        File outputFile = new File(downloadsDir, fileName);

        try (FileInputStream inputStream = new FileInputStream(tempFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            copyStream(inputStream, outputStream);
        }

        return Uri.fromFile(outputFile);
    }

    private static void copyStream(FileInputStream inputStream, OutputStream outputStream) throws Exception {
        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
    }

    private static int drawMultilineText(Canvas canvas, String text, int x, int y, Paint paint, int maxCharsPerLine) {
        if (text == null || text.trim().isEmpty()) {
            return y;
        }

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (line.length() + word.length() > maxCharsPerLine) {
                canvas.drawText(line.toString(), x, y, paint);
                y += 15;
                line = new StringBuilder();
            }

            if (line.length() > 0) {
                line.append(" ");
            }

            line.append(word);
        }

        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, y, paint);
            y += 15;
        }

        return y;
    }

    private static String formatDuration(int minutes) {
        if (minutes <= 0) return "-";

        int hours = minutes / 60;
        int remaining = minutes % 60;

        if (hours <= 0) return remaining + " min";
        if (remaining == 0) return hours + "h";

        return hours + "h" + remaining;
    }

    private static String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private static String makeSafeFileName(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "itineraire_traveling";
        }

        return title.trim()
                .toLowerCase()
                .replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}