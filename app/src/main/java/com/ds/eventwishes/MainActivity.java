package com.ds.eventwishes;

import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ds.eventwishes.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Set up Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // Configure Bottom Navigation
        BottomNavigationView navView = binding.navView;
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_editor, R.id.navigation_profile)
                .build();

        NavigationUI.setupWithNavController(navView, navController);

        // Set up navigation listener to handle bottom navigation visibility
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Show bottom navigation only for main navigation items
            if (destination.getId() == R.id.navigation_home ||
                destination.getId() == R.id.navigation_editor ||
                destination.getId() == R.id.navigation_profile) {
                navView.setVisibility(View.VISIBLE);
            } else {
                // Hide for other destinations like ScriptEditorFragment
                navView.setVisibility(View.GONE);
            }
        });

        // Handle shared links
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Uri data = intent.getData();
        
        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String templateId = data.getQueryParameter("id");
            if (templateId != null) {
                // Navigate to editor with template ID
                Bundle args = new Bundle();
                args.putString("wishId", templateId);
                navController.navigate(R.id.navigation_editor, args);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}