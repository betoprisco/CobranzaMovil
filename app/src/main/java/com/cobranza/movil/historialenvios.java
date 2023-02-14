package com.cobranza.movil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import Clases.MiClase;
import Clases.datos;

public class historialenvios extends Activity {

    ArrayList<datos>_datos = new ArrayList<datos>();
    String Tipo="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historial_envios);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.historial_envio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comentario:
                MuestraDatos("Select PerspectivaDet || ' - ' || ' Status: '|| ' ' ||Status, FechaCrt, ' / ' || fechaEnvio , 'Id: '|| ' ' ||custid  from historial  " );
                Toast.makeText(getBaseContext(), "Comentarios!" , Toast.LENGTH_SHORT).show();
                Tipo="COMENTARIO";
                return true;
            case R.id.abonos:
                MuestraDatos("Select 'Abono: '|| ' ' || monto,Nombre,'    Status: '|| ' ' || Status, folio from Abonos " );
                Log.i("HISTORIAL ENVIO", "******************************");
                Tipo="ABONO";
                return true;
            case R.id.decomisos:
                MuestraDatos("Select 'Artículo: '|| ' ' || Art,'Id: '|| ' ' ||Custid,'    Status: '|| ' ' || EstadoEnvio, FolioDecomiso from decomisos " );
                Tipo="DECOMISO";
                return true;
            case R.id.convenio:
                MuestraDatos("Select ' Custid: '|| ' ' || Custid,' Opcion: '|| ' ' ||opcion,', Estatus: '|| ' ' || estatus, ' fecha creacion: '|| ' ' || fechaCRT from convenio " );
                Tipo="CONVENIO";
            default:
                return super.onOptionsItemSelected(item);
            //.93
        }
    }

    public void MuestraDatos(String cad){

        /******************************/
        /*Lectura del archivo CONFIG*/
        String rutaBD_USRCOB=historialenvios.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/
        try{
            String strSql=cad;

            Log.i("HISTORIAL ENVIO", cad);
            SQLiteDatabase db= null;

            String rutaBDCobradores=historialenvios.this.getApplicationContext().getDatabasePath("cobradores").toString();

            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Cursor c = db.rawQuery(strSql, null);

            if(c!=null)
            {
                Log.i("_DATOS", "Entra");

                _datos.clear();
                while (c.moveToNext())
                {
                    datos datox= new datos(c.getString(3), c.getString(0),c.getString(1)+c.getString(2) +" \n " );
                    _datos.add(datox);

                    Log.i("_DATOS", c.getString(3));
                }
            }
            db.close();
        }
        catch(Exception e)
        {
            Toast toast2 = Toast.makeText(getBaseContext(), " error 88: Buscando Corte "+ e.getMessage()+ Cobrador , Toast.LENGTH_LONG);
            toast2.show();

        }//fin try
        ListView lstcliente = findViewById(R.id.LstEnvios);
        try{
            AdaptadorDatos adaptador= new AdaptadorDatos(historialenvios.this);
            lstcliente.setAdapter(adaptador);
        }catch(Exception e)
        {
            Toast toast2 = Toast.makeText(getBaseContext(), " error 88.2: llama lista Corte "+ e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }//fin try 2

        lstcliente.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text
                Toast.makeText(getApplicationContext(), _datos.get(position).getId(), Toast.LENGTH_SHORT).show();
                if(Tipo.contains("DECOMISO")){
                    Intent intent = new Intent(historialenvios.this,ReimprimeDecomisos.class);
                    intent.putExtra("folioDecomiso", _datos.get(position).getId());
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(historialenvios.this,Reimpresion.class);
                    intent.putExtra("folio", _datos.get(position).getId());
                    intent.putExtra("idmenu", 3);
                    intent.putExtra("Tipo", Tipo);

                    startActivity(intent);
                }
            }
        });
    }

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
}
