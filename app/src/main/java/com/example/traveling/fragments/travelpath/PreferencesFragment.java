package com.example.traveling.fragments.travelpath;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.models.RoutePreferences;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.widget.Filter;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.traveling.models.LocationSuggestion;
import com.example.traveling.repositories.PhotonRepository;

public class PreferencesFragment extends Fragment {
    private PhotonRepository photonRepository;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingDestinationSearch;
    private LocationSuggestion selectedDestinationSuggestion;
    private boolean isSelectingDestination = false;

    private AutoCompleteTextView dropdownDestination;
    private TextInputEditText inputMustSeePlaces;

    private ChipGroup chipGroupActivities;

    private AutoCompleteTextView dropdownBudget;
    private AutoCompleteTextView dropdownDuration;
    private AutoCompleteTextView dropdownEffort;
    private AutoCompleteTextView dropdownSeason;

    private SwitchMaterial switchAvoidRain;
    private SwitchMaterial switchAvoidHeat;
    private SwitchMaterial switchAvoidCold;
    private SwitchMaterial switchAvoidHumidity;

    private MaterialButton buttonGenerateRoute;

    public PreferencesFragment() {
        // Required empty public constructor
    }

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preferences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupDropdowns();
        setupDestinationAutocomplete();
        setupGenerateButton();
    }

    private void bindViews(View view) {
        dropdownDestination = view.findViewById(R.id.dropdownDestination);
        inputMustSeePlaces = view.findViewById(R.id.inputMustSeePlaces);
        chipGroupActivities = view.findViewById(R.id.chipGroupActivities);

        dropdownBudget = view.findViewById(R.id.dropdownBudget);
        dropdownDuration = view.findViewById(R.id.dropdownDuration);
        dropdownEffort = view.findViewById(R.id.dropdownEffort);
        dropdownSeason = view.findViewById(R.id.dropdownSeason);

        switchAvoidRain = view.findViewById(R.id.switchAvoidRain);
        switchAvoidHeat = view.findViewById(R.id.switchAvoidHeat);
        switchAvoidCold = view.findViewById(R.id.switchAvoidCold);
        switchAvoidHumidity = view.findViewById(R.id.switchAvoidHumidity);

        buttonGenerateRoute = view.findViewById(R.id.buttonGenerateRoute);
    }

    private void setupDropdowns() {
        setDropdown(dropdownBudget, Arrays.asList(
                "Économique",
                "Modéré",
                "Confort",
                "Luxe"
        ), "Modéré");

        setDropdown(dropdownDuration, Arrays.asList(
                "1 jour",
                "2 jours",
                "3 jours",
                "5 jours",
                "7 jours"
        ), "1 jour");

        setDropdown(dropdownEffort, Arrays.asList(
                "Faible",
                "Modéré",
                "Intense"
        ), "Modéré");

        setDropdown(dropdownSeason, Arrays.asList(
                "Peu importe",
                "Printemps",
                "Été",
                "Automne",
                "Hiver"
        ), "Peu importe");
    }

    private void setDropdown(AutoCompleteTextView dropdown, List<String> values, String defaultValue) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(values)
        ) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = values;
                        results.count = values.size();
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        clear();
                        addAll(values);
                        notifyDataSetChanged();
                    }
                };
            }
        };

        dropdown.setAdapter(adapter);
        dropdown.setText(defaultValue, false);
        dropdown.setInputType(0);
        dropdown.setKeyListener(null);
        dropdown.setThreshold(0);

        dropdown.setOnClickListener(v -> dropdown.showDropDown());

        dropdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                dropdown.showDropDown();
            }
        });
    }

    private void setupDestinationAutocomplete() {
        photonRepository = new PhotonRepository();

        dropdownDestination.setThreshold(3);

        dropdownDestination.setOnItemClickListener((parent, view, position, id) -> {
            LocationSuggestion suggestion = (LocationSuggestion) parent.getItemAtPosition(position);

            isSelectingDestination = true;
            selectedDestinationSuggestion = suggestion;
            dropdownDestination.setText(suggestion.getDisplayName(), false);
            dropdownDestination.clearFocus();
            isSelectingDestination = false;
        });

        dropdownDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSelectingDestination) {
                    return;
                }

                selectedDestinationSuggestion = null;

                String query = s == null ? "" : s.toString().trim();

                if (pendingDestinationSearch != null) {
                    searchHandler.removeCallbacks(pendingDestinationSearch);
                }

                if (query.length() < 3) {
                    return;
                }

                pendingDestinationSearch = () -> searchDestination(query);
                searchHandler.postDelayed(pendingDestinationSearch, 450);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void searchDestination(String query) {
        photonRepository.searchLocations(query, new PhotonRepository.OnLocationSuggestionsLoadedListener() {
            @Override
            public void onSuccess(List<LocationSuggestion> suggestions) {
                if (!isAdded()) {
                    return;
                }

                ArrayAdapter<LocationSuggestion> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        suggestions
                );

                dropdownDestination.setAdapter(adapter);

                if (!suggestions.isEmpty() && dropdownDestination.hasFocus()) {
                    dropdownDestination.showDropDown();
                }
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }

                // Silent failure: user can still type a destination manually.
            }
        });
    }

    private void setupGenerateButton() {
        buttonGenerateRoute.setOnClickListener(v -> {
            RoutePreferences preferences = buildPreferencesFromForm();

            if (preferences == null) {
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("routePreferences", preferences);

            RouteOptionsFragment fragment = new RouteOptionsFragment();
            fragment.setArguments(bundle);

            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).openFragmentWithBackStack(fragment);
            }
        });
    }

    @Nullable
    private RoutePreferences buildPreferencesFromForm() {
        String destination = dropdownDestination.getText().toString().trim();

        if (TextUtils.isEmpty(destination)) {
            Toast.makeText(requireContext(), "Veuillez indiquer une destination.", Toast.LENGTH_SHORT).show();
            return null;
        }

        List<String> activities = getSelectedActivities();

        if (activities.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez sélectionner au moins une activité.", Toast.LENGTH_SHORT).show();
            return null;
        }

        RoutePreferences preferences = new RoutePreferences();
        preferences.setDestination(destination);
        preferences.setActivities(activities);
        preferences.setBudgetLevel(dropdownBudget.getText().toString().trim());
        preferences.setDurationDays(parseDurationDays(dropdownDuration.getText().toString()));
        preferences.setEffortLevel(dropdownEffort.getText().toString().trim());
        preferences.setPreferredSeason(dropdownSeason.getText().toString().trim());

        preferences.setAvoidRain(switchAvoidRain.isChecked());
        preferences.setAvoidHeat(switchAvoidHeat.isChecked());
        preferences.setAvoidCold(switchAvoidCold.isChecked());
        preferences.setAvoidHumidity(switchAvoidHumidity.isChecked());

        preferences.setMustSeePlaces(parseCommaSeparatedPlaces(getText(inputMustSeePlaces)));

        return preferences;
    }

    private List<String> getSelectedActivities() {
        List<String> selectedActivities = new ArrayList<>();

        for (int i = 0; i < chipGroupActivities.getChildCount(); i++) {
            View child = chipGroupActivities.getChildAt(i);

            if (child instanceof Chip) {
                Chip chip = (Chip) child;

                if (chip.isChecked()) {
                    selectedActivities.add(chip.getText().toString());
                }
            }
        }

        return selectedActivities;
    }

    private List<String> parseCommaSeparatedPlaces(String rawText) {
        List<String> places = new ArrayList<>();

        if (TextUtils.isEmpty(rawText)) {
            return places;
        }

        String[] parts = rawText.split(",");

        for (String part : parts) {
            String clean = part.trim();

            if (!clean.isEmpty()) {
                places.add(clean);
            }
        }

        return places;
    }

    private int parseDurationDays(String durationText) {
        if (durationText == null) {
            return 1;
        }

        String digitsOnly = durationText.replaceAll("[^0-9]", "");

        if (digitsOnly.isEmpty()) {
            return 1;
        }

        try {
            return Integer.parseInt(digitsOnly);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String getText(TextInputEditText input) {
        if (input.getText() == null) {
            return "";
        }

        return input.getText().toString().trim();
    }
}