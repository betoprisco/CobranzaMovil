package com.cobranza.convenios;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cobranza.movil.Listacliente2;
import com.cobranza.movil.R;
import com.cobranza.movil.inicio;

import java.text.DecimalFormat;

import Clases.MiClase;

public class DetalleConvenio extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_convenio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String custid;
        custid=getIntent().getExtras().getString("custid");

        BuscarCliente(custid);
    }

    public void BuscarCliente(String idCliente){
        String consulta,qcn1,qcn2,nombreCliente;

        Float PM=null;
        Float total=null;
        int opción = 0;

        MiClase funciones = new MiClase();

        TextView cliente= findViewById(R.id.txt_custidConv);
        TextView PM1= findViewById(R.id.txt_pm);
        TextView opc= findViewById(R.id.txtOpc);
        TextView opc2= findViewById(R.id.txtOpc2);

        RadioButton op1= findViewById(R.id.radioButton1);
        RadioButton op2= findViewById(R.id.radioButton2);

        Cursor c=null;

        String rutaBD_USRCOB= DetalleConvenio.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        String rutaBD_Cobrador=DetalleConvenio.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
        //String rutaBD_CobradorDatos=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        consulta="Select nombre from customer where custid like '%" + idCliente + "%'";
        c=funciones.EjecutaSQL(consulta, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

        if (c.getCount()>0){
            c.moveToFirst();
            nombreCliente=c.getString(0);

            //SI SE ENCONTRÓ EL CLIENTE EN CUSTOMER SE BUSCA LO DEMÁS - JAPP - 31-08-2017
            cliente.setText(idCliente + " - " + nombreCliente);

            DecimalFormat formato=new DecimalFormat("0.00");

            consulta="select pm from letras where trim(custid)=trim('" + idCliente + "') limit 1";
            c=funciones.EjecutaSQL(consulta, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            //**** trae una letra para hacer el calculo ****
            if (c.getCount()>0){
                while(c.moveToNext())
                    PM=Float.valueOf(c.getString(0)).floatValue();
            }

            //**** trae el saldo total ****
            consulta="Select ifnull(SaldoT,0) SaldoT from dscob where custid like '%" + idCliente + "%'";
            c=funciones.EjecutaSQL(consulta, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            if(c.getCount()>0){
                while(c.moveToNext())
                    total=Float.valueOf(c.getString(0)).floatValue();
            }

            consulta="select opcion from convenios where custid like '%" + idCliente + "%'";
            c=funciones.EjecutaSQL(consulta, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            if(c.getCount()>0){
                while(c.moveToNext())
                    opción=Integer.valueOf(c.getString(0));
            }

            if(opción==1){
                qcn1=String.valueOf(formato.format(total/PM));

                op1.setText("Opción 1: " + String.valueOf(PM +200) + ", " + qcn1 + " Quincenas ");
                op1.setChecked(true);

                op2.setVisibility(View.GONE);
            }
            else{
                qcn2=String.valueOf(formato.format(total/PM *2));
                op2.setText("Opción 2: " + String.valueOf((PM *2) +250) + ", " + qcn2 + " Quincenas ");

                op2.setChecked(true);
                op1.setVisibility(View.GONE);
            }

            PM1.setText("Letra: " + String.valueOf(PM) + "Saldo T. " + total);
        }
        else
        {
            //SI NO SE ENCUENTRÓ ES PORQUE PUEDE QUE YA NO LO TENGA ASIGNADO PERO NO TENGA ALGÚN OTRO CONVENIO - JAPP - 31-08-2017
            cliente.setText("No se encontraron datos del cliente, quizás ya no se encuentra asignado a su cartera o no tiene actualizada la base de datos.");
            PM1.setVisibility(View.GONE);
            op2.setVisibility(View.GONE);
            op1.setVisibility(View.GONE);
            opc.setVisibility(View.GONE);
            opc2.setVisibility(View.GONE);
        }
    }
}
