package com.ds.eventwishes;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.ds.eventwishes.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private boolean isFirstLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle splash screen transition
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> isFirstLaunch);
        
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Configure bottom navigation
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_my_templates,
                    R.id.navigation_create,
                    R.id.navigation_history,
                    R.id.navigation_more)
                    .build();
                    
            NavigationUI.setupWithNavController(binding.navView, navController);
        }

        // After setup, mark first launch as complete
        isFirstLaunch = false;
    }

    @Override
    public void onBackPressed() {
        if (navController != null && navController.getCurrentDestination() != null) {
            if (navController.getCurrentDestination().getId() == R.id.navigation_home) {
                // Show exit confirmation dialog when on home screen
                new AlertDialog.Builder(this)
                    .setTitle(R.string.exit_dialog_title)
                    .setMessage(R.string.exit_dialog_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> super.onBackPressed())
                    .setNegativeButton(R.string.no, null)
                    .show();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && 
               NavigationUI.navigateUp(navController, appBarConfiguration)
               || super.onSupportNavigateUp();
    }
}