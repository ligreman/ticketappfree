package com.ligresoftware.ticketappfree;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ligresoftware.ticketappfree.databinding.ActivityMainBinding;
import com.ligresoftware.ticketappfree.utils.Constants;
import com.ligresoftware.ticketappfree.utils.InputFilterMinMax;
import com.ligresoftware.ticketappfree.utils.InputFilterMinMaxFloat;
import com.ligresoftware.ticketappfree.utils.MyTwoCents;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity mActivity = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        fillFields();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //Validación de campos
        EditText importe = findViewById(R.id.cost);
        importe.setFilters(new InputFilter[]{new InputFilterMinMaxFloat(1.00f, 9999.99f, mActivity)});
        //Hago que sólo pueda tener dos decimales
        MyTwoCents watcher = new MyTwoCents();
        importe.addTextChangedListener(watcher);

        EditText personas = findViewById(R.id.peopleAmount);
        personas.setFilters(new InputFilter[]{new InputFilterMinMax(1, 999, mActivity)});

        //Evento listener del botón
        final Button button = findViewById(R.id.calculate);
        button.setOnClickListener(v -> sendImporte());
    }

    @Override
    protected void onResume() {
        fillFields();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Mandar el importe a la Actividad de cálculo y muestra resultado
     */
    private void sendImporte() {
        EditText editTextImporte = findViewById(R.id.cost);
        EditText editTextComensales = findViewById(R.id.peopleAmount);
        String importeTexto = editTextImporte.getText().toString();
        String comensalesTexto = editTextComensales.getText().toString();

        if (importeTexto.compareTo("") == 0) {
            Toast.makeText(this, getString(R.string.main_toast_insert_value), Toast.LENGTH_SHORT).show();
        } else {
            float importe = Float.parseFloat(importeTexto);

            if (comensalesTexto.compareTo("") == 0 || comensalesTexto.compareTo("0") == 0) {
                comensalesTexto = "1";
            }
            int comensales = Integer.parseInt(comensalesTexto);

            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra(Constants.EXTRA_IMPORTE, importe);
            intent.putExtra(Constants.EXTRA_COMENSALES, comensales);
            startActivity(intent);
        }
    }

    private void fillFields() {
        //Cojo los valores del SharedPreferences
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String valor1 = SP.getString("ticketOne", "0");

        //Al menos ha de existir un ticket, así que si el primero está vacío le mando a Configuración
        if (valor1.equals("0")) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            //Pongo el valor de los cheques
            TextView cheque1 = findViewById(R.id.cheque1);
            TextView cheque2 = findViewById(R.id.cheque2);

            cheque1.setText(valor1 + "" + getString(R.string.coin));

            //Segundo cheque
            String valor2 = SP.getString("ticketTwo", "0");

            if (valor2.equals("0")) {
                cheque2.setText("-");
            } else {
                cheque2.setText(valor2 + "" + getString(R.string.coin));
            }
        }
    }
}