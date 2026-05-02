package com.example.traveling.fragments.travelshare;

import android.os.Bundle;
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
import com.example.traveling.models.Post;
import com.example.traveling.repositories.PostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerSearchPosts;
    private TextView textSearchStatus;
    private TextView tabPublications;
    private TextView tabMap;

    private PostRepository postRepository;
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

        bindViews(view);
        setupRecycler();
        setupTabs();
        setupSearchInput();
        setupPlaceTypeChips();
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
    }

    private void setupFilterButton() {
        buttonFilters.setOnClickListener(v -> showPeriodFilterDialog());
    }

    private void showPeriodFilterDialog() {
        String[] options = {
                "Toutes périodes",
                "Aujourd’hui",
                "Cette semaine",
                "Ce mois-ci",
                "Plage personnalisée"
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Filtrer par période")
                .setSingleChoiceItems(options, getSelectedPeriodIndex(), (dialog, which) -> {
                    if (which == 0) {
                        selectedPeriodLabel = "Toutes périodes";
                        selectedStartDate = null;
                        selectedEndDate = null;
                        dialog.dismiss();
                        loadFirstPage();
                    } else if (which == 1) {
                        selectedPeriodLabel = "Aujourd’hui";
                        setTodayRange();
                        dialog.dismiss();
                        loadFirstPage();
                    } else if (which == 2) {
                        selectedPeriodLabel = "Cette semaine";
                        setThisWeekRange();
                        dialog.dismiss();
                        loadFirstPage();
                    } else if (which == 3) {
                        selectedPeriodLabel = "Ce mois-ci";
                        setThisMonthRange();
                        dialog.dismiss();
                        loadFirstPage();
                    } else {
                        dialog.dismiss();
                        showCustomStartDatePicker();
                    }
                })
                .setNegativeButton("Annuler", null)
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
        tabPublications.setOnClickListener(v -> {
            selectPublicationsTab();
        });

        tabMap.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Vue carte à venir.",
                    Toast.LENGTH_SHORT).show();
        });

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
        });
    }

    private void applyLocalFilters() {
        visiblePosts.clear();

        for (Post post : allPosts) {
            if (matchesSearch(post)) {
                visiblePosts.add(post);
            }
        }

        postGridAdapter.submitList(visiblePosts);

        if (visiblePosts.isEmpty()) {
            textSearchStatus.setText("Aucun résultat trouvé.");
        } else {
            textSearchStatus.setText(visiblePosts.size() + " résultat(s)");
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void selectPublicationsTab() {
        tabPublications.setTextColor(getResources().getColor(R.color.travel_primary, null));
        tabPublications.setTypeface(null, android.graphics.Typeface.BOLD);

        tabMap.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        tabMap.setTypeface(null, android.graphics.Typeface.NORMAL);

        recyclerSearchPosts.setVisibility(View.VISIBLE);
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
}