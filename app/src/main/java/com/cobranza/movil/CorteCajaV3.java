package com.cobranza.movil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import Clases.MiClase;
import Clases.datos;
import com.cobranza.impresora.PrinterActivity;

public class   CorteCajaV3 extends Activity {

    String texto,titulo,strSql;

    ArrayList<datos>_datos = new ArrayList<datos>();

    static final int DATE_DIALOG_ID = 0;

    private TextView mDateDisplay;
    private Button mPickDate;

    private int mYear,mMonth,mDay;
    private static final int DIALOGO_ALERTA = 1;

    MiClase clase = new MiClase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corte_caja_v3);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        try{

            Button btn_BuscaCorte =  findViewById(R.id.btn_corte_buscacorte);
            btn_BuscaCorte.setOnClickListener(buscaCorte);

            Button btn_ImprimeCorte =   findViewById(R.id.btn_corte_imprimir);
            btn_ImprimeCorte.setOnClickListener(ImprimeCorte);

        }catch(Exception e){
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        /*
         * Mostrar display de fecha
         */

        /************MOSTRAR MSJ DE FECHA***************/
        try{
            // capture our View elements
            mDateDisplay =  findViewById(R.id.txt_corte_fecha );
            mPickDate =  findViewById(R.id.btn_corte_calendario );

            // add a click listener to the button
            mPickDate.setOnClickListener(new View.OnClickListener() {
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
        }catch(Exception e){
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        /**********************************/
        /*Fin del display de la fecha*/
    }

    private View.OnClickListener buscaCorte = new View.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            Log.i("Sql....", "Sql entra 22   ");
            /*
             * Asigno los valores de los componentes de la actividad
             * para hacer la busqueda
             * */
            TextView total = findViewById(R.id.txt_corte_total);
            TextView fecha=  findViewById(R.id.txt_corte_fecha );

            Log.i("Boton", "Entro al boton");
            /*
             * Desarrollo la consulta, abonos del dia actual
             * antes de las 8 y los abonos del día de ayer
             * despues de las 8 pm
             * */
            String sql="";
            String FechaA = clase.FechaAnterior(mMonth+1, mYear, mDay);
            Integer mes=mMonth+1;

            String dia_,mes_;

            if (mDay<10){
                dia_='0'+Integer.toString(mDay);
            }else{
                dia_=Integer.toString(mDay);
            }
            if (mes<10){
                mes_='0'+Integer.toString(mes);
            }else{
                mes_=Integer.toString(mes);
            }

            try{
				/*
				 * Consulta para mostrar la cantidad del corte
				 * *
				*/

                sql ="Select ifnull(sum(monto),0) total from Abonos " +
                        " where fechaAbo between  datetime('"+ FechaA +"') and   datetime('" + mYear +"-"+ mes_ +"-"+ dia_ + " 20:00:00')  ";// + FechaA;

                String rutaBDCobradores=CorteCajaV3.this.getApplicationContext().getDatabasePath("cobradores").toString();

                Cursor c = clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES, MiClase.TypeSQL.SELECT,rutaBDCobradores);

                if (c!= null){
                    while(c.moveToNext())
                        total.setText(c.getString(0));
                }

            }catch(Exception e){
                message("Error Corte 1.0 ",e.toString());
            }

            /*
             * Consulta para mostrar el listado de abonos
             * generados en la fecha del corte.
             * */

            String sql2="";
            Log.i("Sql....", "Sql entra 22   ");

             try{
                sql2="Select  'Abono: '|| ' ' || monto,Nombre,'    Status: '|| ' ' || Status,'Folio: '|| ' ' || Folio,monto  from Abonos   " +
                        " where fechaAbo between  datetime('"+ FechaA +"') and   datetime('" + mYear +"-"+ mes_ +"-"+ dia_ + " 20:00:00')" ;

                Log.i("Sql....", "Sql entra"+ sql2);

                 String rutaBDCobradores=CorteCajaV3.this.getApplicationContext().getDatabasePath("cobradores").toString();

                strSql=sql2;
                Cursor c2 = clase.EjecutaSQL(sql2, MiClase.TypeBase.COBRADORES , MiClase.TypeSQL.SELECT,rutaBDCobradores);
                if (c2!=null){

                    /*Limpio el array*/
                    _datos.clear();

                    while(c2.moveToNext()){
                        datos datox= new datos(c2.getString(0),c2.getString(3), c2.getString(1)+c2.getString(2) +" \n " );
                        _datos.add(datox);
                    }
                }

            }catch(Exception e){
                message("Error corte 1.5 - "+sql2, e.toString());
            }

            /*
             * Creo la lista que mostrara los abonos
             * */

            ListView lstcliente=  findViewById(R.id.lst_corte_abonos);
            AdaptadorDatos adaptador= new AdaptadorDatos(CorteCajaV3.this);
            try{
                lstcliente.setAdapter(adaptador);
            }catch(Exception e){message("Error corte 2.0 Lista", e.toString());}
        }
    };

    public void message(String head, String text){
        titulo=head;
        texto=text;
        showDialog( DIALOGO_ALERTA);
    }

    class AdaptadorDatos extends ArrayAdapter {
        Activity context;

        AdaptadorDatos(Activity context) {
            super(context, R.layout.activity_corte_caja_v3, _datos);
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

    private void updateDisplay() {
        mDateDisplay.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mYear).append("-")
                        .append(mMonth + 1).append("-")
                        .append(mDay).append(" "));
    }

    /*IMPRIME CORTE*/
    private View.OnClickListener ImprimeCorte= new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Log.i("Blue", "sql" + strSql);

            if (strSql.equals("")) {
                Toast.makeText(getBaseContext(), "Necesita buscar un Corte de caja", Toast.LENGTH_LONG).show();
            } else {
                Intent intent1 = new Intent(CorteCajaV3.this, PrinterActivity.class);
                intent1.putExtra("sql", strSql);
                intent1.putExtra("TIPO", "CORTE");
                startActivity(intent1);
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialogo = null;

        switch (id) {
            case DATE_DIALOG_ID:
                removeDialog(DATE_DIALOG_ID);
                return new DatePickerDialog(this, mDateSetListener,   mYear, mMonth, mDay);
            case DIALOGO_ALERTA:
                removeDialog(DIALOGO_ALERTA);
                dialogo = alerta();
                break;
			/*
    		case DIALOGO_CONFIRMACION:
    			dialogo = crearDialogoConfirmacion();
    			break;
    		case DIALOGO_SELECCION:
    			dialogo = crearDialogoSeleccion();
    			break;
    			*/
        }
        return dialogo;
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

    /*
     * FIN METODOS PARA MOSTRAR CALENDARIO
     * */
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
}

