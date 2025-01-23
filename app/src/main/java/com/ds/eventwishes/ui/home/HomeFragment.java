package com.ds.eventwishes.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.ds.eventwishes.R;
import com.ds.eventwishes.databinding.FragmentHomeBinding;
import com.ds.eventwishes.databinding.LayoutEmptyStateBinding;
import com.ds.eventwishes.databinding.LayoutErrorStateBinding;
import com.ds.eventwishes.model.Category;
import com.ds.eventwishes.model.Template;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private LayoutEmptyStateBinding emptyBinding;
    private LayoutErrorStateBinding errorBinding;
    private HomeViewModel viewModel;
    private TemplateAdapter templateAdapter;
    private CategoryAdapter categoryAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        emptyBinding = LayoutEmptyStateBinding.bind(binding.emptyView.getRoot());
        errorBinding = LayoutErrorStateBinding.bind(binding.errorView.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupAdapters();
        setupObservers();
        setupSwipeRefresh();
        setupRetryButton();

        // Initial load
        viewModel.loadTemplates(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        emptyBinding = null;
        errorBinding = null;
    }

    private void setupAdapters() {
        // Setup template adapter
        templateAdapter = new TemplateAdapter();
        templateAdapter.setOnTemplateClickListener(template -> {
            if (template != null) {
                Bundle args = new Bundle();
                args.putString("templateId", template.getId());
                args.putString("title", template.getTitle());
                args.putString("category", template.getCategory());
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_to_detail, args);
            }
        });
        
        binding.recyclerView.setAdapter(templateAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);

        // Add scroll listener for pagination
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Only load more if scrolling down
                if (dy <= 0) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Check if end of list is approaching
                if (!viewModel.getIsLoading().getValue() && viewModel.hasMore().getValue()) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) {
                        viewModel.loadMore();
                    }
                }
            }
        });

        // Setup category adapter
        categoryAdapter = new CategoryAdapter();
        categoryAdapter.setOnCategorySelectedListener(category -> {
            viewModel.selectCategory(category);
        });
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
        binding.categoriesRecyclerView.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupObservers() {
        // Observe templates
        viewModel.getTemplates().observe(getViewLifecycleOwner(), templates -> {
            templateAdapter.submitList(new ArrayList<>(templates));
            updateVisibility();
        });

        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.setCategories(categories);
        });

        // Observe selected category
        viewModel.getSelectedCategory().observe(getViewLifecycleOwner(), category -> {
            categoryAdapter.setSelectedCategory(category);
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
            updateVisibility();
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
        });
    }

    private void setupRetryButton() {
        errorBinding.retryButton.setOnClickListener(v -> {
            viewModel.refresh();
        });
    }

    private void updateVisibility() {
        List<Template> templates = viewModel.getTemplates().getValue();
        boolean hasTemplates = templates != null && !templates.isEmpty();
        boolean hasError = viewModel.getError().getValue() != null;
        boolean isLoading = viewModel.getIsLoading().getValue() == Boolean.TRUE;

        binding.recyclerView.setVisibility(hasTemplates ? View.VISIBLE : View.GONE);
        binding.emptyView.getRoot().setVisibility(!hasTemplates && !hasError && !isLoading ? View.VISIBLE : View.GONE);
        binding.errorView.getRoot().setVisibility(hasError && !isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }
}
