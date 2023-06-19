package com.ligresoftware.ticketappfree.utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TicketUtils {

    /********************************/
    /** Cálculos para dos tickets   */
    /********************************/
    public final static List<Object> processTwoTickets(float importe, float ticketOne, float ticketTwo, boolean ticketOnlyBestDistro, String moneySymbol) {
        List<Object> retorno = new ArrayList<Object>(); //Guarda el groupData y childrenDara para devolverlos
        Map<Float, List<Pair<Integer, Integer>>> resultadosOrdenados = new TreeMap<Float, List<Pair<Integer, Integer>>>(); //Mapa intermedio para ordenar resultados
        Map<Float, List<Pair<Integer, Integer>>> resultadosNegativosOrdenados = new TreeMap<Float, List<Pair<Integer, Integer>>>(); //Mapa intermedio para ordenar resultados

        //Each entry in the ArrayList corresponds to one group in the expandable list.
        //The Maps contain the data for each row.
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();

        //This process is similar for a child, except it is one-level deeper so the data backing is specified
        //as a <List>, where the first List corresponds to the group of the child, the second List corresponds
        //to the position of the child within the group, and finally the Map holds the data for that particular child.
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

        //Determino qué ticket es el de mayor cantidad
        float mayor = Math.max(ticketOne, ticketTwo);
        float menor = Math.min(ticketOne, ticketTwo);

        //¿Cuantas veces puedo quitar mayor de importe?
        Float vecesFloat = Float.valueOf(importe / mayor);
        int veces = vecesFloat.intValue();

        if (importe % mayor > 0) {
            //Si sobra algo le sumo uno al número de veces ya que no me importa pasarme del importe un poco
            veces++;
        }

        for (int tkt1 = 0; tkt1 <= veces; tkt1++) {
            float resto_original = importe - (tkt1 * mayor);
            float resto2;
            int tkt2 = 0;

            if (resto_original > 0) {

                //Aún no he pagado todo
                do {
                    tkt2++; //sumo un ticket de los pequeños
                    resto2 = resto_original - (tkt2 * menor);
                } while (resto2 > 0);

            } else {
                //He pagado ya todo con los tickets mayores
                resto2 = resto_original;
            }

            //++Para el resultado en que falte un poco de dinero
            float resto2_aux = resto2;
            int tkt1_aux = tkt1;
            int tkt2_aux = tkt2;
            if (tkt2_aux == 0) {
                //Aquí he de quitar del otro ticket
                tkt1_aux--;
                resto2_aux += mayor;
            } else {
                tkt2_aux--;
                resto2_aux += menor;
            }

            //Log.d("- RESTO -", "Falta "+resto2_aux+" ["+tkt1_aux+"x"+ticketOne+" y "+tkt2_aux+"x"+ticketTwo+"]");

            //Pongo en positivo el resto2 y redondeado a 2 decimales, y el resto2_aux
            resto2 = Math.abs(resto2);
            resto2 = Math.round(resto2 * 100.0f) / 100.0f;
            resto2_aux = Math.abs(resto2_aux);
            resto2_aux = Math.round(resto2_aux * 100.0f) / 100.0f;

            //Almacén de resultados ordenado intermedio
            List<Pair<Integer, Integer>> listado;
            if (resultadosOrdenados.containsKey(resto2)) {
                //Ya existía el grupo de "Sobra x"
                listado = resultadosOrdenados.get(resto2); //Obtengo la lista guardada para actualizarla
            } else {
                //nuevo grupo Sobra
                listado = new ArrayList<Pair<Integer, Integer>>();
            }
            listado.add(new Pair<Integer, Integer>(tkt1, tkt2)); //Añado el nuevo
            resultadosOrdenados.put(resto2, listado); //Actualizo los resultados

            //Log.d("DISTRO POSITIVA", "T1:"+tkt1+" // T2:"+tkt2);

            //++Almacén de resultados negativos en que falta dinero
            List<Pair<Integer, Integer>> listado_aux;
            Pair<Integer, Integer> pareja = new Pair<Integer, Integer>(tkt1_aux, tkt2_aux);
            if (resultadosNegativosOrdenados.containsKey(resto2_aux)) {
                //Ya existía el grupo de "Falta x"
                listado_aux = resultadosNegativosOrdenados.get(resto2_aux); //Obtengo la lista guardada para actualizarla
            } else {
                //nuevo grupo Falta
                listado_aux = new ArrayList<Pair<Integer, Integer>>();
            }
            //Para evitar duplicados compruebo si ya existía esta pareja de ticket1 y ticket2
            if (!listado_aux.contains(pareja)) {
                listado_aux.add(pareja); //Añado el nuevo
                resultadosNegativosOrdenados.put(resto2_aux, listado_aux); //Actualizo los resultados

                //Log.d("DISTRO NEGATIVA", "T1:"+tkt1_aux+" // T2:"+tkt2_aux);
            }
        }

        //++Cojo la entrada de los negativos más alta (la más cercana a 0)
        for (Map.Entry<Float, List<Pair<Integer, Integer>>> entrada_aux : resultadosNegativosOrdenados.entrySet()) {
            List<Pair<Integer, Integer>> nuevo = entrada_aux.getValue();
            float key = -1 * entrada_aux.getKey();

            resultadosOrdenados.put(key, nuevo); //la meto en el almacén bueno

            break; //solo el primer resultado que es en el que menos falta
        }

        // Iterator para el Mapa de resultados ordenados
        for (Map.Entry<Float, List<Pair<Integer, Integer>>> entrada : resultadosOrdenados.entrySet()) {
            //entrada: getKey para el Float, y getValue para la List de Pairs
            List<Pair<Integer, Integer>> listado = entrada.getValue();

            //Meto el valor del grupo (lo que sobra al pagar)
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);

            Float sobrante = entrada.getKey();
            if (sobrante < 0) {
                curGroupMap.put("DATA_MAS_MENOS", "-");
            } else {
                curGroupMap.put("DATA_MAS_MENOS", "+");
            }
            sobrante = Math.abs(sobrante);

            curGroupMap.put("DATA_SOBRECOSTE", sobrante.toString().replaceAll("\\.0$", ""));
            curGroupMap.put("DATA_TOTAL_PAGADO", String.valueOf(importe + entrada.getKey()).replaceAll("\\.0$", ""));
            curGroupMap.put("DATA_MONEY_SYMBOL", moneySymbol);

            //Ahora los hijos, que son el número de tickets
            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int it = 0; it < listado.size(); it++) {
                Pair<Integer, Integer> tickets = listado.get(it);

                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);

                //Los labels-tickets los pongo en el orden tal y como el usuario los introdujo
                curChildMap.put("DATA_TICKET_ONE_VALUE", String.valueOf(ticketOne).replaceAll("\\.0$", ""));
                curChildMap.put("DATA_TICKET_TWO_VALUE", String.valueOf(ticketTwo).replaceAll("\\.0$", ""));

                //Como siempre me vienen los valores calculados en la variable tickets, ordenados en first=valor_del_mayor y second=valor_del_menor
                if (ticketOne == mayor) {
                    //El One se corresponde con el Amount One
                    curChildMap.put("DATA_TICKET_ONE_AMOUNT", tickets.first.toString());
                    curChildMap.put("DATA_TICKET_TWO_AMOUNT", tickets.second.toString());
                } else {
                    //El One se corresponde con el Amount Two
                    curChildMap.put("DATA_TICKET_ONE_AMOUNT", tickets.second.toString());
                    curChildMap.put("DATA_TICKET_TWO_AMOUNT", tickets.first.toString());
                }

                //Log.d("- RESULTADO ORD -", "Sobran "+entrada.getKey()+" ["+tickets.first+"x"+ticketOne+" y "+tickets.second+"x"+ticketTwo+"]");
            }
            childData.add(children);

            //Si sólo quiero ver el mejor resultado posible, me salgo aquí del bucle
            if (ticketOnlyBestDistro && (entrada.getKey() > 0)) {
                //Si he impreso un valor positivo es cuando me salgo ya
                break;
            }
        }

        retorno.add(0, groupData);
        retorno.add(1, childData);

        return retorno;
    }


    /********************************/
    /** Cálculos para 1 solo ticket */
    /********************************/
    public final static List<Object> processOneTicket(float importe, float ticketOne, String moneySymbol) {
        List<Object> retorno = new ArrayList<Object>(); //Guarda el groupData y childrenDara para devolverlos

        //Each entry in the ArrayList corresponds to one group in the expandable list.
        //The Maps contain the data for each row.
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();

        //This process is similar for a child, except it is one-level deeper so the data backing is specified
        //as a <List>, where the first List corresponds to the group of the child, the second List corresponds
        //to the position of the child within the group, and finally the Map holds the data for that particular child.
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();

        //Como sólo hay un ticket, simplemente veo cuántas veces puedo dividir el importe por el ticket
        Float vecesFloat = importe / ticketOne;
        int veces = vecesFloat.intValue();
        int veces_aux = veces;

        if (importe % ticketOne > 0) {
            //Si sobra algo le sumo uno al número de veces ya que no me importa pasarme del importe un poco
            veces++;
        }

        float resto = importe - (veces * ticketOne);
        float resto_aux = importe - (veces_aux * ticketOne);

        //Pongo en positivo el resto y redondeado a 2 decimales
        resto = Math.abs(resto);
        resto = Math.round(resto * 100.0f) / 100.0f;
        resto_aux = Math.abs(resto_aux);
        resto_aux = Math.round(resto_aux * 100.0f) / 100.0f;

        //-------------- FALTANTE --------------------
        //Meto el valor del grupo (lo que falta al pagar)
        Map<String, String> curGroupMap = new HashMap<String, String>();
        groupData.add(curGroupMap);

        curGroupMap.put("DATA_MAS_MENOS", "-");
        curGroupMap.put("DATA_SOBRECOSTE", String.valueOf(resto_aux).replaceAll("\\.0$", ""));
        curGroupMap.put("DATA_TOTAL_PAGADO", String.valueOf(importe - resto_aux).replaceAll("\\.0$", ""));
        curGroupMap.put("DATA_MONEY_SYMBOL", moneySymbol);

        //Ahora el hijo, que en este caso sólo hay uno
        List<Map<String, String>> children = new ArrayList<Map<String, String>>();
        Map<String, String> curChildMap = new HashMap<String, String>();
        children.add(curChildMap);

        curChildMap.put("DATA_TICKET_ONE_AMOUNT", String.valueOf(veces_aux));
        curChildMap.put("DATA_TICKET_ONE_VALUE", String.valueOf(ticketOne).replaceAll("\\.0$", ""));
        curChildMap.put("DATA_TICKET_TWO_AMOUNT", "");
        curChildMap.put("DATA_TICKET_TWO_VALUE", "");

        childData.add(children);

        //-------------- SOBRANTE --------------------
        //Meto el valor del grupo (lo que sobra al pagar)
        curGroupMap = new HashMap<String, String>();
        groupData.add(curGroupMap);

        curGroupMap.put("DATA_MAS_MENOS", "+");
        curGroupMap.put("DATA_SOBRECOSTE", String.valueOf(resto).replaceAll("\\.0$", ""));
        curGroupMap.put("DATA_TOTAL_PAGADO", String.valueOf(importe + resto).replaceAll("\\.0$", ""));
        curGroupMap.put("DATA_MONEY_SYMBOL", moneySymbol);

        //Ahora el hijo, que en este caso sólo hay uno
        children = new ArrayList<Map<String, String>>();
        curChildMap = new HashMap<String, String>();
        children.add(curChildMap);

        curChildMap.put("DATA_TICKET_ONE_AMOUNT", String.valueOf(veces));
        curChildMap.put("DATA_TICKET_ONE_VALUE", String.valueOf(ticketOne).replaceAll("\\.0$", ""));
        curChildMap.put("DATA_TICKET_TWO_AMOUNT", "");
        curChildMap.put("DATA_TICKET_TWO_VALUE", "");

        childData.add(children);

        //Preparo el return
        retorno.add(0, groupData);
        retorno.add(1, childData);

        return retorno;
    }
}
