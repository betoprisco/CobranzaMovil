package Clases;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.Calendar;

public class MiClase {
    public enum TypeBase {COBRADORES,Id_COBRADOR,DESCARGA_USUARIOS,Id_COBRADOR_DATOS};
    public enum TypeSQL {SELECT,UPDATE};
    public enum TypePrinter {EDOCTA,ABONO,CORTE};

    //@SuppressLint("SdCardPath")
    public Cursor EjecutaSQL(String sql, TypeBase Conec, TypeSQL tipoSQL,String ruta){
        try{
            SQLiteDatabase db= null;

            if (Conec==TypeBase.COBRADORES)	{
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                Log.i("Entra ---", "Entra ejecuta" + sql);
            }
            if (Conec==TypeBase.Id_COBRADOR){
                /*
                 * Lectura del archivo cobrador
                 * */

                /******************************/
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            }

            //PARA LA BASE DE DATOS DE LOS CLIENTES (REFERENCIAS, ARTÍCULOS, LIQUIDE, DOMICILIO ALTERNO - JAPP - 26-02-2016
            if (Conec==TypeBase.Id_COBRADOR_DATOS){
                /*
                 * Lectura del archivo cobrador
                 * */

                /******************************/
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            }

            if (Conec == TypeBase.DESCARGA_USUARIOS)
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            if(tipoSQL==TypeSQL.SELECT){
                return  db.rawQuery(sql, null);
            }else{
                db.execSQL(sql);
            }

            Log.i("Entra ---", "Entra ejecuta" + sql);
        }catch(Exception e){
            Log.i("Entra Catch EjecutaSQL", e.toString());
        }
        return  null;
    }

    public String Direcciones_URL( TypeBase tipo){
        String url="";
        if (tipo== TypeBase.DESCARGA_USUARIOS ){
            //conexion por internet
            url = "http://sbvconect.dyndns.org/Terminal/";
        }
        return url;
    }

