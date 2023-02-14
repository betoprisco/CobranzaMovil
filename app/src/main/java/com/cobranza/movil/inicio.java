package com.cobranza.movil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;

import Clases.ClaseConexiones;
import Clases.Clase_creaBDcobranza;
import Clases.MiClase;
import com.cobranza.convenios.ListaConvenios;

public class inicio extends Activity implements Runnable
{
    public String lati ="";
    public String longi = "";

    private ProgressDialog pd;

    private Location currentLocation;
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    MiClase clase = new MiClase();
    int opción;
    EnviaDatos_Asinck MiHiloAsinck = new EnviaDatos_Asinck();

    final Handler mHandler=new Handler();
    String AndroidVersion,AppVersion;
    boolean flag = true;

    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;

    /********************************************************/

    public void CambiaStatus(String folio, String resp,String Tabla, String Clave){
        String sql = "";
        if (Tabla.equals("Historial")){
            // para guardar fecha envío
            final Calendar d = Calendar.getInstance();
            int anio = d.get(Calendar.YEAR);
            int mes = d.get(Calendar.MONTH);
            int dia = d.get(Calendar.DAY_OF_MONTH);
            int hora = d.get(Calendar.HOUR_OF_DAY);
            int minuto = d.get(Calendar.MINUTE);
            int segundo = d.get(Calendar.SECOND);

            String fechaPrueba = String.valueOf(anio)+"-" + String.valueOf(mes+1)+"-"+
                    String.valueOf(dia) +" "+ String.valueOf(hora)+":"+ String.valueOf(minuto)+
                    ":"+ String.valueOf(segundo);

            sql ="update "+ Tabla +" set Status='ENVIADO', fechaEnvio='"+ fechaPrueba +"' where "+
                    Clave +"='"+folio+"'";
        }


        if (Tabla.equals("decomisos")){
            sql ="update "+ Tabla +" set estadoEnvio='ENVIADO' where "+ Clave +"='"+folio+"'";
        }else if(Tabla.equals("imagenDecomiso")){
            sql ="update "+ Tabla +" set enviado='ENVIADO' where "+ Clave +"='"+folio+"'";

        }
        else{
            sql ="update "+ Tabla +" set Status='ENVIADO' where "+ Clave +"='"+folio+"'";
        }

        SQLiteDatabase db= null;

        String rutaBDCobradores=inicio.this.getApplicationContext()
                .getDatabasePath("cobradores").toString();

        db = SQLiteDatabase.openDatabase(rutaBDCobradores,null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        db.execSQL(sql);

        /*fecha Actual*/

        Date horaActual = new Date();
        horaActual.toGMTString();

        db.close();
    }

    /**********************************************************/
    /***********************************************************/
    /*LECTURA DE ARCHIVO CONFIG para sacar el id de cobrador*/
    public String Lectura(){
        String texto="";

        String rutaBD_USRCOB=inicio.this.getApplicationContext().getFilesDir().toString() +
                "/BD_USRCOB.zip";

        String sql= "Select CobID, Nombre  from Tbl_Usuarios where sesion = '1' ";
        SQLiteDatabase db= null;
        db = SQLiteDatabase.openDatabase(rutaBD_USRCOB,null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext()){
            texto = c.getString(0);
        }

        Log.i("inicio", texto);

        return texto;
    }


    public String LecturaNombreCob(){
        String texto="";

        String sql= "Select CobID, Nombre  from Tbl_Usuarios where sesion = '1' ";
        SQLiteDatabase db= null;

        String rutaBD_USRCOB=inicio.this.getApplicationContext().getFilesDir().toString() +
                "/BD_USRCOB.zip";

        db = SQLiteDatabase.openDatabase(rutaBD_USRCOB,null,
                SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext()){
            texto = c.getString(1);
        }

        Log.i("inicio", texto);

        return texto;
    }

    /***********************************************************/
    /*Checo si hay internet*/
    public  boolean ChecaConexion(Context ctx){
        boolean bTieneConexion = false;
        ConnectivityManager connec =  (ConnectivityManager)ctx.getSystemService(Context
                .CONNECTIVITY_SERVICE);

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

    //PARA REVISAR SI TIENE ACTIVADO EL PERMISO DE ACCEDER A LA UBICACIÓN - JAPP - 05-09-2018
    private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showExplanation("Se requieren los permisos", "Es necesario " +
                                "habilitar los permisos de acceder a la ubicación para usar la app",
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                        REQUEST_PERMISSION_ACCESS_FINE_LOCATION);
            }
        } else {
            Toast.makeText(inicio.this, "GPS Activado", Toast.LENGTH_SHORT).show();
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
        if (requestCode == REQUEST_PERMISSION_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {
                Toast.makeText(getBaseContext(), "¡Es necesario acceder a su ubicación para" +
                        " poder usar la app!", Toast.LENGTH_LONG);
                this.finish();
            }
            return;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AndroidVersion = Build.VERSION.RELEASE_OR_CODENAME;
        }else
            AndroidVersion = Build.VERSION.RELEASE;

        AppVersion=BuildConfig.VERSION_NAME;

        /******************************/
        /*ejecuta gps*/

        Log.i("onCreate", "onCreate--------------------------");

        //SE REVISA SI TIENE EL PERMISO PARA SABER LA UBICACIÓN - JAPP - 05-09-2019
        showPhoneStatePermission();

        writeSignalGPS();

        Clase_creaBDcobranza db = new Clase_creaBDcobranza(this);
        db.getWritableDatabase();

        MiHiloAsinck.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //Declaramos los controles
        final ListView lstOpciones = findViewById(R.id.LstOpciones);

        final String[] menu = new String[]{"Colonia","Periodos Vencidos","Promesas Incumplidas",
                "Nombres","Proceso de Envío","Corte de Caja", "Descargar","Promesas Vencidas",
                "Promesas A Vencer","No Gest Sin Pago","Pagos Totales","Clientes sin Pago",
                "Convenios","Limpiar Datos"};
        ArrayAdapter<String> adaptador = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,menu);
        lstOpciones.setAdapter(adaptador);

