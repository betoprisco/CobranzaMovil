package com.cobranza.movil;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import Clases.MiClase;
import Clases.MiClase_DatosCliente;

public class ListadoReferenciasCliente extends Activity {
    Cursor CursorReferencias=null;

    public String custid;
    String texto;
    String titulo;

    public ArrayList<Referencia> _datos = new ArrayList<Referencia>();

    private static final int DIALOGO_ALERTA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.referencias);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        custid= getIntent().getExtras().getString("custid");

        TextView txt_custid=findViewById(R.id.txt_Referencias_Custid);
        txt_custid.setText(custid);

        ReferenciasCliente();
    }

    public void ReferenciasCliente(){
        MiClase_DatosCliente clase_datoscli = new MiClase_DatosCliente();

        String rutaBD_USRCOB=ListadoReferenciasCliente.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        String rutaBD_Cobrador=ListadoReferenciasCliente.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        CursorReferencias=null;
        CursorReferencias=clase_datoscli.obtenerReferencias(custid,rutaBD_Cobrador);

        try{
            if(CursorReferencias!=null)
            {
                _datos.clear();
                while (CursorReferencias.moveToNext())
                {
                    //Nombre,Parentesco,Domicilio,Cruzamientos,Tel,Cel
                    Referencia datox= new Referencia(CursorReferencias.getString(0),CursorReferencias.getString(1),CursorReferencias.getString(2), CursorReferencias.getString(3),CursorReferencias.getString(4)
                            , CursorReferencias.getString(5) +" \n ");
                    _datos.add(datox);
                }
            }
        }catch(Exception e){
            message("Referencias del clientes 2.0 ",e.toString());
        }

        ListView listaReferencias = findViewById(R.id.Referencias);

        try{

            AdaptadorDatos adaptador= new AdaptadorDatos(ListadoReferenciasCliente.this);
            listaReferencias.setAdapter(adaptador);
        }catch(Exception e){
            message("Referencias del Cliente 2.1",e.toString());
        }
    }

    @SuppressWarnings("deprecation")
    public void message(String head, String text){
        titulo=head;
        texto=text;
        showDialog(DIALOGO_ALERTA);
    }

    class AdaptadorDatos extends ArrayAdapter {

        Activity context;

        AdaptadorDatos(Activity context) {
            super(context, R.layout.lista_referencias, _datos);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View item = convertView;
            ViewHolder holder;

            if(item == null)
            {
                LayoutInflater inflater = context.getLayoutInflater();
                item = inflater.inflate(R.layout.lista_referencias, null);

                holder = new ViewHolder();

                holder.nombre=item.findViewById(R.id.nombreReferencia);
                holder.parentesco=item.findViewById(R.id.parentescoReferencia);
                holder.domicilio=item.findViewById(R.id.domicilioReferencia);
                holder.cruzamientos=item.findViewById(R.id.cruzamientosReferencia);
                holder.teléfono=item.findViewById(R.id.telReferencia);
                holder.celular=item.findViewById(R.id.celularReferencia);

                item.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)item.getTag();
            }

            holder.nombre.setText(_datos.get(position).obtenerNombre());
            holder.parentesco.setText(_datos.get(position).obtenerParentesco());
            holder.domicilio.setText(_datos.get(position).obtenerDomicilio());
            holder.cruzamientos.setText(_datos.get(position).obtenerCruzamientos());
            holder.teléfono.setText(_datos.get(position).obtenerTeléfono());
            holder.celular.setText(_datos.get(position).obtenerCelular());

            return(item);
        }
    }

    static class ViewHolder {

        TextView nombre;
        TextView parentesco;
        TextView domicilio;
        TextView cruzamientos;
        TextView teléfono;
        TextView celular;
    }
}
