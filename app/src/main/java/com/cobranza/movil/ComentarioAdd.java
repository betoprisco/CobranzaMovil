package com.cobranza.movil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import Clases.MiClase;

public class ComentarioAdd extends Activity implements Runnable, View.OnClickListener {
    public String custid;
    public int idmenu;
    public String filtro;
    public String lati ="";
    public String longi = "";
    String Cobrador="";
    /*gps*/

    private Location currentLocation;
    private LocationManager mLocationManager;

    private MyLocationListener mLocationListener;
    public int ContadorIntentosGPS=0;

    ProgressDialog dialog;
    int increment;

    private TextView mDateDisplay;
    private Button mPickDate;

    private int mYear;
    private int mMonth;
    private int mDay;

    static final int DATE_DIALOG_ID = 0;

    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;

    MiClase clase = new MiClase();

    String rutaBD_Cobrador;
    String rutaBD_CobradorDatos;

    //PARA REVISAR SI TIENE ACTIVADO EL PERMISO DE ACCEDER A LA UBICACIÓN - JAPP - 05-09-2018
    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Se requieren los permisos", "Es necesario habilitar los permisos de acceder a la ubicación para usar la app",
                        Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            }
        } else {
            Toast.makeText(ComentarioAdd.this, "GPS Activado", Toast.LENGTH_SHORT).show();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maincomentario);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //SE REVISA SI TIENE EL PERMISO PARA SABER LA UBICACIÓN - JAPP - 05-09-2019
        showPhoneStatePermission();
        writeSignalGPS();

        /******************************/
        /*Lectura del archivo CONFIG*/
        String rutaBD_USRCOB=ComentarioAdd.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************

         // TODO Auto-generated method stub
         /*tomo el valor del id del cliente*/

        rutaBD_Cobrador=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
        rutaBD_CobradorDatos=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        filtro=getIntent().getExtras().getString("filtro");
        custid= getIntent().getExtras().getString("Custid");
        idmenu=getIntent().getExtras().getInt("idmenu");

        TextView txt_Cliente =  findViewById(R.id.txt_Custid);
        txt_Cliente.setText(custid);

        /*********** CODIGO PARA LAS LISTAS ********/
        CargaListas();
        Spinner spinner =  findViewById(R.id.Lista_Perspectiva  );
        spinner.setOnItemSelectedListener(new MyOnItemSelectedListener());
        Spinner spinner2 =  findViewById(R.id.Lista_Accion  );
        spinner2.setOnItemSelectedListener(new MyOnItemSelectedListener());
        /***********************/

        /************MOSTRAR MSJ DE FECHA***************/

        // capture our View elements
        mDateDisplay =  findViewById(R.id.txt_FechaSeg );
        mPickDate =  findViewById(R.id.btn_Fecha );

        // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        // display the current date
        updateDisplay();

        /**********************************/
        /*agregamos comentario*/
        Button btn_AgregaComen =  findViewById(R.id.btn_AgregaComen);

        btn_AgregaComen.setOnClickListener(this);
        /*fin comentarios*/

        //BOTÓN PARA LA DIRECCIÓN ALTERNA - JAPP - 07-04-2016
        Button botónDirAlterna = findViewById(R.id.btn_modificaTel);
        botónDirAlterna.setOnClickListener(dirAlterna);
    }

    //LLAMAR A LA PANTALLA DE LA DIRECCIÓN ALTERNA - JAPP - 07-04-2016
    private View.OnClickListener dirAlterna = new View.OnClickListener(){
        @Override
        public void onClick(View v)
        {
            Cursor c = null;
            String cliente="";

            String sql="select nombre from customer where custid like  '%" + custid + "%'";
            try{
                c=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

                if (c!=null){
                    c.moveToFirst();
                    cliente=c.getString(0);

                    Intent direcciónAlterna = new Intent();
                    direcciónAlterna.setClass(ComentarioAdd.this, Diralta.class );
                    direcciónAlterna.putExtra("custid", custid);
                    direcciónAlterna.putExtra("cliente", cliente);

                    startActivity(direcciónAlterna);
                }
            }catch(Exception e){
            }
        }
    };

    public class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                                   long arg3) {
            // TODO Auto-generated method stub

            //**********************************************************************
            //MANDO EL CONTEXTO Y EL VALOR SELECCIONADO DE CUALQUIERA DE LAS LIXTAS
            //**********************************************************************
            ActualizaListaAccion(arg0.getContext(),arg0.getItemAtPosition((int) arg3).toString());
        }

        private <Perspectiva> void ActualizaListaAccion(Context text, String Perspectiva ) {
            // TODO Auto-generated method stub
            //************************************
            //CREO LAS VARIABLES PARA LOS CAMPOS
            //************************************
            Spinner s =  findViewById(R.id.Lista_Accion);
            TextView monto =  findViewById(R.id.Txt_Monto);
            TextView lbl_Monto =  findViewById(R.id.lbl_Monto);
            TextView lbl_Fecha = findViewById(R.id.lbl_Fecha );
            TextView txt_FechaSeg=  findViewById(R.id.txt_FechaSeg);
            Button btn_Fecha =  findViewById(R.id.btn_Fecha);

            //**********************************************************************
            //DEPENDIENDO EL VALOR DE LA LISTA PERSPECIVA ACTUALIZO LA LISTA ACCION
            //**********************************************************************
            if ( Perspectiva.equals("BUENA") ){
                ArrayAdapter<?> adapter2 = ArrayAdapter.createFromResource(ComentarioAdd.this, R.array.array_ACCION_BUENA  , android.R.layout.simple_spinner_item );
                s.setAdapter(adapter2);
            }else{
                monto.setVisibility(View.INVISIBLE);
                lbl_Monto.setVisibility(View.INVISIBLE);
            }
            if (Perspectiva.equals("MALA")){
                ArrayAdapter<?> adapter2 = ArrayAdapter.createFromResource(ComentarioAdd.this, R.array.array_ACCION_MALA  , android.R.layout.simple_spinner_item );
                s.setAdapter(adapter2);
            }
            if (Perspectiva.equals("INDEFINIDA")){

                ArrayAdapter<?> adapter2 = ArrayAdapter.createFromResource(ComentarioAdd.this, R.array.array_ACCION_INDEFINIDA , android.R.layout.simple_spinner_item );
                s.setAdapter(adapter2);
            }

            //**********************************************************************
            //MUESTRO LA FECHA SOLO CUANDO SEA PROMESA O CONVENIO
            //**********************************************************************
            if( Perspectiva.equals("PROMESA" ) || Perspectiva.equals("CONVENIO")){
                lbl_Fecha.setVisibility(View.VISIBLE);
                txt_FechaSeg.setVisibility(View.VISIBLE);
                btn_Fecha.setVisibility(View.VISIBLE);
            }else{
                lbl_Fecha.setVisibility(View.INVISIBLE);
                txt_FechaSeg.setVisibility(View.INVISIBLE);
                btn_Fecha.setVisibility(View.INVISIBLE);
            }

            //**********************************************************************
            //SOLO CUANDO ES ABONO A LA CUENTA O CONVENIO MUESTRO CAMPO DE MONTO
            //**********************************************************************

            if( Perspectiva.equals("ABONO A LA CUENTA" ) || Perspectiva.equals("CONVENIO")  || Perspectiva.equals("PROMESA")){
                monto.setVisibility(View.VISIBLE);
                lbl_Monto.setVisibility(View.VISIBLE);
            }else{

                monto.setVisibility(View.INVISIBLE);
                lbl_Monto.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
            Log.i("onNothingSelected ", "jjjjjjjjjjjjjjjjj");
        }

    }
    void CargaListas(){

        /***** MUESTRO LA PRIMER LISTA *****/

        Spinner spinner =  findViewById(R.id.Lista_Perspectiva  );

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.array_Perspectivas, android.R.layout.simple_spinner_item );

        spinner.setAdapter(adapter);

        /***** FIN DE LA PRIMER LISTA*****/
    }

    /************************************************/
    /*****METODOS PARA MOSTRAR CALENDARIO **********/
    /**********************************************/
    // updates the date we display in the TextView
    private void updateDisplay() {
        mDateDisplay.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mDay).append("-")
                        .append(mMonth + 1).append("-")
                        .append(mYear).append(" "));

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener,   mYear, mMonth, mDay);
        }
        return null;
    }

    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

    /****************************************************/
    /*****FIN METODOS PARA MOSTRAR CALENDARIO **********/
    /**************************************************/


    //******************************************************
    //************INSERTA COMENTARIOS **********************
    //******************************************************
    public void AgregaComentario(){

        // TODO Auto-generated method stub

        Spinner s2 = findViewById(R.id.Lista_Perspectiva);
        Spinner accion = findViewById(R.id.Lista_Accion);

        try{
            int valor=ValidaCampos( s2.getSelectedItem().toString(),accion.getSelectedItem().toString());

            if ( valor== 0){
                Barra();
            }else{
                String cadena="ADVERTENCIA: Imposible Guardar, verifica los campos. ";

                if (valor==1)cadena = cadena + " * MONTO ";
                if (valor==2)cadena = cadena + " * Fecha ";
                if (valor==3)cadena = cadena + " * TEL ";
                if (valor==4)cadena = cadena + " * COMENTARIO ";
                if (valor==5)cadena="ADVERTENCIA: No se pudo guardar la perspectiva, menos de 4 periodos que se puso como localizable";
                if (valor==10)cadena = cadena + " * validacion ";

                Toast toastx = Toast.makeText(getBaseContext(), cadena , Toast.LENGTH_LONG);
                toastx.show();
            }
        } catch (Exception e) {
            Toast toast = Toast.makeText(getBaseContext(), "ERROR2: "+ e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // fin de los comentario

    public int  ValidaCampos(String perspectiva, String accion){
        /**COMPONENTES DE LA ACTIVIDAD**/
        try{
            TextView TxtMonto =  findViewById(R.id.Txt_Monto );
            TextView Txt_Comentario=  findViewById(R.id.txt_Comentario);
            TextView Txt_Fecha=  findViewById(R.id.txt_FechaSeg);
            if (  perspectiva.equals("BUENA") ){
                if (TxtMonto.getText().toString().equals("")) return  1;
                if (Txt_Fecha.getText().toString().equals("")) return  2;
            }else{
                if(perspectiva.equals("MALA") && accion.equals("ILOCALIZABLE")){
                    //SE BUSCA AL CLIENTE EN LA TABLA DE CLIENTES LOCALIZABLES PARA VER SI TIENE MÁS DE 60 DÍAS - JAPP - 07-03-2019
                    String sql="select tiempo from clienteslocalizables where trim(custid) =  '" + custid.trim() + "'";
                    Cursor c = null;

                    try{
                        c=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);


                        if (c!=null){
                            c.moveToFirst();

                            if(Integer.parseInt(c.getString(0)) <= 60)
                                return 5;
                        }
                    }catch(Exception e){
                        //SI ENTRA AL CATCH ES PORQUE EL CLIENTE NO TIENE NINGÚN HISTORIAL, ENTONCES SE DEJARÁ QUE PROCEDA CON EL PROCESO - JAPP - 10-06-2019

                        /*Toast toast=Toast.makeText(getBaseContext(),"Error Cliente Localizable: " + e.toString(),Toast.LENGTH_LONG);
                        toast.show();*/
                    }
                }
                TxtMonto.setText("0.00");
            }
            if (Txt_Comentario.getText().toString().equals("")) return 4;
            return 0;
        }catch(Exception e){
            Toast toast = Toast.makeText(getBaseContext(), "ERROR3: "+ e.getMessage().toString(), Toast.LENGTH_SHORT);
            toast.show();
            return 10;
        }
    }

    @Override
    public void onClick(View arg0) {
        AgregaComentario();
    }

    //BUSQUEDA DE POSISCION GPS

    private void writeSignalGPS() {
        DialogInterface.OnCancelListener dialogCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        };

        try{

            Thread thread = new Thread(ComentarioAdd.this);
            thread.start();

        }catch (Exception e){
            Toast toast2 = Toast.makeText(getBaseContext(), "ERROR2: Buscando señal "+ e.getMessage().toString(), Toast.LENGTH_LONG);
            toast2.show();
        }
    }

    public void run() {
        try{
            String LocationType = LocationManager.GPS_PROVIDER;
            mLocationManager =     (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                Looper.prepare();
                mLocationListener = new MyLocationListener();
                mLocationManager.requestLocationUpdates(LocationType, 0, 0, mLocationListener);

                Looper.loop();
                Looper.myLooper().quit();
            }
        } catch  (SecurityException e){
            Toast toast2 = Toast.makeText(getBaseContext(), "ERROR2: Señal no encontrada "+ e.getMessage().toString(), Toast.LENGTH_LONG);
            toast2.show();
        }
    }
    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                setCurrentLocation(loc);

                lati=String.valueOf(loc.getLatitude()) ;
                longi=String.valueOf(loc.getLongitude()) ;

                Button btn =  findViewById(R.id.btn_AgregaComen);
                btn.setVisibility(View.VISIBLE);
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
                    Toast toast2 = Toast.makeText(getBaseContext(), "ERROR1: Handler muestra ubicasion "+ e.getMessage().toString(), Toast.LENGTH_LONG);
                    toast2.show();
                }
            }
        };
    }

    //------------GPS--------------------

    @SuppressWarnings("deprecation")
    public void RegistraDatos(){
        try{

            String sql="";

            Spinner s =  findViewById(R.id.Lista_Accion);
            Spinner s2 =  findViewById(R.id.Lista_Perspectiva);
            TextView Txt_Comentario=  findViewById(R.id.txt_Comentario);

            TextView TxtMonto =  findViewById(R.id.Txt_Monto );
            TextView Txt_Tel =  findViewById(R.id.txt_Tel);

            TextView txt_Fecha=  findViewById(R.id.txt_FechaSeg);

            //DATOS PARA LA CREACION DEL FOLIO
            Date horaActual = new Date();
            horaActual.toGMTString();

            String Folio=Integer.toString(horaActual.getYear())+Integer.toString(horaActual.getMonth())+Integer.toString(horaActual.getDay())
                    +Integer.toString(horaActual.getHours())+Integer.toString(horaActual.getMinutes())+Integer.toString(horaActual.getSeconds());

            String idGestion = generaAleatorio();

            String rutaBDCobradores=ComentarioAdd.this.getApplicationContext().getDatabasePath("cobradores").toString();

            sql="insert into historial (UserUpd,custid,MontoAPagar,historial,tel,Perspectiva,PerspectivaDet,";
            sql = sql +" Longitude,Latitude,status,FechaSeg,folio,FechaCrt,idGestion)  ";
            sql =sql+"values ('"+Cobrador+"','"+custid+"',"+TxtMonto.getText()+",";
            sql =sql+"'"+Txt_Comentario.getText().toString()+"','"+Txt_Tel.getText().toString()+"','"+s2.getSelectedItem().toString()+"',";
            sql = sql+"'"+s.getSelectedItem().toString()+"','"+longi+"','"+lati+"','ESPERA','"+txt_Fecha.getText()+"','"+Folio+"',datetime(current_timestamp, 'localtime'),'" + idGestion + "')";

            /*STATUS PARA EL ENVIO DE DATOS
             *
             * ESPERA:Cuando los datos no se han enviado al ws para ser guardados en la case de datos
             * ENVIADO:Cuando los datos se han enviado al ws para ser guardados en la case de datos
             *
             */

            SQLiteDatabase db= null;
            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            db.execSQL(sql);
            db.close();

            //ELIMINA DE LA LISTA DE NO GESTIONADOS - JAPP - 06-04-2016 - INICIO
            sql="delete from noGestionados  ";
            sql =sql+"where trim(custid) like '"+custid.trim()+"%'";

            clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR_DATOS , MiClase.TypeSQL.UPDATE,rutaBD_CobradorDatos);
            //ELIMINA DE LA LISTA DE NO GESTIONADOS - JAPP - 06-04-2016 - FIN
            /***TERMINA INSERT DE BASE DE DATOS***/

            if(idmenu==7){
                Intent intent = new Intent(ComentarioAdd.this,Listacliente2.class);
                intent.putExtra("idmenu", idmenu);
                intent.putExtra("filtro", filtro);
                startActivity(intent);
                this.finish(); ///cierra la ventana de abono
            }else{
                Intent intent = new Intent(ComentarioAdd.this,listacliente.class);
                intent.putExtra("idmenu", idmenu);
                intent.putExtra("filtro", filtro);
                startActivity(intent);
                this.finish(); ///cierra la ventana de abono
            }
        }catch(Exception e){
            /*Toast toast2 = Toast.makeText(getBaseContext(), "ERROR1.5: "+ e.getMessage().toString(), Toast.LENGTH_LONG);
            toast2.show();*/
        }
    }

    String generaAleatorio(){
        Random r = new Random();
        String numero;
        numero = String.valueOf(r.nextInt(999999999));
        return numero;
    }

    public void Barra(){
        increment = 1;

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Buscando GPS...");
        // set the progress to be horizontal
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // reset the bar to the default value of 0
        dialog.setProgress(0);

        // get the maximum value
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

                        Log.i("--While", "Progress Bar +++ ++ + ");
                        // active the update handler

                        progressHandler.sendMessage(progressHandler.obtainMessage());
                    }

                    Log.i("Para el dismiss", "*********************");
                    dialog.dismiss();

                    RegistraDatos();

                } catch (java.lang.InterruptedException e) {
                    /*Toast toast2 = Toast.makeText(getBaseContext(), "ERROR1.6: "+ e.getMessage().toString(), Toast.LENGTH_LONG);
                    toast2.show();*/
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

}