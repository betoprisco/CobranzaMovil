package com.cobranza.movil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import Clases.MiClase;

public class login extends Activity {

    public TextView texto;
    public ProgressDialog pd;

    String Mensaje,AndroidVersion;

    MiClase	clase = new MiClase();

    private final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE=1;

    //PARA REVISAR SI TIENE ACTIVADO EL PERMISO DE ACCEDER A LA UBICACIÓN - JAPP - 05-09-2018
    /*private void showPhoneStatePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showExplanation("Se requieren los permisos",
                        "Es necesario habilitar los permisos de acceder a la ubicación " +
                                "para usar la app",
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            Toast.makeText(login.this, "Permiso de escritura concedido", Toast.LENGTH_SHORT).show();
        }
    }

    //revisarPermisoDispositivosCercanos
    //PARA REVISAR SI TIENE ACTIVADO EL PERMISO DE DISPOSITIVOS CERCANOS - JAPP - 05-01-2023
    /*private void revisarPermisoDispositivosCercanos() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_CONNECT)) {
                showExplanation("Se requieren los permisos",
                        "Es necesario habilitar el permiso para poder imprimir",
                        Manifest.permission.BLUETOOTH_CONNECT, 2);
            } else {
                requestPermission(Manifest.permission.BLUETOOTH_CONNECT, 2);
            }
        } else {
            Toast.makeText(login.this, "Permiso para imprimir concedido",
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
            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getBaseContext(),"¡Es necesario el permiso de escritura " +
                            "para usar la app!",Toast.LENGTH_LONG);
                    //this.finish();
                }
                return;
            }
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getBaseContext(),"¡Es necesario el permiso para imprimir!",
                            Toast.LENGTH_LONG);
                    this.finish();
                }
                return;
            }
        }
    }
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button boton = findViewById(R.id.Inicio_btn_DescargaBD);
        boton.setOnClickListener(DescargaArchivo);

        Button btsesion = findViewById(R.id.Inicio_btn_Entrar);
        btsesion.setOnClickListener(Accede);

        //asignamos el TextView para mostrar luego los datos procesados
        texto = findViewById(R.id.txt_msg);
        TextView txt = findViewById(R.id.txt_msg );

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            AndroidVersion = Build.VERSION.RELEASE_OR_CODENAME;
        }else
            AndroidVersion = Build.VERSION.RELEASE;
        texto.setText(AndroidVersion);*/

        //SE REVISA EL PERMISO DE ESCRITURA EN EL ALMACENAMIENTO DEL TELÉFONO - JAPP - 12-10-2018
        //showPhoneStatePermission();

