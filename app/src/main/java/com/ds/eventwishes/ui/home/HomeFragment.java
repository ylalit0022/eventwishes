package com.ds.eventwishes.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.Template;
import com.ds.eventwishes.databinding.FragmentHomeBinding;
import com.ds.eventwishes.databinding.LayoutEmptyStateBinding;
import com.ds.eventwishes.databinding.LayoutErrorStateBinding;
import com.ds.eventwishes.model.Category;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements TemplateAdapter.OnTemplateClickListener {
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
        setupSearch();
        setupObservers();
        setupSwipeRefresh();
        setupRetryButton();
    }

    private void setupAdapters() {
        // Setup template adapter
        templateAdapter = new TemplateAdapter(requireContext(), this);
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
                if (!viewModel.isLoadingMore().getValue() && viewModel.hasMore().getValue()) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) {
                        viewModel.loadNextPage();
                    }
                }
            }
        });

        // Setup category adapter
        categoryAdapter = new CategoryAdapter();
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
        categoryAdapter.setOnCategoryClickListener((category, position) -> {
            categoryAdapter.setSelectedPosition(position);
            viewModel.setSelectedCategory(category);
        });
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });
    }

    private void setupObservers() {
        // Observe templates
        viewModel.getFilteredTemplates().observe(getViewLifecycleOwner(), templates -> {
            templateAdapter.submitList(templates);
        });

        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
            updateViewVisibility(isLoading, viewModel.getError().getValue(), templateAdapter.getCurrentList().isEmpty());
        });

        // Observe loading more state
        viewModel.isLoadingMore().observe(getViewLifecycleOwner(), isLoadingMore -> {
            binding.loadingMore.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
        });

        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                errorBinding.errorText.setText(error);
                updateViewVisibility(false, error, templateAdapter.getCurrentList().isEmpty());
            }
        });

        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryAdapter.submitList(categories);
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshData();
        });
    }

    private void setupRetryButton() {
        errorBinding.retryButton.setOnClickListener(v -> {
            viewModel.refreshData();
        });
    }

    private void updateViewVisibility(boolean isLoading, String error, boolean isEmpty) {
        if (isLoading) {
            binding.loadingView.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyView.getRoot().setVisibility(View.GONE);
            binding.errorView.getRoot().setVisibility(View.GONE);
        } else if (error != null) {
            binding.loadingView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyView.getRoot().setVisibility(View.GONE);
            binding.errorView.getRoot().setVisibility(View.VISIBLE);
        } else if (isEmpty) {
            binding.loadingView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyView.getRoot().setVisibility(View.VISIBLE);
            binding.errorView.getRoot().setVisibility(View.GONE);
        } else {
            binding.loadingView.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.getRoot().setVisibility(View.GONE);
            binding.errorView.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public void onTemplateClick(Template template) {
        Bundle args = new Bundle();
        args.putString("templateId", template.getId());
        args.putString("htmlContent", template.getHtmlContent());
        args.putString("title", template.getTitle());
        args.putString("description", template.getCategory());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_editor, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        emptyBinding = null;
        errorBinding = null;
    }
}
