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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Date;

import Clases.Clase_Encriptacion;
import Clases.MiClase;

import com.cobranza.impresora.PrinterActivity;

public class AbonosTarjeta extends Activity implements Runnable{

    String custid,Cliente,txtMonto,txtMora_,chk,PagoRequerido,Cobrador="",nombreTar,tipoTar,numTar
            ,mesExp,anioExp,digitosVerif,lati="",longi="",montoApoyoCovid19="0";
    int increment;

    public static final String url2=
            "http://sbvconect.dyndns.org/EstadoCuentaCliente/EstadoCuenta.asmx";
    public static final String NameSpace2="http://EdoCliente.org/";
    public static final String Metodo2="AbonosTiendasQuality";
    public static final String accionSoap2="http://EdoCliente.org/AbonosTiendasQuality";

    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    private Location currentLocation;
    private static final int MNU_OPC1 = 1;
    private static final int MNU_OPC2 = 2;

    EditText nombreTarjetahabiente;
    ProgressDialog dialog;

    Button boton;

    String usoPromoCovid19;

    //PARA REVISAR SI TIENE ACTIVADO EL PERMISO DE ACCEDER A LA UBICACIÓN - JAPP - 05-09-2018
    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Se requieren los permisos",
                        "Es necesario habilitar los permisos del GPS para usar la app",
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                        REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            }
        } else {
            Toast.makeText(AbonosTarjeta.this, "GPS Activado",
                    Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getBaseContext(),
                            "¡Es necesario acceder a su ubicación para poder aplicar abonos!"
                            ,Toast.LENGTH_LONG);
                    this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abonos_tarjeta);
        //----------- SE AGREGO A CADA CLASE DE ACTIVITY PARA NO CAMBIAR ROTACION DE PANTALLA
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //SE REVISA SI TIENE EL PERMISO PARA SABER LA UBICACIÓN - JAPP - 05-09-2019
        showPhoneStatePermission();

        custid= getIntent().getExtras().getString("custid");
        Cliente=getIntent().getExtras().getString("cliente");
        txtMonto = getIntent().getExtras().getString("monto");
        txtMora_ = getIntent().getExtras().getString("mora");
        chk = getIntent().getExtras().getString("mora2");
        PagoRequerido = getIntent().getExtras().getString("pagoReq");
        montoApoyoCovid19=getIntent().getExtras().getString("montoApoyoCovid19");

        //POR SI NO TENÍA DERECHO A LA PROMO
        usoPromoCovid19=getIntent().getExtras().getString("usoPromoCovid19");

        EditText cust_id = findViewById(R.id.TxtIdCliente);
        cust_id.setText(custid);
        EditText pagoReq = findViewById(R.id.txt_PagoReq);
        pagoReq.setText(txtMonto);

        //---------------------------Lectura del archivo CONFIG
        String rutaBD_USRCOB=AbonosTarjeta.this.getApplicationContext().getFilesDir().toString()
                + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        Cobrador=clase.Lectura(rutaBD_USRCOB);
        //---------------------------

        boton =  findViewById(R.id.btn_ProcAbono);
        boton.setOnClickListener(ProcesaTarjeta);

    }

    // handler for the background updating
    Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
            dialog.incrementProgressBy(increment);
        }
    };

    @Override
    public void run() {
        // TODO Auto-generated method stub
		/*Intent intent2 = new Intent();
		intent2.setClass(AbonosTarjeta.this, Abonos2.class );*/
        try{
            String LocationType = LocationManager.GPS_PROVIDER;
            mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Looper.prepare();
                mLocationListener=new MyLocationListener();
                mLocationManager.requestLocationUpdates(LocationType,0,0,
                        mLocationListener);

                Looper.loop();
                Looper.myLooper().quit();
            }
        }catch(SecurityException e){
            Toast toast2=Toast.makeText(getBaseContext(), "Error2: Señal no encontrada"
                    +e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }
    }

    private View.OnClickListener ProcesaTarjeta = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            boton.setEnabled(false);

            nombreTarjetahabiente =  findViewById(R.id.txtNombre);
            EditText tipoTarjeta =  findViewById(R.id.txtTipoTarjeta);
            EditText numTarjeta =  findViewById(R.id.txtNumTarjeta);
            EditText mesExpiracion =  findViewById(R.id.txtFecha);
            EditText AnioExp =  findViewById(R.id.txtanio);
            EditText digCerificacion =  findViewById(R.id.txtDigVerificacion);

            String strNombre = nombreTarjetahabiente.getText().toString();
            String strTipo = tipoTarjeta.getText().toString();
            String strNum = numTarjeta.getText().toString();
            String strMes = mesExpiracion.getText().toString();
            String strAnio = AnioExp.getText().toString();
            String strDig = digCerificacion.getText().toString();

            //----------------------- VALIDAR DATOS INGRESADOS
            int valida = 0;
            String valoresMes[] = {"01","02","03","04","05","06","07","08","09","10","11","12"};

            int largoNombre = strNombre.length();
            int largoNum = strNum.length();
            int largoMes = strMes.length();
            int largoAnio = strAnio.length();
            int largoDig = strDig.length();

            boolean nombreAceptables = strNombre.matches("([a-z]|[A-Z]|\\s)+");
            boolean NumAceptables = strNum.matches("\\d{16}");
            boolean mesAceptables = strMes.matches("\\d{2}");
            boolean anioAceptables = strAnio.matches("\\d{2}");
            boolean DigAceptables = strDig.matches("\\d{3}");
            int compareMes = "00".compareTo(strMes);
            int compareAnio = "00".compareTo(strAnio);
            int compareDig = "000".compareTo(strDig);

            if (nombreAceptables != true){valida += 1; }
            if (NumAceptables != true){valida += 1; }
            if (mesAceptables != true){valida += 1; }
            else{
                if (largoMes !=2) { valida += 1; }
                else{
                    if (compareMes == 0){ valida += 1; }
                    else{
                        int compareValor = 0;
                        for(int i=0;i<valoresMes.length;i++) {
                            if(valoresMes[i].equals(strMes)){ compareValor += 1; }
                        }
                        if(compareValor < 1){ valida += 1; }
                    }
                }
            }

            if (!anioAceptables){valida += 1; }
            else{
                if (largoAnio !=2) { valida += 1; }
                else{
                    if (compareAnio == 0){ valida += 1; }
                    else{
                        int numeroAnio = Integer.parseInt(strAnio);
                        if(numeroAnio < 21 || numeroAnio > 36){ valida += 1; }
                    }
                }
            }
            if (DigAceptables != true){valida += 1; }
            if (largoNombre <5) { valida += 1; }
            if (largoNum != 16) { valida += 1; }
            if (largoDig != 3) { valida += 1; }
            if (compareDig == 0){ valida += 1; }

            if (valida == 0){ //------ PASA VALIDACIÓN

                //encriptar datos
                Clase_Encriptacion encriptado = new Clase_Encriptacion();

                try{
                    String nombreEncriptado = encriptado.encriptarCadena(strNombre);
                    String tipoEncriptado = encriptado.encriptarCadena(strTipo);
                    String numEncriptado = encriptado.encriptarCadena(strNum);
                    String mesEncriptado = encriptado.encriptarCadena(strMes);
                    String anioEncriptado = encriptado.encriptarCadena(strAnio);
                    String digEncriptado = encriptado.encriptarCadena(strDig);

                    // Obtener Bytes
                    byte[] byNombre = nombreEncriptado.getBytes();
                    byte[] byTipo = tipoEncriptado.getBytes();
                    byte[] byNum = numEncriptado.getBytes();
                    byte[] byMes = mesEncriptado.getBytes();
                    byte[] byAnio = anioEncriptado.getBytes();
                    byte[] byDig = digEncriptado.getBytes();

                    nombreTar = Base64.encodeToString(byNombre, Base64.DEFAULT);
                    tipoTar = Base64.encodeToString(byTipo, Base64.DEFAULT);
                    numTar = Base64.encodeToString(byNum, Base64.DEFAULT);
                    mesExp = Base64.encodeToString(byMes, Base64.DEFAULT);
                    anioExp = Base64.encodeToString(byAnio, Base64.DEFAULT);
                    digitosVerif = Base64.encodeToString(byDig, Base64.DEFAULT);

                }
                catch ( Exception e){
                    Toast.makeText(getBaseContext(), "Error al realizar la conversion... ", Toast.LENGTH_LONG).show();
                }
                new dataSend().execute();


            } //------ VALIDA
            else{
                Toast.makeText(getBaseContext(), "Datos incorrectos... ", Toast.LENGTH_LONG).show();
                boton.setEnabled(true);
            }
        }
    };

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
                    //pd.dismiss();
                    mLocationManager.removeUpdates(mLocationListener);
                    if (currentLocation!=null) {
                        Toast.makeText(getBaseContext(), "posicion: "+currentLocation.toString() , Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e){
                    Toast toast2 = Toast.makeText(getBaseContext(), "ERROR1: Handler muestra ubicasion "+ e.getMessage(), Toast.LENGTH_LONG);
                    toast2.show();

                }
            }
        };
    }
    private class dataSend extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            SoapPrimitive resultado;

            //TIMEOUT DE ESPERA A CONEXION HTTP
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 6000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

            if (ChecaConexion(getApplicationContext())==false){
                Toast.makeText(getBaseContext(), "Falla internet comen" , Toast.LENGTH_LONG).show();
            }
            else{
                EditText pagoReq = findViewById(R.id.txt_PagoReq);
                String strTotal = pagoReq.getText().toString();

                SoapObject request = new SoapObject(NameSpace2, Metodo2);

                request.addProperty("id", custid);
                request.addProperty("m", strTotal); //--------- VARIABLE Q ENVÍA EL MONTO TOTAL A COBRAR
                //request.addProperty("m", "1"); //-------------- MONTO DE PRUEBA
                request.addProperty("na", nombreTar);
                //request.addProperty("t", tipoTar);
                request.addProperty("nb", numTar);
                request.addProperty("f", mesExp);
                request.addProperty("a", anioExp);
                request.addProperty("d", digitosVerif);
                //SI EL PAGO REQUERIDO ES MAYOR O IGUAL AL MONTO A PAGAR PARA EL APOYO DE COVID SIGNIFICA QUE EL CLIENTE TIENE LA PROMOCIÓN- JAPP - 25-05-2020
                if ((Double.parseDouble(strTotal)>=Double.parseDouble(montoApoyoCovid19)) && Double.parseDouble(montoApoyoCovid19)>0
                 && usoPromoCovid19.equals("Si")){
                    request.addProperty("c", Cobrador + "-covid19");
                }
                else{
                    request.addProperty("c", Cobrador);
                }

                //sobre
                SoapSerializationEnvelope sobre = new SoapSerializationEnvelope (SoapEnvelope.VER11 );
                sobre.dotNet = true;
                sobre.setOutputSoapObject(request);

                HttpTransportSE transporte= new HttpTransportSE(url2);
                try{
                    // llamada
                    transporte.call(accionSoap2, sobre);
                    //resultado
                    resultado = (SoapPrimitive)sobre.getResponse();
                    return resultado.toString();
                } catch (Exception e) {
                    return e.toString();
                }
            }
            return "Something";
        }

        protected void onPostExecute(String result){
            if  (result.equals("A")){ //------------------------------ Transacción Aprovada
                Barra("0.0");
            }
            else if  (result.equals("D")){ //------------------------ Hubo un error en la transacción
                Toast.makeText(AbonosTarjeta.this, "Transacción denegada", Toast.LENGTH_LONG).show();
            }
            else if  (result.equals("E")){ //------------------------ Hubo un error en la transacción
                Toast.makeText(AbonosTarjeta.this, "msg ws--> Hubo un error en la transacción", Toast.LENGTH_LONG).show();
            }
            else if  (result.equals("ED")){ //------------------------ Hubo un error en la transacción
                Toast.makeText(AbonosTarjeta.this, "msg ws--> Error no reconocido", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(AbonosTarjeta.this, "msg ws--> "+ result, Toast.LENGTH_LONG).show();
            }
            boton.setEnabled(true);
        }
    }

    /*Checo si hay internet*/
    public  boolean ChecaConexion(Context ctx){
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
    }

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

        int maximum = 100;
        dialog.setMax(maximum);
        dialog.show();

        // create a thread for updating the progress bar
        Thread background = new Thread (new Runnable() {
            public void run() {
                boolean flag=false;
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

            String Folio=Integer.toString(horaActual.getYear())+"-"+Integer.toString(horaActual.getMonth())+"-"+Integer.toString(horaActual.getDay())
                    +" "+Integer.toString(horaActual.getHours())
                    +":"+Integer.toString(horaActual.getMinutes())+":"+Integer.toString(horaActual.getSeconds());

            /******************************************************************/
            String sql="insert into Abonos (Cobrador,custid,PagoReq,Monto,Status,Long,Lati,Nombre,Folio,fechaAbo,A_Mora,apoyoCovid19)";
            sql =sql+" values ('"+Cobrador+"','"+custid+"', " + PagoRequerido  + ", "+ txtMonto +",'ENVIADO','"+ longi +"','"+lati+"','";

            sql= sql + Cliente +"','"+GeneraFolio(Cobrador)+"', datetime(current_timestamp, 'localtime')";


            if (chk == "SI"){
                sql =sql+" ,"+ txtMora_;
            }else{
                sql =sql+" ,-1";
            }

            //SE REVISA SI PAGÓ EL MONTO DE LA PROMOCIÓN DE COVID19 - JAPP - 25-05-2020
            if ((Double.parseDouble(txtMonto)>=Double.parseDouble(montoApoyoCovid19)) && Double.parseDouble(montoApoyoCovid19)>0
                && usoPromoCovid19.equals("Si")){
                sql =sql+ " ,'Si'";
            }
            else{
                sql =sql+ " ,'No'";
            }

            sql = sql+")";

            String rutaBDCobradores=AbonosTarjeta.this.getApplicationContext().getDatabasePath("cobradores").toString();

            SQLiteDatabase db= null;
            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            db.execSQL(sql);
            db.close();

            EditText pagoReq = findViewById(R.id.txt_PagoReq);
            String strTotal = pagoReq.getText().toString();

            /*INSERT EN COMENTARIO*/
            sql="insert into historial (UserUpd,custid,MontoAPagar,historial,tel,Perspectiva,PerspectivaDet,";
            sql = sql +" Longitude,Latitude,status,FechaSeg,folio,FechaUpd,FechaCrt)  ";
            sql =sql+"values ('"+Cobrador+"','"+custid+"',"+strTotal+",";
            sql =sql+"'ABONO A LA CUENTA','','BUENA',";
            sql = sql+"'ABONO A LA CUENTA','"+longi+"','"+lati+"','ENVIADO',datetime(current_timestamp, 'localtime'),'"+ Folio
                    +"',datetime(current_timestamp, 'localtime'),datetime(current_timestamp, 'localtime'))";

            SQLiteDatabase db2= null;
            db2 = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            db2.execSQL(sql);
            db2.close();

            /***TERMINA INSERT DE BASE DE DATOS***/

        } catch (Exception e) {
            Toast toast = Toast.makeText(getBaseContext(), "ERROR AbonoInsert1: "+ e.getMessage().toString(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /*************************************/
    /*********GENERA FOLIO DE FACTURA****/
    /************************************/

    public String  GeneraFolio(String Cobrador){
        SQLiteDatabase db = null;
        String rutaBD_Cobrador=AbonosTarjeta.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null,SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        String Folio=Cobrador;
        /*
         * CONSULTA PARA OBTENER FECHA
         * */

        String strSql="SELECT strftime('%d%m%Y%H%M','NOW','localtime')  Fecha";

        Cursor cFecha = db.rawQuery(strSql,null);

        if (cFecha!=null){
            cFecha.moveToFirst();
            Folio= Folio + cFecha.getString(0);
        }
        cFecha.close();

        return Folio;
    }

    public void Imprime(){
        Intent intent = new Intent(AbonosTarjeta.this, PrinterActivity.class);
        intent.putExtra("TIPO", "NOTA");
        intent.putExtra("custid", custid);
        intent.putExtra("folio", "");
        startActivity(intent);
        this.finish();//para que cierre la pantalla de abono Merly
    }

    /***********************************/
    /**********CREO EL MENU************/
    /*********************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MNU_OPC1, Menu.NONE, "REGRESAR").setIcon(R.mipmap.pasteselected);
        menu.add(Menu.NONE, MNU_OPC2, Menu.NONE, "INICIO").setIcon(R.mipmap.icon );

        return true;
    }
    /*********************************/
}
