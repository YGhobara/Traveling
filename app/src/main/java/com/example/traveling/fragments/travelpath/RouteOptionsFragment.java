package com.example.traveling.fragments.travelpath;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.models.RoutePreferences;

public class RouteOptionsFragment extends Fragment {

    private RoutePreferences routePreferences;

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

        ImageButton buttonBack = view.findViewById(R.id.buttonBackOptions);
        buttonBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Temporary visual confirmation. We will replace this with route cards next.
        TextView title = view.findViewById(R.id.textRouteOptionsTitle);

        if (title != null) {
            title.setText("Options pour " + routePreferences.getDestination());
        }
    }
}