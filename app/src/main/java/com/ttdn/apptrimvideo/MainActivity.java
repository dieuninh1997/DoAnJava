package com.ttdn.apptrimvideo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ttdn.apptrimvideo.fragments.SettingsFragment;
import com.ttdn.apptrimvideo.fragments.SupportFragment;
import com.ttdn.apptrimvideo.fragments.VideoFragment;
import com.ttdn.apptrimvideo.utils.Constant;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Main Activity";
    private final int REQUEST_OVERLAY_PERMISSION = 10;
    //For Android M
    public static String[] M_VERSION_REQUEST_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private boolean overlayPermission = false, allOtherPermission = false;

    private android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
    private boolean isOpenSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIntentData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!hasAllPermissionsGranted()) {
                requestCameraPermissions();
//                MenuControllerService.menuControllerService
//                        .getMenuControlSession().getMenuControlLayout().setVisibility(View.VISIBLE);
            } else {
                allOtherPermission = true;
            }

            if (!Settings.canDrawOverlays(getApplication())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getApplication().getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            } else {
                overlayPermission = true;
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fm.addOnBackStackChangedListener(() -> {
            if (fm.getBackStackEntryCount() == 0) {
                setFirstItem(navigationView);
            }
        });

        fm.beginTransaction().replace(R.id.frame, new VideoFragment()).commit();

        if (!isOpenSettings) {
            setFirstItem(navigationView);
        } else {
            openSettings(navigationView);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermissions() {
        if (shouldShowRationale()) {
            Toast.makeText(this, getResources().getString(R.string.need_allow), Toast.LENGTH_SHORT).show();
        }
        requestPermissions(M_VERSION_REQUEST_PERMISSIONS, Constant.REQUEST_ALL_PERMISSION);
    }
    //For Android M
    @TargetApi(Build.VERSION_CODES.M)
    private boolean shouldShowRationale() {
        for (String permission : M_VERSION_REQUEST_PERMISSIONS) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }
    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasAllPermissionsGranted() {
        for (String permission : M_VERSION_REQUEST_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Constant.REQUEST_ALL_PERMISSION) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allOtherPermission = false;
                    showMissingPermissionError(permissions[0]);
                    return;
                } else {
                    allOtherPermission = true;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMissingPermissionError(String permission) {
        Toast.makeText(this, getResources().getString(R.string.need_allow) + permission, Toast.LENGTH_LONG).show();
    }


    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            isOpenSettings = intent.getBooleanExtra(Constant.EXTRA_KEY_OPEN_SETTINGS, false);
            Log.d(TAG, "getIntentData: open settings value: " + isOpenSettings);
        }
    }

    private void openSettings(NavigationView navigationView) {
        MenuItem menuItem = navigationView.getMenu().getItem(1);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(menuItem.getTitle());
        }
        menuItem.setChecked(true);
        onNavigationItemSelected(menuItem);
    }

    private void setFirstItem(NavigationView navigationView) {
        MenuItem menuItem = navigationView.getMenu().getItem(0);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(menuItem.getTitle());
        }
        menuItem.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() > 0) {
            cleanBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void cleanBackStack() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        android.support.v4.app.FragmentTransaction transaction = fm.beginTransaction();
        switch (id){
            case R.id.nav_videos:
                if(fm.getBackStackEntryCount()>0){
                    cleanBackStack();
                }
                transaction.replace(R.id.frame, new VideoFragment()).commit();
                break;
            case R.id.nav_trim_video:
                startActivity(new Intent(this, TrimVideoActivity.class));
                break;
            case R.id.nav_settings:
                transaction.addToBackStack(null);
                transaction.replace(R.id.frame, new SettingsFragment()).commit();
                break;
            case R.id.nav_about:
                transaction.addToBackStack(null);
                transaction.replace(R.id.frame, new SupportFragment()).commit();
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
