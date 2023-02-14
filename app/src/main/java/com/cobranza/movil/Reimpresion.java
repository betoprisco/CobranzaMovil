package com.cobranza.movil;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import Clases.MiClase;
import com.cobranza.impresora.PrinterActivity;

public class Reimpresion extends Activity {
    String Folio,Tipo,custid,Cobrador;

    private static final int MNU_OPC1 = 1;
    private static final int MNU_OPC2 = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reimpresion);
        /*recibo parametros*/

        Folio = getIntent().getExtras().getString("folio");
        Tipo=getIntent().getExtras().getString("Tipo");

        BuscaDatosFolio();
    }
    public void BuscaDatosFolio(){
        MiClase clase=new MiClase();
        try{
            Folio=Folio.replace("Folio: ", "");
            String sql="select monto, custid, Status,nombre from Abonos where folio=trim('"+ Folio +"')";

            Toast.makeText(getBaseContext(), Folio, Toast.LENGTH_LONG).show();

            String rutaBDCobradores=Reimpresion.this.getApplicationContext().getDatabasePath("cobradores").toString();

            Cursor c = clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES, MiClase.TypeSQL.SELECT,rutaBDCobradores);

            TextView txt_custid = findViewById(R.id.Reimpresion_lbl_custid );
            TextView txt_Abono = findViewById(R.id.Reimpresion_lbl_Abono );
            TextView txt_Status = findViewById(R.id.Reimpresion_lbl_StatusEnvio );
            TextView txt_cliente = findViewById(R.id.Reimpresion_lbl_Cliente);
            if(c!=null)
            {
                while (c.moveToNext()){
                    custid=c.getString(1);
                    txt_custid.setText(c.getString(1));
                    txt_Abono.setText(c.getString(0));
                    txt_Status.setText(c.getString(2));
                    txt_cliente.setText(c.getString(3));
                }
            }
        }
        catch(Exception e)
        {
            Toast toast2 = Toast.makeText(getBaseContext(), " error 88: Buscando Corte "+ e.getMessage() , Toast.LENGTH_LONG);
            toast2.show();
        }//fin try
    }
    @Override
    public  boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MNU_OPC1, Menu.NONE, "IMPRIMIR").setIcon(R.mipmap.printer);

        menu.add(Menu.NONE, MNU_OPC2, Menu.NONE, "envio").setIcon(R.mipmap.icon);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MNU_OPC1:
                /******************************/
                /*Lectura del archivo CONFIG*/
                String rutaBD_USRCOB=Reimpresion.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
                /*Lectura del archivo CONFIG*/
                MiClase clase = new MiClase();
                Cobrador=clase.Lectura(rutaBD_USRCOB);
                /*****************************/

                if(Tipo.equals("ABONO")){
                    Intent intent1 = new Intent(Reimpresion.this, PrinterActivity.class);
                    intent1.putExtra("TIPO", "REIMPRIME");
                    intent1.putExtra("custid", custid);
                    intent1.putExtra("folio", Folio);
                    intent1.putExtra("sql", "");
                    startActivity(intent1);
                }



                return true;
            case MNU_OPC2:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
