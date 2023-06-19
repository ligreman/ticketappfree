package com.ligresoftware.ticketappfree;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.text.DecimalFormat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final Button button = findViewById(R.id.button_settings_back);
        button.setOnClickListener(v -> onBackPressed());

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            //Validación de preferencias
            findPreference("ticketOne").setOnPreferenceChangeListener((preference, newValue) -> checkInterval(newValue, 0.1f, 100f));

            findPreference("ticketTwo").setOnPreferenceChangeListener((preference, newValue) -> checkInterval(newValue, 0f, 100f));
        }


        private boolean checkInterval(Object newValue, float min, float max) {
            boolean valido = false;

            try {
                Float valor = Float.parseFloat(newValue.toString());
                if (valor >= min && valor <= max) {
                    valido = true;
                } else {
                    DecimalFormat df = new DecimalFormat();
                    df.setMaximumFractionDigits(2);

                    Toast.makeText(getActivity(), getString(R.string.max_values_string, df.format(min), df.format(max)), Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException nfe) {
                valido = false;
            }

            return valido;
        }
    }
}