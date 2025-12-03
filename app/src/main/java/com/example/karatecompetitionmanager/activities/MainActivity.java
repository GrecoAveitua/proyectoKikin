package com.example.karatecompetitionmanager.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.karatecompetitionmanager.R;
import com.google.android.material.navigation.NavigationView;
import com.example.karatecompetitionmanager.fragments.CategoriesFragment;
import com.example.karatecompetitionmanager.fragments.CompetitorsFragment;
import com.example.karatecompetitionmanager.fragments.HistoryFragment;
import com.example.karatecompetitionmanager.fragments.ManageCategoryCompetitorsFragment;
import com.example.karatecompetitionmanager.fragments.StartCompetitionFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(new CompetitorsFragment());
            navigationView.setCheckedItem(R.id.nav_competitors);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        int id = item.getItemId();

        if (id == R.id.nav_competitors) {
            fragment = new CompetitorsFragment();
        } else if (id == R.id.nav_categories) {
            fragment = new CategoriesFragment();
        } else if (id == R.id.nav_manage_category_competitors) {
            fragment = new ManageCategoryCompetitorsFragment();
        } else if (id == R.id.nav_start_competition) {
            fragment = new StartCompetitionFragment();
        } else if (id == R.id.nav_history) {
            fragment = new HistoryFragment();
        }

        if (fragment != null) {
            loadFragment(fragment);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}