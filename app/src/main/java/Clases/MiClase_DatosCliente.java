package Clases;

import android.database.Cursor;
import android.util.Log;

public class MiClase_DatosCliente {
    public String Nombre,Direccion,UltPago,Colonia,Tel1,Tel2;
    public double SaldoV,Mora,PagoReq,SaldoT,Abono,PM,vencido,SaldoT2;
    public int LetrasV;
    public String Error="";
    public boolean Error_ =false;

    String sql_ ="";

    /*
     * Datos para reimpresion de abonos
     * */
    String FechaAbo="";
    double r_Abono = 0;
    double A_Mora = 0;
    double AbonoCuenta=0;

    MiClase clase = new MiClase();
    /*
     * Metodo para obtener datos personales
     * */
    @SuppressWarnings("null")
    public void DatosPersonalesCliente(String custid,String rutaBD_Cobrador){
        String sql="";
        try{
            custid=custid.trim();
            sql="select Custid,nombre,direccion,colonia,tel1,tel2 from customer  where custid like '%"+custid+"%' escape ' '";

            Cursor c = clase.EjecutaSQL(sql,MiClase.TypeBase.Id_COBRADOR,MiClase.TypeSQL.SELECT,rutaBD_Cobrador );
            if (c!=null)
            {
                while (c.moveToNext())	{
                    Nombre=c.getString(1);
                    Direccion=c.getString(2);
                    Colonia=c.getString(3);
                    Tel1=c.getString(4);
                    Tel2=c.getString(5);
                }
            }
        }catch(Exception e){
            Error="Datos C 1.1 "+ e.toString();
            Error_=true;
        }
        Error_=false;
    }

    public void DatosCuentaCliente(String custid,String tipo, String Folio,String rutaBD_Cobrador,String rutaBDCobradores){
        try{
            /*
             * Obtengo el PM
             * */
            /*************************/
            custid=custid.trim();

            String sql="SELECT Vencido,SaldoT,Moratorios,numletvenc,PagoReq FROM dscob where trim(custid) = trim('" + custid + "')";

            Cursor c=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT,rutaBD_Cobrador);

            if(c!=null)
            {
                c.moveToFirst();
                SaldoV= MiClase.Redondear(Double.parseDouble(c.getString(0)),2 );
                SaldoT= MiClase.Redondear(Double.parseDouble(c.getString(1)), 2) ;
                Mora=Double.parseDouble(c.getString(2));
                LetrasV= Integer.parseInt( c.getString(3));
                PagoReq=MiClase.Redondear(Double.parseDouble(c.getString(4)),2 );
                PM+=PagoReq;
            }
            /*
             * obtengo los abonos del clienete
             * */
            sql="Select Monto,FechaAbo,A_mora,Folio from Abonos  where trim(custid)= trim('"+custid+"') " ;


            if(tipo.equals("REIMPRIME")){
                sql= sql + " and folio= trim('" + Folio + "')";
            }else{
                sql= sql + " order by fechaabo desc limit 1";
            }

            Cursor c2=clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES , MiClase.TypeSQL.SELECT,rutaBDCobradores);

