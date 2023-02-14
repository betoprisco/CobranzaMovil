package com.cobranza.movil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

import Clases.MiClase;

public class Diralta extends Activity {
    String cliente, Custid = "";

    MiClase clase = new MiClase();
    Calendar calendario = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diralta);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cliente = getIntent().getExtras().getString("cliente");
        Custid = getIntent().getExtras().getString("custid");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        getMenuInflater().inflate(R.menu.nueva_dir_alterna, menu);
        return true;
    }

    @SuppressLint("NewApi")
    public boolean onOptionsItemSelected(MenuItem item) {

        String sql, respuesta = "";
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_WEEK);
        int min = calendario.get(Calendar.MINUTE);
        int seg = calendario.get(Calendar.SECOND);

        String rutaBD_USRCOB=Diralta.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        String Cobrador = clase.Lectura(rutaBD_USRCOB);

        String year = Integer.toString(anio);

        year=year.substring(2,2);

        String idreg = year  + "" + mes + ""+ dia + "" + min + ""+ seg;

        Log.i("id del registro", cliente);
        EditText tel1 = findViewById(R.id.Cl_tel1);
        EditText tel2 = findViewById(R.id.Cl_tel2);
        EditText colonia = findViewById(R.id.Cl_colonia);
        EditText direccion = findViewById(R.id.Cl_direccion);

        switch (item.getItemId()) {
            case R.id.guardar:
                if (tel1.getText().toString().isEmpty()){
                    Toast.makeText(this,"Debe llenar el Campo Teléfono 1" , Toast.LENGTH_LONG).show();
                }else{

                    String ruta=Diralta.this.getApplicationContext().getDatabasePath("cobradores").toString();

                    sql = "Insert into dirAlterna(Custid, Nombre, Addr1,Addr2, Fax, Phone,FechaCrt,IdRegistro,Status,Cobrador) " +
                            "Values('" + Custid + "','" + cliente + "','" + direccion.getText() + "','" + colonia.getText() + "','" + tel1.getText() + "','" + tel2.getText()
                            + "', datetime(current_timestamp, 'localtime'), '" + idreg +  "','ESPERA','" + Cobrador.trim() + "')";
                    respuesta =  clase.EjecutaSQLUpdate(sql, MiClase.TypeBase.COBRADORES, MiClase.TypeSQL.UPDATE,ruta);
                    Toast.makeText(this,respuesta , Toast.LENGTH_LONG).show();

                    if (respuesta.equals("Actualizado")){
                        Toast.makeText(this,"Entro a actualizado..." , Toast.LENGTH_LONG).show();
                        this.finish();
                    }else{
                        Toast.makeText(this,"Entro al Else..." , Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            case R.id.regresar:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}