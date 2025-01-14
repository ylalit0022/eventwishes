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
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_profile)
                    .build();
                    
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);
        }

        // After setup, mark first launch as complete
        isFirstLaunch = false;
    }

    @Override
    public void onBackPressed() {
        if (navController != null && 
            navController.getCurrentDestination() != null && 
            navController.getCurrentDestination().getId() == R.id.navigation_home) {
            // If we're on the home screen, show exit dialog
            showExitDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showExitDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit, null);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set dialog background to be rounded
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        }

        // Setup click listeners
        dialogView.findViewById(R.id.btnNo).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnYes).setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity();
        });

        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && 
               NavigationUI.navigateUp(navController, (AppBarConfiguration) null)
               || super.onSupportNavigateUp();
    }
}