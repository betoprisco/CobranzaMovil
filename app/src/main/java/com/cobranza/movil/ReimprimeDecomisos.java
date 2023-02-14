package com.cobranza.movil;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import Clases.MiClase;
import com.cobranza.impresora.PrinterActivity;

public class ReimprimeDecomisos extends Activity {
    String folioDecomiso;

    EditText custid,idCobrador,nombreCobrador,descrArt,detallesArt;
    TextView fechaDecomiso,embarque,claveArt,fechaCompra,siteID,bodega,noSerie;
    Button buttonDecomisa;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reimprime_decomisos);

        folioDecomiso=getIntent().getExtras().getString("folioDecomiso");

        custid=findViewById(R.id.editTextCustid);
        idCobrador=findViewById(R.id.idCobrador);
        nombreCobrador=findViewById(R.id.nombreCobrador);
        descrArt=findViewById(R.id.descrArt);
        detallesArt=findViewById(R.id.detallesArt);

        fechaDecomiso=findViewById(R.id.fechaDecomiso);
        embarque=findViewById(R.id.embarque);
        claveArt=findViewById(R.id.claveArt);
        fechaCompra=findViewById(R.id.fechaCompra);
        siteID=findViewById(R.id.siteID);
        bodega=findViewById(R.id.bodega);
        noSerie=findViewById(R.id.noSerie);

        buscarDecomiso();

        buttonDecomisa= findViewById(R.id.buttonDecomisar);
        buttonDecomisa.setOnClickListener(reimprime);
    }

    public void buscarDecomiso(){
        String sql;

        MiClase clase=new MiClase();

        sql="Select Custid,ClaveArt,Embarque,FechaDecomiso,Detalles,Art,FechaCompra,Cobrador,AutorizaDecomiso,FolioDecomiso,BodegaRecibe,tiendaDeCompra,NoSerie ";
        sql+="from decomisos where trim(FolioDecomiso) ='"+folioDecomiso.trim()+"'";

        String rutaBDCobradores=ReimprimeDecomisos.this.getApplicationContext().getDatabasePath("cobradores").toString();

        try{
            Cursor c=clase.EjecutaSQL(sql,MiClase.TypeBase.COBRADORES,MiClase.TypeSQL.SELECT,rutaBDCobradores);

            if(c.getCount()>0){
                while(c.moveToNext()){
                    custid.setText(c.getString(0).trim());
                    claveArt.setText(c.getString(1).trim());
                    embarque.setText(c.getString(2).trim());
                    fechaDecomiso.setText(c.getString(3).trim());
                    detallesArt.setText(c.getString(4).trim());
                    descrArt.setText(c.getString(5).trim());
                    fechaCompra.setText(c.getString(6).trim());
                    idCobrador.setText(c.getString(7).trim());
                    nombreCobrador.setText(c.getString(8).trim());
                    //FOLIO DECOMISO (9)
                    bodega.setText(c.getString(10).trim());
                    siteID.setText(c.getString(11).trim());
                    noSerie.setText(c.getString(12).trim());
                }
            }
        }catch(Exception e){
            Toast toast = Toast.makeText(getBaseContext(), "ERROR AL OBTENER DECOMISO: "+ e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private View.OnClickListener reimprime=new View.OnClickListener(){
        @Override
        public void onClick(View v){
            Intent intent = new Intent(ReimprimeDecomisos.this, PrinterActivity.class);
            intent.putExtra("TIPO", "DECOMISO");
            intent.putExtra("FolioDecomiso", folioDecomiso);

            startActivity(intent);
        }
    };
}
