package martino.barcodedistancefinder2.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import martino.barcodedistancefinder2.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Settings");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.root_preferences);
            Preference button = findPreference(getString(R.string.statisticsButton));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(), Statistics.class);
                    startActivity(intent,  ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                    return true;
                }
            });
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            //setPreferencesFromResource(R.xml.root_preferences, rootKey);

        }
    }
}