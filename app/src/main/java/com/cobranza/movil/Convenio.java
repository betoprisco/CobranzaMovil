package com.cobranza.movil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import Clases.MiClase;

public class Convenio extends Activity {

    public  String custid;
    public String cliente;
    public String sql,qcn1,qcn2;
    public float PM,total;
    String texto;
    String titulo;
    MiClase clases = new MiClase();
    private static final int DIALOGO_ALERTA = 1, DIALOGO_FINAL=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convenio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //obtengo los datos que se pasaron
        custid = getIntent().getExtras().getString("custid");
        cliente = getIntent().getExtras().getString("cliente");


        TextView cli =  findViewById(R.id.txt_custidConv);
        cli.setText(custid + " - " + cliente);

        //para traer los datos de las opciones
        datos();

        Button aceptar = (Button) findViewById(R.id.bt_acepConv);
        aceptar.setOnClickListener(agrega);
    }

    View.OnClickListener agrega = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            //saco de los campos el valor para la insersion
            String seleccionado = "0";
            String moratorios = "0";
            String pagos = "0";
            String quincenas ="0";
            String saldoT ="0";
            String Letras = "";
            RadioButton r1 = findViewById(R.id.radioButton1);
            RadioButton r2 = findViewById(R.id.radioButton2);

            if(r1.isChecked()){ //Opcion 1
                seleccionado = "1";
                moratorios = "200";
                pagos = String.valueOf(PM + 200);
                quincenas = qcn1;
                saldoT = String.valueOf(total);
                Letras = String.valueOf(PM);
            }else if(r2.isChecked()){//Opcion 2
                seleccionado = "2";
                moratorios = "300";
                pagos = String.valueOf((PM * 2) + 300);
                quincenas = qcn2;
                saldoT = String.valueOf(total);
                Letras = String.valueOf(PM);
            }
            try {
                //si no selecciono una opcion no le deja guardar
                if (seleccionado.equals("0")){

                    message("¡¡ERROR!!", "Seleccione una de las dos opciones.", DIALOGO_FINAL);
                }
                else{

                    ///fecha
                    SimpleDateFormat simpleDateFormat =
                            new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                    String format = simpleDateFormat.format(new Date());
                    Log.i("MainActivity", "Current Timestamp: " + format);
                    //insercion

                    String ruta=Convenio.this.getApplicationContext()
                            .getDatabasePath("cobradores").toString();
                    String rutaBD_USRCOB=Convenio.this.getApplicationContext().getFilesDir()
                            .toString() + "/BD_USRCOB.zip";

                    sql = "";
                    sql = "insert into convenio (Custid, opcion, Pago, Mora, quincenas,SaldoT," +
                            "Letra,Usuario,fechaCRT,Estatus) "
                            + " values('" + custid.trim() + "', '" + seleccionado +"','" +pagos  +
                            "', '" + moratorios + "', '"+ quincenas +"','" +saldoT +"','" + PM +
                            "', '" + clases.Lectura(rutaBD_USRCOB) +  "','" + format +
                            "','ESPERA') ";
                    clases.EjecutaSQLUpdate(sql, MiClase.TypeBase.COBRADORES,
                            MiClase.TypeSQL.UPDATE,ruta);

                    message("CONVENIO", "Se ha registrado exitosamente el convenio",
                            DIALOGO_FINAL);
                }
            } catch (Exception e) {
                message("ERROR", "A ocurrido un error al guardar el convenio. Intente" +
                        " nuevamente o contacte a sistemas 800-581-90-98", DIALOGO_ALERTA);
            }

        }
    };

    void datos(){
        sql = "";
        TextView PM1 =  findViewById(R.id.txt_pm);
        RadioButton op1 =  findViewById(R.id.radioButton1);
        RadioButton op2 =  findViewById(R.id.radioButton2);
        DecimalFormat formato = new DecimalFormat("0.00");

        Cursor c = null;

        String ruta=Convenio.this.getApplicationContext().getDatabasePath("cobradores")
                .toString();

        String rutaBD_USRCOB=Convenio.this.getApplicationContext().getFilesDir().toString() +
                "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        String rutaBD_Cobrador=Convenio.this.getApplicationContext().getFilesDir().toString() +
                "/" + Cobrador + ".zip";

        //**** trae una letra para hacer el calculo ****
        sql = "SELECT pm FROM letras where Trim(custid) = Trim('" + custid + "') limit 1";
        c = clases.EjecutaSQL(sql,MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT
                ,rutaBD_Cobrador);
        if (c !=null){
            while(c.moveToNext()){
                PM = Float.valueOf(c.getString(0)).floatValue();
            }
        }
        //**** trae el saldo total ****
        sql = "";
        sql = "SELECT Ifnull(SaldoT,0) SaldoT FROM dscob where custid like '%" + custid + "%'";
        c = clases.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,
                rutaBD_Cobrador);

        if (c!=null){
            while(c.moveToNext()){
                total = Float.valueOf(c.getString(0)).floatValue();
            }
        }

        qcn1 = String.valueOf(Math.round(total/PM));
        qcn2 = String.valueOf(Math.round(total/(PM * 2) ));

        PM1.setText("Letra : $" + String.valueOf(PM) + ", Saldo T. $" + total);

        op1.setText("Opcion 1 : PM + 200 = $" + String.valueOf(PM + 200) + " en " + qcn1
                + " Quincenas " );
        op2.setText("Opcion 2 : (PM x 2) + 300 = $" + String.valueOf((PM * 2) + 300) + " en " + qcn2
                + " Quincenas");
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialogo = null;

        switch(id)
        {
            case DIALOGO_ALERTA:
                dialogo = alerta();
                break;
            case DIALOGO_FINAL:
                dialogo = alerta();
                break;
            default:
                dialogo = null;
                break;
        }
        return dialogo;
    }

    public Dialog alerta(){
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        dialogo.setTitle(titulo);
        dialogo.setMessage(texto);
        dialogo.setPositiveButton("OK", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Log.i("alerta", "fffff");
                if (titulo.contentEquals("CONVENIO")){
                    finish();
                }

            }
        });

        return dialogo.create();
    }

    public void message(String head, String text, int numeracion){
        titulo=head;
        texto=text;
        showDialog(numeracion);
        Log.i("message", "fffff");
    }
}