        //SE REVISA EL PERMISO DE DISPOSITIVOS CERCANOS - JAPP - 05-01-2023
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S){
            revisarPermisoDispositivosCercanos();
        }*/
    }

    private View.OnClickListener DescargaArchivo = new View.OnClickListener(){

        @Override
        public void onClick(View v) {

            if (ChecaConexion(getApplicationContext())){
                donwload();
            }else{
                Toast.makeText(getBaseContext(), "Problemas en la conexión de INTERNET " , Toast.LENGTH_SHORT).show();
            }
        }

    };

    private View.OnClickListener Accede = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            String bandera = "";
            MiClase clase = new MiClase();

            TextView usu = findViewById(R.id.Inicio_txt_usr);
            TextView contra = findViewById(R.id.Inicio_txt_PWD);

            String ruta=login.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";

            String sql = "Update Tbl_Usuarios set sesion = '0' "; //Actualizo a 0 todos los usuarios
            clase.EjecutaSQLUpdate(sql, MiClase.TypeBase.DESCARGA_USUARIOS, MiClase.TypeSQL.UPDATE,ruta);

            //ruta=getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";

            bandera = clase.verificaUsuario(usu.getText().toString().trim(), contra.getText().toString().trim(),ruta);

            if (bandera.equals("NO")){
                Toast.makeText(getBaseContext(), "Usuario no encontrado o Esta Desactivado. Actualice la BD de usuarios", Toast.LENGTH_LONG).show();

            }else if (bandera.equals("NO EXISTE BASE")){
                Toast.makeText(getBaseContext(), "NO EXISTE LA BASE DE DATOS, INTENTE DESCARGANDOLA DE NUEVO" + bandera, Toast.LENGTH_LONG).show();

            }else if (bandera.equals("NECESITA ACTUALIZAR")){
                Toast.makeText(getBaseContext(), "NECESITA ACTUALIZAR SU BASE DE DATOS", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getBaseContext(), "ACCEDIENDO AL SISTEMA", Toast.LENGTH_LONG).show();
                accede(usu.getText().toString());
            }
        }
    };

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

    public void accede(String usuario){
        try {
            MiClase clase = new MiClase();

            String ruta=login.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";

            String sql = "Update Tbl_Usuarios set sesion = '1' where Trim(usuario) = '" + usuario.trim() + "'"; //Actualizo a 1 el usuario que estara activo
            clase.EjecutaSQLUpdate(sql, MiClase.TypeBase.DESCARGA_USUARIOS, MiClase.TypeSQL.UPDATE,ruta);

            Intent intento = new Intent(login.this, inicio.class);

            startActivity(intento);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Ocurrió un error al momento de ingresar, por favor intente de nuevo", Toast.LENGTH_LONG).show();
        }
    }
    public void donwload(){
        // Mostrar el ProgressDialog en este Thread
        //pd = ProgressDialog.show(this, "", "Espere un momento por favor...", true, false);

        // Se comienza la nueva Thread que descargará los datos necesarios
        new DownloadTask().execute("Parametros que necesite el DownloadTask");
    }


    /**
     * Muestra el texto resultado
     */
    public void mostrarResultado(String textoAMostrar){
        this.texto.setText(textoAMostrar);

    }
    /**
     * Subclase privada que crea un hilo aparte para realizar
     * las acciones que deseemos.
     */


    private class DownloadTask extends AsyncTask<String, Void, Object> {
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            pd = new ProgressDialog(login.this);
            pd.setMessage("Descargando usuarios. Por favor espere...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }
        protected String doInBackground(String... args) {
            Log.i("Mi app", "Empezando hilo en segundo plano");

            //Descargo la base de datos de Usuarios.
            downloadBD("UsuCob","BD_USRCOB");

            return "Base de datos descargada";
        }

        protected void onPostExecute(Object result) {
            // Pasamos el resultado de los datos a la Acitvity principal
            login.this.mostrarResultado((String)result);
            pd.dismiss();
            if(Mensaje==null)
                texto.setText(result.toString());
            else
                texto.setText(Mensaje);
        }

        protected void downloadBD(String vendedor,String NombreBD){
            //creando carpeta
            /*File file=new File("/sdcard/MovilHelp/");
            if(!file.exists()) {

                file.mkdirs();
                Log.v("TestHttpActivity", "carpeta creada ej");

            } else {

                Log.v("TestHttpActivity", "carpeta no creada ej");
            }*/

            int port = 192;

            String url = clase.Direcciones_URL(MiClase.TypeBase.DESCARGA_USUARIOS) + vendedor + ".zip";
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();

                httpclient.getCredentialsProvider().setCredentials(new AuthScope(url, port),new UsernamePasswordCredentials("administrador", "maxuxac"));

                URI uri = new URI(url);
                HttpGet method = new HttpGet(uri);
                HttpResponse response = httpclient.execute(method);



                InputStream in =  response.getEntity().getContent();
                FileOutputStream f = new FileOutputStream(new File(getApplicationContext().getFilesDir().toString() + "/", NombreBD + ".zip"));

                int len1 ;

                while ( (len1 = in.read()) != -1 ) {
                    f.write(len1);
                }
                f.close();
                in.close();
                Log.v("TestHttpActivity", "download file");
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                Mensaje=e.getMessage();

                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                /*TextView txt = findViewById(R.id.txt_msg );
                */
                Mensaje=e.getMessage();

                e.printStackTrace();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                Mensaje=e.getMessage();
            }


        }// fin donwloadBD
    }
}