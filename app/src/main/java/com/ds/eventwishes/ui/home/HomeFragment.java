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
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup RecyclerView
        adapter = new TemplateAdapter(new ArrayList<>(), this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
        
        // Setup SwipeRefreshLayout
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTemplates();
            }
        });
        
        // Setup retry button
        binding.retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTemplates();
            }
        });
        
        // Initial load
        loadTemplates();
    }

    private void loadTemplates() {
        binding.swipeRefresh.setRefreshing(true);
        binding.errorView.setVisibility(View.GONE);
        
        ApiClient.getInstance().getTemplates().enqueue(new Callback<List<Template>>() {
            @Override
            public void onResponse(Call<List<Template>> call, Response<List<Template>> response) {
                if (!isAdded()) return; // Check if fragment is still attached
                
                binding.swipeRefresh.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Template> templates = response.body();
                    adapter.updateTemplates(templates);
                    
                    // Show empty state if no templates
                    if (templates.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<Template>> call, Throwable t) {
                if (!isAdded()) return; // Check if fragment is still attached
                
                binding.swipeRefresh.setRefreshing(false);
                showError();
            }
        });
    }

    private void showError() {
        binding.errorView.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        Snackbar.make(binding.getRoot(), R.string.error_loading, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onTemplateClick(Template template) {
        Bundle args = new Bundle();
        args.putString("wishId", template.getId());
        args.putString("htmlContent", template.getHtmlContent());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_home_to_editor, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