        lstOpciones.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                // TODO Auto-generated method stub

                Intent intent;
                try
                {
                    switch(arg2){
                        case 0: case 1: case 2:
                            llamarSubmenu(arg2);
                            break;
                        case 3:
                            intent = new Intent(inicio.this,Listacliente2.class);
                            intent.putExtra("idmenu", 3);
                            startActivity(intent);
                            break;
                        case 4:
                            intent = new Intent(inicio.this,historialenvios.class);
                            intent.putExtra("idmenu", 3);
                            startActivity(intent);
                            break;
                        case 5:
                            intent = new Intent(inicio.this,CorteCajaV3.class);
                            intent.putExtra("idmenu", 3);
                            startActivity(intent);
                            break;
                        case 6:
                            if (ChecaConexion(getBaseContext())){
                                iniciaDescarga();
                            }else{
                                MessageBox("Sin Internet");
                            }
                            break;
                        case 7:
                            intent = new Intent(inicio.this,Listacliente2.class);
                            intent.putExtra("idmenu", 7);
                            startActivity(intent);
                            break;
                        case 8:
                            intent = new Intent(inicio.this,Listacliente2.class);
                            intent.putExtra("idmenu", 8);
                            startActivity(intent);
                            break;
                        case 9:
                            intent = new Intent(inicio.this,Listacliente2.class);
                            intent.putExtra("idmenu", 9);
                            startActivity(intent);
                            break;
                        case 10:
                            intent = new Intent(inicio.this,Listacliente2.class);
                            intent.putExtra("idmenu", 10);
                            startActivity(intent);
                            break;
                        case 11:
                            intent = new Intent(inicio.this,Listacliente2.class);
                            intent.putExtra("idmenu", 11);
                            startActivity(intent);
                            break;
                        case 12:
                            intent = new Intent(inicio.this, ListaConvenios.class);
                            intent.putExtra("idmenu", 11);
                            startActivity(intent);
                            break;
                        case 13:
                            limpiarDatos();
                            break;
                    }
                }
                catch (Exception e)
                {
                    MessageBox(e.getMessage());
                }

            }});
    }

    public void limpiarDatos(){
        AlertDialog.Builder builder = new AlertDialog.Builder(inicio.this);
        builder.setMessage(R.string.dialog_limpiar_datos)
                .setPositiveButton(R.string.dialog_continuar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String consulta;
                        String textError="Sin Error";
                        Boolean error = false;

                        SQLiteDatabase db= null;

                        consulta = "DELETE FROM Abonos WHERE Status='ENVIADO'";

                        String rutaBDCobradores=inicio.this.getApplicationContext()
                                .getDatabasePath("cobradores").toString();

                        db = SQLiteDatabase.openDatabase(rutaBDCobradores,null,
                                SQLiteDatabase.NO_LOCALIZED_COLLATORS);

                        try{
                            db.execSQL(consulta);
                        }catch(Exception e){
                            error=true;
                            textError=e.toString();
                        }

                        db.close();

                        consulta = "DELETE FROM historial WHERE status='ENVIADO'";

                        db = SQLiteDatabase.openDatabase(rutaBDCobradores,null,
                                SQLiteDatabase.NO_LOCALIZED_COLLATORS);

                        try{
                            db.execSQL(consulta);
                        }catch(Exception e){
                            error=true;
                            textError=e.toString();
                        }

                        db.close();

                        consulta = "DELETE FROM decomisos WHERE EstadoEnvio='ENVIADO'";

                        db = SQLiteDatabase.openDatabase(rutaBDCobradores,null,
                                SQLiteDatabase.NO_LOCALIZED_COLLATORS);

                        try{
                            db.execSQL(consulta);
                        }catch(Exception e){
                            error=true;
                            textError=e.toString();
                        }

                        db.close();

                        consulta = "DELETE FROM dirAlterna WHERE Status='ENVIADO'";

                        db = SQLiteDatabase.openDatabase(rutaBDCobradores,null,
                                SQLiteDatabase.NO_LOCALIZED_COLLATORS);

                        try{
                            db.execSQL(consulta);
                        }catch(Exception e){
                            error=true;
                            textError=e.toString();
                        }

                        db.close();

                        consulta = "DELETE FROM convenio WHERE estatus='ENVIADO'";

                        db = SQLiteDatabase.openDatabase(rutaBDCobradores,null,
                                SQLiteDatabase.NO_LOCALIZED_COLLATORS);

                        try{
                            db.execSQL(consulta);
                        }catch(Exception e){
                            error=true;
                            textError=e.toString();
                        }

                        db.close();

                        if(error==false){
                            Toast toast2 = Toast.makeText(getBaseContext(), "¡Datos " +
                                    "limpiados con éxito!", Toast.LENGTH_LONG);
                            toast2.show();
                        }else{
                            Toast toast2 = Toast.makeText(getBaseContext(), textError,
                                    Toast.LENGTH_LONG);
                            toast2.show();
                        }

                    }
                })
                .setNegativeButton(R.string.dialog_salir, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                })
                .setTitle(R.string.dialog_title);
        // Create the AlertDialog object and return it
        builder.create();
        builder.show();
    }

    public void iniciaDescarga(){
        opción=6;

        if (ChecaConexion(getBaseContext())){
            pd=ProgressDialog.show(this, "Descargando Bases de Datos",
                    "Espere un momento por favor...",true,false);
            EnviaDatos_Asinck descargaBD = new EnviaDatos_Asinck();
            descargaBD.execute();
        }
    }

    public void llamarSubmenu(int idmenu){
        Intent intent = new Intent(inicio.this,submenu.class);
        intent.putExtra("idmenu", idmenu);
        startActivity(intent);
    }

    public void MessageBox(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    //BUSQUEDA DE POSISCION GPS
    private void writeSignalGPS() {
        DialogInterface.OnCancelListener dialogCancel = new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {}
        };
        try{
            Thread hiloGPS = new Thread(inicio.this);
            hiloGPS.start();
        }catch (Exception e){
            Toast toast2 = Toast.makeText(getBaseContext(), "ERROR2: Buscando señal "+
                    e.getMessage().toString(), Toast.LENGTH_LONG);
            toast2.show();
        }
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
        } catch (SecurityException e) {
            Toast toast2 = Toast.makeText(getBaseContext(), "ERROR2: Señal no encontrada " +
                    e.getMessage().toString(), Toast.LENGTH_LONG);
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
                    //pd.dismiss();
                    mLocationManager.removeUpdates(mLocationListener);
                    if (currentLocation!=null) {

                        Toast.makeText(getBaseContext(), "posicion: "+
                                currentLocation.toString() , Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e){
                    Toast toast2 = Toast.makeText(getBaseContext(),
                            "ERROR1: Handler muestra ubicasion "+
                                    e.getMessage().toString(), Toast.LENGTH_LONG);
                    toast2.show();

                }
            }
        };

    }

    //------------GPS--------------------
    public class EnviaDatos_Asinck extends AsyncTask<Void,Void,Boolean>{

        MiClase clase = new MiClase();

        public void EnviaNotificacion(CharSequence TextoEstado,CharSequence titulo,
                                      CharSequence descripcion,Context contexto){
            try{
                mBuilder = new NotificationCompat.Builder(getApplicationContext());
                mBuilder.setSmallIcon(android.R.drawable.ic_menu_upload);
                mBuilder.setContentTitle(titulo)
                        .setContentText(descripcion)
                        .setAutoCancel(false)
                        .setSound(null);

                mNotificationManager = (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                {
                    int importance = NotificationManager.IMPORTANCE_LOW;
                    NotificationChannel notificationChannel = new NotificationChannel("002",
                            "NOTIFICATION_CHANNEL_NAME", importance);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setSound(null,null);
                    notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500,
                            400, 300, 200, 400});
                    assert mNotificationManager != null;
                    mBuilder.setChannelId("002");
                    mNotificationManager.createNotificationChannel(notificationChannel);
                }
                assert mNotificationManager != null;
                mNotificationManager.notify(0 /* Request Code */, mBuilder.build());

            }catch(Exception e){
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            flag=true;

            try{
                if(opción==6){
                    String Mensaje;

                    String rutaBD_USRCOB=inicio.this.getApplicationContext().getFilesDir()
                            .toString() + "/BD_USRCOB.zip";
                    String Cobrador= clase.Lectura(rutaBD_USRCOB);

                    // creando carpeta
                    /*File file=new File("/sdcard/MovilHelp/");
                    if(!file.exists()) {
                        file.mkdirs();
                        Log.v("TestHttpActivity", "carpeta creada ej");
                    } else
                        Log.v("TestHttpActivity", "carpeta no creada ej");
                    */
                    int port = 192;

                    String url = "http://sbvconect.dyndns.org/Terminal/" + Cobrador + ".zip";

                    try {
                        DefaultHttpClient httpclient = new DefaultHttpClient();

                        httpclient.getCredentialsProvider().setCredentials(
                                new AuthScope(url, port),
                                new UsernamePasswordCredentials("administrador",
                                        "maxuxac"));

                        URI uri = new URI(url);
                        HttpGet method = new HttpGet(uri);
                        HttpResponse response = httpclient.execute(method);

                        InputStream in =  response.getEntity().getContent();
                        FileOutputStream f =
                                new FileOutputStream(new File(getApplicationContext()
                                        .getFilesDir().toString() + "/", Cobrador + ".zip"));

                        int len1 ;

                        while ( (len1 = in.read()) != -1 ) {
                            f.write(len1);
                        }
                        f.close();
                        in.close();
                        Log.v("TestHttpActivity", "download file");
                    } catch (ClientProtocolException e) {
                        Mensaje = "error1" + e.getMessage();
                        MessageBox(Mensaje);
                    } catch (IOException e) {
                        Mensaje = "error2" +  e.getMessage();
                        MessageBox(Mensaje);
                    } catch (URISyntaxException e) {
                        Mensaje = "error3" + e.getMessage();
                        MessageBox(Mensaje);
                    }catch (NetworkOnMainThreadException e){
                        Mensaje = "error4 " + e.getMessage();
                        MessageBox(Mensaje);
                    }

                    File file2=new File(getApplicationContext().getFilesDir().toString() +
                            "/", Cobrador + ".zip");
                    //File file3=new File("/sdcard/"+Cobrador+".zip");

                    Log.i("antes file2", "Entra");

                    if(file2.exists()) {
                        /*Elimino el archivo del MovilHelp*/
                        Log.i("file2", "Entra");
                 //       file3.delete();
                        /*Copio el archivo al sdcard*/

                  /*      try{
                            FileInputStream fis = new FileInputStream(file2); //origen
                            FileOutputStream fos = new FileOutputStream(file3); //destino
                            FileChannel canalFuente = fis. getChannel();
                            FileChannel canalDestino = fos. getChannel();
                            canalFuente.transferTo(0, canalFuente.size(), canalDestino);
                            fis.close();
                            fos.close();

                        }catch (IOException e){}

                        file2.delete();*/

                        Mensaje = "Archivo descargado "+Cobrador;
                    }else
                        Mensaje = "Problemas para descargar el archivo: "+ Cobrador;

                    //PARA LOS DATOS

                    // creando carpeta

                    /*file=new File("/sdcard/MovilHelp/");
                    if(!file.exists()) {
                        file.mkdirs();
                        Log.v("TestHttpActivity", "carpeta creada ej");
                    } else
                        Log.v("TestHttpActivity", "carpeta no creada ej");
*/
                    url = "http://sbvconect.dyndns.org/Terminal/" + Cobrador + "_DATOS.zip";

                    try {
                        DefaultHttpClient httpclient = new DefaultHttpClient();

                        httpclient.getCredentialsProvider().setCredentials(
                                new AuthScope(url, port),
                                new UsernamePasswordCredentials("administrador",
                                        "maxuxac"));

                        URI uri = new URI(url);
                        HttpGet method = new HttpGet(uri);
                        HttpResponse response = httpclient.execute(method);

                        InputStream in =  response.getEntity().getContent();
                        FileOutputStream f =
                                new FileOutputStream(new File(getApplicationContext()
                                        .getFilesDir().toString() + "/", Cobrador + "_DATOS.zip"));

                        int len1 ;

                        while ( (len1 = in.read()) != -1 ) {
                            f.write(len1);
                        }
                        f.close();
                        in.close();
                        Log.v("TestHttpActivity", "download file");
                    } catch (ClientProtocolException e) {
                        Mensaje = "error1" + e.getMessage();
                    } catch (IOException e) {
                        Mensaje = "error2" +  e.getMessage();
                    } catch (URISyntaxException e) {
                        Mensaje = "error3" + e.getMessage();
                    }

                    file2=new File(getApplicationContext().getFilesDir().toString() +
                            "/", Cobrador + "_DATOS.zip");
                    //file3=new File("/sdcard/"+Cobrador+"_DATOS.zip");

                    Log.i("antes file2", "Entra");

                    if(file2.exists()) {
                        /*Elimino el archivo del MovilHelp*/
                        /*Log.i("file2", "Entra");
                        file3.delete();
                        /*Copio el archivo al sdcard*/

                        /*try{
                            FileInputStream fis = new FileInputStream(file2); //origen
                            FileOutputStream fos = new FileOutputStream(file3); //destino
                            FileChannel canalFuente = fis. getChannel();
                            FileChannel canalDestino = fos. getChannel();
                            canalFuente.transferTo(0, canalFuente.size(), canalDestino);
                            fis.close();
                            fos.close();

                        }catch (IOException e){}

                        file2.delete();*/

                        Mensaje = "Archivo descargado "+Cobrador+"_DATOS";

                        //GuardaRegistro();
                    }else
                        Mensaje = "Problemas para descargar el archivo: "+ Cobrador+"_DATOS";

                    opción = 0;
                    pd.dismiss();
                    return null;

                }

                while (flag){
                    EnviaNotificacion("Movil", "Ejecución",
                            "Eliminando Registros", getBaseContext());
                    EliminaRegistros();

                    GPS_Abonos();
                    GPS_Historial();

                    EnviaNotificacion("Movil", "Ejecución",
                            "Termina Actualización GPS", getBaseContext());

                    if (ChecaConexion(getBaseContext())){
                        EnviaAndroidVersion();
                        EnviaHistorial();

                        EnviaAbonos();

                        enviaDirAlterna();

                        enviarDecomisos();
                        enviaFotoDeco();
                        enviaBDInstalacion();
                        EnviaNotificacion("Móvil", "Ejecución Móvil",
                                "Envía Datos", getBaseContext());


                        enviaConvenios();

                    }else{
                        EnviaNotificacion("Movil", "Ejecución",
                                "Sin Internet", getBaseContext());
                    }

                    /*
                     * a dormir por 4 min
                     */
                    Thread.sleep(240000 );

                }

            }catch(Exception e){
                EnviaNotificacion("Movil", "Error-Ejecucion", e.toString(),
                        getBaseContext());

            }

            return null;
        }

        public void EliminaRegistros(){
            try{
                String rutaBDCobradores=inicio.this.getApplicationContext()
                        .getDatabasePath("cobradores").toString();

                String sql="delete from Abonos where status in ('ENVIADO') and " +
                        "datetime(FechaAbo,'+3 day') <= datetime(current_timestamp, 'localtime')";
                clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES,
                        MiClase.TypeSQL.UPDATE,rutaBDCobradores);

                sql="delete from historial where status in ('ENVIADO') and " +
                        "datetime(FechaCrt,'+3 day') <= datetime(current_timestamp, 'localtime')";
                clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES,
                        MiClase.TypeSQL.UPDATE,rutaBDCobradores);

                sql="delete from convenio where estatus in ('ENVIADO') and " +
                        "datetime(fechaCRT,'+3 day') <= datetime(current_timestamp, 'localtime')";
                clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES,
                        MiClase.TypeSQL.UPDATE,rutaBDCobradores);

            }catch (Exception e){
                //Message("Exception Elimina registros",e.toString());
            }
        }

        public void EnviaAndroidVersion(){
            Cursor c =null;
            String idGestion = "";

            Log.i("JAPP", "EnviaAndroidVersion");

            String ruta=inicio.this.getApplicationContext().getFilesDir().toString() +
                    "/BD_USRCOB.zip";

            String sql= "Select CobID  from Tbl_Usuarios where sesion = '1' ";
            SQLiteDatabase db= null;


            db = SQLiteDatabase.openDatabase(ruta,null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            c = db.rawQuery(sql, null);

            while (c.moveToNext()){
                ClaseConexiones conn = new ClaseConexiones();
                String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.
                        HISTORIAL);
                String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion
                        .VERSIONDEANDROID);
                //ES IGUAL PARA TODOS
                String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.HISTORIAL_INTER);
                String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones
                        .TypeOperacion.VERSIONDEANDROID);

                SoapObject request = new SoapObject(NameSpace_,Metodo_ );

                /****************************************/
                /*INICIO PROCESO DE ENVIO DE DATOS A WS */
                /****************************************/

                idGestion = c.getString(0) ;

                request.addProperty("Cobrador", idGestion);
                request.addProperty("VersionDeAndroid",AndroidVersion);
                request.addProperty("VersionDeApp",AppVersion);

                EnviaDatosWS_("Versión de Android",request,url_,accion_soap_);

            }
        }

        public void EnviaHistorial(){//este es el bueno envio historial **********
            Cursor c =null;
            String idGestion = "";

            Log.i("++Entra++", "EnviaHistorial");

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            c=clase.Datos_Historial(rutaBDCobradores);

            if (c!=null){

                ClaseConexiones conn = new ClaseConexiones();
                String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.HISTORIAL);
                String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion.HISTORIAL);
                String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.HISTORIAL_INTER);
                String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones
                        .TypeOperacion.HISTORIAL);

                while (c.moveToNext()){
                    SoapObject request = new SoapObject(NameSpace_,Metodo_ );

                    /****************************************/
                    /*INICIO PROCESO DE ENVIO DE DATOS A WS */
                    /****************************************/

                    idGestion = c.getString(13) +" " +c.getString(12);

                    request.addProperty("CustId", c.getString(0));
                    request.addProperty("Comentario",c.getString(1));
                    request.addProperty("Perspectiva", c.getString(2));
                    request.addProperty("Accion",c.getString(3));
                    request.addProperty("User", c.getString(4));
                    request.addProperty("Monto", c.getString(5));
                    request.addProperty("tel", c.getString(6));
                    //El 7 es el status(ENVIADO, ESPERA)
                    request.addProperty("Longitud",c.getString(8));
                    request.addProperty("Latitud", c.getString(9));
                    request.addProperty("FechaSeg", c.getString(10));
                    request.addProperty("Fecha", c.getString(12));
                    //EL 11 ES EL FOLIO
                    request.addProperty("Correo", "");
                    request.addProperty("VCobMovil",R.string.app_version);
                    request.addProperty("idGestion", idGestion);

                    if (EnviaDatosWS_("HISTORIAL",request,url_,accion_soap_)){
                        CambiaStatus(c.getString(11),"-","Historial","folio");
                    }
                }
            }//if
        }//public void EnviaHistorial()

        public void GPS_Abonos(){

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            String sql ="update abonos set long='1',lati='1"+
                    "' where lati =''";
            clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES, MiClase.TypeSQL
                    .UPDATE,rutaBDCobradores);
        }
        public void GPS_Historial(){
            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            String sql ="update abonos set Longitude='1',Latitude='1"+
                    "' where Longitude=''";
            clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES, MiClase.TypeSQL.UPDATE,
                    rutaBDCobradores);
        }


        public void EnviaAbonos(){
            Cursor c=null;

            /*
             * Llamo a la consulta para q me regrese los abonos sin enviar.
             */

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();
            c=clase.Datos_Abonos(rutaBDCobradores);

            /*
             * Si la consulta genero algún resultado inicia el proceso de envios
             */
            if (c!=null){
                /*
                 * Declaro las variables necesarias para realizar la conexión
                 * con el web service
                 */

                ClaseConexiones conn = new ClaseConexiones();
                String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.ABONOS);
                String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion.ABONOS);
                String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.ABONOS_INTER);
                String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones.TypeOperacion.ABONOS);

                /*
                 * while para recorrer el cursor con los datos de la consula
                 * */

                while(c.moveToNext()){
                    try{
                        /*
                         * Genero los parametros de envio
                         */

                        SoapObject request = new SoapObject(NameSpace_,Metodo_ );

                        /****************************************/
                        /*INICIO PROCESO DE ENVIO DE DATOS A WS */
                        /****************************************/

                        EnviaNotificacion("Movil", "Envío de Abonos",
                                Integer.toString(c.getCount()) , getBaseContext());

                        request.addProperty("CustId", c.getString(0));
                        request.addProperty("cobrador", c.getString(1));
                        request.addProperty("Abono", c.getString(2));
                        request.addProperty("fecha", c.getString(9));//<---Fecha
                        request.addProperty("Longitud",c.getString(5));
                        request.addProperty("Latitud",c.getString(6));
                        request.addProperty("FolioFac", c.getString(8));
                        request.addProperty("Mora", c.getString(10));
                        request.addProperty("convenio", c.getString(11));
                        request.addProperty("opcConvenio", c.getString(12));
                        request.addProperty("apoyoCovid19", c.getString(13));

                        /*
                         *Llamo al metodo q envia los datos al ws
                         */
                        EnviaNotificacion("Movil", "Envío de Abonos",
                                c.getString(5), getBaseContext());
                        if (EnviaDatosWS_("ABONO",request,url_,accion_soap_)){
                            CambiaStatus(c.getString(7),"-","Abonos","Id");
                        }
                    }catch(Exception e){
                        EnviaNotificacion("Movil", "Error en el Envío de Abonos",
                                e.toString(), getBaseContext());

                    }// fin de try catch
                }//while que recorre cursor de datos

            }//If q valida datos de consulta

        }// fin del metodo EnviaAbonos

        //PARA QUE ENVÍE LOS DECOMISOS - JAPP - 17-03-2016 - INICIO
        public void enviarDecomisos(){
            Cursor c=null;

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            c=clase.datosDecomisos(rutaBDCobradores);

            if (c!=null){
                ClaseConexiones conn = new ClaseConexiones();

                //SE USA EL MISMO QUE EL ABONO PORQUE EL VALOR SIEMPRE ES "http://tempuri.org/"
                // - JAPP - 17-03-2016
                String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.ABONOS);
                String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion.DECOMISO);
                String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.DECOMISOS_INTER);
                String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones
                        .TypeOperacion.DECOMISO);

                /*
                 * while para recorrer el cursor con los datos de la consula
                 * */

                while(c.moveToNext()){
                    try{
                        /*
                         * Genero los parametros de envio
                         */

                        SoapObject request = new SoapObject(NameSpace_,Metodo_ );

                        /****************************************/
                        /*INICIO PROCESO DE ENVIO DE DATOS A WS */
                        /****************************************/

                        EnviaNotificacion("Móvil", "Envío Decomisos 1.0",
                                Integer.toString(c.getCount()) , getBaseContext());

                        request.addProperty("Custid",c.getString(0));
                        request.addProperty("ClaveArt",c.getString(1));
                        request.addProperty("Embarque",c.getString(2));
                        request.addProperty("FechaDecomiso",c.getString(3));
                        request.addProperty("Detalles",c.getString(4));
                        request.addProperty("Art",c.getString(5));
                        request.addProperty("FechaCompra",c.getString(6));
                        request.addProperty("Cobrador",c.getString(7));
                        request.addProperty("AutorizaDecomiso",c.getString(8));
                        request.addProperty("FolioDecomiso",c.getString(9));
                        request.addProperty("Status",c.getString(10));
                        request.addProperty("BodegaRecibe",c.getString(11));
                        request.addProperty("tiendaDeCompra",c.getString(12));
                        request.addProperty("Longitud",c.getString(13));
                        request.addProperty("Latitud",c.getString(14));
                        request.addProperty("NoSerie",c.getString(15));
                        request.addProperty("Funcionamiento",c.getString(17));
                        request.addProperty("Accesorios",c.getString(18));

                        /*
                         *Llamo al metodo q envia los datos al ws
                         */
                        EnviaNotificacion("Movil", "Decomiso-Envío 1.0",
                                c.getString(5), getBaseContext());
                        if (EnviaDatosWS_("DECOMISOS",request,url_,accion_soap_)){
                            CambiaStatus(c.getString(16),"-","decomisos","Id");
                        }
                    }catch(Exception e){
                        EnviaNotificacion("Móvil", "Envío Decomisos 1.0",
                                e.toString(), getBaseContext());
                    }// fin de try catch
                }//while que recorre cursor de datos

            }//If q valida datos de consulta

        }// fin del metodo EnviaAbonos
        //PARA QUE ENVÍE LOS DECOMISOS - JAPP - 17-03-2016 - FIN

        //envia la foto del decomiso
        public void enviaFotoDeco(){
            Cursor c = null;

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            c = clase.datosIMGdeco(rutaBDCobradores);

            try {

                if (c!=null){
                    ClaseConexiones conn = new ClaseConexiones();

                    String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.ABONOS);
                    String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion.IMGDECO);
                    String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.DECOMISOS_INTER);
                    String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones
                            .TypeOperacion.IMGDECO);

                    while(c.moveToNext()){
                        SoapObject request = new SoapObject(NameSpace_,Metodo_ );

                        Bitmap imagen_decodificar = BitmapFactory.decodeFile (
                                Environment.getExternalStorageDirectory()+
                                        "/DecomisosF/"+ c.getString(1));

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        imagen_decodificar.compress(Bitmap.CompressFormat.JPEG, 70, out);
                        byte[] imagebyte = out.toByteArray();
                        String strBase64 = Base64.encode(imagebyte);

                        request.addProperty("buffer", strBase64);
                        request.addProperty("NombreIMG", c.getString(1));
                        request.addProperty("Folio", c.getString(0));

                        if (EnviaDatosWS_("DECOMISOS",request,url_,accion_soap_)){
                            CambiaStatus(c.getString(0),"-","imagenDecomiso",
                                    "idDeco");
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception

                Toast.makeText(getBaseContext(), "Error al enviar imagen", Toast.LENGTH_LONG);
            }
        }

        public void enviaDirAlterna(){
            Cursor c = null;

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            c = clase.Datos_Diralt(rutaBDCobradores);


            if (c != null){
                ClaseConexiones conn = new ClaseConexiones();
                String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.ABONOS);
                String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion.DIRECCION);
                String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.ABONOS_INTER);
                String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones
                        .TypeOperacion.DIRECCION);

                while(c.moveToNext()){
                    try{
                        SoapObject request = new SoapObject(NameSpace_,Metodo_);
                        Log.i("Entra++++ ","Envio Direccion");
                        request.addProperty("custid", c.getString(0));
                        request.addProperty("Nombre", c.getString(1));
                        request.addProperty("Addr1", c.getString(2));
                        request.addProperty("Addr2", c.getString(3));
                        request.addProperty("Fax", c.getString(4));
                        request.addProperty("Phone", c.getString(5));
                        request.addProperty("FechaCrt", c.getString(6));
                        request.addProperty("IdRegistro", c.getString(7));
                        request.addProperty("Cobrador", c.getString(8));


                        if (EnviaDatosWS_("DIRECCION",request,url_,accion_soap_)){
                            CambiaStatus(c.getString(0),"-","dirAlterna",
                                    "CustID" );
                        }

                    }catch(Exception e){
                        Log.i("Entra++++ ", e.toString());
                        EnviaNotificacion("Movil", "DirAlterna-Envio ",
                                e.toString(), getBaseContext());
                    }
                }
            }
        }

        public void enviaBDInstalacion(){

            Log.i("enviaBDInstalacion", "+++++++++ enviaBDInstalacion ++++++++++++++");

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            Cursor c = null;
            c = clase.VerificaInstalacion(rutaBDCobradores); //obtengo los datos de la consulta

            if (c != null){ //si la consulta no tiene datos

                ClaseConexiones conn = new ClaseConexiones();
                String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.ABONOS);
                String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion.ACTUALIZO_BD);
                String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.ACTUALIZO_BD_INTER);
                String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones.TypeOperacion
                        .ACTUALIZO_BD);

                while(c.moveToNext()){
                    try {
                        SoapObject request = new SoapObject(NameSpace_,Metodo_);
                        Log.i("Entra++++ ","Envio Base de datos");
                        String cobra = Lectura();
                        String nombre = LecturaNombreCob();

                        request.addProperty("cobrador", cobra);
                        request.addProperty("creo", c.getString(0));
                        request.addProperty("nombre", nombre);

                        if (EnviaDatosWS_("ACTUALIZO_BD",request,url_,accion_soap_)){
                            Log.i("EnviaDatosWS_",
                                    "******************EnviaDatosWS_***********************");
                            CambiaCreado(c.getString(0),"-","Creacion",
                                    "CustID" );
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }

        public boolean  EnviaDatosWS_(String tipo,SoapObject request, String url_,
                                      String AccionSoap_){
            //	sobre

            Log.i(tipo, "******************************************************************");

            Boolean flag= false;

            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope (SoapEnvelope.VER11 );

            sobre.dotNet = true;

            sobre.setOutputSoapObject(request);
            // transporte

            HttpTransportSE transporte= new HttpTransportSE(url_);

            try{

                transporte.call(AccionSoap_, sobre);
                //resultado
                SoapPrimitive resultado = (SoapPrimitive)sobre.getResponse() ;

                Log.i(resultado.toString(), "***********************************************" +
                        "*******************");

                if  (resultado.toString().equals("1")){
                    flag= true;
                }else{
                    EnviaNotificacion("Movil", tipo + "-Envio",
                            resultado.toString(), getBaseContext());
                }

            } catch (Exception e) {
                Log.i("BETOPRISCO",e.toString());
            }
            return flag;
        }//Fin del metodo EnviaDatosWS



        public void CambiaCreado(String folio, String resp,String Tabla, String Clave){
            // CambiaStatus(c.getString(7),"-","decomisos","Id");
            Log.i(Tabla, "****************************CambiaCreado***********************" +
                    "***************");
            String sql = "";
            if (Tabla == "Creacion"){
                Log.i(Tabla, "********************************CambiaCreado2****************" +
                        "******************");
                sql ="update "+ Tabla +" set Nuevo='1' ";
                Log.i(Tabla, sql );
            } else if (Tabla.equals("convenio")){
                Log.i(Tabla, "********************************CambiaCreado2****************" +
                        "******************");
                sql ="update "+ Tabla +" set estatus='ENVIADO' ";
                Log.i(Tabla, sql );
            }


            SQLiteDatabase db= null;

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null,
                    SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            db.execSQL(sql);

            /*fecha Actual*/

            Date horaActual = new Date();
            horaActual.toGMTString();

            db.close();

        }

        public void enviaConvenios(){
            Log.i("enviaConvenio", "+++++++++ EnviaConvenio ++++++++++++++");

            String rutaBDCobradores=inicio.this.getApplicationContext()
                    .getDatabasePath("cobradores").toString();

            Cursor c = null;
            c = clase.verificaConvenio(rutaBDCobradores);

            if (c!=null){

                //Datos para la conexion
                ClaseConexiones conn = new ClaseConexiones();
                String NameSpace_ =conn.Cadenas_NameSpace(ClaseConexiones.TypeOperacion.ABONOS);
                String Metodo_ = conn.Cadenas_Metodos(ClaseConexiones.TypeOperacion.CCONVENIO);
                String url_ = conn.Cadenas_url(ClaseConexiones.TypeURL.ACTUALIZO_BD_INTER);
                String accion_soap_ = conn.Cadenas_AccionSoap(ClaseConexiones
                        .TypeOperacion.CCONVENIO);

                while(c.moveToNext()){
                    try {

                        SoapObject request = new SoapObject(NameSpace_,Metodo_);
                        Log.i("Entra ","Envio convenio");

                        request.addProperty("Custid", c.getString(0));
                        request.addProperty("Opcion", c.getString(1));
                        request.addProperty("Pago", c.getString(2));
                        request.addProperty("Mora", c.getString(3));
                        request.addProperty("Quincenas", c.getString(4));
                        request.addProperty("SaldoT", c.getString(5));
                        request.addProperty("Letra", c.getString(6));
                        request.addProperty("FechaCRT", c.getString(7));
                        request.addProperty("Cobrador", c.getString(8));

                        Log.i("Entra ","Envio convenio 2");
                        if (EnviaDatosWS_("CCONVENIO",request,url_,accion_soap_)){
                            Log.i("EnviaDatosWS_", "******************************" +
                                    "******EnviaDatosWS_ enviaConvenios ************************");
                            CambiaCreado(c.getString(0),"-","convenio",
                                    "CustID" );
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }
    }
}