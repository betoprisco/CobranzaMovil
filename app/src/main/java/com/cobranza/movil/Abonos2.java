package com.cobranza.movil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

import Clases.MiClase;
import com.cobranza.impresora.PrinterActivity;

public class Abonos2 extends Activity implements Runnable {

    String NombreCob, custid, PagoRequerido, filtro, Cliente, tlvencidas, morat, tumin, Cobrador,montoApoyoCovid19,liquideComparar;
    public String lati = "", longi = "";
    int idmenu, metodo,increment;
    Double vMora, totmin, valor_a, valor_b,moraMin;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location currentLocation;
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;

    EditText op_a, op_b, mora_;
    ProgressDialog dialog;

    LinearLayout ApoyoCOVID19Principal;
    TextView txtApoyoCOVID19Monto;
    CheckBox ApoyoCovid19;

    //PARA REVISAR SI TIENE ACTIVADO EL PERMISO DE ACCEDER A LA UBICACIÓN - JAPP - 05-09-2018
    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Se requieren los permisos", "Es necesario habilitar los permisos del GPS para usar la app",
                        Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            }
        } else {
            Toast.makeText(Abonos2.this, "GPS Activado", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getBaseContext(),"¡Es necesario acceder a su ubicación para poder aplicar abonos!",Toast.LENGTH_LONG);
                    this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.abonos2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /******************************/
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();

        String rutaBD_USRCOB=Abonos2.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        NombreCob = clase.LecturaNombreCob(rutaBD_USRCOB);
        /*****************************/
        /*************************************************/
        /*RECIBO LOS PARAMETROS DE LA ACTIVIDAD ANTERIOR*/
        /***********************************************/

        custid = getIntent().getExtras().getString("Custid");
        idmenu = getIntent().getExtras().getInt("idmenu");
        PagoRequerido = getIntent().getExtras().getString("PagoReq");
        filtro = getIntent().getExtras().getString("filtro");
        Cliente = getIntent().getExtras().getString("Cliente");
        tlvencidas = getIntent().getExtras().getString("tlvencidas");
        morat = getIntent().getExtras().getString("morat");

        montoApoyoCovid19=getIntent().getExtras().getString("montoCovid19");

        liquideComparar=getIntent().getExtras().getString("montoLiquide");

        ApoyoCOVID19Principal=findViewById(R.id.ApoyoCOVID19Principal);
        txtApoyoCOVID19Monto=findViewById(R.id.txtApoyoCOVID19Monto);
        ApoyoCovid19=findViewById(R.id.chkApoyoCOVID19);

        String rutaBDCobradores=Abonos2.this.getApplicationContext().getDatabasePath("cobradores").toString();
        Cobrador=clase.Lectura(rutaBD_USRCOB);

        //SI EL MONTO DE APOYO AL COVID19 ES MAYOR A CERO ENTONCES SE MUESTRA EL ESPACIO PARA EL MENSAJE - JAPP - 25-05-2020
        if(montoApoyoCovid19.equals("0")){
            ApoyoCOVID19Principal.setVisibility(View.GONE);
        }
        else {
            //SE BUSCA SI NO HA HECHO UN ABONO Y SI ESTÁ ENVIADO - JAPP - 30-05-2020
            String sql;

            sql = "select count(*)";
            sql = sql + " from Abonos where Status='ENVIADO' and apoyoCovid19='Si' and trim(custid)='" + custid.trim() + "'";

            SQLiteDatabase db = null;

            db = SQLiteDatabase.openDatabase(rutaBDCobradores, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Cursor c = db.rawQuery(sql, null);

            if (c != null) {
                c.moveToFirst();
                if (c.getString(0).equals("1")) {
                    //SI HAY DATOS ES QUE YA PAGÓ, USÓ LA PROMO Y YA ESTÁ ENVIADO EL ABONO, ENTONCES SE OCULTA EL MENSAJE DE LA PROMO
                    ApoyoCOVID19Principal.setVisibility(View.GONE);
                } else
                    txtApoyoCOVID19Monto.setText("$" + montoApoyoCovid19);


            }
        }

        String met = getIntent().getExtras().getString("metodo"); //----------- TARJETA

        //CUANDO SE LLAMA A ESTA PANTALLA DESDE LA CLASE "clientedet.java", NO SE MANDA EL VALOR EXTRA "metodo", POR LO QUE AL ENTRAR DESDE ESA CLASE A ESTA CLASE, MARCA ERROR Y SE DETIENE LA APLICACIÓN.
        //POR ESO SE PUSO EL TRY/CATCH PARA QUE CUANDO SEA ESE CASO OCULTE EL BOTÓN DE ABONO CON TARJETA Y SOLO PERMITA EL ABONO EN EFECTIVO.
        try
        {
            metodo = Integer.valueOf(met);
        }
        catch (Exception e){
            metodo=1;
        }

        vMora = Double.valueOf(morat).doubleValue();
        /******************************************/

        //SE BUSCA EL MONTO MÍNIMO DEL MORA, QUE AHORA ES DINÁMICO - JAPP - 08-04-2020
        String sql;
        Cursor c = null;

        sql="SELECT * FROM moraMin";



        String rutaBD_Cobrador=Abonos2.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        try{
            c=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            if (c!=null){
                c.moveToFirst();
                moraMin= Double.valueOf(c.getString(0));
            }else{
                moraMin=200.0;
            }
        }catch(Exception e){
            moraMin=200.0;
        }
        //A PETICIÓN DE RANDO, SE CAMBIA A 200 DE MORA MÍNIMO - JAPP - 31-10-2018 - ¡THIS IS HALLOWEEN, THIS IS HALLOWEEN!
        totmin = Double.valueOf(tlvencidas) * moraMin;
        tumin = String.valueOf(totmin);

        /*ejecuta gps*/
        //SE REVISA SI TIENE EL PERMISO PARA SABER LA UBICACIÓN - JAPP - 05-09-2019
        showPhoneStatePermission();
        writeSignalGPS();

        TextView totconv = findViewById(R.id.tVconvenio2);
        totconv.setText(String.valueOf(clase.totconvenio(custid,rutaBD_Cobrador,rutaBDCobradores)).toString());

        TextView tvconv = findViewById(R.id.tVconv1); //----- CONVENIO.
        tvconv.setText(clase.tieneConvenio(custid,rutaBD_Cobrador,rutaBDCobradores));

        TextView cletras = findViewById(R.id.tVpruebas);
        cletras.setText(morat);

        EditText pago = findViewById(R.id.txt_PagoReq);
        pago.setText(PagoRequerido);

        Button boton = findViewById(R.id.btn_ProcAbono);
        Button botonTarjeta = findViewById(R.id.btn_ProcAbonoTar); //----------- Abono con Tarjeta

        CheckBox chk = findViewById(R.id.chk_Mora);

        //metodo=0 - tarjeta
        //metodo=1 - efectivo
        if (metodo != 1) {
            boton.setVisibility(View.INVISIBLE);
            chk.setVisibility(View.INVISIBLE);
            //------------------------------------------ QUIERE DECIR QUE ES ABONO CON TARJETA
            if (ChecaConexion(getApplicationContext()) == false) {
                Toast.makeText(getBaseContext(), "Falla conexion internet", Toast.LENGTH_LONG).show();
                //break;
            }
        } else {
            botonTarjeta.setVisibility(View.INVISIBLE);

        }
        botonTarjeta.setOnClickListener(IrAbonoConTarjeta);//--------------------------- Abono con Tarjeta

        boton.setOnClickListener(Procesa);

        //BOTÓN PARA LA DIRECCIÓN ALTERNA - JAPP - 07-04-2016
        Button botónDirAlterna = findViewById(R.id.btn_modificaTel);
        botónDirAlterna.setOnClickListener(dirAlterna);

        /*************************************************************/
        /********CHECK BOX PARA MORATORIOS****************************/
        /*************************************************************/

        //--SI TIENE CONVENIO NO DEJA MODIFICAR MORATORIO --
        if (tvconv.getText().toString().trim().equals("ACTIVO") || tvconv.getText().toString().trim().equals("Reciente")) {
            //chk.setVisibility(View.INVISIBLE);
            chk.setEnabled(false);
            chk.setText("El cliente tiene convenio activo o reciente.");
            Log.i("Entra", "Entra si tiene convenio");
        }

        //RANDO DIJO QUE AHORA VALIDE SI EL CLIENTE TIENE 3 O MENOS LETRAS VENCIDAS NO PERMITA QUE SE MODIFIQUE EL MORA - JAPP - 07-01-2021
        if (Integer.parseInt(tlvencidas) <=3) {
            //chk.setVisibility(View.INVISIBLE);
            chk.setEnabled(false);
            chk.setText("El cliente tiene 3 o menos letras vencidas");
            Log.i("Entra", "Entra si tiene convenio");
        }

        chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               @Override
               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   // TODO Auto-generated method stub
                   CheckBox chk =  findViewById(R.id.chk_Mora);
                   EditText mora =  findViewById(R.id.txt_Amora);
                   TextView lbl_Mora =  findViewById(R.id.lbl_Mora);

                   if (chk.isChecked()) {
                       Toast.makeText(getBaseContext(), "Moratorio Activado", Toast.LENGTH_SHORT).show();
                       mora.setVisibility(View.VISIBLE);
                       lbl_Mora.setVisibility(View.VISIBLE);
                       mora.setText(tumin);
                   } else {
                       Toast.makeText(getBaseContext(), "Moratorio Desactivado", Toast.LENGTH_SHORT).show();
                       mora.setVisibility(View.INVISIBLE);
                       lbl_Mora.setVisibility(View.INVISIBLE);
                       mora.setText("");
                   }

               }
           }
        );
        /*************************************************************/

        // TODO Auto-generated method stub
    }

    public void run() {
        try {
            String LocationType = LocationManager.GPS_PROVIDER;
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Looper.prepare();
                mLocationListener = new MyLocationListener();
                mLocationManager.requestLocationUpdates(LocationType, 0, 0, mLocationListener);

                Looper.loop();
                Looper.myLooper().quit();
            }
        } catch  (SecurityException e){
            Toast toast2 = Toast.makeText(getBaseContext(), "ERROR2: Señal no encontrada "+ e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                setCurrentLocation(loc);

                lati=String.valueOf(loc.getLatitude()) ;
                longi=String.valueOf(loc.getLongitude()) ;
            }
            else{
                String coordenadas = "sin localizar";
                Toast.makeText( getApplicationContext(),coordenadas,Toast.LENGTH_SHORT).show();
            }
        }

        private void setCurrentLocation(Location loc) {
            currentLocation = loc;
        }

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }
        public void onStatusChanged(String provider, int status,Bundle extras) {
            // TODO Auto-generated method stub
        }
        private Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                try{
                    mLocationManager.removeUpdates(mLocationListener);
                    if (currentLocation!=null) {
                        Toast.makeText(getBaseContext(), "posicion: "+currentLocation.toString() , Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e){
                    Toast toast2 = Toast.makeText(getBaseContext(), "ERROR1: Handler muestra ubicación "+ e.getMessage(), Toast.LENGTH_SHORT);
                    toast2.show();
                }
            }
        };

    }

    //BUSQUEDA DE POSISCION GPS
    private void writeSignalGPS() {
        DialogInterface.OnCancelListener dialogCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        };

        try{
            Thread thread = new Thread(Abonos2.this);
            thread.start();
        }catch (Exception e){
            Toast toast2 = Toast.makeText(getBaseContext(), "ERROR2: Buscando señal "+ e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }
    }

    public  boolean ChecaConexion(Context ctx){ //-------------- PARA CHECAR LA CONEXION A INTERNET

        boolean bTieneConexion = false;
        ConnectivityManager connec =  (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Con esto recogemos todas las redes que tiene el móvil (wifi, gprs...)
        NetworkInfo[] redes = connec.getAllNetworkInfo();

        for(int i=0; i<2; i++){
            //Si alguna tiene conexión ponemos el boolean a true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED){
                bTieneConexion = true;
            }
        }
        return bTieneConexion;
    }//------------------------------------------- CHECAR CONEXION A INTERNET

    private View.OnClickListener IrAbonoConTarjeta = new View.OnClickListener(){// ------- ABONO CON TARJETA
        @Override
        public void onClick(View v) {//2
            boolean flag =false;
            TextView folioFac=  findViewById(R.id.txt_Folio);
            op_a =  findViewById(R.id.txt_PagoReq);
            op_b =  findViewById(R.id.txt_MontoPago);

            CheckBox chk=  findViewById(R.id.chk_Mora);

            Boolean flag2 =true;

            if(chk.isChecked()){
                mora_ =  findViewById(R.id.txt_Amora);

                if (Double.parseDouble(mora_.getText().toString()) < totmin){
                    Toast.makeText(getBaseContext(), "El valor a Mora: "+mora_.getText().toString()+" no puede ser menor al monto minimo de mora " + totmin, Toast.LENGTH_LONG).show();
                    flag2=false;
                }else if(Double.parseDouble(mora_.getText().toString()) > vMora ){
                    Toast.makeText(getBaseContext(), "El valor a Mora: "+mora_.getText().toString()+" no puede ser mayor al monto total de mora " + totmin, Toast.LENGTH_LONG).show();
                    flag2=false;
                }
            }
            if (flag2){
                if(VerificaAbono()>0){
                    Toast.makeText(getBaseContext(), "IMPOSIBLE REGISTRAR: El Cliente tiene menos de una Hora que registro su pago ID.-> "+ custid+".", Toast.LENGTH_LONG).show();
                }
                else{
                    if( op_b.getText().toString().length() > 0)//&& folioFac.getText().toString().length()>0)
                    {
                        //REVISO QUE EL MONTO A ABONAR NO SEA MAYOR AL LIQUIDE MÁS 1000 PESOS
                        if((Double.parseDouble(op_b.getText().toString()) > Double.parseDouble(liquideComparar))
                                && Double.parseDouble(liquideComparar)>0 ){
                            AlertDialog alertDialog = new AlertDialog.Builder(Abonos2.this).create();
                            alertDialog.setTitle("Aviso");
                            alertDialog.setMessage("El monto no puede ser mayor a $" + liquideComparar);
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Aceptar", (dialog, which) -> {

                            });
                            alertDialog.show();
                        }
                        else{
                            String mora2;
                            if (chk.isChecked())
                                mora2 = "si";
                            else
                                mora2 = "no";


                            EditText mora = findViewById(R.id.txt_Amora);
                            valor_a = Double.parseDouble(op_a.getText().toString());
                            valor_b = Double.parseDouble(op_b.getText().toString());
                            String cambio= "Cambio: "+ Double.toString((valor_a - valor_b));
                            Intent intent = new Intent();
                            intent.setClass(Abonos2.this, AbonosTarjeta.class );
                            intent.putExtra("custid", custid);
                            intent.putExtra("monto", op_b.getText().toString());
                            intent.putExtra("mora", mora.getText().toString());
                            intent.putExtra("mora2", mora2);
                            intent.putExtra("pagoReq", PagoRequerido);
                            intent.putExtra("cliente", Cliente);
                            intent.putExtra("montoApoyoCovid19",montoApoyoCovid19);

                            if(ApoyoCovid19.isChecked()){
                                intent.putExtra("usoPromoCovid19","Si");
                            }
                            else{
                                intent.putExtra("usoPromoCovid19","No");
                            }

                            startActivity(intent);
                        }
                    }
                    else{
                        Toast.makeText(getBaseContext(), "Ingrese el monto a abonar y el folio de recibo.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }

    };	// ----------------- ABONO CON TARJETA


    public int  VerificaAbono(){
        try{
            Date horaActual = new Date();
            horaActual.toGMTString();
            horaActual.toString();

            String dia =String.valueOf(horaActual.getDay());
            String Hr =String.valueOf(horaActual.getHours());
            if (dia.length()==1) {
                dia="0"+dia;
            }
            if (Hr.length()==1) {
                Hr="0"+Hr;
            }

            String sql="SELECT fechaAbo,  datetime('now') ,* from Abonos where  trim(custid)=trim('"+custid+"') and datetime(FechaAbo,'+1 hour') >=  datetime(current_timestamp, 'localtime') ";//"select custid as total from  Abonos where custid='"+ custid +"' and (strftime('%d', fechaAbo)='"+dia+"' and strftime('%H', fechaAbo)>='"+Hr+"')";
            /*
             * DESARROLLO DE LA CONSULTA
             *
             * */
            SQLiteDatabase db= null;

            String rutaBDCobradores=Abonos2.this.getApplicationContext().getDatabasePath("cobradores").toString();

            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Cursor c = db.rawQuery(sql, null);

            if(c!=null) return c.getCount();

        } catch (Exception e) {
            Toast toast = Toast.makeText(getBaseContext(), "ERROR busca folio x"+ e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
        return 0 ;
    }

    /********************************/
    /******PROCESA ABONO************/
    /******************************/
    private View.OnClickListener Procesa = new View.OnClickListener(){//1

        @Override
        public void onClick(View v)
        {//2
            // TODO Auto-generated method stub
            boolean flag =false;

            TextView folioFac=  findViewById(R.id.txt_Folio);
            op_a =  findViewById(R.id.txt_PagoReq);
            op_b =  findViewById(R.id.txt_MontoPago);

            CheckBox chk=  findViewById(R.id.chk_Mora);

            Boolean flag2 =true;

            if(chk.isChecked()){
                mora_ =  findViewById(R.id.txt_Amora);
                //checo el moratorio que paga no sea menor del minimo letrasV * 150
                if (Double.parseDouble(mora_.getText().toString()) < totmin){
                    Toast.makeText(getBaseContext(), "El valor a Mora: "+mora_.getText().toString()+" no puede ser menor al monto minimo de mora " + totmin, Toast.LENGTH_LONG).show();
                    flag2=false;
                }else if(Double.parseDouble(mora_.getText().toString()) > vMora ){
                    Toast.makeText(getBaseContext(), "El valor a Mora: "+mora_.getText().toString()+" no puede ser mayor al monto total de mora " + totmin, Toast.LENGTH_LONG).show();
                    flag2=false;
                }
            }
            if (flag2)
            {
                if(VerificaAbono()>0)
                {
                    Toast.makeText(getBaseContext(), "IMPOSIBLE REGISTRAR: El Cliente tiene menos de una hora que registró su pago ID.-> "
                            + custid+".", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if( op_b.getText().toString().length() > 0)//&& folioFac.getText().toString().length()>0)
                    {
                        //REVISO QUE EL MONTO A ABONAR NO SEA MAYOR AL LIQUIDE MÁS 1000 PESOS
                        if((Double.parseDouble(op_b.getText().toString()) > Double.parseDouble(liquideComparar))
                                && Double.parseDouble(liquideComparar)>0 ){
                            AlertDialog alertDialog = new AlertDialog.Builder(Abonos2.this).create();
                            alertDialog.setTitle("Aviso");
                            alertDialog.setMessage("El monto no puede ser mayor a $" + liquideComparar);
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"Aceptar", (dialog, which) -> {

                            });
                            alertDialog.show();
                        }
                        else{
                            /******************************/
                            /*Lectura del archivo CONFIG*/
                            String rutaBD_USRCOB=Abonos2.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
                            /*Lectura del archivo CONFIG*/
                            MiClase clase = new MiClase();
                            Cobrador=clase.Lectura(rutaBD_USRCOB);
                            /*****************************/
                            valor_a = Double.parseDouble(op_a.getText().toString());
                            valor_b = Double.parseDouble(op_b.getText().toString());
                            String cambio= "Cambio: "+ Double.toString((valor_a - valor_b));
                            Barra(cambio);
                        }

                    }else
                    {
                        Toast.makeText(getBaseContext(), "Ingrese el monto a abonar y el folio de recibo.", Toast.LENGTH_LONG).show();
                    }
                    /*4*/
                }
            }/**/
        }
    };	//1

    //------------GPS--------------------

    public void Barra(String cambio){
        increment = 1;//Integer.parseInt(et.getText().toString());

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Buscando GPS...");
        // set the progress to be horizontal
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // reset the bar to the default value of 0
        dialog.setProgress(0);

        // get the maximum value
        //EditText max = (EditText) findViewById(R.id.maximum);
        // convert the text value to a integer
        int maximum = 100;//Integer.parseInt(max.getText().toString());
        // set the maximum value
        dialog.setMax(maximum);
        // display the progressbar
        dialog.show();

        // create a thread for updating the progress bar
        Thread background = new Thread (new Runnable() {
            public void run() {
                try {
                    // enter the code to be run while displaying the progressbar.
                    //
                    // This example is just going to increment the progress bar:
                    // So keep running until the progress value reaches maximum value
                    while (dialog.getProgress()< dialog.getMax()) {
                        // wait 500ms between each update
                        if ( lati.equals("")){
                            Thread.sleep(500);
                        }else{
                            Thread.sleep(50);
                        }
                        // active the update handler
                        progressHandler.sendMessage(progressHandler.obtainMessage());
                    }

                    dialog.dismiss();

                    ProcesaAbono();
                    Imprime();

                } catch (java.lang.InterruptedException e) {
                    Toast toast2 = Toast.makeText(getBaseContext(), "ERROR1.6: "+ e.getMessage(), Toast.LENGTH_LONG);
                    toast2.show();
                }
            }
        });

        // start the background thread
        background.start();
    }

    // handler for the background updating
    Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {

            dialog.incrementProgressBy(increment);
        }
    };

    void ProcesaAbono(){
        try{
            /*INSERT DE ABONO*/

            /*STATUS PARA EL ENVIO DE DATOS
             *
             * ESPERA:Cuando los datos no se han enviado al ws para ser guardados en la case de datos
             * ENVIADO:Cuando los datos se han enviado al ws para ser guardados en la case de datos
             *
             * */

            //DATOS PARA LA CREACION DEL FOLIO
            Date horaActual = new Date();
            horaActual.toGMTString();
            String conv = "";
            String totc = "";
            String tipConv = "";

            String Folio=Integer.toString(horaActual.getYear())+"-"+Integer.toString(horaActual.getMonth())+"-"+Integer.toString(horaActual.getDay())+" "+Integer.toString(horaActual.getHours())+":"+Integer.toString(horaActual.getMinutes())+":"+Integer.toString(horaActual.getSeconds());

            TextView tvconvactivo =  findViewById(R.id.tVconv1);
            TextView tvcotot =  findViewById(R.id.tVconvenio2);

            conv = tvconvactivo.getText().toString();
            totc = tvcotot.getText().toString();
            /******************************/


            /******************************************************************/

            String sql="insert into Abonos (Cobrador,custid,Monto,PagoReq,Long,Lati,Nombre,Folio,fechaAbo";

            /******SI SELECCIONA MORA*****/
            CheckBox chk= (CheckBox) findViewById(R.id.chk_Mora);

            sql =sql+" ,A_Mora";

            //si tiene convenio
            if (conv.trim().equals("Reciente") || conv.trim().equals("ACTIVO")){
                sql =sql+" ,convenio";
                sql =sql+ " ,OpcConvenio";
            }

            sql =sql+ " ,apoyoCovid19";

            sql = sql+")";

            sql =sql+" values ('"+Cobrador+"','"+custid+"', " + valor_b + ", "+PagoRequerido+",'"+ longi +"','"+lati+"','";

            sql= sql + Cliente +"','"+GeneraFolio(Cobrador)+"', datetime(current_timestamp, 'localtime')";

            //SE REVISA SI PAGÓ EL MONTO DE LA PROMOCIÓN DE COVID19 - JAPP - 25-05-2020
            if ((valor_b>=Double.parseDouble(montoApoyoCovid19)) && Double.parseDouble(montoApoyoCovid19)>0){
                if(ApoyoCovid19.isChecked())
                    sql =sql + " ,0"; //CERO DE MORA PARA LA PROMO DE COVID - 11-08-2020
                else{
                    if (chk.isChecked()){
                        sql =sql+" ,"+ mora_.getText().toString();
                    }else{
                        sql =sql+" ,-1";
                    }
                }
            }
            else{//SI NO SE PAGA EL PM SE HACEN LAS VALIDACIONES NORMALES - JAPP - 11-08-2020
                if (chk.isChecked()){
                    sql =sql+" ,"+ mora_.getText().toString();
                }else{
                    sql =sql+" ,-1";
                }
            }

            String rutaBDCobradores=Abonos2.this.getApplicationContext().getDatabasePath("cobradores").toString();
            String rutaBD_USRCOB=Abonos2.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*****************************/

            String rutaBD_Cobrador=Abonos2.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

            if (conv.trim().equals("Reciente") || conv.trim().equals("ACTIVO")){
                sql =sql+" ," + totc;
                sql =sql+" ," + clase.opcionConvenio(custid,rutaBD_Cobrador,rutaBDCobradores);
            }

            //SE REVISA SI PAGÓ EL MONTO DE LA PROMOCIÓN DE COVID19 - JAPP - 25-05-2020
            if ((valor_b>=Double.parseDouble(montoApoyoCovid19)) && Double.parseDouble(montoApoyoCovid19)>0){
                if(ApoyoCovid19.isChecked()){
                    sql =sql+ " ,'Si'";
                }
                else{
                    sql =sql+ " ,'No'";
                }
            }
            else{
                sql =sql+ " ,'No'";
            }

            sql = sql+")";

            SQLiteDatabase db= null;
            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);


            db.execSQL(sql);
            db.close();

            /*INSERT EN COMENTARIO*/
            sql="insert into historial (UserUpd,custid,MontoAPagar,historial,tel,Perspectiva,PerspectivaDet,";
            sql = sql +" Longitude,Latitude,status,FechaSeg,folio,FechaUpd,FechaCrt)  ";
            sql =sql+"values ('"+Cobrador+"','"+custid+"',"+valor_b+",";
            sql =sql+"'ABONO A LA CUENTA','','BUENA',";
            sql = sql+"'ABONO A LA CUENTA','"+longi+"','"+lati+"','ESPERA',datetime(current_timestamp, 'localtime'),'"+ Folio +"',datetime(current_timestamp, 'localtime'),datetime(current_timestamp, 'localtime'))";

            SQLiteDatabase db2= null;
            db2 = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            db2.execSQL(sql);
            db2.close();

            //ELIMINA DE LA LISTA DE NO GESTIONADOS - JAPP - 06-04-2016 - INICIO
            sql="delete from noGestionados  ";
            sql =sql+"where trim(custid) like '"+custid.trim()+"%'";

            String rutaBD_CobradorDatos=Abonos2.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

            clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR_DATOS , MiClase.TypeSQL.UPDATE,rutaBD_CobradorDatos);
            //ELIMINA DE LA LISTA DE NO GESTIONADOS - JAPP - 06-04-2016 - FIN

            /***TERMINA INSERT DE BASE DE DATOS***/
        } catch (Exception e) {
            Toast toast = Toast.makeText(getBaseContext(), "ERROR AbonoInsert1: "+ e.getMessage().toString(), Toast.LENGTH_LONG);
            toast.show();
        }
    }
    /******************************/
    /*********FIN PROCESA ABONO****/
    /******************************/

    /*************************************/
    /*********GENERA FOLIO DE FACTURA****/
    /************************************/

    public String  GeneraFolio(String Cobrador){
        SQLiteDatabase db = null;

        String rutaBD_Cobrador=Abonos2.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null,SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        String Folio=Cobrador;
        /*
         * CONSULTA PARA OBTENER FECHA
         * */

        String strSql="SELECT strftime('%d%m%Y%H%M','NOW','localtime')  Fecha";

        Cursor cFecha = db.rawQuery(strSql,null);

        if (cFecha!=null)
        {
            cFecha.moveToFirst();
            Folio= Folio + cFecha.getString(0);
        }
        cFecha.close();

        return Folio;
    }

    public void Imprime(){
        Intent intent = new Intent(Abonos2.this,PrinterActivity.class);
        intent.putExtra("TIPO", "NOTA");
        intent.putExtra("custid", custid);
        intent.putExtra("folio", "");
        startActivity(intent);
        this.finish();//para que cierre la pantalla de abono Merly
    }

    //LLAMAR A LA PANTALLA DE LA DIRECCIÓN ALTERNA - JAPP - 07-04-2016
    private View.OnClickListener dirAlterna = new View.OnClickListener(){

        @Override
        public void onClick(View v)
        {
            Intent direcciónAlterna = new Intent();
            direcciónAlterna.setClass(Abonos2.this, Diralta.class );
            direcciónAlterna.putExtra("custid", custid);
            direcciónAlterna.putExtra("cliente", Cliente);
            startActivity(direcciónAlterna);
        }
    };
}