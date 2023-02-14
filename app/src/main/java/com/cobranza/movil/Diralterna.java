package com.cobranza.movil;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import Clases.MiClase;

public class Diralterna extends Activity {

    String custid = "";
    MiClase clase = new MiClase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diralterna);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        custid = getIntent().getExtras().getString("custid");
        traeDatos(custid);
    }

    public void traeDatos(String custid){

        Cursor c = null;
        int Cont = 1;

        String direccion = "";

        EditText tel1 = findViewById(R.id.Dir1_tel1);
        EditText tel2 = findViewById(R.id.Dir1_tel2);
        EditText dir1 = findViewById(R.id.Dir1_direccion);
        EditText tel21 = findViewById(R.id.Dir2_tel1);
        EditText tel22 = findViewById(R.id.Dir2_tel2);
        EditText dir21 = findViewById(R.id.Dir2_direccion);

        String rutaBD_USRCOB=Diralterna.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        //String rutaBD_Cobrador=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
        String rutaBD_CobradorDatos=Diralterna.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        c= clase.Dir_Alterna(custid.trim(),rutaBD_CobradorDatos);

        if (c!=null){
            Log.i("Entra", "Entra ------fffffff");
            while(c.moveToNext()){

                direccion = c.getString(1).trim() +"\n"+ c.getString(0).trim();
                Log.i("Entra", direccion);

                if (Cont == 1){
                    tel1.setText(c.getString(2));
                    tel2.setText(c.getString(3));
                    dir1.setText(direccion);
                }else if (Cont == 2){
                    tel21.setText(c.getString(2));
                    tel22.setText(c.getString(3));
                    dir21.setText(c.getString(0) +  c.getString(1) );
                }
                Cont +=1;
            }
        }
    }
}
