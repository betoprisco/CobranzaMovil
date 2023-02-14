package com.cobranza.movil;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranza.impresora.PrinterActivity;

import Clases.MiClase;

public class clientedet extends Activity
{
    public String filtro;
    public int idmenu;
    public String custid;
    String Cobrador;
    private static final int MNU_OPC1 = 1;
    private static final int MNU_OPC3 = 3;
    private static final int MNU_OPC4 = 4;

    TextView txtMora;

    @Override
    public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clientedet);

        try{
            filtro=getIntent().getExtras().getString("filtro");
            idmenu=getIntent().getExtras().getInt("idmenu");
            custid= getIntent().getExtras().getString("custid");

            Button boton =  findViewById(R.id.btn_AgregaComen );
            boton.setOnClickListener(IraComentarioAdd);

            TextView txtnombre = findViewById(R.id.txtnombre);
            TextView txtdireccion = findViewById(R.id.txtdireccion);


            TextView txtsaldov = findViewById(R.id.txtsaldov);
            TextView txtperiodvencid = findViewById(R.id.txtperiodvencid);
            TextView txtnumletvenc = findViewById(R.id.txtnumletvenc);
            TextView txtutlpago = findViewById(R.id.txtultpago);

            TextView txtReq = findViewById(R.id.txt_Req);
            TextView txtUltAbono = findViewById(R.id.txtultAbono);
            TextView lbl_UltAbono = findViewById(R.id.lbl_UltAbono);
            txtMora=findViewById(R.id.txt_Mora);

            SQLiteDatabase db = null;

            /******************************/
            /*Lectura del archivo CONFIG*/
            String rutaBD_USRCOB=clientedet.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            String rutaBD_Cobrador=clientedet.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

            db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            String strSql;
            strSql="select Custid,nombre, direccion,colonia	from customer where custid like  '%" + custid + "%'";

            Cursor c = db.rawQuery(strSql,null);

            if (c!=null)
            {
                c.moveToFirst();
                txtnombre.setText(c.getString(1));
                txtdireccion.setText(c.getString(2) + "," + c.getString(3));
                custid=c.getString(0);
            }
            strSql=" SELECT ifnull(Vencido,0) Vencido,periodvencid,numletvenc,ultpago,Ifnull(Moratorios, '0') as Moratorios ," +
                    " PagoReq,CtaPag,Paydate	FROM dscob where custid like '%" + custid + "%'";
            c=db.rawQuery(strSql, null);
            if(c!=null)
            {
                c.moveToFirst();

                txtsaldov.setText(Double.toString(Redondear(Double.parseDouble(c.getString(0)),1)));
                txtperiodvencid.setText(c.getString(1));
                txtnumletvenc.setText(c.getString(2));
                txtutlpago.setText(c.getString(3));

                txtMora.setText(Double.toString(Redondear(Double.parseDouble(c.getString(4)),1)));
                txtReq.setText(Double.toString(Redondear(Double.parseDouble(c.getString(5)),2)));

                if (Double.valueOf(c.getString(6)).doubleValue()>0) {
                    txtutlpago.setText(c.getString(7));

                    txtUltAbono.setText(String.valueOf( (Double.valueOf(c.getString(6)).doubleValue()+Double.valueOf(c.getString(4)).doubleValue())));
                    txtUltAbono.setVisibility(View.VISIBLE);
                    lbl_UltAbono.setVisibility(View.VISIBLE);
                }
            }
            db.close();
        } catch (Exception e) {
            Toast toast = Toast.makeText(getBaseContext(), "ERROR1: Clientedet "+ e.getMessage().toString(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static double Redondear(double numero,int digitos)
    {
        return Math.ceil(numero);
    }

    private View.OnClickListener IraComentarioAdd = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(clientedet.this, ComentarioAdd.class );
            intent.putExtra("Custid", custid);
            intent.putExtra("idmenu", idmenu);
            intent.putExtra("filtro", filtro);
            startActivity(intent);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MNU_OPC1, Menu.NONE, "COMENTARIO").setIcon(R.mipmap.pasteselected);
        menu.add(Menu.NONE, MNU_OPC3, Menu.NONE, "IR A INICIO").setIcon(R.mipmap.icon );
        menu.add(Menu.NONE, MNU_OPC4, Menu.NONE, "IMPRIMIR").setIcon(R.mipmap.printer);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case MNU_OPC1:
                intent.setClass(clientedet.this, ComentarioAdd.class );
                intent.putExtra("Custid", custid);
                intent.putExtra("idmenu", idmenu);
                intent.putExtra("filtro", filtro);
                startActivity(intent);

                return true;
            case MNU_OPC3:
                Intent intent3 = new Intent();
                intent3.setClass(clientedet.this, inicio.class );

                startActivity(intent3);
                return true;
            case MNU_OPC4:
                Intent intent4 = new Intent();
                intent4.setClass(clientedet.this, PrinterActivity.class );
                intent4.putExtra("custid", custid);
                intent4.putExtra("TIPO", "EDOCTA");
                intent4.putExtra("folio", "");
                startActivity(intent4);
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}