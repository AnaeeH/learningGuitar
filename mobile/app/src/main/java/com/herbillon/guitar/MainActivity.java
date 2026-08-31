package com.herbillon.guitar;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.Locale;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.herbillon.guitar.utils.KeepAliveScheduler;

public class MainActivity extends AppCompatActivity {
    private ProgressBar globalProgressBar;
    private LinearLayout wakeupContainer;
    private TextView wakeupCountdownText;
    private CountDownTimer wakeupTimer;
    private static boolean wakeupDone = false;
    private KeepAliveScheduler keepAliveScheduler;

    @Override
    // Code exécuté lorsque l'appli se lance pour la première fois
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        globalProgressBar = findViewById(R.id.globalProgressBar);
        wakeupContainer = findViewById(R.id.wakeupContainer);
        wakeupCountdownText = findViewById(R.id.wakeupCountdownText);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, navController);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            toolbar.getMenu().clear();
            if (destination.getId() == R.id.nav_tablature) {
                toolbar.inflateMenu(R.menu.toolbar_tablature);
                toolbar.setBackgroundColor(getColor(R.color.dark_background));
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
        keepAliveScheduler = new KeepAliveScheduler(app.guitarAPI);
        if (!wakeupDone) {
            wakeupDone = true;
            startWakeupCountdown(app);
        } else {
            // wakeup already played this session, fetch immediately
            app.guitarAPI.fetchChords("");
            app.guitarAPI.fetchMusics("recent", false, false, null, null);
            app.guitarAPI.fetchMusicLastPlayed();
        }
    }

    private void startWakeupCountdown(GuitarApp app) {
        final long WAKEUP_DURATION_MS = 80 * 1000L;
        app.guitarAPI.pingApi();
        showLoading();
        wakeupContainer.setVisibility(View.VISIBLE);

        wakeupTimer = new CountDownTimer(WAKEUP_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = (millisUntilFinished + 999) / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                wakeupCountdownText.setText(String.format(Locale.US, "%d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                hideLoading();
                wakeupContainer.setVisibility(View.GONE);
                app.guitarAPI.fetchChords("");
                app.guitarAPI.fetchMusics("recent", false, false, null, null);
                app.guitarAPI.fetchMusicLastPlayed();
            }
        };
        wakeupTimer.start();
    }

    public void showLoading() {
        if (globalProgressBar != null)
            globalProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (globalProgressBar != null)
            globalProgressBar.setVisibility(View.GONE);
        if (wakeupContainer != null)
            wakeupContainer.setVisibility(View.GONE);
    }

    @Override
    // Code exécuter lorsque l'appli passe en premier plan
    protected void onResume() {
        super.onResume();
        keepAliveScheduler.start();
    }

    @Override
    // Code exécuté lorsque l'appli passe en arrière plan
    protected void onPause() {
        super.onPause();
        keepAliveScheduler.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeupTimer != null) {
            wakeupTimer.cancel();
        }
    }
}