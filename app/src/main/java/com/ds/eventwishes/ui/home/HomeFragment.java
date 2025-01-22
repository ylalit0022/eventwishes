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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.Template;
import com.ds.eventwishes.databinding.FragmentHomeBinding;
import com.ds.eventwishes.model.Category;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements TemplateAdapter.OnTemplateClickListener {
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private TemplateAdapter templateAdapter;
    private CategoryAdapter categoryAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
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
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupObservers() {
        // Observe categories
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            Log.d("HomeFragment", "Received categories: " + categories.size());
            categoryAdapter.setCategories(categories);
            // Don't auto-select any category, let user choose or use "All"
        });

        // Observe templates
        viewModel.getFilteredTemplates().observe(getViewLifecycleOwner(), templates -> {
            Log.d("HomeFragment", "Received filtered templates: " + 
                (templates != null ? templates.size() : "null"));
            
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);

            if (templates == null || templates.isEmpty()) {
                binding.emptyView.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
            } else {
                binding.emptyView.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
                templateAdapter.submitList(templates);
            }
        });

        // Observe loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                binding.emptyView.setVisibility(View.GONE);
                binding.errorView.setVisibility(View.GONE);
            }
        });

        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            binding.swipeRefresh.setRefreshing(false);
            if (error != null) {
                Log.e("HomeFragment", "Error: " + error);
                binding.errorView.setVisibility(View.VISIBLE);
                binding.errorText.setText(error);
                binding.recyclerView.setVisibility(View.GONE);
                binding.emptyView.setVisibility(View.GONE);
            } else {
                binding.errorView.setVisibility(View.GONE);
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshData();
        });
    }

    private void setupRetryButton() {
        binding.retryButton.setOnClickListener(v -> viewModel.refreshData());
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
    }
}
