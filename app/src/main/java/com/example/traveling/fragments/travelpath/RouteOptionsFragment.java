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
public class RouteOptionsFragment extends Fragment {
    private ArrayList<RouteOption> generatedRoutes = new ArrayList<>();
    private RoutePreferences routePreferences;
    private TravelPathAiRepository travelPathAiRepository;
    private TextView textRouteOptionsTitle;
    private TextView textRouteStatus;
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

        travelPathAiRepository = new TravelPathAiRepository();

        if (!generatedRoutes.isEmpty()) {
            showGeneratedRoutes(generatedRoutes);
        } else {
            generateRoutesWithAi();
        }
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
        progressRouteGeneration = view.findViewById(R.id.progressRouteGeneration);
        recyclerRouteOptions = view.findViewById(R.id.recyclerRouteOptions);

        textRouteOptionsTitle.setText("Options pour " + routePreferences.getDestination());
    }

    private void generateRoutesWithAi() {
        setLoading(true);

        travelPathAiRepository.generateRoutes(routePreferences, new TravelPathAiRepository.GenerateRoutesCallback() {
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

                generatedRoutes.clear();
                generatedRoutes.addAll(routes);

                showGeneratedRoutes(generatedRoutes);
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
        });
    }

    private void showErrorState(String message) {
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