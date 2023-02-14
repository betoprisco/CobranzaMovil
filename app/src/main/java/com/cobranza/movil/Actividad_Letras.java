package com.cobranza.movil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import Clases.MiClase;
import com.cobranza.impresora.PrinterActivity;

public class Actividad_Letras extends Activity {
    public String filtro;
    public int idmenu;
    public String custid;
    public String cliente;
    public String tlvencidas;

    String texto;
    String titulo;
    String pago_requerido,liquideComparar="0";
    String morat;
    String montoCovid19;
    Double MontoSumarPagReq=0.0;

    private static final int DIALOGO_ALERTA = 1;

    MiClase clase = new MiClase();

    Button verMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad__letras);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        filtro=getIntent().getExtras().getString("filtro");
        idmenu=getIntent().getExtras().getInt("idmenu");
        custid= getIntent().getExtras().getString("custid").trim();

        //BOTÓN PARA ABRIR GOOGLE MAPS - JAPP - 21-01-2020
        verMapa=findViewById(R.id.buttonVerGPS);
        verMapa.setOnClickListener(verListaGest);

        DatosCliente(custid);
        //LetrasCliente(custid);
        Button boton =  findViewById(R.id.btn_ActividadLetras_btnLista);
        boton.setOnClickListener(VerLetras);

        //para convenio de cobranza
        Button btconvenio =  findViewById(R.id.btn_Convenio);
        btconvenio.setOnClickListener(verConvenio);

        Button Bdiralt = findViewById(R.id.bt_VerDir);
        Bdiralt.setOnClickListener(verDireccion);

        Button Baltadir = findViewById(R.id.bt_agregaDir);
        Baltadir.setOnClickListener(AgregaDir);

        //PARA IMPRIMIR EL AVISO DEL CLIENTE - JAPP - 08-02-2016
        Button botónAviso = findViewById(R.id.bt_ImprimirAviso);
        botónAviso.setOnClickListener(verAviso);

        //BOTÓN PARA MOSTRAR LAS REFERENCIAS DEL CLIENTE - JAPP - 18-02-2016
        Button botónReferencias = findViewById(R.id.bt_VerReferencias);
        botónReferencias.setOnClickListener(verReferencias);

        //BOTÓN PARA MOSTRAR COMPRAS DEL CLIENTE - JAPP - 25-02-2016
        Button botónCompras = findViewById(R.id.bt_VerCompras);
        botónCompras.setOnClickListener(verCompras);
    }

    private View.OnClickListener verListaGest = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intento = new Intent();
            intento.putExtra("custid", custid);
            intento.setClass(Actividad_Letras.this, listadoUltiGestiones.class);
            startActivity(intento);

        }
    };



    //PARA IMPRIMIR EL AVISO DEL CLIENTE - JAPP - 08-02-2016 - INICIO
    private View.OnClickListener verAviso=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            Cursor c = null;

            String rutaBD_USRCOB=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            String rutaBD_Cobrador=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
            //String rutaBD_CobradorDatos=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

            c = clase.aviso(custid,rutaBD_Cobrador);

            if (c !=null){
                if (c.getCount()>0){
                    Intent intent = new Intent(Actividad_Letras.this, PrinterActivity.class);
                    intent.putExtra("TIPO", "AVISO");
                    intent.putExtra("custid", custid);

                    startActivity(intent);
                    Toast.makeText(getBaseContext(), "Imprimir Aviso", Toast.LENGTH_LONG ).show();
                }
                else
                    Toast.makeText(getBaseContext(), "¡El cliente no tiene un aviso asignado! " + custid, Toast.LENGTH_LONG ).show();
            }else{
                Toast.makeText(getBaseContext(), "¡El cliente no tiene un aviso asignado! " + custid, Toast.LENGTH_LONG ).show();
            }

        }
    };
    //PARA IMPRIMIR EL AVISO DEL CLIENTE - JAPP - 08-02-2016 - FIN

    //PARA MOSTRAS LAS REFERENCIAS DEL CLIENTE - JAPP - 18-02-2016 - INICIO
    private View.OnClickListener verReferencias=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            Cursor c = null;

            String rutaBD_USRCOB=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            //String rutaBD_Cobrador=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
            String rutaBD_CobradorDatos=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

            c = clase.referencias(custid,rutaBD_CobradorDatos);

            if (c !=null){
                if (c.getCount()>0){
                    Intent intent = new Intent(Actividad_Letras.this,ListadoReferenciasCliente.class);
                    intent.putExtra("custid", custid);

                    startActivity(intent);
                    Toast.makeText(getBaseContext(), "Referencias", Toast.LENGTH_LONG ).show();
                }
                else
                    Toast.makeText(getBaseContext(), "¡El cliente no tiene referencias! " + custid, Toast.LENGTH_LONG ).show();
            }else{
                Toast.makeText(getBaseContext(), "¡El cliente no tiene referencias! " + custid, Toast.LENGTH_LONG ).show();
            }

        }
    };
    //PARA IMPRIMIR LAS REFERENCIAS DEL CLIENTE - JAPP - 18-02-2016 - FIN

    //PARA MOSTRAS LAS COMPRAS DEL CLIENTE - JAPP - 25-02-2016 - INICIO
    private View.OnClickListener verCompras=new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            Cursor c = null;

            String rutaBD_USRCOB=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            //String rutaBD_Cobrador=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
            String rutaBD_CobradorDatos=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

            c = clase.items(custid,rutaBD_CobradorDatos);

            if (c !=null){
                if (c.getCount()>0){
                    Intent intent = new Intent(Actividad_Letras.this,ListadoComprasCliente.class);
                    intent.putExtra("custid", custid);

                    startActivity(intent);
                    Toast.makeText(getBaseContext(), "Artículos", Toast.LENGTH_LONG ).show();
                }
                else
                    Toast.makeText(getBaseContext(), "¡El cliente no tiene artículos! " + custid, Toast.LENGTH_LONG ).show();
            }else{
                Toast.makeText(getBaseContext(), "¡El cliente no tiene artículos! " + custid, Toast.LENGTH_LONG ).show();
            }

        }
    };
    //PARA MOSTRAR LAS COMPRAS DEL CLIENTE - JAPP - 18-02-2016 - FIN

    private View.OnClickListener login=new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(Actividad_Letras.this, login.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener VerLetras=new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent.setClass(Actividad_Letras.this, ListadoLetrasCliente.class);
            intent.putExtra("custid", custid);
            intent.putExtra("cliente", cliente);
            startActivity(intent);
        }

    };

    private View.OnClickListener verConvenio = new View.OnClickListener() {
        //Para convenios
        @Override
        public void onClick(View v) {

            String rutaBD_USRCOB=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            String rutaBDCobradores=Actividad_Letras.this.getApplicationContext().getDatabasePath("cobradores").toString();
            String rutaBD_Cobrador=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";


            if (clase.verConvenio(custid,rutaBD_Cobrador,rutaBDCobradores)){
                message("Convenio","No cumple caracteristicas.");
            }else {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(Actividad_Letras.this, Convenio.class);
                intent.putExtra("custid", custid);
                intent.putExtra("cliente", cliente);
                startActivity(intent);
            }
        }
    };

    private View.OnClickListener AgregaDir = new  View.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intento = new Intent();
            intento.putExtra("custid", custid);
            intento.putExtra("cliente", cliente);
            intento.setClass(Actividad_Letras.this, Diralta.class);
            startActivity(intento);
        }

    };

    private View.OnClickListener verDireccion = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Cursor c = null;

            String rutaBD_USRCOB=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            //String rutaBD_Cobrador=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
            String rutaBD_CobradorDatos=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

            c = clase.Dir_Alterna(custid,rutaBD_CobradorDatos);

            if (c !=null){
                if(c.getCount()>0){
                    Intent intent = new Intent();
                    intent.putExtra("custid", custid);
                    intent.setClass(Actividad_Letras.this, Diralterna.class);
                    startActivity(intent);
                    Toast.makeText(getBaseContext(), "Entra Dirección Alterna", Toast.LENGTH_LONG ).show();
                }
                else
                    Toast.makeText(getBaseContext(), "El cliente no tiene Dirección Alterna" + custid, Toast.LENGTH_LONG ).show();
            }else{
                Toast.makeText(getBaseContext(), "El cliente no tiene Dirección Alterna" + custid, Toast.LENGTH_LONG ).show();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_actividad__letras, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.comentario:

                intent.setClass(Actividad_Letras.this, ComentarioAdd.class );

                intent.putExtra("Custid", custid);
                intent.putExtra("idmenu", idmenu);
                intent.putExtra("filtro", filtro);
                startActivity(intent);

                return true;

            case R.id.abonosEfect:

                Intent intent2 = new Intent();
                intent2.setClass(Actividad_Letras.this, Abonos2.class );
                intent2.putExtra("Custid", custid);
                intent2.putExtra("idmenu", idmenu);
                intent2.putExtra("filtro", filtro);
                intent2.putExtra("tlvencidas", tlvencidas);
                Log.i("tlvencidas", tlvencidas);
                intent2.putExtra("morat", morat);
                //Cliente
                intent2.putExtra("Cliente", cliente);
                intent2.putExtra("PagoReq",pago_requerido);
                intent2.putExtra("metodo", "1");
                intent2.putExtra("montoCovid19", montoCovid19);

                //EL MONTO A COMPARAR DEL LIQUIDE - JAPP - 30-05-2021
                intent2.putExtra("montoLiquide", liquideComparar);
                startActivity(intent2);

                return true;

            case R.id.abonosTarj:
                Intent intent5 = new Intent();
                intent5.setClass(Actividad_Letras.this, Abonos2.class );
                intent5.putExtra("Custid", custid);
                intent5.putExtra("idmenu", idmenu);
                intent5.putExtra("filtro", filtro);
                intent5.putExtra("tlvencidas", tlvencidas);
                Log.i("tlvencidas", tlvencidas);
                intent5.putExtra("morat", morat);
                intent5.putExtra("Cliente", cliente);
                intent5.putExtra("PagoReq",pago_requerido);
                intent5.putExtra("metodo", "0");
                intent5.putExtra("montoCovid19", montoCovid19);

                intent5.putExtra("montoLiquide", liquideComparar);
                startActivity(intent5);

                return true;

            case R.id.irInicio:
                Intent intent3 = new Intent();
                intent3.setClass(Actividad_Letras.this, inicio.class );

                startActivity(intent3);
                return true;

            case R.id.imprimir:

                try{
                    Intent intent4 = new Intent();
                    intent4.setClass(Actividad_Letras.this, PrinterActivity.class );
                    Log.i("Entra Letras", "scanactivity clase");
                    intent4.putExtra("custid", custid);
                    intent4.putExtra("idmenu", idmenu);
                    intent4.putExtra("TIPO", "EDOCTA");
                    //Cliente
                    intent4.putExtra("Cliente",  cliente);
                    intent4.putExtra("PagoReq",  pago_requerido  );
                    intent4.putExtra("folio", "");

                    startActivity(intent4);
                }catch(Exception e){
                    message("menu 1.2",e.toString() + ""  );
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void DatosCliente(String custid){
        TextView txtnombre = findViewById(R.id.ActividadLetras_Cliente);
        TextView txtdireccion = findViewById(R.id.ActividadLetras_direccion);
        TextView txtsaldov = findViewById(R.id.ActividadLetras_saldo);
        TextView txtperiodvencid = findViewById(R.id.ActividadLetras_Periodos);
        TextView txtnumletvenc = findViewById(R.id.ActividadLetras_LetrasV);
        TextView txtutlpago = findViewById(R.id.ActividadLetras_UltPago);
        TextView txtMora = findViewById(R.id.ActividadLetras_moratorios);
        TextView txtReq = findViewById(R.id.ActividadLetras_PagoRequerido);
        TextView txtsaldoT =findViewById(R.id.ActividadLetras_saldoTotal);
        TextView txt_custid = findViewById(R.id.ActividadLetras_Custid);

        TextView textLiquideCon = findViewById(R.id.ActividadLetras_Liquide);

        TextView últimoUsuario = findViewById(R.id.ultUsuario);
        TextView perspectivaDet = findViewById(R.id.perspectiva);
        TextView historial = findViewById(R.id.historial);
        TextView fechaUpd = findViewById(R.id.fecha);
        TextView fechaSeg = findViewById(R.id.fechaSeguimiento);
        TextView monto = findViewById(R.id.montoPago);

        txt_custid.setText(custid);

        String rutaBD_USRCOB=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        String rutaBD_Cobrador=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        Cursor c = null;

        String sql;

        try{
            sql=" SELECT Ifnull(Vencido,0) Vencido,periodvencid,numletvenc,ultpago,Ifnull(Moratorios, '0') as Moratorios , " +
                    " PagoReq,CtaPag,Paydate,Ifnull(SaldoT,0) SaldoT ,MontoSumar	";
            sql+=" FROM dscob where custid like '%" + custid + "%'";

            c=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            if (c!=null){
                c.moveToFirst();

                txtsaldov.setText(Double.toString(clase.Redondear(Double.parseDouble(c.getString(0)),1)));
                txtperiodvencid.setText(c.getString(1));
                txtnumletvenc.setText(c.getString(2));
                tlvencidas = c.getString(2);
                txtutlpago.setText(c.getString(3));
                txtsaldoT.setText(Double.toString(clase.Redondear(Double.parseDouble(c.getString(8)),2))) ;
                txtMora.setText(Double.toString(clase.Redondear(Double.parseDouble(c.getString(4)),1)));
                morat = Double.toString(clase.Redondear(Double.parseDouble(c.getString(4)),1));
                txtReq.setText(Double.toString(clase.Redondear(Double.parseDouble(c.getString(5)),2)));
                pago_requerido= (String) txtReq.getText();

                MontoSumarPagReq=clase.Redondear(Double.parseDouble(c.getString(9)),2);

                MontoSumarPagReq=Double.parseDouble(pago_requerido) + MontoSumarPagReq;

                if (Double.valueOf(c.getString(6)).doubleValue()>0)
                    txtutlpago.setText(c.getString(7));
            }
        }catch(Exception e){
            message("Error DatosCliente 1.1",e.toString());
        };

        sql="select Custid,nombre, direccion,colonia	from customer where custid like  '%" + custid.trim() + "%'";
        try{
            c=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            if (c!=null){
                c.moveToFirst();
                txtnombre.setText(c.getString(1));
                cliente=c.getString(1);
                txtdireccion.setText(c.getString(2) + "," + c.getString(3));
                custid=c.getString(0);
            }else{
                message("Consulta","Sin resultados.");
            }
        }catch(Exception e){
            message("Error DatosCliente 1.0",e.toString());
        }

        //APOYO COVID19 - JAPP - 25-05-2020
        Cursor cursorCovid19;
        sql="select custid from COVID19 where CustID like  '%" + custid.trim() + "%'";
        try{
            cursorCovid19=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            if (cursorCovid19!=null && cursorCovid19.getCount()>0){
                cursorCovid19.moveToFirst();

                //BUSCO EL MONTO - JAPP - 26-05-2020 - ¡FELIZ CUMPLEAÑOS BETO!
                sql="select PM from UltLetraV where CustID like  '%" + custid.trim() + "%'";
                cursorCovid19=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

                cursorCovid19.moveToFirst();
                montoCovid19=(cursorCovid19.getString(0));

            }else{
                montoCovid19="0";
            }
        }catch(Exception e){
            message("Error DatosCliente 1.0",e.toString());
        }

        //LIQUIDE CON - JAPP - 09-02-2016 - INICIO
        Cursor cursorLiquide=null;

        sql="SELECT liquide,MontoAdicional FROM Liquide Where custid like '%" + custid.trim() + "%'";

        String rutaBD_CobradorDatos=Actividad_Letras.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        Double liquide=0.0,montoSumar=0.0;

        try{
            cursorLiquide=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR_DATOS , MiClase.TypeSQL.SELECT,rutaBD_CobradorDatos);

            if (cursorLiquide!=null && cursorLiquide.getCount()>0){
                cursorLiquide.moveToFirst();
                textLiquideCon.setText(Double.toString(clase.Redondear(Double.parseDouble(cursorLiquide.getString(0)),1)));
                liquide=clase.Redondear(Double.parseDouble(cursorLiquide.getString(0)),1);
                montoSumar= clase.Redondear(Double.parseDouble(cursorLiquide.getString(1)),1);

                liquideComparar=Double.toString(liquide + montoSumar);
            }
            else
                liquideComparar=Double.toString(MontoSumarPagReq);
        }catch(Exception e){
            message("Error Liquide Con",e.toString());
        }
        //LIQUIDE CON - JAPP - 09-02-2016 - FIN

        //ÚLTIMO HISTORIAL - JAPP - 01-04-2016 - INICIO
        Cursor cursorGestiones=null;

        sql="SELECT custid,userupd,perspectivadet,historial,fechaupd,fechaseg,montoapagar FROM gestiones Where custid like '%" + custid.trim() + "%'";
        try{
            cursorGestiones=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR_DATOS , MiClase.TypeSQL.SELECT,rutaBD_CobradorDatos);

            if (cursorGestiones!=null&& cursorGestiones.getCount()>0){
                cursorGestiones.moveToFirst();
                últimoUsuario.setText(cursorGestiones.getString(1));
                perspectivaDet.setText(cursorGestiones.getString(2));
                historial.setText(cursorGestiones.getString(3));
                fechaUpd.setText(cursorGestiones.getString(4));
                fechaSeg.setText(cursorGestiones.getString(5));
                monto.setText(cursorGestiones.getString(6));
            }
        }catch(Exception e){
            message("Error Última Gestión ",e.toString());
        }
        //ÚLTIMO HISTORIAL - JAPP - 01-04-2016 - FIN

        //VER COORDENADAS - JAPP - 19-02-2020 - INICIO
        Cursor cursorUltVerif=null;

        sql="SELECT custid,Descr,Latitud,Longitud FROM UltCoor Where custid like '%" + custid.trim() + "%'";
        try{
            cursorUltVerif=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR_DATOS , MiClase.TypeSQL.SELECT,rutaBD_CobradorDatos);

            if (cursorUltVerif!=null&& cursorUltVerif.getCount()>0){
                cursorUltVerif.moveToFirst();

                if (!cursorUltVerif.getString(0).contains("null")){

                    verMapa.setEnabled(true);
                }


            }
        }catch(Exception e){
            message("Error Última Verificación ",e.toString());
        }
    }
    /*
     * CREACION DE LOS DIALOGOS
     * */

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialogo = null;

        switch(id)
        {
            case DIALOGO_ALERTA:
                dialogo = alerta();
                break;
            default:
                dialogo = null;
                break;
        }
        return dialogo;
    }

    private Dialog alerta()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(titulo);
        builder.setMessage(texto);
        builder.setPositiveButton("OK", new Dialog.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }

    public void message(String head, String text){
        titulo=head;
        texto=text;
        showDialog( DIALOGO_ALERTA);
    }
}
