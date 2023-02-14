package com.cobranza.movil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import Clases.MiClase;
import Clases.datos;

public class Listacliente2 extends Activity {
    public int idmenu;
    public String filtro;

    String texto;
    String titulo;
    private static final int DIALOGO_ALERTA = 1;

    public String strSql="";
    public ArrayList<datos> _datos = new ArrayList<datos>();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listacliente);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//----------- PARA NO ROTAR PANTALLA

        filtro=getIntent().getExtras().getString("filtro");
        idmenu=getIntent().getExtras().getInt("idmenu");


        BuscaClientes(0);
        Button boton =  findViewById(R.id.btn_BuscaClienteLista);
        boton.setOnClickListener(EjecutaBoton);

        // TODO Auto-generated method stub
    }
    /*BUSCA CORTE*/
    private View.OnClickListener EjecutaBoton = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            BuscaClientes(1);
        }

    };
    public void BuscaClientes(int valor){

        try{
            TextView cliente = findViewById(R.id.txt_ClienteLista);

            switch(idmenu)
            {
                case 0:
                    strSql="Select custid,direccion,colonia,nombre from customer where colonia like '" + filtro +"'";
                    if (valor==1){
                        strSql = strSql+ " and (custid like '%" + cliente.getText() + "%' or nombre like '%" + cliente.getText()  + "%') ";
                    }
                    strSql = strSql+ " and custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    strSql = strSql+ " order by direccion";
                    break;
                case 1:
                    strSql="select a.custid,a.direccion,a.colonia,a.nombre from customer a inner join dsCob b on trim(a.custid)=trim(b.custid) where b.PeriodVencid= "+filtro;
                    if (valor==1){
                        strSql = strSql+ " and (a.custid like '%" + cliente.getText()  + "%' or a.nombre like '%" + cliente.getText()  + "%')";
                    }
                    strSql = strSql+ " and a.custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    strSql=strSql+" order by a.colonia,a.direccion";
                case 2:break;

                case 3:
                    strSql="Select custid,direccion,colonia,nombre from customer where";
                    if (valor==1){
                        strSql = strSql+ "  (custid like '%" + cliente.getText()  + "%' or nombre like '%" + cliente.getText()  + "%') and";
                    }
                    strSql = strSql+ "   custid not in (select a.custid from dsCob a inner join customer b on trim(a.custid)=trim(b.custid) where (saldoV/numLetVenc)>ctapag)";
                    break;
                case 7:
                    //PROMESAS VENCIDAS
                    strSql="Select custid,calle,colonia,billname from PromesasVencidas ";
                    if (valor==1)
                        strSql = strSql+ " where (custid like '%" + cliente.getText()  + "%' or billname like '%" + cliente.getText()  + "%')";

                    strSql=strSql+" order by colonia, calle";
                    break;
                case 8:
                    //PROMESAS A VENCER
                    strSql="Select custid,calle,colonia,billname from PromesasPorVencer ";
                    if (valor==1)
                        strSql = strSql+ " where (custid like '%" + cliente.getText()  + "%' or billname like '%" + cliente.getText()  + "%')";

                    strSql=strSql+" order by colonia, calle";
                    break;
                case 9:
                    //NO GESTIONADOS SIN PAGO
                    strSql="Select custid,calle,colonia,billname from noGestionados ";
                    if (valor==1)
                        strSql = strSql+ " where (custid like '%" + cliente.getText()  + "%' or billname like '%" + cliente.getText()  + "%')";
                    break;
                case 10:
                    //PAGOS TOTALES
                    strSql="Select custid,PagoAcumulado,UltPago from PagosTotales ";
                    if (valor==1)
                        strSql = strSql+ " where (custid like '%" + cliente.getText()  + "%')";
                    strSql = strSql+ " order by  UltPago desc";
                    break;
                case 11:
                    //CLIENTES SIN PAGO
                    strSql="Select custid,calle,colonia,Billname from sinPagos ";
                    if (valor==1)
                        strSql = strSql+ " where (custid like '%" + cliente.getText()  + "%')";
                    strSql = strSql+ " order by  colonia asc";
                    break;
            }

            SQLiteDatabase db= null;

            /******************************/
            String rutaBD_USRCOB=Listacliente2.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            /*Lectura del archivo CONFIG*/
            MiClase clase = new MiClase();
            String Cobrador=clase.Lectura(rutaBD_USRCOB);
            /*inicio inicio = new inicio();

            String Cobrador= inicio.Lectura();
            /*****************************/

            String rutaBD_Cobrador=Listacliente2.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";
            String rutaBD_CobradorDatos=Listacliente2.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + "_DATOS.zip";

            db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);



            Cursor c=null;
            if (idmenu==9)
                c=clase.EjecutaSQL(strSql, MiClase.TypeBase.Id_COBRADOR_DATOS , MiClase.TypeSQL.SELECT,rutaBD_CobradorDatos);
            else if(idmenu==11)
                c=clase.EjecutaSQL(strSql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);
            else
                c = db.rawQuery(strSql, null);

            if(c!=null){
                _datos.clear();
                if (idmenu==10){
                    while (c.moveToNext()){
                        datos datox= new datos(c.getString(0),c.getString(2), c.getString(1)+" \n " );
                        _datos.add(datox);
                    }
                }
                else{
                    while (c.moveToNext()){
                        datos datox= new datos(c.getString(0),c.getString(3), c.getString(1)+c.getString(2) +" \n " );
                        _datos.add(datox);
                    }
                }
            }
            db.close();
        }
        catch(Exception e)
        {
            Toast toast2 = Toast.makeText(getBaseContext(), "id menu: "+idmenu +" error 1: Lista Cliente "+ e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();
        }
        ListView lstcliente =findViewById(R.id.LstCliente);
        try{
            AdaptadorDatos adaptador= new AdaptadorDatos(Listacliente2.this);
            lstcliente.setAdapter(adaptador);
        }catch(Exception e)
        {
            Toast toast2 = Toast.makeText(getBaseContext(), " error 88.2: llama lista Corte "+ e.getMessage(), Toast.LENGTH_LONG);
            toast2.show();

        }//fin try 2

        lstcliente.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                // TODO Auto-generated method stub
                Toast.makeText(getBaseContext(), " custid lista inicial ", Toast.LENGTH_LONG);
                Intent intent = new Intent(Listacliente2.this,Actividad_Letras.class);
                intent.putExtra("custid", _datos.get(arg2).getId());
                intent.putExtra("idmenu", idmenu);
                intent.putExtra("filtro", filtro);
                startActivity(intent);
            }

        } );

    }

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