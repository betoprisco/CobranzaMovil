package com.cobranza.movil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import Clases.MiClase;
import Clases.UltimasGest;

public class listadoUltiGestiones extends Activity {

    String custid;
    String titulo;
    String texto;

    private static final int DIALOGO_ALERTA = 1;


    MiClase clase = new MiClase();
    public ArrayList<UltimasGest> _datos = new ArrayList<UltimasGest>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listado_ult_gestiones);

        custid = getIntent().getExtras().getString("custid");

        TextView txt_custid = findViewById(R.id.txt_ultgest_Custid);
        txt_custid.setText(custid);

        ultimasGestiones();
    }

    public void ultimasGestiones() {

        //ÚLTIMAS GESTIONES - JAPP - 01-04-2016 - INICIO
        Cursor cursorGestiones = null;
        String sql;

        String rutaBD_USRCOB=listadoUltiGestiones.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        //String rutaBD_Cobrador=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
        String rutaBD_CobradorDatos=listadoUltiGestiones.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        sql = "SELECT custid,Descr,Latitud,Longitud FROM UltCoor Where custid like '%" + custid.trim() + "%'";
        try {
            cursorGestiones = clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR_DATOS, MiClase.TypeSQL.SELECT,rutaBD_CobradorDatos);

            if(cursorGestiones!=null)
            {
                _datos.clear();
                while (cursorGestiones.moveToNext())
                {
                    if(cursorGestiones.getDouble(2)==0.0 || cursorGestiones.getDouble(3)==0.0){
                        UltimasGest datox = new UltimasGest(cursorGestiones.getString(0), cursorGestiones.getString(1) + " - Sin Datos", cursorGestiones.getDouble(2),
                                cursorGestiones.getDouble(3));
                        _datos.add(datox);
                    }
                    else{
                        UltimasGest datox = new UltimasGest(cursorGestiones.getString(0), cursorGestiones.getString(1), cursorGestiones.getDouble(2),
                                cursorGestiones.getDouble(3));
                        _datos.add(datox);
                    }


                }
            }
            cursorGestiones.close();//MVLA
        } catch (Exception e) {
            message("Error Última Gestión ", e.toString());
        }
        //ÚLTIMO HISTORIAL - JAPP - 01-04-2016 - FIN



        ListView listaUltGest = findViewById(R.id.ultgest);

        try {

            AdaptadorDatos adaptador = new AdaptadorDatos(listadoUltiGestiones.this);
            listaUltGest.setAdapter(adaptador);
        } catch (Exception e) {
            message("Compras del Cliente", e.toString());
        }

        //SE AGREGA EL EVENTO PARA QUE ABRA LA VISTA PARA DECOMISAR ARTÍCULO - JAPP - 14-03-2016
        listaUltGest.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                if(_datos.get(arg2).obtenerLatitud() != 0.0 || _datos.get(arg2).obtenerLongitud() != 0.0){
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + _datos.get(arg2).obtenerLatitud() + "," + _datos.get(arg2).obtenerLongitud() + "(" + _datos.get(arg2).obtenerCustid() + ")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                }
            }
        });
    }

    class AdaptadorDatos extends ArrayAdapter {

        Activity context;

        AdaptadorDatos(Activity context) {
            super(context, R.layout.lista_gestiones, _datos);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View item = convertView;
            ViewHolder holder;

            if (item == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                item = inflater.inflate(R.layout.lista_gestiones, null);

                holder = new ViewHolder();
                holder.idCliente = item.findViewById(R.id.custid);
                holder.descripción = item.findViewById(R.id.historialgest);
                holder.latitud = item.findViewById(R.id.latitud);
                holder.longitud = item.findViewById(R.id.longitud);

                item.setTag(holder);
            } else {
                holder = (ViewHolder) item.getTag();
            }

            holder.idCliente.setText(_datos.get(position).obtenerCustid());
            holder.descripción.setText(_datos.get(position).obtenerhistorialgest());
            holder.latitud.setText(String.valueOf(_datos.get(position).obtenerLatitud()));
            holder.longitud.setText(String.valueOf(_datos.get(position).obtenerLongitud()));

            return (item);
        }
    }

    static class ViewHolder {

        TextView idCliente;
        TextView descripción;
        TextView latitud;
        TextView longitud;
    }

    public void message(String head, String text) {
        titulo = head;
        texto = text;
        showDialog(DIALOGO_ALERTA);

    }
}

