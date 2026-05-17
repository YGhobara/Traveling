package com.example.traveling.fragments.travelpath;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.traveling.repositories.TravelPathAiRepository;
import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.adapters.RouteOptionAdapter;
import com.example.traveling.models.RouteOption;
import com.example.traveling.models.RoutePreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import com.google.android.material.button.MaterialButton;
import com.example.traveling.models.WeatherForecastSummary;
import com.example.traveling.repositories.OpenMeteoRepository;
import com.example.traveling.repositories.UnsplashRepository;

public class RouteOptionsFragment extends Fragment {
    private ArrayList<RouteOption> generatedRoutes = new ArrayList<>();
    private RoutePreferences routePreferences;
    private TravelPathAiRepository travelPathAiRepository;
    private OpenMeteoRepository openMeteoRepository;
    private UnsplashRepository unsplashRepository;
    private TextView textRouteOptionsTitle;
    private TextView textWeatherSummary;
    private View layoutWeatherSummary;
    private WeatherForecastSummary currentWeatherSummary;
    private TextView textRouteStatus;
    private MaterialButton buttonRegenerateRoutes;
    private ProgressBar progressRouteGeneration;
    private RecyclerView recyclerRouteOptions;
    private RouteOptionAdapter routeOptionAdapter;
    private static final String TAG = "RouteOptionsFragment";

    public RouteOptionsFragment() {
        // Required empty public constructor
    }

