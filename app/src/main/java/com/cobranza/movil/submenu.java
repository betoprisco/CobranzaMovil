package com.cobranza.movil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import Clases.MiClase;
import Clases.datos;

public class submenu extends Activity
    {
        public int idmenu;
        String valor="";
        ArrayList<datos> _datos = new ArrayList<datos>();
        @Override
        public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submenu);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//---- PARA NO ROTAR PANTALLA

        idmenu=getIntent().getExtras().getInt("idmenu");

        final int idmenu=getIntent().getExtras().getInt("idmenu");
        String strSql ="";
        switch (idmenu)
        {
            case 0:
                valor="COLONIA";
                strSql="select colonia,count(*) from customer group by colonia";
                break;
            case 1:
                valor="PERIODOSV";
                strSql="select PeriodVencid,count(*) from dsCob group by PeriodVencid order by PeriodVencid desc ";
                break;
            case 2:
                strSql="";
                break;
        }

        SQLiteDatabase db= null;
        try
        {
            /******************************/
            /*Lectura del archivo CONFIG*/
            String rutaBD_USRCOB=submenu.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            String rutaBD_Cobrador=submenu.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

            db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        catch(Exception e){MessageBox("SUBMENU: "+e.getMessage() + "--open");}
        try
        {
            Cursor c = db.rawQuery(strSql,null);
            if (c!=null)
            {
                while(c.moveToNext())
                {
                    datos datox = new datos(c.getString(0),"Clientes(" + c.getString(1)+ ")","" );
                    _datos.add(datox);
                }
            }
            db.close();
        }
        catch (Exception e)
        {
            MessageBox(e.getMessage());
        }

        AdaptadorDatos adaptador= new AdaptadorDatos(this);
        ListView LstSubmenu = findViewById(R.id.LstSubmenu);
        LstSubmenu.setAdapter(adaptador);

        LstSubmenu.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(submenu.this,Listacliente2.class);
                intent.putExtra("idmenu", idmenu);
                intent.putExtra("valor", valor);
                intent.putExtra("filtro", _datos.get(arg2).getId());
                startActivity(intent);
            }

        });
    }
        @SuppressWarnings("unchecked")
        class AdaptadorDatos extends ArrayAdapter {

            Activity context;

            AdaptadorDatos(Activity context) {
                super(context, R.layout.listitem_datos, _datos);
                this.context = context;
            }

            public View getView(int position, View convertView, ViewGroup parent) {

                View item = convertView;
                ViewHolder holder;

                if(item == null)
                {
                    LayoutInflater inflater = context.getLayoutInflater();
                    item = inflater.inflate(R.layout.listitem_datos, null);

                    holder = new ViewHolder();
                    holder.Id=item.findViewById(R.id.LblId);
                    holder.dato=item.findViewById(R.id.LblDato);
                    holder.dato2=item.findViewById(R.id.LblDato2);

                    item.setTag(holder);
                }
                else
                {
                    holder = (ViewHolder)item.getTag();
                }

                holder.Id.setText((_datos.get(position).getId()));
                holder.dato.setText(_datos.get(position).getDato());
                holder.dato2.setText(_datos.get(position).getDato2());

                return(item);
            }
        }

        static class ViewHolder {

            TextView Id;
            TextView dato;
            TextView dato2;
        }
        public void MessageBox(String message){
    }
}
