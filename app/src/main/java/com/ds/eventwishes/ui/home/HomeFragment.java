package com.ds.eventwishes.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.ds.eventwishes.R;
import com.ds.eventwishes.api.ApiClient;
import com.ds.eventwishes.api.Template;
import com.ds.eventwishes.databinding.FragmentHomeBinding;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements TemplateAdapter.OnTemplateClickListener {
    private FragmentHomeBinding binding;
    private TemplateAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        
        setupRecyclerView();
        setupSwipeRefresh();
        setupRetryButton();
        
        fetchTemplates();
        
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new TemplateAdapter(requireContext(), new ArrayList<>(), this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(this::fetchTemplates);
    }

    private void setupRetryButton() {
        binding.retryButton.setOnClickListener(v -> fetchTemplates());
    }

    @Override
    public void onTemplateClick(Template template) {
        Bundle args = new Bundle();
        args.putString("wishId", template.getId());
        args.putString("htmlContent", template.getHtmlContent());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_editor, args);
    }

    private void fetchTemplates() {
        binding.swipeRefresh.setRefreshing(true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.errorView.setVisibility(View.GONE);

        ApiClient.getInstance().getTemplates().enqueue(new Callback<List<Template>>() {
            @Override
            public void onResponse(@NonNull Call<List<Template>> call, @NonNull Response<List<Template>> response) {
                binding.swipeRefresh.setRefreshing(false);
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Template> templates = response.body();
                    adapter.updateTemplates(templates);
                    if (templates.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    String errorMessage = "Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMessage += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Template>> call, @NonNull Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                binding.progressBar.setVisibility(View.GONE);
                showError("Network error: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void showError(String message) {
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);
        binding.errorView.setVisibility(View.VISIBLE);
        binding.errorText.setText(message);
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
