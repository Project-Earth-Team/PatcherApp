package dev.projectearth.patcher;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import dev.projectearth.patcher.utils.AndroidUtils;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new AboutFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class AboutFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.about_preferences, rootKey);

            findPreference("version").setSummary(BuildConfig.VERSION_NAME);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            switch (preference.getKey()) {
                case "github":
                    AndroidUtils.showURL(requireContext(), "https://github.com/Project-Earth-Team/PatcherApp");
                    return true;
                case "website":
                    AndroidUtils.showURL(requireContext(), "https://projectearth.dev");
                    return true;

                default:
                    break;
            }

            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The back button
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}