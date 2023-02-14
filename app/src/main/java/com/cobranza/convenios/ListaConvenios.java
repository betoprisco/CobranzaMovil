package com.cobranza.convenios;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranza.movil.Listacliente2;
import com.cobranza.movil.R;
import com.cobranza.movil.inicio;

import java.util.ArrayList;

import Clases.MiClase;
import Clases.datos;

public class ListaConvenios extends Activity {

    public ArrayList<datos>_datos = new ArrayList<datos>();
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_convenios);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        BuscarClientes();
    }

    public void BuscarClientes(){
        String consulta;

        MiClase funciones = new MiClase();
        Cursor c = null;

        String rutaBD_USRCOB= ListaConvenios.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        String rutaBD_Cobrador=ListaConvenios.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
        //String rutaBD_CobradorDatos=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        consulta="select custid,FechaCrtMov,Status from convenios order by custid ";
        c = funciones.EjecutaSQL(consulta, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

        if (c.getCount()>0){
            while(c.moveToNext()){
                datos datox = new datos(c.getString(0),c.getString(1),c.getString(2) + " \n");
                _datos.add(datox);
            }

            ListView listaClientes =  findViewById(R.id.LstCliente);

            AdaptadorDatos adaptador = new AdaptadorDatos(ListaConvenios.this);
            listaClientes.setAdapter(adaptador);

            listaClientes.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                    Intent intent = new Intent(ListaConvenios.this,DetalleConvenio.class);
                    intent.putExtra("custid",_datos.get(arg2).getId());
                    startActivity(intent);
                }
            });
        }
        else{
            Toast mensaje = Toast.makeText(getBaseContext(),"No hay convenios",Toast.LENGTH_LONG);
            mensaje.show();
        }
    }

    class AdaptadorDatos extends ArrayAdapter {
        Activity context;

        AdaptadorDatos(Activity context){
            super(context,R.layout.listitem_datos,_datos);
            this.context=context;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            View item = convertView;
            ViewHolder holder;

            if(item==null){
                LayoutInflater inflater = context.getLayoutInflater();
                item = inflater.inflate(R.layout.listitem_datos, null);

                holder=new ViewHolder();
                holder.Id=item.findViewById(R.id.LblId);
                holder.dato=item.findViewById(R.id.LblDato);
                holder.dato2=item.findViewById(R.id.LblDato2);

                item.setTag(holder);
            }
            else
                holder=(ViewHolder)item.getTag();

            holder.Id.setText((_datos.get(position).getId()));
            holder.dato.setText(_datos.get(position).getDato());
            holder.dato2.setText(_datos.get(position).getDato2());

            return(item);
        }
    }
    static class ViewHolder{
        TextView Id;
        TextView dato;
        TextView dato2;
    }
}
