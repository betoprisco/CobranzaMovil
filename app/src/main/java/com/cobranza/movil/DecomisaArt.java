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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import Clases.MiClase;
import com.cobranza.impresora.PrinterActivity;


public class DecomisaArt extends Activity implements Runnable {

    String claveArtículo,nombreArtículo,shipperid,fechaDeCompra,custID,tienda,noSerieArtículo,nombreDelCobrador,idDelCobrador,custName,fechaDelDecomiso,nombreBodega,newSerieArticulo,latitud=""
            ,folioDecomiso, longitud="",funcionamiento,accesorios;
    int increment;

    Cursor cursorCompras;

    MiClase datos = new MiClase();
    inicio inicio=new inicio();

    EditText nombreCliente,custid,idCobrador,nombreCobrador,descrArt,detallesArt,txtSeriest,textoFuncionamiento,textoAccesorios;
    TextView fechaDecomiso,embarque,claveArt,fechaCompra,siteID,bodega,noSerie;
    Button buttonDecomisa;
    AlertDialog.Builder dialogo;
    ProgressDialog dialog;

    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;
    private final int REQUEST_PERMISSION_CAMERA=2;

    private ImageView img;
    private Button boton;

    public String nombrefoto;

    LocationManager mLocationManager;
    MyLocationListener mLocationListener;
    Location currentLocation;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decomisa_art);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        claveArtículo=getIntent().getExtras().getString("ClaveArt").trim();
        nombreArtículo=getIntent().getExtras().getString("NombreArt");
        shipperid=getIntent().getExtras().getString("Embarque");
        fechaDeCompra=getIntent().getExtras().getString("fechaCompra");
        custID=getIntent().getExtras().getString("custid");

        String rutaBD_USRCOB=DecomisaArt.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);
        /*****************************/

        //String rutaBD_Cobrador=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
        String rutaBD_CobradorDatos=DecomisaArt.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        cursorCompras=datos.itemsDecomiso(shipperid,claveArtículo,rutaBD_CobradorDatos);
        cursorCompras.moveToFirst();

        tienda=cursorCompras.getString(6);
        noSerieArtículo=cursorCompras.getString(3);

        nombreCliente=findViewById(R.id.editTextNombreCliente);
        custid=findViewById(R.id.editTextCustid);
        idCobrador=findViewById(R.id.idCobrador);
        nombreCobrador=findViewById(R.id.nombreCobrador);
        descrArt=findViewById(R.id.descrArt);
        detallesArt=findViewById(R.id.detallesArt);
        txtSeriest=findViewById(R.id.txtSeries);//se agrego por modificacion solicitada por modesto.

        fechaDecomiso=findViewById(R.id.fechaDecomiso);
        embarque=findViewById(R.id.embarque);
        claveArt=findViewById(R.id.claveArt);
        fechaCompra=findViewById(R.id.fechaCompra);
        siteID=findViewById(R.id.siteID);
        bodega=findViewById(R.id.bodega);
        noSerie=findViewById(R.id.noSerie);
        textoAccesorios=findViewById(R.id.editTextAccesorios);
        textoFuncionamiento=findViewById(R.id.editTextFuncionamiento);

        buttonDecomisa= findViewById(R.id.buttonDecomisar);

        nombreDelCobrador=clase.LecturaNombreCob(rutaBD_USRCOB);
        idDelCobrador=clase.Lectura(rutaBD_USRCOB);
        custName=obtenerNombreCliente(idDelCobrador,custID);
        fechaDelDecomiso=obtenerFecha(idDelCobrador);
        obtenerBodega(idDelCobrador);

        nombreCliente.setText(custName);
        custid.setText(custID);
        idCobrador.setText(idDelCobrador);
        nombreCobrador.setText(nombreDelCobrador);
        fechaDecomiso.setText(fechaDelDecomiso);
        embarque.setText(shipperid);
        claveArt.setText(claveArtículo);
        fechaCompra.setText(fechaDeCompra);
        siteID.setText(tienda);
        bodega.setText(nombreBodega);
        descrArt.setText(nombreArtículo);
        noSerie.setText(noSerieArtículo);

        //SE REVISA SI TIENE EL PERMISO PARA SABER LA UBICACIÓN - JAPP - 05-09-2019
        showPhoneStatePermission();
        obtenerSeñalGPS();

        buttonDecomisa.setOnClickListener(decomisar);
        dialogo = new AlertDialog.Builder(this);

        nombrefoto = getCode();


        //Relacionamos con el XML
        img = this.findViewById(R.id.imageView1);
        boton =  this.findViewById(R.id.btnTomaFoto);
        //Añadimos el Listener Boton
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creamos el Intent para llamar a la Camara
                Intent cameraIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //Creamos una carpeta en la memeria del terminal
                File imagesFolder = new File(Environment.getExternalStorageDirectory(), "DecomisosF");
                imagesFolder.mkdirs();
                //añadimos el nombre de la imagen
                File image = new File(imagesFolder, nombrefoto + ".jpg");

                //Uri uriSavedImage = Uri.fromFile(image);
                Uri uriSavedImage = FileProvider.getUriForFile(DecomisaArt.this,BuildConfig.APPLICATION_ID + ".provider",image);

                //Le decimos al Intent que queremos grabar la imagen
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                //Lanzamos la aplicacion de la camara con retorno (forResult)
                startActivityForResult(cameraIntent, 1);
            }
        });
    }
    public String obtenerNombreCliente(String Cobrador,String CustID){
        SQLiteDatabase db = null;

        String rutaBD_Cobrador=DecomisaArt.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
        //String rutaBD_CobradorDatos=getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null,SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        CustID = CustID.trim();
        String nombre;
        String sql="select nombre from customer where custid like '" + CustID + "%'";

        Cursor cursorNombre = null;

        cursorNombre=db.rawQuery(sql, null);
        cursorNombre.moveToFirst();

        nombre=cursorNombre.getString(0);

        return nombre;
    }

    public String obtenerFecha(String Cobrador){
        SQLiteDatabase db = null;

        String rutaBD_Cobrador=DecomisaArt.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null
                , SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        String fecha=null;
        //CONSULTA PARA OBTENER FECHA

        String strSql="SELECT strftime('%Y-%m-%d','NOW','localtime') Fecha";

        Cursor cFecha = db.rawQuery(strSql,null);

        if (cFecha!=null)
        {
            cFecha.moveToFirst();
            fecha=cFecha.getString(0);
        }
        cFecha.close();

        return fecha;
    }

    public void obtenerBodega(String Cobrador){
        if(Cobrador.contains("CTM") || Cobrador.contains("BAC") || Cobrador.contains("MAD")){
            nombreBodega="QSCTMBCTM";
            return;
        }
        if(Cobrador.contains("CUN")||Cobrador.contains("LEO")){
            nombreBodega="QSCUNBCUN";
            return;
        }
        if(Cobrador.contains("PLA")){
            nombreBodega="QSPLABPLA";
            return;
        }
        if(Cobrador.contains("CAR")||Cobrador.contains("ATA")||Cobrador.contains("SAB")){
            nombreBodega="QSCARBCAR";
            return;
        }
    }

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
            Toast.makeText(DecomisaArt.this, "GPS Activado", Toast.LENGTH_SHORT).show();
        }
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showExplanation("Cámara", "Es necesario habilitar el permiso para acceder a la cámara",
                        Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA);
            } else {
                requestPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA);
            }
        } else {
            Toast.makeText(DecomisaArt.this, "Permiso a la Cámara concedido", Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Importante")
                            .setMessage("Es necesario acceder a la ubicación para usar la app")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    builder.create().show();
                }
                return;
            }
            case REQUEST_PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Importante")
                            .setMessage("Es necesario acceder a la cámara para hacer un decomiso")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    builder.create().show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void obtenerSeñalGPS(){
        DialogInterface.OnCancelListener dialogCancel = new DialogInterface.OnCancelListener(){
            public void onCancel(DialogInterface dialog){

            }
        };
        try{
            Thread thread=new Thread(DecomisaArt.this);
            thread.start();
        }catch(Exception e){
            Toast toast2=Toast.makeText(getBaseContext(), "Error2: Buscando señal "+e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }
    }

    private View.OnClickListener decomisar=new View.OnClickListener(){
        @Override
        public void onClick(View v)
        {
            //para verificar si tomo la foto del decomiso
            File archivo = new File(Environment.getExternalStorageDirectory() + "/DecomisosF/",nombrefoto + ".jpg"  );

            if (archivo.exists()){
                mensajes("Foto","Existe Foto");

                txtSeriest=findViewById(R.id.txtSeries);
                //aqui se pondra para que valide el num de serie o el imei
                if(txtSeriest.getText().toString().trim().length() > 4){
                    newSerieArticulo = txtSeriest.getText().toString().trim();
                    buscarGPS();
                }else{
                    dialogo.setTitle("Alerta");
                    dialogo.setMessage("Necesita poner el Numero de serie.");
                    dialogo.setPositiveButton("OK", null);
                    dialogo.create();
                    dialogo.show();
                }
            }else{
                Toast.makeText(getBaseContext(), "NO Existe la foto", Toast.LENGTH_LONG).show();
            }
        }
    };

    public void mensajes(String titulo, String Mensj){
        dialogo.setTitle(titulo);
        dialogo.setMessage(Mensj);
        dialogo.setPositiveButton("OK", null);
        dialogo.create();
        dialogo.show();
    }

    protected void buscarGPS() {
        // TODO Auto-generated method stub
        int máximo=100;

        increment=1;

        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Buscando GPS...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setMax(máximo);
        dialog.show();

        Thread background=new Thread(new Runnable(){
            public void run(){
                try{
                    while(dialog.getProgress() < dialog.getMax()){
                        if(latitud.equals(""))
                            Thread.sleep(500);
                        else
                            Thread.sleep(50);

                        progressHandler.sendMessage(progressHandler.obtainMessage());
                    }
                    dialog.dismiss();
                    procesaDecomiso();
                    imprime();
                }catch(java.lang.InterruptedException e){

                }
            }
        });
        background.start();
    }
    Handler progressHandler=new Handler(){
        public void handleMessage(Message msg){
            dialog.incrementProgressBy(increment);
        }
    };

    protected void procesaDecomiso() {
        // TODO Auto-generated method stub
        String sql;
        String detalles;
        String artículo;
        String rutaIMG;

        rutaIMG =  nombrefoto + ".jpg";
        detalles=detallesArt.getText().toString();
        detalles=detalles.replaceAll("'"," ");
        artículo=descrArt.getText().toString();
        funcionamiento=textoFuncionamiento.getText().toString().trim();
        accesorios=textoAccesorios.getText().toString().trim();

        folioDecomiso=GeneraFolio(idDelCobrador);
        try{
            /*STATUS PARA EL ENVIO DE DATOS
             *
             * ESPERA:Cuando los datos no se han enviado al ws para ser guardados en la case de datos
             * ENVIADO:Cuando los datos se han enviado al ws para ser guardados en la case de datos
             *
             * */
            sql="insert into decomisos(Custid,ClaveArt,Embarque,FechaDecomiso,Detalles,Art,FechaCompra,Cobrador,AutorizaDecomiso,FolioDecomiso,BodegaRecibe,tiendaDeCompra,Longitud,Latitud" +
                    ",NoSerie,Ruta,Funcionamiento,Accesorios) " +
                    "values('"+custID+"','"+claveArtículo+"','"+shipperid+"','"+fechaDelDecomiso+"','"+detalles+"','"+artículo+"','"+fechaDeCompra+"','"+idDelCobrador+"','"+nombreDelCobrador
                    +"','"+folioDecomiso+"','"+ nombreBodega+"','"+tienda+"','"+longitud+"','"+latitud+"','"+newSerieArticulo+"','" + rutaIMG +"','" + funcionamiento +
                    "','" + accesorios + "')"; /*se cambio el num de serie por si es otro articulo, se agrego para la ruta de la foto*/

            SQLiteDatabase db= null;

            String rutaBDCobradores=DecomisaArt.this.getApplicationContext().getDatabasePath("cobradores").toString();

            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            db.execSQL(sql);
            db.close();


            //Guarda los datos de la imagen para poder ser enviada aparte
            sql = "";
            sql = "INSERT INTO imagenDecomiso(idDeco,ruta, enviado) VALUES('" + folioDecomiso + "', '" + rutaIMG +  "','ESPERA')";
            db = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            db.execSQL(sql);
            db.close();


        }catch(Exception e){
            Toast toast = Toast.makeText(getBaseContext(), "ERROR INSERTAR DECOMISO: "+ e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public String GeneraFolio(String Cobrador){
        SQLiteDatabase db = null;

        String rutaBD_Cobrador=DecomisaArt.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null,SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        String Folio=Cobrador;
        //CONSULTA PARA OBTENER FECHA

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
    public void imprime(){
        Intent intent = new Intent(DecomisaArt.this, PrinterActivity.class);
        intent.putExtra("TIPO", "DECOMISO");
        intent.putExtra("FolioDecomiso", folioDecomiso);

        startActivity(intent);
        this.finish();
    }

    private String getCode()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date() );
        String photoCode = "pic_" + date;
        return photoCode;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Comprovamos que la foto se a realizado
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Creamos un bitmap con la imagen recientemente
            //almacenada en la memoria
            Bitmap bMap = BitmapFactory.decodeFile( Environment.getExternalStorageDirectory()+ "/DecomisosF/"+ nombrefoto + ".jpg");
            //Añadimos el bitmap al imageView para
            //mostrarlo por pantalla
            img.setImageBitmap(bMap);
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try{
            String LocationType = LocationManager.GPS_PROVIDER;
            mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Looper.prepare();
                mLocationListener=new MyLocationListener();

                showPhoneStatePermission();
                mLocationManager.requestLocationUpdates(LocationType,0,0,mLocationListener);

                Looper.loop();
                Looper.myLooper().quit();
            }
        }catch(SecurityException e){
            Toast toast2=Toast.makeText(getBaseContext(), "Error2: Señal no encontrada"+e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location loc){
            if(loc!=null){
                setCurrentLocation(loc);
                latitud=String.valueOf(loc.getLatitude());
                longitud=String.valueOf(loc.getLongitude());
            }
            else{
                String coordenadas="sin localizar";
                Toast.makeText(getApplicationContext(), coordenadas, Toast.LENGTH_SHORT).show();
            }
        }

        private void setCurrentLocation(Location loc){
            currentLocation = loc;
        }

        public void onProviderDisabled(String provider){

        }
        public void onProviderEnabled(String provider){

        }

        public void onStatusChanged(String provider,int status,Bundle xtras){

        }

        private Handler handler=new Handler(){
            public void handleMessage(Message msg){
                try{
                    mLocationManager.removeUpdates(mLocationListener);
                    if(currentLocation!=null)
                        Toast.makeText(getBaseContext(), "posición: "+currentLocation.toString(), Toast.LENGTH_LONG).show();
                }catch(Exception e){
                    Toast toast2=Toast.makeText(getBaseContext(), "Error1: Handler muestra ubicación " +e.getMessage(), Toast.LENGTH_LONG);
                    toast2.show();
                }
            }
        };
    }
}
