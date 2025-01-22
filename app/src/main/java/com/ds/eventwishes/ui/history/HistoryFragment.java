package com.ds.eventwishes.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ds.eventwishes.R;
import com.ds.eventwishes.databinding.FragmentHistoryBinding;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupClearButton();
        observeData();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyRecyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.secondary
        );
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshWishes();
            binding.searchInput.setText("");
        });
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.searchWishes("%" + s.toString() + "%");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClearButton() {
        binding.clearHistoryButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_history_title)
                .setMessage(R.string.clear_history_message)
                .setPositiveButton(R.string.clear, (dialog, which) -> {
                    viewModel.clearHistory();
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
        });
    }

    private void observeData() {
        viewModel.getWishHistory().observe(getViewLifecycleOwner(), wishes -> {
            adapter.submitList(wishes);
            binding.emptyView.setVisibility(wishes.isEmpty() ? View.VISIBLE : View.GONE);
            binding.swipeRefresh.setRefreshing(false);
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
