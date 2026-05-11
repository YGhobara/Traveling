package com.example.traveling.fragments.travelpath;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.models.RouteOption;

import java.util.Locale;

public class RouteDetailFragment extends Fragment {

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
    }

    private void setupBackButton(View view) {
        ImageButton buttonBack = view.findViewById(R.id.buttonBackRouteDetail);

        if (buttonBack != null) {
            buttonBack.setOnClickListener(v ->
                    requireActivity().getSupportFragmentManager().popBackStack()
            );
        }
    }

    private void bindRoute(View view) {
        TextView textTitle = view.findViewById(R.id.textRouteDetailTitle);
        TextView textSummary = view.findViewById(R.id.textRouteDetailSummary);
        TextView textBudget = view.findViewById(R.id.textRouteDetailBudget);
        TextView textDuration = view.findViewById(R.id.textRouteDetailDuration);
        TextView textEffort = view.findViewById(R.id.textRouteDetailEffort);

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