    public static RouteOptionsFragment newInstance(RoutePreferences preferences) {
        RouteOptionsFragment fragment = new RouteOptionsFragment();

        Bundle args = new Bundle();
        args.putSerializable("routePreferences", preferences);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            routePreferences = (RoutePreferences) getArguments().getSerializable("routePreferences");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (routePreferences == null) {
            Toast.makeText(requireContext(), "Préférences introuvables.", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        bindViews(view);
        setupBackButton(view);
        setupRecycler();
        setupRegenerateButton();
        travelPathAiRepository = new TravelPathAiRepository();
        openMeteoRepository = new OpenMeteoRepository();
        unsplashRepository = new UnsplashRepository();

        if (!generatedRoutes.isEmpty()) {
            displayWeatherSummary(currentWeatherSummary);
            showGeneratedRoutes(generatedRoutes);
        } else {
            generateRoutesWithAi();
        }
    }

    private void setupRegenerateButton() {
        buttonRegenerateRoutes.setOnClickListener(v -> {
            generatedRoutes.clear();
            routeOptionAdapter.submitList(new ArrayList<>());
            generateRoutesWithAi();
        });
    }

    private void showGeneratedRoutes(List<RouteOption> routes) {
        setLoading(false);
        textRouteStatus.setVisibility(View.GONE);
        recyclerRouteOptions.setVisibility(View.VISIBLE);
        routeOptionAdapter.submitList(routes);
    }

    private void bindViews(View view) {
        textRouteOptionsTitle = view.findViewById(R.id.textRouteOptionsTitle);
        textRouteStatus = view.findViewById(R.id.textRouteStatus);
        layoutWeatherSummary = view.findViewById(R.id.layoutWeatherSummary);
        textWeatherSummary = view.findViewById(R.id.textWeatherSummary);
        progressRouteGeneration = view.findViewById(R.id.progressRouteGeneration);
        recyclerRouteOptions = view.findViewById(R.id.recyclerRouteOptions);
        buttonRegenerateRoutes = view.findViewById(R.id.buttonRegenerateRoutes);
        textRouteOptionsTitle.setText("Options pour " + routePreferences.getDestination());
    }

    private void generateRoutesWithAi() {
        setLoading(true);

        if (routePreferences.hasDestinationCoordinates()
                && routePreferences.getStartDate() != null
                && !routePreferences.getStartDate().trim().isEmpty()) {

            openMeteoRepository.getDailyForecast(
                    routePreferences.getDestinationLatitude(),
                    routePreferences.getDestinationLongitude(),
                    routePreferences.getStartDate(),
                    routePreferences.getDurationDays(),
                    new OpenMeteoRepository.WeatherCallback() {
                        @Override
                        public void onSuccess(WeatherForecastSummary summary) {
                            if (!isAdded()) {
                                return;
                            }

                            callAiWithWeather(summary);
                        }

                        @Override
                        public void onError(Exception exception) {
                            if (!isAdded()) {
                                return;
                            }

                            // Weather is useful, but should not block itinerary generation.
                            callAiWithWeather(null);
                        }
                    }
            );

        } else {
            callAiWithWeather(null);
        }
    }

    private void callAiWithWeather(WeatherForecastSummary weatherSummary) {
        currentWeatherSummary = weatherSummary;
        displayWeatherSummary(currentWeatherSummary);
        travelPathAiRepository.generateRoutes(
                routePreferences,
                weatherSummary,
                new TravelPathAiRepository.GenerateRoutesCallback() {
                    @Override
                    public void onSuccess(List<RouteOption> routes) {
                        Log.d(TAG, "AI success. Routes count: " + (routes == null ? 0 : routes.size()));

                        if (!isAdded()) {
                            return;
                        }

                        setLoading(false);

                        if (routes == null || routes.isEmpty()) {
                            showErrorState("Aucun parcours n'a pu être généré. Veuillez réessayer.");
                            return;
                        }

                        for (RouteOption route : routes) {
                            route.setDestination(routePreferences.getDestination());
                        }

                        fetchImagesForRoutes(routes);
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "AI generation failed", exception);

                        if (!isAdded()) {
                            return;
                        }

                        setLoading(false);
                        showErrorState("Service de génération indisponible pour le moment. Veuillez réessayer plus tard.");
                    }
                }
        );
    }

    private void fetchImagesForRoutes(List<RouteOption> routes) {
        if (routes == null || routes.isEmpty()) {
            showErrorState("Aucun parcours n'a pu être généré. Veuillez réessayer.");
            return;
        }

        final int[] completed = {0};

        for (int i = 0; i < routes.size(); i++) {
            RouteOption route = routes.get(i);
            int imagePage = i + 1;
            String query = buildUnsplashQuery(route);

            unsplashRepository.searchImage(query, imagePage, new UnsplashRepository.UnsplashImageCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    if (!isAdded()) {
                        return;
                    }

                    route.setImageUrl(imageUrl);
                    completed[0]++;

                    if (completed[0] >= routes.size()) {
                        generatedRoutes.clear();
                        generatedRoutes.addAll(routes);
                        showGeneratedRoutes(generatedRoutes);
                    }
                }

                @Override
                public void onError(Exception exception) {
                    if (!isAdded()) {
                        return;
                    }

                    String fallbackQuery = route.getDestination() + " travel";

                    unsplashRepository.searchImage(fallbackQuery, imagePage, new UnsplashRepository.UnsplashImageCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            if (!isAdded()) {
                                return;
                            }

                            route.setImageUrl(imageUrl);
                            completed[0]++;

                            if (completed[0] >= routes.size()) {
                                generatedRoutes.clear();
                                generatedRoutes.addAll(routes);
                                showGeneratedRoutes(generatedRoutes);
                            }
                        }

                        @Override
                        public void onError(Exception fallbackException) {
                            if (!isAdded()) {
                                return;
                            }

                            completed[0]++;

                            if (completed[0] >= routes.size()) {
                                generatedRoutes.clear();
                                generatedRoutes.addAll(routes);
                                showGeneratedRoutes(generatedRoutes);
                            }
                        }
                    });
                }
            });
        }
    }

    private String buildUnsplashQuery(RouteOption route) {
        String destination = route.getDestination() != null
                ? route.getDestination()
                : routePreferences.getDestination();

        String summary = route.getSummary() == null ? "" : route.getSummary();

        if (summary.toLowerCase().contains("nature")) {
            return destination + " nature travel";
        }

        if (summary.toLowerCase().contains("musée")
                || summary.toLowerCase().contains("culture")
                || summary.toLowerCase().contains("monument")) {
            return destination + " architecture museum travel";
        }

        if (summary.toLowerCase().contains("gastronomie")
                || summary.toLowerCase().contains("restaurant")) {
            return destination + " food city travel";
        }

        return destination + " travel city";
    }

    private void displayWeatherSummary(WeatherForecastSummary weatherSummary) {
        if (textWeatherSummary == null) {
            return;
        }

        if (weatherSummary == null || !weatherSummary.isAvailable()
                || weatherSummary.getSummaryText() == null
                || weatherSummary.getSummaryText().trim().isEmpty()) {
            layoutWeatherSummary.setVisibility(View.GONE);
            return;
        }

        layoutWeatherSummary.setVisibility(View.VISIBLE);
        textWeatherSummary.setText("Météo prévue prise en compte :\n" + weatherSummary.getSummaryText());
    }

    private void showErrorState(String message) {
        if (layoutWeatherSummary != null) {
            layoutWeatherSummary.setVisibility(View.GONE);
        }
        routeOptionAdapter.submitList(new ArrayList<>());
        recyclerRouteOptions.setVisibility(View.GONE);
        textRouteStatus.setVisibility(View.VISIBLE);
        textRouteStatus.setText(message);
    }

    private void setupBackButton(View view) {
        ImageButton buttonBack = view.findViewById(R.id.buttonBackOptions);
        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    private void setupRecycler() {
        routeOptionAdapter = new RouteOptionAdapter(routeOption -> openRouteDetail(routeOption));

        recyclerRouteOptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRouteOptions.setAdapter(routeOptionAdapter);
    }

    private void showInitialUnavailableState() {
        setLoading(false);

        List<RouteOption> emptyRoutes = new ArrayList<>();
        routeOptionAdapter.submitList(emptyRoutes);

        textRouteStatus.setVisibility(View.VISIBLE);
        textRouteStatus.setText("Le service de génération intelligente sera connecté dans l'étape suivante.");
    }

    private void setLoading(boolean loading) {
        progressRouteGeneration.setVisibility(loading ? View.VISIBLE : View.GONE);
        recyclerRouteOptions.setVisibility(loading ? View.GONE : View.VISIBLE);
        textRouteStatus.setVisibility(View.VISIBLE);

        if (layoutWeatherSummary != null && loading) {
            layoutWeatherSummary.setVisibility(View.GONE);
        }

        if (buttonRegenerateRoutes != null) {
            buttonRegenerateRoutes.setEnabled(!loading);
        }

        if (loading) {
            textRouteStatus.setText("Génération des parcours...");
        }
    }

    private void openRouteDetail(RouteOption routeOption) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("routeOption", routeOption);

        RouteDetailFragment fragment = new RouteDetailFragment();
        fragment.setArguments(bundle);

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).openFragmentWithBackStack(fragment);
        }
    }
}