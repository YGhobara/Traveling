package com.example.traveling.fragments.travelpath;
import com.example.traveling.models.RouteStep;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.models.RouteOption;

import java.util.Locale;
import android.view.LayoutInflater;
import java.util.Locale;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import android.graphics.Typeface;

import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import com.example.traveling.local.SavedRouteEntity;
import com.example.traveling.repositories.SavedRouteRepository;
import com.example.traveling.utils.RouteJsonMapper;

import org.json.JSONException;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.example.traveling.utils.RoutePdfExporter;

import java.io.File;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class RouteDetailFragment extends Fragment {
    private SavedRouteRepository savedRouteRepository;
    private MaterialButton buttonLikeRoute;
    private MaterialButton buttonSaveRoute;
    private MaterialButton buttonShareRoute;
    private MaterialButton buttonExportRoutePdf;
    private ImageView imageRouteDetail;
    private RouteOption routeOption;

    public RouteDetailFragment() {
        // Required empty public constructor
    }

    public static RouteDetailFragment newInstance(RouteOption routeOption) {
        RouteDetailFragment fragment = new RouteDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable("routeOption", routeOption);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            routeOption = (RouteOption) getArguments().getSerializable("routeOption");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (routeOption == null) {
            Toast.makeText(requireContext(), "Parcours introuvable.", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        setupBackButton(view);
        bindRoute(view);
        savedRouteRepository = new SavedRouteRepository(requireContext());
        setupActionButtons(view);
    }

    private void setupActionButtons(View view) {
        buttonLikeRoute = view.findViewById(R.id.buttonLikeRoute);
        buttonSaveRoute = view.findViewById(R.id.buttonSaveRoute);
        buttonShareRoute = view.findViewById(R.id.buttonShareRoute);
        buttonExportRoutePdf = view.findViewById(R.id.buttonExportRoutePdf);

        updateLikeButton();

        buttonLikeRoute.setOnClickListener(v -> {
            routeOption.setLiked(!routeOption.isLiked());
            updateLikeButton();
        });

        buttonSaveRoute.setOnClickListener(v -> saveRouteLocally());

        buttonShareRoute.setOnClickListener(v -> shareRoute());

        buttonExportRoutePdf.setOnClickListener(v -> exportRoutePdf());
    }

    private void saveRouteLocally() {
        try {
            String destination = routeOption.getDestination();

            SavedRouteEntity entity = RouteJsonMapper.toEntity(routeOption, destination);

            savedRouteRepository.saveRoute(entity, new SavedRouteRepository.SaveRouteCallback() {
                @Override
                public void onSuccess(long routeId) {
                    if (!isAdded()) {
                        return;
                    }

                    routeOption.setSaved(true);
                    buttonSaveRoute.setEnabled(false);
                    Toast.makeText(requireContext(), "Parcours enregistré localement.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(Exception exception) {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(requireContext(), "Erreur lors de la sauvegarde.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            Toast.makeText(requireContext(), "Erreur de préparation du parcours.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportRoutePdf() {
        try {
            File pdfFile = RoutePdfExporter.exportRouteToPdf(requireContext(), routeOption);

            Uri pdfUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    pdfFile
            );

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Exporter le parcours en PDF"));

        } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    "Erreur lors de l'export PDF.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setupBackButton(View view) {
        ImageButton buttonBack = view.findViewById(R.id.buttonBackRouteDetail);

        if (buttonBack != null) {
            buttonBack.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack()
            );
        }
    }

    private void updateLikeButton() {
        if (buttonLikeRoute == null) {
            return;
        }

        if (routeOption.isLiked()) {
            buttonLikeRoute.setIconResource(R.drawable.ic_favorite_filled);
        } else {
            buttonLikeRoute.setIconResource(R.drawable.ic_favorite_outline);
        }
    }

    private void shareRoute() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, routeOption.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, buildShareText());

        startActivity(Intent.createChooser(intent, "Partager le parcours"));
    }

    private String buildShareText() {
        StringBuilder builder = new StringBuilder();

        builder.append(routeOption.getTitle()).append("\n\n");
        builder.append(routeOption.getSummary()).append("\n\n");
        builder.append("Budget estimé : ")
                .append(String.format(Locale.FRANCE, "%.0f €", routeOption.getEstimatedBudget()))
                .append("\n");
        builder.append("Durée : ")
                .append(formatDuration(routeOption.getEstimatedDurationMinutes()))
                .append("\n");
        builder.append("Effort : ")
                .append(routeOption.getEffortLevel())
                .append("\n\n");

        if (routeOption.getSteps() != null && !routeOption.getSteps().isEmpty()) {
            builder.append("Étapes :\n");

            for (int i = 0; i < routeOption.getSteps().size(); i++) {
                RouteStep step = routeOption.getSteps().get(i);
                builder.append(i + 1)
                        .append(". ")
                        .append(safeText(step.getName(), "Étape"))
                        .append(" — ")
                        .append(safeText(step.getPeriod(), "Moment"))
                        .append("\n");
            }
        }

        return builder.toString();
    }

    private void bindRoute(View view) {
        imageRouteDetail = view.findViewById(R.id.imageRouteDetail);
        bindRouteImage();
        TextView textTitle = view.findViewById(R.id.textRouteDetailTitle);
        TextView textSummary = view.findViewById(R.id.textRouteDetailSummary);
        TextView textBudget = view.findViewById(R.id.textRouteDetailBudget);
        TextView textDuration = view.findViewById(R.id.textRouteDetailDuration);
        TextView textEffort = view.findViewById(R.id.textRouteDetailEffort);
        LinearLayout containerSteps = view.findViewById(R.id.containerRouteSteps);

        if (containerSteps != null) {
            bindSteps(containerSteps);
        }

        if (textTitle != null) {
            textTitle.setText(routeOption.getTitle());
        }

        if (textSummary != null) {
            textSummary.setText(routeOption.getSummary());
        }

        if (textBudget != null) {
            textBudget.setText(String.format(Locale.FRANCE, "%.0f €", routeOption.getEstimatedBudget()));
        }

        if (textDuration != null) {
            textDuration.setText(formatDuration(routeOption.getEstimatedDurationMinutes()));
        }

        if (textEffort != null) {
            textEffort.setText(routeOption.getEffortLevel());
        }
    }

    private void bindRouteImage() {
        if (imageRouteDetail == null) {
            return;
        }

        if (TextUtils.isEmpty(routeOption.getImageUrl())) {
            imageRouteDetail.setVisibility(View.GONE);
            return;
        }

        imageRouteDetail.setVisibility(View.VISIBLE);

        Glide.with(requireContext())
                .load(routeOption.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.bg_post_placeholder)
                .error(R.drawable.bg_post_placeholder)
                .into(imageRouteDetail);
    }

    private void bindSteps(LinearLayout containerSteps) {
        containerSteps.removeAllViews();

        if (routeOption.getSteps() == null || routeOption.getSteps().isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("Aucune étape détaillée disponible.");
            emptyText.setTextColor(0xFF64748B);
            emptyText.setTextSize(14);
            containerSteps.addView(emptyText);
            return;
        }

        Map<Integer, List<RouteStep>> stepsByDay = new TreeMap<>();

        for (RouteStep step : routeOption.getSteps()) {
            int day = step.getDayNumber();

            if (day <= 0) {
                day = 1;
            }

            if (!stepsByDay.containsKey(day)) {
                stepsByDay.put(day, new ArrayList<>());
            }

            stepsByDay.get(day).add(step);
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (Map.Entry<Integer, List<RouteStep>> entry : stepsByDay.entrySet()) {
            int dayNumber = entry.getKey();
            List<RouteStep> daySteps = entry.getValue();

            TextView dayTitle = new TextView(requireContext());
            dayTitle.setText("Jour " + dayNumber);
            dayTitle.setTextColor(0xFF0F172A);
            dayTitle.setTextSize(18);
            dayTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            dayTitle.setPadding(0, 18, 0, 10);

            containerSteps.addView(dayTitle);

            for (int i = 0; i < daySteps.size(); i++) {
                RouteStep step = daySteps.get(i);

                View stepView = inflater.inflate(R.layout.item_route_step, containerSteps, false);

                TextView textStepNumber = stepView.findViewById(R.id.textStepNumber);
                TextView textStepName = stepView.findViewById(R.id.textStepName);
                TextView textStepMeta = stepView.findViewById(R.id.textStepMeta);
                TextView textStepDescription = stepView.findViewById(R.id.textStepDescription);
                TextView textStepDuration = stepView.findViewById(R.id.textStepDuration);
                TextView textStepCost = stepView.findViewById(R.id.textStepCost);
                TextView textStepTravelNext = stepView.findViewById(R.id.textStepTravelNext);

                textStepNumber.setText(String.valueOf(i + 1));
                textStepName.setText(safeText(step.getName(), "Étape"));
                textStepMeta.setText(safeText(step.getPeriod(), "Moment") + " • " + safeText(step.getCategory(), "Activité"));
                textStepDescription.setText(safeText(step.getDescription(), "Aucune description disponible."));

                textStepDuration.setText("Durée : " + step.getEstimatedDurationMinutes() + " min");
                textStepCost.setText(String.format(Locale.FRANCE, "Coût : %.0f €", step.getEstimatedCost()));

                if (step.getTravelToNextMinutes() > 0) {
                    textStepTravelNext.setVisibility(View.VISIBLE);
                    textStepTravelNext.setText(
                            "Trajet suivant : "
                                    + step.getTravelToNextMinutes()
                                    + " min • "
                                    + safeText(step.getTravelToNextMode(), "déplacement")
                    );
                } else {
                    textStepTravelNext.setVisibility(View.GONE);
                }

                containerSteps.addView(stepView);
            }
        }
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private String formatDuration(int minutes) {
        if (minutes <= 0) {
            return "-";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (hours <= 0) {
            return remainingMinutes + " min";
        }

        if (remainingMinutes == 0) {
            return hours + "h";
        }

        return hours + "h" + remainingMinutes;
    }
}