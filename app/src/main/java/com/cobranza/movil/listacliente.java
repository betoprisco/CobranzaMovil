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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import Clases.MiClase;
import Clases.datos;

public class listacliente extends Activity
{
    public int idmenu;
    public String filtro;
    public String strSql="";

    ArrayList<datos>_datos = new ArrayList<datos>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listacliente);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //--- para no rotar pantalla

        Button btnBusca = findViewById(R.id.btn_BuscaClienteLista);
        btnBusca.setOnClickListener(BuscaCli);

        try{
            filtro=getIntent().getExtras().getString("filtro");
            idmenu=getIntent().getExtras().getInt("idmenu");

            switch(idmenu)
            {
                case 0:
                    strSql="Select custid,direccion,colonia,nombre from customer where colonia like '" + filtro ;
                    strSql = strSql+ "' and custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    strSql = strSql+ " order by direccion";
                    break;
                case 1:
                    strSql="select a.custid,a.direccion,a.colonia,a.nombre from customer a inner join dsCob b on trim(a.custid)=trim(b.custid) where b.PeriodVencid= "+filtro;
                    strSql = strSql+ " and a.custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    strSql=strSql+" order by a.colonia,a.direccion";
                case 2:break;

                case 3:
                    strSql="Select custid,direccion,colonia,nombre from customer ";
                    strSql = strSql+ "  where custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    break;
            }

            SQLiteDatabase db= null;

            /******************************/
            /*Lectura del archivo CONFIG*/
            String rutaBD_USRCOB=listacliente.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            String rutaBD_Cobrador=listacliente.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
            //String rutaBD_CobradorDatos=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

            db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            //db = SQLiteDatabase.openDatabase("/sdcard/"+Cobrador+".zip",null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Cursor c = db.rawQuery(strSql, null);

            if(c!=null)
            {
                while (c.moveToNext())
                {
                    datos datox= new datos(c.getString(0),c.getString(3), c.getString(1)+c.getString(2) +" \n " );
                    _datos.add(datox);
                }
            }
            db.close();
        }
        catch(Exception e)
        {
            Toast toast2 = Toast.makeText(getBaseContext(), "id menu: "+idmenu +" error 1: Lista Cliente "+ e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }

        ListView lstcliente = findViewById(R.id.LstCliente);
        AdaptadorDatos adaptador= new AdaptadorDatos(this);
        lstcliente.setAdapter(adaptador);

        lstcliente.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                Toast.makeText(getBaseContext(), " custid lista inicial ", Toast.LENGTH_LONG);

                Intent intent = new Intent(listacliente.this,clientedet.class);
                intent.putExtra("custid", _datos.get(arg2).getId());
                intent.putExtra("idmenu", idmenu);
                intent.putExtra("filtro", filtro);
                startActivity(intent);
            }

        } );
    }
    private View.OnClickListener Regresa = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(listacliente.this, submenu.class );
            intent.putExtra("idmenu", idmenu);
            startActivity(intent);
        }



    };

    private View.OnClickListener BuscaCli = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            TextView cliente = findViewById(R.id.txt_ClienteLista);

            switch(idmenu)
            {
                case 0:
                    strSql="Select custid,direccion,colonia,nombre from customer where colonia like '" + filtro ;
                    strSql = strSql+ "' and custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    strSql = strSql+ " and (custid like '%"+ cliente.getText()+"%' or nombre like '%"+ cliente.getText()+"%')";
                    strSql = strSql+ " order by direccion";
                    break;
                case 1:
                    strSql="select a.custid,a.direccion,a.colonia,a.nombre from customer a inner join dsCob b on trim(a.custid)=trim(b.custid) where b.PeriodVencid= "+filtro;
                    strSql = strSql+ " and a.custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    strSql = strSql+ " and (custid like '%"+ cliente.getText()+"%' or nombre like '%"+ cliente.getText()+"%')";
                    strSql=strSql+" order by a.colonia,a.direccion";
                case 2:break;

                case 3:
                    strSql="Select custid,direccion,colonia,nombre from customer ";
                    strSql = strSql+ " and (custid like '%"+ cliente.getText()+"%' or nombre like '%"+ cliente.getText()+"%')";
                    strSql = strSql+ "  where custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    break;
            }
            SQLiteDatabase db= null;
            //try{
            /******************************/
            /*Lectura del archivo CONFIG*/
            String rutaBD_USRCOB=listacliente.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);

            String rutaBD_Cobrador=listacliente.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

            /*****************************/
            db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null
                    , SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
    };

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