    //@SuppressLint("SdCardPath")
    public String EjecutaSQLUpdate(String sql, TypeBase Conec, TypeSQL tipoSQL,String ruta){
        try{

            SQLiteDatabase db= null;

            if (Conec==TypeBase.COBRADORES)
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            //if (Conec==TypeBase.Id_COBRADOR){
                /*
                 * Lectura del archivo cobrador
                 * */

              //  String Cobrador= Lectura();
                /******************************/
                //db = SQLiteDatabase.openDatabase("/sdcard/"+ Cobrador +".zip",null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                /*db = SQLiteDatabase.openDatabase("/sdcard/"+ Cobrador +".zip",null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            }*/

            if (Conec==TypeBase.DESCARGA_USUARIOS){
                /******************************/
                //db = SQLiteDatabase.openDatabase(contexto.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip",null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            }

            if(tipoSQL==TypeSQL.SELECT){
                return  "Incorrecto seleccione el metodo adecuado";
            }else{
                db.execSQL(sql);
                return  "Actualizado";
            }
        }catch(Exception e){
            Log.i("Entra Catch", e.toString());
            return  "Error dir Alterna" + e.getMessage().toString();
        }
    }

    public Cursor Datos_Historial(String rutaBDCobradores){
        String sql =" select custid,historial,perspectiva,perspectivadet,userupd,montoApagar,tel,";
        sql=sql+" status,trim(longitude) as longitude,trim(latitude) as latitude,FechaSeg,folio,FechaCrt,idGestion from historial where status not in ('ENVIADO')";

        return EjecutaSQL(sql,TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);
    }

    public Cursor Datos_Abonos(String rutaBDCobradores){
        String sql ="";
        sql="select custid,Cobrador,Monto,Fecha,PagoReq,Long,Lati,id,folio,FechaAbo,A_Mora,convenio,OpcConvenio,apoyoCovid19";
        sql=sql+" from Abonos where Status not in ('ENVIADO') group by custid";

        return EjecutaSQL(sql,TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);
    }

    //DATOS A ENVIAR DE LOS DECOMISOS - JAPP - 17-03-2016 - INICIO
    public Cursor datosDecomisos(String rutaBDCobradores){
        String sql ="";
        sql="select Custid,ClaveArt,Embarque,FechaDecomiso,Detalles,Art,FechaCompra,Cobrador,AutorizaDecomiso,FolioDecomiso,Status,BodegaRecibe,tiendaDeCompra,Longitud,Latitud,NoSerie,id," +
                "Funcionamiento,Accesorios";
        sql=sql+" from decomisos where EstadoEnvio not in ('ENVIADO') group by custid";

        return EjecutaSQL(sql,TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);
    }
    //DATOS A ENVIAR DE LOS DECOMISOS - JAPP - 17-03-2016 - FIN

    public Cursor datosIMGdeco(String rutaBDCobradores){
        String sql ="";
        sql="select * from imagenDecomiso where enviado = 'ESPERA'";
        return EjecutaSQL(sql,TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);
    }


    public Cursor Dir_Alterna(String Custid,String rutaBD_CobradorDatos){
        String sql=" Select  trim(Addr1), trim(Addr2), trim(Fax), trim(Phone)  from SOAddress where Trim(custid) = '" + Custid.trim() + "'";
        Log.i("Sql", sql);
        return EjecutaSQL(sql,TypeBase.Id_COBRADOR_DATOS, TypeSQL.SELECT,rutaBD_CobradorDatos);
    }

    public Cursor aviso(String Custid,String rutaBD_Cobrador){
        String sql=" Select  *  from Avisos where Trim(custid) = '" + Custid.trim() + "'";
        Log.i("Sql", sql);
        return EjecutaSQL(sql,TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);
    }

    public Cursor referencias(String Custid,String rutaBD_CobradorDatos){
        String sql=" Select  *  from referencias where Trim(custid) = '" + Custid.trim() + "'";
        Log.i("Sql", sql);
        return EjecutaSQL(sql,TypeBase.Id_COBRADOR_DATOS, TypeSQL.SELECT,rutaBD_CobradorDatos);
    }

    public Cursor items(String Custid,String rutaBD_CobradorDatos){
        String sql=" Select * from xposline where Trim(custid) = '" + Custid.trim() + "'";
        Log.i("Sql", sql);
        return EjecutaSQL(sql,TypeBase.Id_COBRADOR_DATOS, TypeSQL.SELECT,rutaBD_CobradorDatos);
    }

    public Cursor itemsDecomiso(String embarque,String claveArtículo,String rutaBD_CobradorDatos){
        String sql=" Select * from xposline where Trim(lastshipperid) = '" + embarque.trim() + "' and trim(Invtid) = '" + claveArtículo.trim() + "'";
        Log.i("Sql", sql);
        return EjecutaSQL(sql,TypeBase.Id_COBRADOR_DATOS, TypeSQL.SELECT,rutaBD_CobradorDatos);
    }

    public Cursor Datos_Diralt(String rutaBDCobradores){
        String sql = "Select Custid,Nombre, Addr1,Addr2,Fax,Phone,FechaCrt,IdRegistro, Cobrador from dirAlterna where Status = 'ESPERA' ";

        return EjecutaSQL(sql,TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);
    }

    public Cursor VerificaInstalacion(String rutaBDCobradores){
        String sql = "Select Nuevo from Creacion where Trim(Nuevo) = '0' ";
        return EjecutaSQL(sql,TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);
    }

    public Cursor verificaConvenio(String rutaBDCobradores){
        String sql = "SELECT Custid,Opcion,Pago,Mora,Quincenas,SaldoT,Letra,FechaCRT,Usuario FROM convenio where estatus = 'ESPERA' ";

        return EjecutaSQL(sql, TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);
    }

   /* public static boolean BuscaArchivo(String Archivo){
        boolean ban;
        String sFichero =Archivo;
        //Intentamos abrir el fichero
        File fichero = new File(sFichero);
        //verificamos que exista
        if (fichero.exists())
            ban = true;
        else
            ban = false;

        return ban;
    }
*/
    public boolean verConvenio(String custid, String rutaBD_Cobrador,String rutaBDCobradores){
        /*Esta funcion sirve para verificar que el cliente pueda tener convenio MVLA
         * 4 periodos vencidos o mas
         * No tener convenio vigente
         * No tener un convenio invalidado en los últimos 60 días
         * si no cumple esas caracteristicas no puede acceder a poner convenio
         * */

        Cursor c = null;
        String sql = "";
        boolean banderita = false;
        int LetrasV = 0;
        String conve = "";

        sql = "Select PeriodVencid from dsCob where Trim(custid) = '" + custid.trim() + "'";
        c = EjecutaSQL(sql, TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);

        if (c!=null){
            while(c.moveToNext()){
                LetrasV = Integer.valueOf(c.getString(0)).intValue();
            }
        }

        if (LetrasV < 4){
            banderita = true;
        }

        sql = "SELECT convenio FROM customer where Trim(custid) = '"+ custid.trim() +"' ";
        c = EjecutaSQL(sql, TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);

        if(c!=null){
            while(c.moveToNext()){
                conve = c.getString(0);
            }
        }

        if( !conve.equals("NO") && !conve.equals("INVALIDO"))
            banderita = true;

        //PARA QUE BUSQUE EL CONVENIO EN LA TABLA DE CONVENIOS CON EL ID DEL COBRADOR- JAPP - 12-09-2017
        sql = "SELECT Custid FROM convenio where Trim(Custid) = '"+ custid.trim() +"' ";
        c = EjecutaSQL(sql, TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);

        if(c!=null){
            if(c.getCount()>0){
                banderita = true;
            }
        }

        //PARA QUE BUSQUE SI SE SUBIÓ UN CONVENIO DESDE EL MÓVIL SE BUSCA EN LA TABLA CONVENIOS DE LA BASE DE DATOS DE LA APP- JAPP - 25-01-2019
        sql = "SELECT Custid FROM convenio where Trim(Custid) = '"+ custid.trim() +"' ";
        c = EjecutaSQL(sql, TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);

        if(c!=null){
            if(c.getCount()>0){
                banderita = true;
            }
        }

        Log.i("Letras ", conve + " -- " + sql);
        return banderita;
    }

    public String tieneConvenio(String custid,String rutaBD_Cobrador,String rutaBDCobradores){
        /*verifica si tiene convenio para la pantalla abonos*/

        Cursor c = null;
        String sql ="";
        String convenio = "";

        /*Tabla clientes COBRADOR.ZIP*/
        sql = "SELECT convenio FROM customer where Trim(custid) = '"+ custid.trim() +"' ";
        c = EjecutaSQL(sql, TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);

        if (c!=null){
            while(c.moveToNext()){
                convenio = c.getString(0);
            }
        }

        /*si le acaba de guardar convenio*/
        if (convenio.equals("") || convenio.trim().equals("NO") || convenio.trim().equals("INVALIDO") ){
            sql ="Select estatus from convenio where Trim(custid) = '" + custid.trim() +  "'";
            c = EjecutaSQL(sql, TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);

            if (c!=null){
                while(c.moveToNext()){
                    Log.i("convenios4 --------", convenio);
                    convenio = "Reciente";
                }
            }
        }
        return convenio;
    }

    public float totconvenio(String custid,String rutaBD_Cobrador,String rutaBDCobradores){
        /*Saca los calculos del total del convenio a pagar*/

        Cursor c = null;
        float conv =0;
        float pm = 0;
        String opcion = "";

        /*Obtiene la letra para calculo de convenio*/
        String sql = "SELECT   capital + interes+ ivacapital + ivainteres  FROM letras where trim(custid) = '" + custid.trim() + "'  order by idletra desc  limit 1 ";
        c = EjecutaSQL(sql, TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);

        if(c!=null){
            while(c.moveToNext()){
                pm = c.getFloat(0);
            }
        }

        /*verifica que tipo de convenio tiene*/
        sql = "SELECT  Opcion  FROM customer where trim(custid) = '" + custid.trim() + "'  and convenio = 'ACTIVO'  "; //verifico en customer si ya tenia convenio
        c = EjecutaSQL(sql, TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);

        if (c!=null){
            while(c.moveToNext()){
                opcion = c.getString(0);
            }
        }

        Log.i("primer paso", opcion);

        //verifico que si tenia convenio anteriormente o lo acaba de crear
        if(opcion.equals(""))
        {
            /*verifica que tipo de convenio le agregaron*/
            sql = "SELECT  Opcion  FROM convenio where trim(custid) = '" + custid.trim() + "'  "; //verifico en convenios nuevos que tipo tiene
            c = EjecutaSQL(sql, TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);

            if (c!=null){
                while(c.moveToNext()){
                    opcion = c.getString(0);
                }
            }
        }

        if (opcion.equals("1")){
            conv = pm+200;
        }
        else if(opcion.equals("2")){
            conv = (pm*2)+250;
        }else{
            conv = 0;
        }
        return conv;
    }

    public String opcionConvenio(String custid,String rutaBD_Cobrador,String rutaBDCobradores){

        Cursor c= null;
        String opcion ="";
        String sql= "";

        sql = "SELECT  Opcion  FROM customer where trim(custid) = '" + custid.trim() + "'  and convenio = 'ACTIVO'  "; //verifico en customer si ya tenia convenio
        c = EjecutaSQL(sql, TypeBase.Id_COBRADOR, TypeSQL.SELECT,rutaBD_Cobrador);

        if(c!=null){
            while(c.moveToNext()){
                opcion = c.getString(0);
            }
        }

        if (opcion.equals("")){
            /*verifica que tipo de convenio le agregaron*/
            sql = "SELECT  Opcion  FROM convenio where trim(custid) = '" + custid.trim() + "'  "; //verifico en convenios nuevos que tipo tiene
            c = EjecutaSQL(sql, TypeBase.COBRADORES, TypeSQL.SELECT,rutaBDCobradores);

            if (c!=null){
                while(c.moveToNext()){
                    opcion = c.getString(0);
                }
            }
        }
        return opcion;
    }

    /*
     * LECTURA DE ARCHIVO CONFIG
     * */
    public String Lectura(String rutaBD_USRCOB){
        String texto="";
        String sql= "Select CobID, Nombre  from Tbl_Usuarios where sesion = '1' ";
        SQLiteDatabase db= null;
        db = SQLiteDatabase.openDatabase(rutaBD_USRCOB,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext()){
            texto = c.getString(0);
        }

        Log.i("MiClase", texto);

        return texto;
    }



    public static double Redondear(double numero,int digitos)
    {
        return Math.ceil(numero);
    }

    public String LecturaNombreCob(String rutaBD_USRCOB){
        String texto="";
        String sql= "Select CobID, Nombre  from Tbl_Usuarios where sesion = '1' ";
        SQLiteDatabase db= null;
        db = SQLiteDatabase.openDatabase(rutaBD_USRCOB,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext()){
            texto = c.getString(1);
        }
        Log.i("MiClase", texto);
        c.close();
        return texto;
    }

    public String verificaUsuario(String usuario, String Password,String ruta){

        if (existArchivo(ruta)){
            try{
                String sql= "Select  usuario,contrasenia, julianday() - julianday(fechaCrt) dias, descarga from Tbl_Usuarios where Trim(Usuario) = '" + usuario + "' and Trim(contrasenia)  = '" + Password
                        + "' and activo = 1 ";
                SQLiteDatabase db= null;
                double dias = 0.0;
                double descarga = 0.0;
                Log.i("SQL", sql);

                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                Cursor c = db.rawQuery(sql, null);

                if(c!=null) {
                    while (c.moveToNext()){
                        dias = c.getDouble(2);
                        descarga = c.getDouble(3);
                        Log.i("****Entra  ", "para verificar descarga");

                        if (dias > descarga){
                            return "NECESITA ACTUALIZAR";
                        }else{
                            return c.getString(0);
                        }
                    }
                    c.close();
                }else {
                    return "NECESITA ACTUALIZAR" + c.getString(2);
                }
            }catch(Exception e){
                return "NO "  ;
            }

            return "NO";
        }else{
            return "NO EXISTE BASE";
        }
    }

    public static boolean existArchivo(String Archivo){
        boolean result;
        String sFichero =Archivo;
        //Intentamos abrir el fichero
        File fichero = new File(sFichero);
        //verificamos que exista
        if (fichero.exists())
            result = true;
        else
            result = false;

        return result;
    }


    /*public StringBuffer ImprimeCorte( String sql_   ){
        StringBuffer sb = new StringBuffer();

        try{
            String Cob = Lectura();
            String cad=ImprimeEncabezado();
            String Fecha=FechaImpresion();
            Double Total=0.0;
            /*
             * Impresión de encabezado
             * */
      /*      sb.append(cad);
            sb.append("{reset}{center}{i}"+ Cob +"{br}");
            sb.append("{reset}{center}{i}Corte de Caja(Movil){br}");
            sb.append("{reset}{center}" + Fecha);
            sb.append("{br}");
            sb.append("{reset}{center}{s}_____________________________________");
            sb.append("{br}");
            sb.append("{br}");

            Cursor c2 = EjecutaSQL(sql_,MiClase.TypeBase.COBRADORES,MiClase.TypeSQL.SELECT);
            if(c2!=null)
            {
                while (c2.moveToNext()){

                    sb.append("{reset}{center}{s}______________________________________");
                    sb.append("{br}");
                    sb.append("{reset}{center}{b}"+c2.getString(3));

                    sb.append("{br}");
                    sb.append("{reset}{center}{b}"+c2.getString(0));

                    sb.append("{br}");
                    sb.append("{reset}{center}{b}"+c2.getString(1));
                    sb.append("{br}");

                    Total+=Double.valueOf(c2.getString(4));;
                }
            }

            sb.append("{br}");
            sb.append("{reset}{center}{s}______________________________________");

            sb.append("{br}");
            sb.append("{reset}{center}{b}Monto Total "+ Double.toString(Total));
            sb.append("{br}");

            sb.append("{br}");
            sb.append("{br}");
            sb.append("{br}");
            sb.append("{br}");
            sb.append("{reset}{center}{s}______________________________________");
            sb.append("{br}");
            sb.append("{reset}{center}{i}COBRADOR: "+LecturaNombreCob() );
            sb.append("{br}");
            sb.append("{br}");
            sb.append("{br}");
            sb.append("{br}");
            sb.append("{br}");
            sb.append("{reset}{center}{s}______________________________________");
            sb.append("{br}");
            sb.append("{reset}{center}{i}JEFE DE CARTERA: ");
            sb.append("{br}");
            /*
             * Pie
             */
            /*cad=ImprimePie();
            sb.append(cad);

        }catch(Exception e){
            sb.append("Error print corte 1.0 "+ e.toString() + "sql = "+sql_);
        }
        return sb;
    }*/

    /*public StringBuffer ImprimeEdoCta(  String custid ){
        /*
         * Declaración de variables
         */
       /* String Fecha ="";
        StringBuffer sb = new StringBuffer();
        String cad ="";
        /*
         * Extraigo los datos del cliente
         * */
       /* MiClase_DatosCliente DatosCliente = new  MiClase_DatosCliente();

        DatosCliente.DatosPersonalesCliente(custid);
        DatosCliente.DatosCuentaCliente(custid,"","");

        /*
         * GENERO EN EL ENCABEZADO
         * */

        //Fecha=FechaImpresion();

        /*
         * Verifico si existe algun error
         * */

        /*if (DatosCliente.Error_==true){
            sb.append(DatosCliente.Error);
        }else{
            try{
                /*
                 * Cuerpo
                 * */
                /*String Cob = Lectura();
                cad=ImprimeEncabezado();
                sb.append(cad);
                sb.append("{reset}{center}{i}"+ Cob +"{br}");
                sb.append("{reset}{center}{i}Estado de Cuenta(Movil){br}");
                sb.append("{reset}{center}" + Fecha);
                sb.append("{br}");
                sb.append("{reset}{center}{s}_____________________________________");
                sb.append("{br}");
                sb.append("{reset}{b}"+ DatosCliente.Nombre );
                sb.append("{br}");
                sb.append("{reset}{i}"+custid);

                sb.append("{br}");
                sb.append("{reset}{center}{s}_____________________________________");
                sb.append("{br}");

                sb.append("{reset}{b}Saldo Vencido: $"+ Double.toString( DatosCliente.SaldoV));
                sb.append("{reset}{right}{b}"+  " {br}");
                sb.append("{reset}{b}Moratorio: $"+ Double.toString( DatosCliente.Mora));
                sb.append("{reset}{right}{b}"+  " {br}");
                sb.append("{reset}{b}Requerido: $"+ Double.toString( DatosCliente.PagoReq));
                sb.append("{reset}{right}{b}"+  " {br}");

                sb.append("{reset}{i}Letras Vencidas: " + Integer.toString( DatosCliente.LetrasV));
                sb.append("{reset}{right}{i}"+" {br}");

                sb.append("{reset}{center}{s}______________________________________");
                sb.append("{br}");

                sb.append("{reset}{b}PM:");
                sb.append("{reset}{right}{b}$ "+ Double.toString( DatosCliente.PM) +" {br}");

                sb.append("{reset}{b}Saldo Total:");
                sb.append("{reset}{right}{b}$ "+ Double.toString( DatosCliente.SaldoT+DatosCliente.Mora) +" {br}");

                sb.append("{reset}{center}{s}______________________________________");
                sb.append("{br}");

                /*
                 * Pie
                 */
               /* cad=ImprimePie();
                sb.append(cad);
            }catch(Exception e){
                sb.append("Error Impresion 2.0 "+ e.toString());
            }
        }
        return sb;
    }*/

    /*
     * GENERO ENCABEZADO DE IMPRESION
     * */
    /*public String ImprimeEncabezado(){

        String sb="";
        sb="{reset}{center}{s}ELECTRONICOS Y MAS, S.A. DE C.V.{br}";
        sb+="{reset}{center}{s}EMA960527GY3{br}";
        sb+="{reset}{center}{s}AV. HEROES No. 71 2DO. PISO LOCAL B{br}";
        sb+="{reset}{center}{s}COL. CENTRO, CP 77000, CHETUMAL, Q.ROO{br}";
        sb+="{reset}{center}{b}QUALITY STORES/COMPUTERAMA";
        sb+="{br}";

        return sb;
    }*/

   /* public String FechaImpresion(){
        /*
         * CONSULTA PARA OBTENER FECHA
         */

        /*String sql="SELECT strftime('%d-%m-%Y','now','localtime')  Fecha";

        Cursor cFecha = EjecutaSQL(sql,MiClase.TypeBase.Id_COBRADOR,MiClase.TypeSQL.SELECT );

        if (cFecha!=null){
            cFecha.moveToFirst();
            return  cFecha.getString(0);
        }
        return "";
    } */
    /*
     * Metodo para generar el pie de la impresion
     * */

    /*public String ImprimePie(){
        String sb ="{reset}{center}{s}______________________________________";
        sb+="{br}";

        sb+="{reset}{center}{b}Actualice su cuenta para evitar ";
        sb+="{reset}{center}{b}que sigan aumentando sus {br}";
        sb+="{reset}{center}{b}intereses moratorios{br}";

        sb+="{reset}{center}{s}______________________________________";

        sb+="{br}";
        sb+="{reset}{center}{s}Para dudas o aclaraciones llame gratis al:{br}";
        sb+="{reset}{center}{s}01-800-667-1168{br}";
        sb+="{reset}{center}{s}Consulte su estado de cuenta en linea:{br}";
        sb+="{reset}{center}{s}www.qualitystores.com.mx{br}";
        sb+="{reset}{center}{s}www.computerama.com.mx{br}";
        sb+="{reset}{center}{s}NOTA: Es posible que este reporte{br}";
        sb+="{reset}{center}{s} no cuente con abonos de {br}";
        sb+="{reset}{center}{s} las ultimas 24 hrs{br}";

        return sb;
    }*/

    /*
     * Reimpresion de abono
     * */

    /*public StringBuffer Reimprime_(String custid,String Folio,String Cobrador){
        StringBuffer sb = new StringBuffer();
        String cad=ImprimeEncabezado();

        MiClase_DatosCliente DatosCliente = new  MiClase_DatosCliente();

        DatosCliente.DatosPersonalesCliente(custid);
        DatosCliente.DatosCuentaCliente(custid,"","");
        /*
         * Obtengo los datos del abono
         * */
        /*DatosCliente.reimpresion(Folio, custid);

        sb.append(cad);
        sb.append("{br}");
        sb.append("{reset}{center}{i}Recibo: "+ Folio);
        sb.append("{br}");
        sb.append("{reset}{center}{i}Fecha: "+ DatosCliente.FechaAbo );
        sb.append("{br}");
        sb.append("{reset}{center}{u}Agente domiciliario: "+Cobrador);

        sb.append("{br}");
        sb.append("{reset}{b}" + DatosCliente.Nombre);
        sb.append("{br}");
        sb.append("{reset}{i}"+custid+"{br}");

        sb.append("{reset}{i}"+DatosCliente.Direccion+"{br}");
        sb.append("{br}");
        sb.append("{reset}{center}{s}_____________________________________");
        sb.append("{br}");

        sb.append("{reset}{b}Pagado:");
        sb.append("{reset}{right}{b}$"+ DatosCliente.r_Abono +" {br}");

        if(DatosCliente.A_Mora>=0){
            sb.append("{reset}{i}Abono a moratorios:");
            sb.append("{reset}{right}{i}$ "+DatosCliente.A_Mora +" {br}");
        }

        sb.append("{reset}{b}Abono a la cuenta:");
        sb.append("{reset}{right}{b}$ "+DatosCliente.AbonoCuenta+" {br}");

        sb.append("{reset}{center}{s}______________________________________");
        sb.append("{br}");

        sb.append("{reset}{b}Saldo vencido:");
        sb.append("{reset}{right}{b}$"+DatosCliente.SaldoV+" {br}");

        sb.append("{reset}{i}Saldo total:");
        sb.append("{reset}{right}{i}$"+DatosCliente.SaldoT+" {br}");
        sb.append("{reset}{center}{s}______________________________________");

        sb.append("{br}");
        sb.append("{br}");
        sb.append("{br}");
        sb.append("{br}");
        sb.append("{reset}{center}{s}______________________________________");
        sb.append("{br}");
        sb.append("{reset}{center}{i}COBRADOR: "+ LecturaNombreCob());
        sb.append("{br}");
        sb.append("{reset}{center}{s}Para dudas o aclaraciones llame gratis al:{br}");
        sb.append("{reset}{center}{s}01-800-667-1168{br}");
        sb.append("{reset}{center}{s}Consulte su estado de cuenta en línea:{br}");
        sb.append("{reset}{center}{s}www.qualitystores.com.mx{br}");
        sb.append("{reset}{center}{s}www.computerama.com.mx{br}");

        return sb;
    }
*/
    /*
     * Metodo para obtener los saldos del cliente
     * */
    @SuppressWarnings("null")
    /*public String[] SaldosCliente(String custid){

        String Saldos[] = null;

        String sql="SELECT Vencido,SaldoT,NumLetVenc,Moratorios	FROM dscob where trim(custid) = trim('" + custid + "')";

        Cursor c3= EjecutaSQL(sql,MiClase.TypeBase.Id_COBRADOR,MiClase.TypeSQL.SELECT );
        if(c3!=null)
        {
            c3.moveToFirst();
            /*
             * VENCIDO
             * */
            //Saldos[0]= Double.toString( Double.parseDouble(c3.getString(0)));
            /*
             * SALDO TOTAL
             * */
            //Saldos[1]= Double.toString( Double.parseDouble(c3.getString(1)));
            /*
             * LETRA V
             * */
            //Saldos[2]= Double.toString( Integer.parseInt(c3.getString(2)));
            /*
             * MORA
             * */
            //Saldos[3]= Double.toString( Redondear(Double.parseDouble(c3.getString(3)),0));
            /*
             * VENCIDO + MORA
             * */
            //Saldos[4]= Double.toString( Redondear(Double.parseDouble(c3.getString(0))+Redondear(Double.parseDouble(c3.getString(3)),0),0));
            /*
             * SALDOT + MORA
             * */
            /*Saldos[5]= Double.toString( Redondear(Double.parseDouble(c3.getString(1))+Double.parseDouble(c3.getString(3)),0));
        }
        return Saldos;
    }*/

    public String FechaAnterior(int mes, int año, int dia){

        Integer dia_=dia-1;
        String DiaA="",Mes_="";

        String fecha= Integer.toString(año) +"-"+ Integer.toString(mes) +"-"+Integer.toString( dia_) +" 20:00:00";

        if(mes<10 && dia>9){
            Mes_="0"+Integer.toString(mes);
            fecha= Integer.toString(año) +"-"+ Mes_ +"-"+ Integer.toString(dia-1) +" 20:00:00";
            return fecha;
        }

        if (mes<10){
            Mes_="0"+Integer.toString(mes);
        }else{
            Mes_=Integer.toString(mes);
        }

        if(dia>1 && dia<10){
            fecha= Integer.toString(año) +"-"+ Mes_ +"-0"+ Integer.toString(dia-1) +" 20:00:00";
        }

        if (dia==1){
            Mes_=Integer.toString(mes-1);
            DiaA=Integer.toString(diaAnterior(mes-1,año));
            fecha= Integer.toString(año) +"-"+ Mes_+"-"+ DiaA +" 20:00:00";
        }
        if(mes==1 && dia == 1){
            Mes_=Integer.toString(mes-1);
            DiaA=Integer.toString(diaAnterior(mes,año));
            fecha= Integer.toString(año-1) +"-"+ Mes_ +"-"+ DiaA +" 20:00:00";
        }

        return fecha;
    }

    public Integer diaAnterior(int mes, int año){
        Calendar c = Calendar.getInstance();
        c.set( Calendar.YEAR, año );
        c.set( Calendar.MONTH, mes );
        return  c.getActualMaximum( Calendar.DAY_OF_MONTH );
    }
}