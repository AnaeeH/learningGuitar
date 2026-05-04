package com.herbillon.guitar;

import android.graphics.Insets;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ProgressBar globalProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        globalProgressBar = findViewById(R.id.globalProgressBar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            toolbar.getMenu().clear();
            if (destination.getId() == R.id.nav_tablature) {
                toolbar.inflateMenu(R.menu.toolbar_tablature);
                toolbar.setBackgroundColor(getColor(R.color.dark_background2));
            } else {
                toolbar.inflateMenu(R.menu.toolbar_menu);
                toolbar.setBackgroundColor(getColor(R.color.dark_mainColor));
            }

            if ((destination.getId() == R.id.nav_music || destination.getId() == R.id.nav_tablature) && arguments != null) {
                toolbar.setSubtitle(arguments.getString("musicArtist"));
            } else {
                toolbar.setSubtitle(null);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_refresh) {
                androidx.fragment.app.Fragment currentFragment = navHostFragment.getChildFragmentManager()
                        .getPrimaryNavigationFragment();

                if (currentFragment instanceof Refreshable) {
                    ((Refreshable) currentFragment).onRefresh();
                }
                return true;
            }
            return false;
        });

        GuitarApp app = (GuitarApp) getApplication();
        showLoading();
        app.guitarAPI.fetchChords("");
        app.guitarAPI.fetchMusics(false, false, null, null);
    }

    public void showLoading() {
        if (globalProgressBar != null)
            globalProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (globalProgressBar != null)
            globalProgressBar.setVisibility(View.GONE);
    }
}