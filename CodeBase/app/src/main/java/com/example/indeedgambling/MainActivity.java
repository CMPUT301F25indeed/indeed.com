/**
 * MainActivity serves as the central host for the application's navigation.
 *
 * Responsibilities:
 * - Applies the app theme before layout inflation.
 * - Sets up view binding for the main layout.
 * - Initializes the NavHostFragment to manage screen navigation.
 * - Supports ActionBar back navigation through NavController.
 *
 * Notes:
 * - This activity holds the NavHostFragment defined in activity_main.xml.
 * - Uses ViewBinding (ActivityMainBinding) to replace findViewById calls.
 */
package com.example.indeedgambling;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.indeedgambling.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    /**
     * Initializes the activity, applies theme, sets up navigation host,
     * and inflates the main UI layout using view binding.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_IndeedGambling);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
        }
    }

    /**
     * Ensures the ActionBar's "navigate up" action works
     * with the Navigation Component's back stack.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        return (navHostFragment != null &&
                navHostFragment.getNavController().navigateUp())
                || super.onSupportNavigateUp();
    }
}
