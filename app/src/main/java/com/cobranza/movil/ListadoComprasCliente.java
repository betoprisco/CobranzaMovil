package com.cobranza.movil;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import Clases.Compra;
import Clases.MiClase;
import Clases.MiClase_DatosCliente;

public class ListadoComprasCliente extends Activity {

    Cursor CursorCompras=null;

    public String custid;
    String texto;
    String titulo;

    public ArrayList<Compra> _datos = new ArrayList<Compra>();

    private static final int DIALOGO_ALERTA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compras);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        custid= getIntent().getExtras().getString("custid");

        TextView txt_custid=findViewById(R.id.txt_Compras_Custid);
        txt_custid.setText(custid);

        ComprasCliente();
    }

    public void ComprasCliente(){
        MiClase_DatosCliente clase_datoscli = new MiClase_DatosCliente();

        String rutaBD_USRCOB=ListadoComprasCliente.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
        /*Lectura del archivo CONFIG*/
        MiClase clase = new MiClase();
        String Cobrador=clase.Lectura(rutaBD_USRCOB);

        String rutaBD_CobradorDatos=ListadoComprasCliente.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

        CursorCompras=null;
        CursorCompras=clase_datoscli.obtenerCompras(custid,rutaBD_CobradorDatos);

        try{
            if(CursorCompras!=null)
            {
                _datos.clear();
                while (CursorCompras.moveToNext())
                {
                    //Nombre,Parentesco,Domicilio,Cruzamientos,Tel,Cel
                    Compra datox= new Compra(CursorCompras.getString(0),CursorCompras.getString(1),CursorCompras.getString(2), CursorCompras.getString(3),CursorCompras.getString(4) +" \n ");
                    _datos.add(datox);
                }
            }
            CursorCompras.close();//MVLA
        }catch(Exception e){
            message("Compras del cliente",e.toString());
        }

        ListView listaCompras = findViewById(R.id.Compras);

        try{

            AdaptadorDatos adaptador= new AdaptadorDatos(ListadoComprasCliente.this);
            listaCompras.setAdapter(adaptador);
        }catch(Exception e){
            message("Compras del Cliente",e.toString());
        }

        //SE AGREGA EL EVENTO PARA QUE ABRA LA VISTA PARA DECOMISAR ART??CULO - JAPP - 14-03-2016
        listaCompras.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Toast.makeText(getBaseContext(), "Decomiso", Toast.LENGTH_SHORT);

                Intent intent = new Intent(ListadoComprasCliente.this,DecomisaArt.class);
                intent.putExtra("ClaveArt", _datos.get(arg2).obtenerIdArt??culo());
                intent.putExtra("NombreArt", _datos.get(arg2).obtenerDescripci??n());
                intent.putExtra("Embarque", _datos.get(arg2).obtenerEmbarque());
                intent.putExtra("fechaCompra", _datos.get(arg2).obtenerFechaVenta());
                intent.putExtra("custid", _datos.get(arg2).obtenerCustid());

                startActivity(intent);
            }
        } );
    }

    @SuppressWarnings("deprecation")
    public void message(String head, String text){
        titulo=head;
        texto=text;
        showDialog(DIALOGO_ALERTA);
    }

    class AdaptadorDatos extends ArrayAdapter {

        Activity context;

        AdaptadorDatos(Activity context) {
            super(context, R.layout.lista_compras, _datos);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View item = convertView;
            ViewHolder holder;

            if(item == null)
            {
                LayoutInflater inflater = context.getLayoutInflater();
                item = inflater.inflate(R.layout.lista_compras, null);

                holder = new ViewHolder();
                holder.idCliente=item.findViewById(R.id.custid);
                holder.descripci??n=item.findViewById(R.id.descr);
                holder.idArt??culo=item.findViewById(R.id.Invtid);
                holder.shipperid=item.findViewById(R.id.shipperid);
                holder.fechaVenta=item.findViewById(R.id.fecha);

                item.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)item.getTag();
            }

            holder.idCliente.setText(_datos.get(position).obtenerCustid());
            holder.descripci??n.setText(_datos.get(position).obtenerDescripci??n());
            holder.idArt??culo.setText(_datos.get(position).obtenerIdArt??culo());
            holder.shipperid.setText(_datos.get(position).obtenerEmbarque());
            holder.fechaVenta.setText(_datos.get(position).obtenerFechaVenta());

            return(item);
        }
    }

    static class ViewHolder {

        TextView idCliente;
        TextView descripci??n;
        TextView idArt??culo;
        TextView shipperid;
        TextView fechaVenta;
    }
}
