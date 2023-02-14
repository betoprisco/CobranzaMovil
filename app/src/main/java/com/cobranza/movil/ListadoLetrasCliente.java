package com.cobranza.movil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import Clases.MiClase;
import Clases.MiClase_DatosCliente;
import Clases.datos;

public class ListadoLetrasCliente extends Activity {
    public ArrayList<datos> _datos = new ArrayList<datos>();
    Cursor CursorLetras=null;
    public String custid;
    public String cliente;
    String Cobrador;
    String texto;
    String titulo;

    private static final int DIALOGO_ALERTA = 1;
    private static final int DIALOGO_CONFIRMACION = 2;
    private static final int DIALOGO_SELECCION = 3;

    MiClase clase = new MiClase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listado_letras_cliente);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        custid= getIntent().getExtras().getString("custid");
        cliente= getIntent().getExtras().getString("cliente");

        TextView txt_cliente= findViewById(R.id.txt_LetrasClientes_Cliente);
        TextView txt_custid= findViewById(R.id.txt_LetrasClientes_Custid);

        txt_cliente.setText(cliente);
        txt_custid.setText(custid);

        LetrasCliente();
    }

    public void LetrasCliente(){
        MiClase_DatosCliente clase_datoscli = new MiClase_DatosCliente();

        CursorLetras=null;

        String rutaBD_USRCOB=ListadoLetrasCliente.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        Cobrador=clase.Lectura(rutaBD_USRCOB);

        String rutaBD_Cobrador=ListadoLetrasCliente.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

        CursorLetras=clase_datoscli.SaldosLetras(custid,rutaBD_Cobrador);

        try{
            if(CursorLetras!=null)
            {
                _datos.clear();
                while (CursorLetras.moveToNext())
                {
                    datos datox= new datos(CursorLetras.getString(0),CursorLetras.getString(2) + " / " +
                            CursorLetras.getString(3)  , CursorLetras.getString(1) +" \n " );
                    _datos.add(datox);
                }
            }
        }catch(Exception e){
            message("Letras Clientes 2.0 ",e.toString());
        }

        /*
         * Generando list view
         * */

        ListView lstLetras = findViewById(R.id.ActividadLetras_listaclientes);

        /*
         *
         * */
        try{

            AdaptadorDatos adaptador= new AdaptadorDatos(ListadoLetrasCliente.this);
            lstLetras.setAdapter(adaptador);
        }catch(Exception e){
            message("Letras clientes 2.1",e.toString());
        }//fin try 2

        lstLetras.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub

                Integer letra_= Integer.parseInt( _datos.get(arg2).getId()) ;
                String sql=" select capital,MoraPago,PM,status," +
                        " (capital+ivacapital+interes+ivainteres) saldo,paydate, MoraAcum " +
                        " from letras where custid like'%"+ custid +"%' and IDLetra="+ letra_ +"";
                try{
                    Cursor c_=null;

                    String rutaBD_Cobrador=ListadoLetrasCliente.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

                    c_=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

                    if(c_!=null){
                        while (c_.moveToNext())
                        {
                            String cadena;
                            cadena="Letra:"+ letra_+ "\n";
                            cadena+="MoraPago: "+ c_.getString(1) +"\n";
                            cadena+="Mora de letra: "+ c_.getString(6) +"\n";
                            cadena+="PM: "+ c_.getString(2) +"\n";
                            cadena+="Saldo: "+ c_.getString(4) +"\n";
                            cadena+="Status: "+ c_.getString(3) +"\n";
                            cadena+="Fecha Abono: "+ c_.getString(5) +"\n";
                            message("Detalles Letra",cadena);
                        }
                    }
                }catch(Exception e){
                    message("Error Letras Cliente 1.x",e.toString());
                }
            }
        } );
    }

    /*
     * CREO EL ADAPTADOR PARA LLENAR EL LISTVIEW
     * */
    /*******************************************/
    class AdaptadorDatos extends ArrayAdapter {
        Activity context;

        AdaptadorDatos(Activity context) {
            super(context, R.layout.listitem_datos, _datos);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View item = convertView;
            ViewHolder holder;

            if(item == null)
            {
                LayoutInflater inflater = context.getLayoutInflater();
                item = inflater.inflate(R.layout.listitem_datos, null);

                holder = new ViewHolder();
                holder.Id=item.findViewById(R.id.LblId);
                holder.dato=item.findViewById(R.id.LblDato);
                holder.dato2=item.findViewById(R.id.LblDato2);

                item.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)item.getTag();
            }

            holder.Id.setText((_datos.get(position).getId()));
            holder.dato.setText(_datos.get(position).getDato());
            holder.dato2.setText(_datos.get(position).getDato2());

            return(item);
        }
    }

    static class ViewHolder {

        TextView Id;
        TextView dato;
        TextView dato2;
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
                removeDialog(DIALOGO_ALERTA);
            }
        });

        return builder.create();
    }

    public void message(String head, String text){
        titulo=head;
        texto=text;
        showDialog(DIALOGO_ALERTA);
    }
}
