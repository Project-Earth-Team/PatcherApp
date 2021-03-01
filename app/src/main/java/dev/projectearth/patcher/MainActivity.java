package dev.projectearth.patcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import lombok.Getter;

public class MainActivity extends AppCompatActivity {
    @Getter
    private static Context appContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContext = getApplicationContext();

        // Set default preferences
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        TextView txtMCETitle = findViewById(R.id.txtMCETitle);
        TextView txtMCEDesc = findViewById(R.id.txtMCEDesc);
        ImageView imgMCEIcon = findViewById(R.id.imgMCEIcon);

        TextView txtPJETitle = findViewById(R.id.txtPJETitle);
        TextView txtPJEDesc = findViewById(R.id.txtPJEDesc);
        ImageView imgPJEIcon = findViewById(R.id.imgPJEIcon);

        Button btnPatch = findViewById(R.id.btnPatch);

        txtMCETitle.setPaintFlags(txtMCETitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtPJETitle.setPaintFlags(txtPJETitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Get Minecraft Earth
        try {
            PackageInfo earthInfo = getPackageManager().getPackageInfo("com.mojang.minecraftearth", 0);
            txtMCEDesc.setText(getString(R.string.activity_main_version, earthInfo.versionName, earthInfo.versionCode));
            txtMCEDesc.append("\n");

            // TODO: Check apk hash here? In case of prior modification
            boolean patchable = false;
            if (earthInfo.versionCode == 2020121703) {
                patchable = true;
            }
            btnPatch.setEnabled(patchable);
            txtMCEDesc.append(getString(R.string.activity_main_patchable, Boolean.toString(patchable)));

            imgMCEIcon.setImageDrawable(earthInfo.applicationInfo.loadIcon(getPackageManager()));
        } catch (PackageManager.NameNotFoundException e) {
            btnPatch.setEnabled(false);
        }

        // Get Project Earth
        try {
            PackageInfo earthInfo = getPackageManager().getPackageInfo("dev.projectearth.prod", 0);
            txtPJEDesc.setText(getString(R.string.activity_main_version, earthInfo.versionName, earthInfo.versionCode));
            imgPJEIcon.setImageDrawable(earthInfo.applicationInfo.loadIcon(getPackageManager()));
        } catch (PackageManager.NameNotFoundException ignored) { }

        btnPatch.setOnClickListener(v -> {
            Intent intent = new Intent(this, InstallerStepsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}