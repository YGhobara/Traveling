package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.app.DatePickerDialog;
import android.widget.ImageButton;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import com.example.traveling.R;
import com.example.traveling.activities.MainActivity;
import com.example.traveling.adapters.PostGridAdapter;
import com.example.traveling.adapters.PostMapInfoWindow;
import com.example.traveling.models.Post;
import com.example.traveling.repositories.PostRepository;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import com.example.traveling.models.LocationSuggestion;
import com.example.traveling.repositories.PhotonRepository;
import com.example.traveling.repositories.FollowRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import android.widget.ArrayAdapter;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerSearchPosts;
    private TextView textSearchStatus;
    private TextView tabPublications;
    private TextView tabMap;
    private MapView mapSearch;
    private MaterialButton buttonFollowPlaceType;
    private boolean isFollowingSelectedPlaceType = false;
    private boolean isMapSelected = false;

    private PostRepository postRepository;
    private FollowRepository followRepository;
    private FirebaseAuth firebaseAuth;
    private PostGridAdapter postGridAdapter;

    private final List<Post> allPosts = new ArrayList<>();

    private static final int PAGE_SIZE = 20;

    private DocumentSnapshot lastVisibleDocument = null;
    private boolean isLoading = false;
    private boolean hasMorePosts = true;

    private TextInputEditText editTextSearch;
    private ChipGroup chipGroupPlaceTypes;

    private final List<Post> visiblePosts = new ArrayList<>();
    private String currentSearchQuery = "";
    private String selectedPlaceType = "Tous";

    private ImageButton buttonFilters;

    private Long selectedStartDate = null;
    private Long selectedEndDate = null;
    private String selectedPeriodLabel = "Toutes périodes";

    private PhotonRepository photonRepository;

    private LocationSuggestion selectedRadiusLocation = null;
    private double selectedRadiusKm = 0.0;
    private String radiusFilterLabel = "";

    public SearchFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        postRepository = new PostRepository();
        photonRepository = new PhotonRepository();
        followRepository = new FollowRepository();
        firebaseAuth = FirebaseAuth.getInstance();

        bindViews(view);
        setupRecycler();
        setupMap();
        setupTabs();
        setupSearchInput();
        setupPlaceTypeChips();
        setupFollowPlaceTypeButton();
        setupFilterButton();
        loadFirstPage();

        return view;
    }

    private void bindViews(View view) {
        recyclerSearchPosts = view.findViewById(R.id.recyclerSearchPosts);
        textSearchStatus = view.findViewById(R.id.textSearchStatus);
        tabPublications = view.findViewById(R.id.tabPublications);
        tabMap = view.findViewById(R.id.tabMap);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        chipGroupPlaceTypes = view.findViewById(R.id.chipGroupPlaceTypes);
        buttonFilters = view.findViewById(R.id.buttonFilters);
        mapSearch = view.findViewById(R.id.mapSearch);
        buttonFollowPlaceType = view.findViewById(R.id.buttonFollowPlaceType);
    }

    private void setupFollowPlaceTypeButton() {
        buttonFollowPlaceType.setOnClickListener(v -> toggleFollowSelectedPlaceType());
        updateFollowPlaceTypeButtonVisibility();
    }

    private void setupMap() {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapSearch.setTileSource(TileSourceFactory.MAPNIK);
        mapSearch.setMultiTouchControls(true);

        GeoPoint defaultPoint = new GeoPoint(48.8566, 2.3522); // Paris default
        mapSearch.getController().setZoom(5.5);
        mapSearch.getController().setCenter(defaultPoint);
    }

    private void setupFilterButton() {
        buttonFilters.setOnClickListener(v -> showPeriodFilterDialog());
    }

    private void showPeriodFilterDialog() {
        String[] options = {
                "Période : toutes périodes",
                "Période : aujourd’hui",
                "Période : cette semaine",
                "Période : ce mois-ci",
                "Période : plage personnalisée",
                "Localisation : autour d’un lieu",
                "Réinitialiser les filtres"
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtres de recherche")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        selectedPeriodLabel = "Toutes périodes";
                        selectedStartDate = null;
                        selectedEndDate = null;
                        loadFirstPage();

                    } else if (which == 1) {
                        selectedPeriodLabel = "Aujourd’hui";
                        setTodayRange();
                        loadFirstPage();

                    } else if (which == 2) {
                        selectedPeriodLabel = "Cette semaine";
                        setThisWeekRange();
                        loadFirstPage();

                    } else if (which == 3) {
                        selectedPeriodLabel = "Ce mois-ci";
                        setThisMonthRange();
                        loadFirstPage();

                    } else if (which == 4) {
                        showCustomStartDatePicker();

                    } else if (which == 5) {
                        showAroundLocationDialog();

                    } else if (which == 6) {
                        resetFilters();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void resetFilters() {
        selectedPeriodLabel = "Toutes périodes";
        selectedStartDate = null;
        selectedEndDate = null;

        selectedRadiusLocation = null;
        selectedRadiusKm = 0.0;
        radiusFilterLabel = "";

        selectedPlaceType = "Tous";
        chipGroupPlaceTypes.check(R.id.chipAll);

        editTextSearch.setText("");

        loadFirstPage();
    }

    private void showAroundLocationDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_radius_filter, null);

        MaterialAutoCompleteTextView dropdownRadiusLocation =
                dialogView.findViewById(R.id.dropdownRadiusLocation);

        MaterialAutoCompleteTextView dropdownRadius =
                dialogView.findViewById(R.id.dropdownRadius);

        ArrayAdapter<LocationSuggestion> locationAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>()
        );

        dropdownRadiusLocation.setAdapter(locationAdapter);
        dropdownRadiusLocation.setThreshold(3);

        final LocationSuggestion[] pickedLocation = new LocationSuggestion[1];
        final boolean[] isSettingRadiusLocationFromSuggestion = {false};
        final ArrayAdapter<LocationSuggestion>[] currentLocationAdapter = new ArrayAdapter[]{locationAdapter};
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable[] pendingSearch = new Runnable[1];

        dropdownRadiusLocation.setOnItemClickListener((parent, view, position, id) -> {
            ArrayAdapter<LocationSuggestion> adapter = currentLocationAdapter[0];

            if (adapter == null || position < 0 || position >= adapter.getCount()) {
                return;
            }

            LocationSuggestion suggestion = adapter.getItem(position);

            if (suggestion != null) {
                pickedLocation[0] = suggestion;

                isSettingRadiusLocationFromSuggestion[0] = true;
                dropdownRadiusLocation.setText(suggestion.getDisplayName(), false);
                dropdownRadiusLocation.dismissDropDown();
                if (pendingSearch[0] != null) {
                    handler.removeCallbacks(pendingSearch[0]);
                }

                dropdownRadiusLocation.clearFocus();
                isSettingRadiusLocationFromSuggestion[0] = false;

                dropdownRadiusLocation.setError(null);
            }
        });

        dropdownRadiusLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isSettingRadiusLocationFromSuggestion[0]) {
                    return;
                }

                pickedLocation[0] = null;

                String query = s == null ? "" : s.toString().trim();

                if (pendingSearch[0] != null) {
                    handler.removeCallbacks(pendingSearch[0]);
                }

                if (query.length() < 3) {
                    locationAdapter.clear();
                    locationAdapter.notifyDataSetChanged();
                    return;
                }

                pendingSearch[0] = () -> photonRepository.searchLocations(
                        query,
                        new PhotonRepository.OnLocationSuggestionsLoadedListener() {
                            @Override
                            public void onSuccess(List<LocationSuggestion> suggestions) {
                                if (!isAdded()) return;

                                ArrayAdapter<LocationSuggestion> freshAdapter = new ArrayAdapter<>(
                                        requireContext(),
                                        android.R.layout.simple_dropdown_item_1line,
                                        suggestions
                                );
                                currentLocationAdapter[0] = freshAdapter;
                                dropdownRadiusLocation.setAdapter(freshAdapter);

                                dropdownRadiusLocation.postDelayed(() -> {
                                    if (!isSettingRadiusLocationFromSuggestion[0]
                                            && pickedLocation[0] == null
                                            && dropdownRadiusLocation.hasFocus()
                                            && !suggestions.isEmpty()) {
                                        dropdownRadiusLocation.dismissDropDown();
                                        dropdownRadiusLocation.showDropDown();
                                    }
                                }, 100);
                            }

                            @Override
                            public void onError(Exception exception) {
                                if (!isAdded()) return;
                            }
                        });

                handler.postDelayed(pendingSearch[0], 350);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        String[] radiusOptions = {"1 km", "5 km", "10 km", "25 km", "50 km"};

        ArrayAdapter<String> radiusAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                radiusOptions
        );

        dropdownRadius.setAdapter(radiusAdapter);
        dropdownRadius.setText("10 km", false);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Autour d’un lieu")
                .setView(dialogView)
                .setPositiveButton("Appliquer", (dialog, which) -> {
                    if (pickedLocation[0] == null) {
                        Toast.makeText(requireContext(),
                                "Sélectionnez un lieu dans les suggestions.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    selectedRadiusLocation = pickedLocation[0];
                    selectedRadiusKm = parseRadius(dropdownRadius.getText().toString());
                    radiusFilterLabel = "Autour de " + selectedRadiusLocation.getDisplayName();

                    loadFirstPage();
                })
                .setNegativeButton("Annuler", null)
                .setNeutralButton("Réinitialiser", (dialog, which) -> {
                    selectedRadiusLocation = null;
                    selectedRadiusKm = 0.0;
                    radiusFilterLabel = "";
                    loadFirstPage();
                })
                .show();
    }

    private int getSelectedPeriodIndex() {
        if ("Aujourd’hui".equals(selectedPeriodLabel)) return 1;
        if ("Cette semaine".equals(selectedPeriodLabel)) return 2;
        if ("Ce mois-ci".equals(selectedPeriodLabel)) return 3;
        if ("Plage personnalisée".equals(selectedPeriodLabel)) return 4;
        return 0;
    }

    private void setTodayRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        selectedStartDate = start.getTimeInMillis();
        selectedEndDate = end.getTimeInMillis();
    }

    private void setThisWeekRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        selectedStartDate = start.getTimeInMillis();
        selectedEndDate = end.getTimeInMillis();
    }

    private void setThisMonthRange() {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);

        selectedStartDate = start.getTimeInMillis();
        selectedEndDate = end.getTimeInMillis();
    }

    private void showCustomStartDatePicker() {
        Calendar now = Calendar.getInstance();

        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar start = Calendar.getInstance();
                    start.set(year, month, dayOfMonth, 0, 0, 0);
                    start.set(Calendar.MILLISECOND, 0);

                    selectedStartDate = start.getTimeInMillis();
                    showCustomEndDatePicker();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showCustomEndDatePicker() {
        Calendar now = Calendar.getInstance();

        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar end = Calendar.getInstance();
                    end.set(year, month, dayOfMonth, 23, 59, 59);
                    end.set(Calendar.MILLISECOND, 999);

                    selectedEndDate = end.getTimeInMillis();
                    selectedPeriodLabel = "Plage personnalisée";

                    loadFirstPage();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setupRecycler() {
        postGridAdapter = new PostGridAdapter(this::openPostDetail);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerSearchPosts.setLayoutManager(layoutManager);
        recyclerSearchPosts.setAdapter(postGridAdapter);

        recyclerSearchPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                boolean nearBottom = visibleItemCount + firstVisibleItemPosition >= totalItemCount - 4;

                if (nearBottom) {
                    loadNextPage();
                }
            }
        });
    }

    private void setupTabs() {
        tabPublications.setOnClickListener(v -> selectPublicationsTab());
        tabMap.setOnClickListener(v -> selectMapTab());

        selectPublicationsTab();
    }

    private void setupSearchInput() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s == null ? "" : s.toString().trim().toLowerCase();
                applyLocalFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupPlaceTypeChips() {
        chipGroupPlaceTypes.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                selectedPlaceType = "Tous";
                loadFirstPage();
                updateFollowPlaceTypeButtonVisibility();
                return;
            }

            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipNature) {
                selectedPlaceType = "Nature";
            } else if (checkedId == R.id.chipMuseum) {
                selectedPlaceType = "Musée";
            } else if (checkedId == R.id.chipMonument) {
                selectedPlaceType = "Monument";
            } else if (checkedId == R.id.chipStreet) {
                selectedPlaceType = "Rue";
            } else if (checkedId == R.id.chipRestaurant) {
                selectedPlaceType = "Restaurant";
            } else if (checkedId == R.id.chipShop) {
                selectedPlaceType = "Magasin";
            } else if (checkedId == R.id.chipBeach) {
                selectedPlaceType = "Plage";
            } else if (checkedId == R.id.chipMountain) {
                selectedPlaceType = "Montagne";
            } else if (checkedId == R.id.chipCity) {
                selectedPlaceType = "Ville";
            } else if (checkedId == R.id.chipOther) {
                selectedPlaceType = "Autre";
            } else {
                selectedPlaceType = "Tous";
            }

            loadFirstPage();
            updateFollowPlaceTypeButtonVisibility();
        });
    }

    private void updateFollowPlaceTypeButtonVisibility() {
        if ("Tous".equals(selectedPlaceType)) {
            buttonFollowPlaceType.setVisibility(View.GONE);
            return;
        }

        buttonFollowPlaceType.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            buttonFollowPlaceType.setText("Se connecter pour suivre " + selectedPlaceType);
            buttonFollowPlaceType.setEnabled(false);
            return;
        }

        buttonFollowPlaceType.setEnabled(false);
        buttonFollowPlaceType.setText("Chargement...");

        followRepository.isFollowingPlaceType(
                currentUser.getUid(),
                selectedPlaceType,
                new FollowRepository.FollowCheckListener() {
                    @Override
                    public void onResult(boolean isFollowing) {
                        if (!isAdded()) return;

                        isFollowingSelectedPlaceType = isFollowing;
                        updateFollowPlaceTypeButtonText();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (!isAdded()) return;

                        isFollowingSelectedPlaceType = false;
                        updateFollowPlaceTypeButtonText();
                    }
                }
        );
    }

    private void updateFollowPlaceTypeButtonText() {
        buttonFollowPlaceType.setEnabled(true);

        if (isFollowingSelectedPlaceType) {
            buttonFollowPlaceType.setText("Suivi : " + selectedPlaceType);
        } else {
            buttonFollowPlaceType.setText("Suivre : " + selectedPlaceType);
        }
    }

    private void toggleFollowSelectedPlaceType() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(),
                    "Connectez-vous pour suivre un type de lieu.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Tous".equals(selectedPlaceType) || TextUtils.isEmpty(selectedPlaceType)) {
            return;
        }

        buttonFollowPlaceType.setEnabled(false);

        if (isFollowingSelectedPlaceType) {
            followRepository.unfollowPlaceType(
                    currentUser.getUid(),
                    selectedPlaceType,
                    new FollowRepository.FollowActionListener() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;

                            isFollowingSelectedPlaceType = false;
                            updateFollowPlaceTypeButtonText();

                            Toast.makeText(requireContext(),
                                    "Type retiré des abonnements.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            if (!isAdded()) return;

                            updateFollowPlaceTypeButtonText();

                            Toast.makeText(requireContext(),
                                    "Erreur : " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            followRepository.followPlaceType(
                    currentUser.getUid(),
                    selectedPlaceType,
                    new FollowRepository.FollowActionListener() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;

                            isFollowingSelectedPlaceType = true;
                            updateFollowPlaceTypeButtonText();

                            Toast.makeText(requireContext(),
                                    "Type suivi.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            if (!isAdded()) return;

                            updateFollowPlaceTypeButtonText();

                            Toast.makeText(requireContext(),
                                    "Erreur : " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
    }

    private void applyLocalFilters() {
        visiblePosts.clear();

        for (Post post : allPosts) {
            if (matchesSearch(post) && matchesRadius(post)) {
                visiblePosts.add(post);
            }
        }

        postGridAdapter.submitList(visiblePosts);

        if (visiblePosts.isEmpty()) {
            textSearchStatus.setText("Aucun résultat trouvé.");
        } else {
            textSearchStatus.setText(visiblePosts.size() + " résultat(s)");
        }

        if (isMapSelected) {
            updateMapMarkers();
        }
    }

    private boolean matchesSearch(Post post) {
        if (TextUtils.isEmpty(currentSearchQuery)) {
            return true;
        }

        String caption = safe(post.getCaption()).toLowerCase();
        String location = safe(post.getLocationName()).toLowerCase();
        String author = safe(post.getAuthorName()).toLowerCase();
        String placeType = safe(post.getPlaceType()).toLowerCase();

        return caption.contains(currentSearchQuery)
                || location.contains(currentSearchQuery)
                || author.contains(currentSearchQuery)
                || placeType.contains(currentSearchQuery);
    }

    private boolean matchesRadius(Post post) {
        if (selectedRadiusLocation == null || selectedRadiusKm <= 0) {
            return true;
        }

        if (!hasValidCoordinates(post)) {
            return false;
        }

        double distanceKm = distanceKm(
                selectedRadiusLocation.getLatitude(),
                selectedRadiusLocation.getLongitude(),
                post.getLatitude(),
                post.getLongitude()
        );

        return distanceKm <= selectedRadiusKm;
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void selectPublicationsTab() {
        isMapSelected = false;

        tabPublications.setTextColor(android.graphics.Color.WHITE);
        tabPublications.setTypeface(null, android.graphics.Typeface.BOLD);
        tabPublications.setBackgroundResource(R.drawable.bg_segment_selected);

        tabMap.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabMap.setBackgroundResource(R.drawable.bg_segment_unselected);

        recyclerSearchPosts.setVisibility(View.VISIBLE);
        mapSearch.setVisibility(View.GONE);

        updatePublicationStatus();
    }

    private void updatePublicationStatus() {
        String suffix = "";

        if (!TextUtils.isEmpty(radiusFilterLabel)) {
            suffix = " · " + radiusFilterLabel + " (" + formatRadius(selectedRadiusKm) + ")";
        }

        if (visiblePosts.isEmpty()) {
            textSearchStatus.setText("Aucun résultat trouvé" + suffix + ".");
        } else {
            textSearchStatus.setText(visiblePosts.size() + " résultat(s)" + suffix);
        }
    }

    private String formatRadius(double radiusKm) {
        if (radiusKm <= 0) return "";

        if (radiusKm == Math.floor(radiusKm)) {
            return String.format(Locale.FRANCE, "%.0f km", radiusKm);
        }

        return String.format(Locale.FRANCE, "%.1f km", radiusKm);
    }

    private double parseRadius(String radiusText) {
        if (radiusText == null) {
            return 10.0;
        }

        String cleaned = radiusText
                .replace("km", "")
                .replace(",", ".")
                .trim();

        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException exception) {
            return 10.0;
        }
    }

    private void selectMapTab() {
        isMapSelected = true;

        tabMap.setTextColor(android.graphics.Color.WHITE);
        tabMap.setTypeface(null, android.graphics.Typeface.BOLD);
        tabMap.setBackgroundResource(R.drawable.bg_segment_selected);

        tabPublications.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        tabPublications.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabPublications.setBackgroundResource(R.drawable.bg_segment_unselected);

        recyclerSearchPosts.setVisibility(View.GONE);
        mapSearch.setVisibility(View.VISIBLE);

        updateMapMarkers();
    }

    private void updateMapMarkers() {
        if (mapSearch == null) return;

        mapSearch.getOverlays().clear();

        GeoPoint firstPoint = null;
        int markerCount = 0;

        for (Post post : visiblePosts) {
            if (!hasValidCoordinates(post)) {
                continue;
            }

            GeoPoint point = new GeoPoint(post.getLatitude(), post.getLongitude());

            Marker marker = new Marker(mapSearch);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(safe(post.getLocationName()));
            marker.setSnippet(safe(post.getCaption()));
            marker.setInfoWindow(new PostMapInfoWindow(mapSearch, post, this::openPostDetail));

            marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                if (clickedMarker.isInfoWindowShown()) {
                    clickedMarker.closeInfoWindow();
                } else {
                    clickedMarker.showInfoWindow();
                    mapView.getController().animateTo(clickedMarker.getPosition());
                }

                return true;
            });

            mapSearch.getOverlays().add(marker);

            if (firstPoint == null) {
                firstPoint = point;
            }

            markerCount++;
        }

        if (firstPoint != null) {
            mapSearch.getController().animateTo(firstPoint);
            mapSearch.getController().setZoom(markerCount == 1 ? 10.5 : 5.5);
        }

        mapSearch.invalidate();

        if (isMapSelected) {
            if (markerCount == 0) {
                textSearchStatus.setText("Aucune publication avec coordonnées pour cette recherche.");
            } else {
                textSearchStatus.setText(markerCount + " point(s) sur la carte");
            }
        }
    }

    private boolean hasValidCoordinates(Post post) {
        return post != null
                && !(post.getLatitude() == 0.0 && post.getLongitude() == 0.0);
    }

    private void loadFirstPage() {
        allPosts.clear();

        visiblePosts.clear();
        postGridAdapter.submitList(visiblePosts);

        lastVisibleDocument = null;
        hasMorePosts = true;

        loadNextPage();
    }

    private void loadNextPage() {
        if (isLoading || !hasMorePosts) return;

        isLoading = true;

        if (allPosts.isEmpty()) {
            textSearchStatus.setText("Chargement des publications...");
        } else {
            textSearchStatus.setText(allPosts.size() + " publication(s) chargée(s)...");
        }

        postRepository.getPublicPostsPage(
                lastVisibleDocument,
                PAGE_SIZE,
                selectedPlaceType,
                selectedStartDate,
                selectedEndDate,
                new PostRepository.OnPaginatedPostsLoadedListener() {
                    @Override
                    public void onSuccess(List<Post> posts, DocumentSnapshot newLastVisibleDocument) {
                        if (!isAdded()) return;

                        isLoading = false;

                        if (posts.isEmpty()) {
                            hasMorePosts = false;

                            if (allPosts.isEmpty()) {
                                textSearchStatus.setText("Aucune publication publique pour le moment.");
                            } else {
                                textSearchStatus.setText(allPosts.size() + " publication(s) publique(s)");
                            }

                            return;
                        }

                        allPosts.addAll(posts);
                        lastVisibleDocument = newLastVisibleDocument;

                        if (posts.size() < PAGE_SIZE) {
                            hasMorePosts = false;
                        }

                        applyLocalFilters();
                    }

                    @Override
                    public void onError(Exception exception) {
                        if (!isAdded()) return;

                        isLoading = false;
                        textSearchStatus.setText("Impossible de charger les publications.");

                        Toast.makeText(requireContext(),
                                "Erreur de chargement.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openPostDetail(Post post) {
        PhotoDetailFragment fragment = new PhotoDetailFragment();

        Bundle args = new Bundle();
        args.putString("postId", post.getId());
        args.putString("imageUrl", post.getImageUrl());
        args.putString("locationName", post.getLocationName());
        args.putDouble("latitude", post.getLatitude());
        args.putDouble("longitude", post.getLongitude());
        args.putString("photonPlaceId", post.getPhotonPlaceId());
        args.putString("authorName", post.getAuthorName());
        args.putString("caption", post.getCaption());
        args.putInt("likeCount", post.getLikeCount());
        args.putInt("commentCount", post.getCommentCount());
        args.putString("placeType", post.getPlaceType());
        args.putLong("createdAt", post.getCreatedAt());

        fragment.setArguments(args);

        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).openFragmentWithBackStack(fragment);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapSearch != null) {
            mapSearch.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapSearch != null) {
            mapSearch.onPause();
        }
        super.onPause();
    }
}