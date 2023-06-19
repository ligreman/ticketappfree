package com.ligresoftware.ticketappfree;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ligresoftware.ticketappfree.databinding.ActivityResultBinding;
import com.ligresoftware.ticketappfree.utils.Constants;
import com.ligresoftware.ticketappfree.utils.TicketUtils;

import java.util.List;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarResult);

        ExpandableListView expandableListView;
        expandableListView = findViewById(R.id.expandableListView);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_result);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        //Variables
        List<Map<String, String>> groupData;
        List<List<Map<String, String>>> childData;
        List<Object> array = null;

        //Recojo el valor del Intent (importe de la consumición)
        Intent previousIntent = getIntent();
        float importe = previousIntent.getFloatExtra(Constants.EXTRA_IMPORTE, 0);
        int comensales = previousIntent.getIntExtra(Constants.EXTRA_COMENSALES, 0);

        //Según el número de comensales, así pagará cada uno.
        if (comensales > 1) {
            float pachas = (importe / comensales) * 100.0f;  //cojo 2 decimales
            pachas = Math.round(pachas); //Lo redondeo
            pachas = pachas / 100.0f; //Lo pongo de nuevo con 2 decimales

            importe = pachas; //el importe que me interesa calcular es lo que te toca pagar a tí
            //Log.d("A PACHAS", "Importe original:"+importe+" // A pachas:"+pachas);

            //Además de eso, el texto cambia
            TextView textoImporteView = findViewById(R.id.result_intro_desc);
            textoImporteView.setText(getString(R.string.result_intro_desc_pachas));
        }

        //Lo pongo en la cabecera
        TextView importeView = findViewById(R.id.result_total_cost);
        importeView.setText(String.valueOf(importe).replaceAll("\\.0$", ""));

        //Si es menor o igual que 0, cagada vuelvo a la página anterior
        if (importe <= 0) {
            //Vuelvo 1 en el historial al Activity anterior
            onBackPressed();
        } else {
            //Cargo los valores por defecto de Preferencias
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

            float ticketOneValue = Float.parseFloat(SP.getString("ticketOne", "0"));
            float ticketTwoValue = Float.parseFloat(SP.getString("ticketTwo", "0"));
            String moneySymbol = getString(R.string.coin);
            boolean ticketOnlyBestDistro = SP.getBoolean("onlyBestDistro", false);

            //El símbolo de moneda en la cabecera
            TextView moneySymbolView = findViewById(R.id.result_money_symbol);
            moneySymbolView.setText(moneySymbol);

            //Layout para los hijos
            int layout_children = 0;

            //Calculo el resultado final
            if (ticketOneValue > 0 && ticketTwoValue > 0) {
                //Calculo con 2 tickets
                array = TicketUtils.processTwoTickets(importe, ticketOneValue, ticketTwoValue, ticketOnlyBestDistro, moneySymbol);

                //Mostrará los dos tickets, selecciono este layout
                layout_children = R.layout.list_children_two_tickets;
            } else if (ticketOneValue > 0 && ticketTwoValue <= 0) {
                //Calculo con 1 ticket
                array = TicketUtils.processOneTicket(importe, ticketOneValue, moneySymbol);

                //Mostrará 1 solo ticket en el hijo
                layout_children = R.layout.list_children_one_ticket;
            }

            groupData = (List<Map<String, String>>) array.get(0);
            childData = (List<List<Map<String, String>>>) array.get(1);

            // Set up our adapter
            ExpandableListAdapter mAdapter = new SimpleExpandableListAdapter(
                    this,
                    groupData,
                    R.layout.list_group,
                    //Qué valores de los metidos en la lista...
                    new String[]{"DATA_SOBRECOSTE", "DATA_TOTAL_PAGADO", "DATA_MONEY_SYMBOL", "DATA_MONEY_SYMBOL", "DATA_MAS_MENOS"},
                    //...muestro en estas posiciones del layout
                    new int[]{R.id.text_group_overcost, R.id.text_group_total_paid, R.id.text_group_money_symbol1, R.id.text_group_money_symbol2, R.id.text_plus_or_minus},
                    childData,
                    layout_children,
                    new String[]{"DATA_TICKET_ONE_AMOUNT", "DATA_TICKET_ONE_VALUE", "DATA_TICKET_TWO_AMOUNT", "DATA_TICKET_TWO_VALUE"},
                    new int[]{R.id.text_children_ticket_one_amount, R.id.text_children_ticket_one_value, R.id.text_children_ticket_two_amount, R.id.text_children_ticket_two_value}
            );
            expandableListView.setAdapter(mAdapter);

            float item_px_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
            float divider_px_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, getResources().getDisplayMetrics());
            float subitem_px_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics());

            float itemsH = item_px_size * mAdapter.getGroupCount();
            float dividersH = divider_px_size * (mAdapter.getGroupCount() - 1);
            expandableListView.getLayoutParams().height = (int) (itemsH + dividersH);
            expandableListView.setAdapter(mAdapter);

            // ListView Group Expand Listener
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {
                    int nb_children = mAdapter.getChildrenCount(groupPosition);
                    expandableListView.getLayoutParams().height += (subitem_px_size * nb_children) + divider_px_size;
                }
            });

            // Listview Group collapsed listener
            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    int nb_children = mAdapter.getChildrenCount(groupPosition);
                    expandableListView.getLayoutParams().height -= (subitem_px_size * nb_children) + divider_px_size;
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_result);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}