            if(c2!=null)
            {
                while (c2.moveToNext()){

                    UltPago=c2.getString(1);
                    Abono=Double.parseDouble(c2.getString(0));
                    Mora=Double.parseDouble(c2.getString(2));
                }
            }
        }catch(Exception e){
            Error="Cuenta 1.0 "+e.toString();
            Error_=true;
        }
        Error_=false;
    }

    public Cursor  SaldosLetras(String custid,String rutaBD_Cobrador){
        String sql="select idletra,Duedate,pm,(capital+ivacapital+interes+ivainteres) saldo,MoraPago,status from letras where custid like'%"+ custid +"%'";
        Cursor CursorLetras=null;
        CursorLetras=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR, MiClase.TypeSQL.SELECT,rutaBD_Cobrador);
        Log.i("Letras", sql);
        return CursorLetras;
    }

    /*FUNCIÓN QUE OBTIENE LAS REFERENCIAS DE LOS CLIENTES - JAPP - 03-02-2016 - INICIO*/
    public Cursor obtenerReferencias(String custid,String rutaBD_Cobrador){
        String sql;
        Cursor c;

        custid=custid.trim();


        sql="SELECT Nombre,Parentesco,Domicilio || ' ' || ifnull(numExterior,'S/N') AS Domicilio,Cruzamientos,Tel,Cel FROM Referencias WHERE custid LIKE '%" +custid+"%'";

        try{
            c = clase.EjecutaSQL(sql,MiClase.TypeBase.Id_COBRADOR_DATOS,MiClase.TypeSQL.SELECT,rutaBD_Cobrador);
            if (c!=null)
                return c;
            else
                return null;
        }
        catch(Exception e){
            return null;
        }
    }
    /*FUNCIÓN QUE OBTIENE LAS REFERENCIAS DE LOS CLIENTES - JAPP - 03-02-2016 - FIN*/


    /*FUNCIÓN QUE OBTIENE LOS ARTÍCULOS DE LOS CLIENTES - JAPP - 03-02-2016 - INICIO*/

    public Cursor obtenerCompras(String custid,String rutaBD_CobradorDatos){
        String sql;
        Cursor c;

        custid=custid.trim();

        sql="SELECT custid,descr,Invtid,LastShipperid,trandate FROM xposline WHERE custid LIKE '%" +custid+"%'";

        try{
            c = clase.EjecutaSQL(sql,MiClase.TypeBase.Id_COBRADOR_DATOS,MiClase.TypeSQL.SELECT,rutaBD_CobradorDatos);
            if (c!=null)
                return c;
            else
                return null;
        }
        catch(Exception e){
            return null;
        }
    }

    /*FUNCIÓN QUE OBTIENE LOS ARTÍCULOS DE LOS CLIENTES - JAPP - 03-02-2016 - FIN*/

    /*public void  Datos_Ult_Letra(String custid){

        String sql="SELECT PM ,SM FROM ultLetraV   where custid like '%"+custid+"%' escape ' '";

        try{
            Cursor c=clase.EjecutaSQL(sql, MiClase.TypeBase.Id_COBRADOR , MiClase.TypeSQL.SELECT);

            if(c!=null){
                while (c.moveToNext())
                    PM= MiClase.Redondear(Double.parseDouble(c.getString(1)),2 );
            }
        }catch(Exception e){
            Error="Datos_Ult_Letra .0 "+e.toString();
            Error_=true;
        }
    }*/

    public void reimpresion (String folio,String custid,String rutaBD_Cobrador,String rutaBDCobradores){

        String sql="Select Monto,FechaAbo,A_mora,Folio from Abonos  where custid like ('%"+custid+"%') and folio like('%"+ folio +"%') ";

        Cursor c2 = clase.EjecutaSQL(sql, MiClase.TypeBase.COBRADORES,MiClase.TypeSQL.SELECT ,rutaBDCobradores);

        if(c2!=null){
            c2.moveToFirst();
            FechaAbo=c2.getString(1);
            r_Abono=Double.parseDouble(c2.getString(0));
            A_Mora=Double.parseDouble(c2.getString(2));
            sql_=sql;

            DatosCuentaCliente(custid,"","",rutaBD_Cobrador,rutaBDCobradores);
            /*
             * datos despues del abono
             * */

            SaldoV=SaldoV+Mora;
            SaldoT=SaldoT+Mora;

            SaldoV=Math.rint(SaldoV);
            SaldoT=Math.rint(SaldoT);
            AbonoCuenta=Math.rint(r_Abono);

            /*
             * si el pago de mora es >= 0
             * se lo resto al mora de la cuenta
             * y se lo resto al
             * */
            if (A_Mora>=0){
                Mora=Mora-A_Mora;
                AbonoCuenta=AbonoCuenta-A_Mora;
            }

            if(r_Abono>=SaldoV){
                SaldoV=0;
                SaldoT=SaldoT-Abono;
            }

            if (r_Abono<SaldoV){
                SaldoV=SaldoV-r_Abono;
                SaldoT=SaldoT-r_Abono;
            }

            SaldoV=clase.Redondear(SaldoV,0);
            SaldoT=clase.Redondear(SaldoT,0);
            SaldoT2=clase.Redondear(SaldoT2,0);
            Mora=clase.Redondear(Mora,0);
        }
    }
}
