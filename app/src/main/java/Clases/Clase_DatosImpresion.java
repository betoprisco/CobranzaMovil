package Clases;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Clase_DatosImpresion {

    String strSql;
    public MiClase clase = new MiClase();
    MiClase_DatosCliente DatosCliente = new  MiClase_DatosCliente();

    public static double Redondear(double numero,int digitos)
    {
        return Math.ceil(numero);
    }

    //PARA IMPRIMIR EL DECOMISO DEL CLIENTE - JAPP - 11-03-2016 - INICIO
    @SuppressLint("NewApi") public String imprimirDecomiso(String folioDecomiso,String rutaBDCobradores,String rutaBD_Cobrador){
        String idCliente = null;
        String nombreCliente = null;
        String domicilioCliente = null;
        String coloniaCliente = null;
        String teléfonoCliente = null;
        String artículo = null;
        String claveArt = null;
        String noSerie = null;
        String embarque = null;
        String fechaDecomiso = null;
        String fechaCompra = null;
        String tienda = null;
        String sb = null;
        String detalles = null;
        String folioDeco = null;
        String Funcionamiento="",Accesorios="";
        SQLiteDatabase decomisos;

        decomisos = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        strSql="select Custid,ClaveArt,Embarque,FechaDecomiso,Art,FechaCompra,FolioDecomiso,tiendaDeCompra,NoSerie,detalles,Funcionamiento,Accesorios ";
        strSql+="from decomisos where trim(FolioDecomiso) = trim('" + folioDecomiso + "')";

        Cursor cursorDecomiso = decomisos.rawQuery(strSql,null);

        if (cursorDecomiso!=null && cursorDecomiso.getCount()>0)
        {
            while (cursorDecomiso.moveToNext()){
                idCliente= cursorDecomiso.getString(0);
                claveArt=cursorDecomiso.getString(1);
                embarque=cursorDecomiso.getString(2);
                fechaDecomiso=cursorDecomiso.getString(3);
                artículo=cursorDecomiso.getString(4);
                fechaCompra=cursorDecomiso.getString(5);
                //FOLIO DECOMISO (6)
                folioDeco=cursorDecomiso.getString(6);
                tienda=cursorDecomiso.getString(7);
                noSerie=cursorDecomiso.getString(8);
                detalles=cursorDecomiso.getString(9);
                Funcionamiento=cursorDecomiso.getString(10);
                Accesorios=cursorDecomiso.getString(11);

            }
        }
        cursorDecomiso.close();

        //SE OBTIENEN LOS DATOS DEL CLIENTE
        SQLiteDatabase datosCliente = null;

        datosCliente = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        strSql="select nombre,direccion,colonia,tel1 from customer where trim(custid) = '" + idCliente.trim() +"'";

        Cursor cursorCliente = datosCliente.rawQuery(strSql,null);

        if (cursorCliente!=null){
            while (cursorCliente.moveToNext()){
                nombreCliente= cursorCliente.getString(0);
                domicilioCliente= cursorCliente.getString(1);
                coloniaCliente=cursorCliente.getString(2);
                teléfonoCliente=cursorCliente.getString(3);
            }
        }

        try{

            sb="{reset}{b}{center} DECOMISO DE ARTÍCULO";
            sb+="{br}{br}";
            sb+="{reset}Fecha:{br}";
            sb+="{reset}" + fechaDecomiso;
            sb+="{br}";
            sb+="{reset}Folio Decomiso:";
            sb+="{br}";
            sb+="{reset}" + folioDeco;
            sb+="{br}";
            sb+="{reset}Cliente:{br}";
            sb+="{reset}" + nombreCliente;
            sb+="{br}";
            sb+="{reset}" + domicilioCliente;
            sb+="{br}";
            sb+="{reset}" + coloniaCliente;
            sb+="{br}";
            sb+="{reset}" + teléfonoCliente;
            sb+="{br}{br}{br}";
            sb+="{reset}{b}{center}DETALLES DEL ARTÍCULO";
            sb+="{br}{br}";
            sb+="{reset}Descripción:{br}";
            sb+="{reset}" + artículo.trim() + " - " + claveArt.trim();
            sb+="{br}";
            sb+="{reset}No. Serie:{br}";
            sb+="{reset}" + noSerie.trim();
            sb+="{br}";
            sb+="{reset}Tienda: " + tienda;
            sb+="{br}";
            sb+="{reset}Fecha de compra:{br}";
            sb+="{reset}" + fechaCompra.trim();
            sb+="{br}";
            sb+="{reset}Embarque:{br}";
            sb+="{reset}" + embarque;
            sb+="{br}";
            sb+="{br}";

            sb+="{reset}Detalles: " + detalles.trim();
            sb+="{br}{br}";
            sb+="Funcionamiento: " + Funcionamiento.trim();
            sb+="\n\n";
            sb+="{reset}Condiciones físicas:_____________________________________________________________________________";
            sb+="_______________________________________________________________________________________________";
            sb+="{br}";
            sb+="{br}";
            sb+="Accesorios: " + Accesorios.trim();
            sb+="\n\n";
            sb+="\n";
            sb+="{reset}{center}{b}Cliente{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="________________________________{br}";
            sb+="{reset}{center}Nombre y Firma";
            sb+="{br}";
            sb+="{br}";
            sb+="{reset}{center}{b}Asesor{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="________________________________{br}";
            sb+="{reset}{center}Nombre y Firma Vo Bo";
            sb+="{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="{reset}Observaciones:_____________________________________________________________________________";
            sb+="_____________________________________________________________________________________________________";
            sb+="{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="________________________________{br}";
            sb+="{reset}{center}Fecha de Recibido";
            sb+="{br}{br}";

        }catch(Exception e){
            sb="Error Impresión Decomiso "+ e.toString();
        }
        cursorCliente.close();

        return sb;
    }

    //PARA IMPRIMIR EL DECOMISO DEL CLIENTE - JAPP - 11-03-2016 - FIN

    //PARA IMPRIMIR EL AVISO DEL CLIENTE - JAPP - 08-02-2016 - INICIO
    @SuppressLint("NewApi")
    public String imprimirAviso(String custid, String rutaBD_Cobrador,String rutaBD_USRCOB){
        String titulo="";
        String mensaje1="";
        String mensaje2="";
        String address="";
        String nombreCliente="";
        String domicilioCliente="";
        String coloniaCliente="";
        String telefonoCliente="";
        String faxCliente="";
        String nombreAval="";
        String direccionAval="";
        String coloniaAval="";
        String montoVencido="";
        String moratorios="";
        String pagoRequerido="";
        String gastoCobranza="0.0";
        String PM="0.0";
        String nombreCobrador="";
        String telefonoCobrador="";
        String fechaImpresion="";
        String fecha="";
        String sb="";
        String encabezado="";

        Integer día;

        Long total;

        SQLiteDatabase avisos = null;

        avisos = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        fecha=FechaImpresion(rutaBD_Cobrador);
        día=Integer.parseInt(fecha.substring(0, 1));

        strSql="select name,addr1,addr2,trim(phone),trim(fax),montovencido,moratorios,pagoreq,gastocobranza";

        if(día==1 || día==2 || día == 3 || día == 16 || día == 17 || día == 18)
            strSql+=",PM";

        strSql+=",trim(avalname),trim(avaladdr1),trim(avaladdr2),trim(mensaje),trim(mensaje2),trim(titulo),trim(mensaje3) from avisos where trim(custid) = trim('" + custid + "')";

        Cursor cursorAvisos = avisos.rawQuery(strSql,null);

        if (cursorAvisos!=null)
        {
            while (cursorAvisos.moveToNext()){
                nombreCliente= cursorAvisos.getString(0);
                domicilioCliente=cursorAvisos.getString(1);
                coloniaCliente=cursorAvisos.getString(2);
                telefonoCliente=cursorAvisos.getString(3);
                faxCliente=cursorAvisos.getString(4);
                montoVencido=cursorAvisos.getString(5);
                moratorios=cursorAvisos.getString(6);
                pagoRequerido=cursorAvisos.getString(7);

                if(!cursorAvisos.getString(8).isEmpty())
                    gastoCobranza=cursorAvisos.getString(8);

                if(día==1 || día==2 || día == 3 || día == 16 || día == 17 || día == 18){
                    PM=cursorAvisos.getString(9);
                    nombreAval=cursorAvisos.getString(10);
                    direccionAval=cursorAvisos.getString(11);
                    coloniaAval=cursorAvisos.getString(12);
                    mensaje1=cursorAvisos.getString(13);
                    mensaje2=cursorAvisos.getString(14);
                    titulo=cursorAvisos.getString(15);
                    address=cursorAvisos.getString(16);
                }
                else{
                    nombreAval=cursorAvisos.getString(9);
                    direccionAval=cursorAvisos.getString(10);
                    coloniaAval=cursorAvisos.getString(11);
                    mensaje1=cursorAvisos.getString(12);
                    mensaje2=cursorAvisos.getString(13);
                    titulo=cursorAvisos.getString(14);
                    address=cursorAvisos.getString(15);
                }
            }
        }
        cursorAvisos.close();

        //SE OBTIENEN LOS DATOS DEL COBRADOR
        SQLiteDatabase cobrador = null;

        cobrador = SQLiteDatabase.openDatabase(rutaBD_USRCOB,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

        strSql="select nombre,phone from tbl_usuarios where sesion = '1' ";

        Cursor cursorCobrador = cobrador.rawQuery(strSql,null);

        if (cursorCobrador!=null){
            while (cursorCobrador.moveToNext()){
                nombreCobrador= cursorCobrador.getString(0);
                telefonoCobrador= cursorCobrador.getString(1);
            }

        }

        //sb="CURSOR COBRADOR";

        try{
            sb+="{br}";
            sb+="{reset}{b} *ES UN AVISO DE LETRAS VENCIDAS, NO ES VÁLIDO COMO COMPROBANTE DE PAGO *";
            sb+="{br}{br}";
            sb+="{reset}Cliente:{br}";
            sb+="{reset}" + nombreCliente;
            sb+="{br}";
            sb+="{reset}" + domicilioCliente;
            sb+="{br}";
            sb+="{reset}" + coloniaCliente;
            sb+="{br}";
            sb+="{reset}" + telefonoCliente;
            sb+="{br}";

            if (nombreAval.isEmpty()){
                sb+="{reset}Aval:{br}";
                sb+="{reset}" + nombreAval;
                sb+="{br}";
                sb+="{reset}" + direccionAval;
                sb+="{br}";
                sb+="{reset}" + coloniaAval;
                sb+="{br}";
            }
            // Math.round(a*100)/100;
            sb+="{reset}Monto Vencido: $" + Math.round(Double.parseDouble(montoVencido));
            sb+="{br}";
            sb+="{reset}Moratorios: $" + Math.round(Double.parseDouble(moratorios));
            sb+="{br}";
            sb+="{reset}Gasto de Cobranza: $" + Math.round(Double.parseDouble(gastoCobranza));

            if(día==1 || día==2 || día == 3 || día == 16 || día == 17 || día == 18){
                sb+="{br}";
                sb+="{reset}Siguiente letra(PM): $" + Math.round(Double.parseDouble(PM));
            }

            total = Math.round(Double.parseDouble(montoVencido)) + Math.round(Double.parseDouble(moratorios)) + Math.round(Double.parseDouble(gastoCobranza)) + Math.round(Double.parseDouble(PM));
            sb+="{br}";
            sb+="{reset}{b}Total: $" + Math.round(total);
            sb+="{br}{br}";

            if(titulo != ""){
                sb+="{br}{b}";
                sb+="{reset}" + titulo;
            }

            if(mensaje1 != ""){
                sb+="{br}";
                sb+="{reset}" + mensaje1;
            }

            if(mensaje2 != ""){
                sb+="{br}";
                sb+="{reset}" + mensaje2;
            }

            if(address != ""){
                sb+="{br}";
                sb+="{reset}" + address;
            }

            sb+="{br}{br}";
            sb+="{reset}{b}Asesor:" + nombreCobrador;
            sb+="{br}";
            sb+="{reset}{b}Teléfono:" + telefonoCobrador;

            sb+="{br}";
            sb+="{reset}{b}Fecha de Impresión:" + fecha;

            //MENSAJE DE LA TIENDA ONLINE - 08-11-2018
            sb+="{br}";
            sb+="{reset}{b}¡AHORA YA PUEDES COMPRAR Y PAGAR EN LÍNEA!";
            sb+="{br}";
            sb+="{reset}{b}VISITA WWW.TIENDASQUALITY.COM.MX";
            sb+="{br}{br}{br}";


        }catch(Exception e){
            sb="Error Impresión Aviso "+ e.toString();
        }
        cursorCobrador.close();

        return sb;
    }

    //PARA IMPRIMIR EL AVISO DEL CLIENTE - JAPP - 08-02-2016 - FIN

    public String ImprimeTickect( String custid,String tipo, String Folio, String sql_,String rutaBD_Cobrador,String rutaBDCobradores,String rutaBD_USRCOB){

        String sb="";

        if (tipo.equals("NOTA")){
            /*******************************************
             * CONSULTA PARA OBTENER LOS DATOS DEL ABONO
             ******************************************/
            SQLiteDatabase db = null;
            String Cliente = "";
            String Direccion="";
            String FechaAbo="";
            double vencido=0;
            double Abono = 0;
            double A_Mora = 0;
            double mora =0;
            double AbonoCuenta =0;
            double SaldoT =0;
            double SaldoT2=0;

            String apoyoCovid19="";
            double PM=0;

            db = SQLiteDatabase.openDatabase(rutaBD_Cobrador,null,SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            strSql="select Custid,nombre, direccion,colonia	from customer where trim(custid) = trim('" + custid + "')";
            Cursor c = db.rawQuery(strSql,null);

            if (c!=null)
            {
                while (c.moveToNext()){
                    Cliente= c.getString(1);
                    Direccion=c.getString(2) + "," + c.getString(3);
                }
            }
            c.close();

            strSql="SELECT Vencido,SaldoT,Moratorios FROM dscob where trim(custid) = trim('" + custid + "')";

            Cursor c3=db.rawQuery(strSql, null);
            if(c3!=null)
            {
                c3.moveToFirst();

                vencido=Double.parseDouble(c3.getString(0));
                SaldoT=Double.parseDouble(c3.getString(1));
                mora=Double.parseDouble(c3.getString(2));
            }

            String strSql2="Select Monto,FechaAbo,A_mora,Folio,apoyoCovid19 from Abonos  where trim(custid)= trim('"+custid+"') " ;

            if(tipo.equals("REIMPRIME")){
                strSql2= strSql2 + " and folio= trim('" + Folio + "')";
            }else{
                strSql2= strSql2 + " order by fechaabo desc limit 1";
            }

            SQLiteDatabase db2= null;
            db2 = SQLiteDatabase.openDatabase(rutaBDCobradores,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            Cursor c2 = db2.rawQuery(strSql2, null);

            if(c2!=null)
            {
                while (c2.moveToNext()){
                    FechaAbo=c2.getString(1);
                    Abono=Double.parseDouble(c2.getString(0));
                    Folio=c2.getString(3);
                    A_Mora=Double.parseDouble(c2.getString(2));
                    apoyoCovid19=c2.getString(4);
                }
            }
            db2.close();

            strSql2="Select PM from letras  where trim(custid)= trim('"+custid+"') LIMIT 1" ;
            c2 = db.rawQuery(strSql2, null);

            if(c2!=null)
            {
                while (c2.moveToNext()){
                    PM=Double.parseDouble(c2.getString(0));
                }
            }
            db.close();

            SaldoT=SaldoT+mora;
            vencido=vencido+mora;

            vencido=Math.rint(vencido);
            SaldoT=Math.rint(SaldoT);
            AbonoCuenta=Math.rint(Abono);

            if (A_Mora>=0){//checa si abono o activo el mora
                //para checar si el modif mora es mayor al monto pagado
                if (Abono < A_Mora ){
                    AbonoCuenta =AbonoCuenta -0;
                }else{
                    AbonoCuenta=AbonoCuenta-A_Mora;
                }
                mora=mora-A_Mora;
            }

            if(Abono>=vencido){
                vencido=0;
                SaldoT=SaldoT-Abono;
            }

            if (Abono<vencido){
                vencido=vencido-Abono;
                SaldoT=SaldoT-Abono;
            }

            vencido=Redondear(vencido,0);
            SaldoT=Redondear(SaldoT,0);
            SaldoT2=Redondear(SaldoT2,0);
            mora=Redondear(mora,0);


            /************************************
             * FIN DE CONSULTA DE DATOS DE ABONO
             * ***********************************/
            try{
                sb="{reset}{center}{s}ELECTRONICOS Y MAS, S.A. DE C.V.{br}";
                sb+="{reset}{center}{s}EMA960527GY3{br}";
                sb+="{reset}{center}{s}AV. HEROES No. 71 2DO. PISO LOCAL B{br}";
                sb+="{reset}{center}{s}COL. CENTRO, CP 77000, CHETUMAL, Q.ROO{br}";

                sb+="{reset}{center}{b}QUALITY STORES/COMPUTERAMA";
                sb+="{br}";
                sb+="{reset}{center}{i}Recibo: "+ Folio;
                sb+="{br}";
                sb+="{reset}{center}{i}Fecha: "+ FechaAbo;
                sb+="{br}";
                sb+="{reset}{center}{u}Agente domiciliario: "+clase.Lectura(rutaBD_USRCOB);
                sb+="{reset}{i}"+custid+"{br}";
                sb+="{br}";
                sb+="{reset}{b}" + Cliente;
                sb+="{br}";
                sb+="{reset}{i}"+custid+"{br}";
                sb+="{reset}{i}"+Direccion+"{br}";
                sb+="{br}";

                if(apoyoCovid19.equals("Si")){
                    sb+="{br}";
                    sb+="{reset}{center}{b}¡Gracias por tu abono!{br}";
                    sb+="{reset}{center}Tu cuenta está al día.{br}";

                    int mes,dia;

                    Date d = new Date();
                    Calendar cal = new GregorianCalendar();
                    cal.setTime(d);

                    mes= cal.get(Calendar.MONTH);
                    //SE LE SUMA UNO PORQUE EMPIEZA EN 0 (ENERO) Y TERMINA EN 11 (DICIEMBRE)
                    mes+=1;

                    dia=cal.get(Calendar.DAY_OF_MONTH);

                    String nombreMes="";

                    switch(mes){
                        case 1:
                            nombreMes="enero";
                            break;
                        case 2:
                            nombreMes="febrero";
                            break;
                        case 3:
                            nombreMes="marzo";
                            break;
                        case 4:
                            nombreMes="abril";
                            break;
                        case 5:
                            nombreMes="mayo";
                            break;
                        case 6:
                            nombreMes="junio";
                            break;
                        case 7:
                            nombreMes="julio";
                            break;
                        case 8:
                            nombreMes="agosto";
                            break;
                        case 9:
                            nombreMes="septiembre";
                            break;
                        case 10:
                            nombreMes="octubre";
                            break;
                        case 11:
                            nombreMes="noviembre";
                            break;
                        case 12:
                            nombreMes="diciembre";
                            break;
                    }

                    if(dia<19)
                        sb+="{reset}{center}Próximo pago: 15 de "+ nombreMes + "{br}";
                    else
                        sb+="{reset}{center}Próximo pago: 30 de " + nombreMes + "{br}";

                    sb+="{reset}{center}por $" + PM;
                    sb+="{br}";
                    sb+="{br}";
                    sb+="{reset}{center}{b}#BorrónYCuentaNueva{br}{br}";
                    /*sb+="{reset}{center}{s}*Libera tu línea de crédito con{br}";
                    sb+="{reset}{center}{s}tu pago del 30 de julio{br}{br}";*/
                }

                sb+="{reset}{center}{s}_____________________________________";
                sb+="{br}";

                sb+="{reset}{b}Pagado:";
                sb+="{reset}{right}{b}$"+ Abono +" {br}";

                sb+="{reset}{center}{s}______________________________________";
                sb+="{br}";

                sb+="{reset}{b}Saldo vencido:";
                sb+="{reset}{right}{b}$"+vencido+" {br}";

                sb+="{reset}{i}Saldo total:";
                sb+="{reset}{right}{i}$"+SaldoT+" {br}";

                sb+="{reset}{center}{s}______________________________________";

                sb+="{br}";
                sb+="{br}";
                sb+="{br}";
                sb+="{br}";
                sb+="{reset}{center}{s}______________________________________";
                sb+="{br}";
                sb+="{reset}{center}{i}COBRADOR: "+ clase.LecturaNombreCob(rutaBD_USRCOB);
                sb+="{br}";
                sb+="{reset}{center}{s}Para dudas o aclaraciones llame gratis al:{br}";
                sb+="{reset}{center}{s}9982963060{br}";
                sb+="{reset}{center}{s}WhatsApp: 9831651446{br}";
                sb+="{reset}{center}{s}Consulte su estado de cuenta en línea:{br}";
                sb+="{reset}{center}{s}www.qualitystores.com.mx{br}";
                sb+="{reset}{center}{s}www.computerama.com.mx{br}";
                //MENSAJE DE LA TIENDA ONLINE - 08-11-2018
                sb+="{br}";
                sb+="{reset}{b}¡AHORA YA PUEDES COMPRAR Y PAGAR EN LÍNEA!";
                sb+="{br}";
                sb+="{reset}{b}VISITA WWW.TIENDASQUALITY.COM.MX";
                sb+="{br}{br}{br}";
            }catch(Exception e){
                sb="Exception1 datos de impresion - ImprimeTickect" + e.toString() ;
            }
        }

        if (tipo.equals("EDOCTA")){
            /*
             * Declaración de variables
             */
            String Fecha ="";
            String cad ="";
            /*
             * Extraigo los datos del cliente
             * */
            DatosCliente.DatosPersonalesCliente(custid,rutaBD_Cobrador);
            DatosCliente.DatosCuentaCliente(custid,"","",rutaBD_Cobrador,rutaBDCobradores);
            /*
             * GENERO EN EL ENCABEZADO
             * */

            Fecha=FechaImpresion(rutaBD_Cobrador);

            if (DatosCliente.Error_==true){
                sb="Error al extraer los datos del cliente";
            }else{
                try{
                    /*
                     * Cuerpo
                     * */

                    String Cob = clase.Lectura(rutaBD_USRCOB);
                    cad=ImprimeEncabezado();
                    sb+=cad;
                    sb+="{reset}{center}{i}"+ Cob +"{br}";
                    sb+="{reset}{center}{i}Estado de Cuenta(Móvil){br}";
                    sb+="{reset}{center}" + Fecha;
                    sb+="{br}";
                    sb+="{reset}{center}{s}_____________________________________";
                    sb+="{br}";
                    sb+="{reset}{b}"+ DatosCliente.Nombre ;
                    sb+="{br}";
                    sb+="{reset}{i}"+custid;

                    sb+="{br}";
                    sb+="{reset}{center}{s}_____________________________________";
                    sb+="{br}";
                    sb+="{reset}{b} **ES UN ESTADO DE CUENTA, NO ES VÁLIDO COMO COMPROBANTE DE PAGO ***";
                    sb+="{br}";
                    sb+="{reset}{b}Saldo Vencido: $"+ Double.toString( DatosCliente.SaldoV);
                    sb+="{reset}{right}{b}"+  " {br}";
                    sb+="{reset}{b}Moratorio: $"+ Double.toString( DatosCliente.Mora);
                    sb+="{reset}{right}{b}"+  " {br}";
                    sb+="{reset}{b}Requerido: $"+ Double.toString( DatosCliente.PagoReq);
                    sb+="{reset}{right}{b}"+  " {br}";

                    sb+="{reset}{i}Letras Vencidas: " + Integer.toString( DatosCliente.LetrasV);
                    sb+="{reset}{right}{i}"+" {br}";
                    sb+="{reset}{center}{s}______________________________________";
                    sb+="{br}";

                    sb+="{reset}{b}PM:";
                    sb+="{reset}{right}{b}$ "+ Double.toString( DatosCliente.PM) +" {br}";

                    sb+="{reset}{b}Saldo Total:";
                    sb+="{reset}{right}{b}$ "+ Double.toString( DatosCliente.SaldoT+DatosCliente.Mora) +" {br}";

                    //MENSAJE DE LA TIENDA ONLINE - 08-11-2018
                    sb+="{br}";
                    sb+="{reset}{b}¡AHORA YA PUEDES COMPRAR Y PAGAR EN LÍNEA!";
                    sb+="{br}";
                    sb+="{reset}{b}VISITA WWW.TIENDASQUALITY.COM.MX";
                    sb+="{br}{br}{br}";

                    sb+="{reset}{center}{s}______________________________________";
                    sb+="{br}";
                    sb+="{reset}{b} **ES UN ESTADO DE CUENTA, NO ES VÁLIDO COMO COMPROBANTE DE PAGO ***";
                    /*
                     * Pie
                     */
                    cad=ImprimePie();
                    sb+=cad;
                }catch(Exception e){
                    sb+="Error Impresion 2.1 "+ e.toString();
                }
            }
        }

        if(tipo.equals("REIMPRIME")){
            String cad=ImprimeEncabezado();
            MiClase_DatosCliente DatosCliente = new  MiClase_DatosCliente();

            DatosCliente.DatosPersonalesCliente(custid,rutaBD_Cobrador);
            DatosCliente.DatosCuentaCliente(custid,"","",rutaBD_Cobrador,rutaBDCobradores);

            /*
             * Obtengo los datos del abono
             * */
            DatosCliente.reimpresion(Folio, custid,rutaBD_Cobrador,rutaBDCobradores);

            sb+=cad;
            sb+="{br}{br}";
            sb+="{reset}{center}{i}REIMPRESIÓN";
            sb+="{br}{br}";
            sb+="{reset}{center}{i}Recibo: "+ Folio;
            sb+="{br}";
            sb+="{reset}{center}{i}Fecha: "+ DatosCliente.FechaAbo ;
            sb+="{br}";
            sb+="{reset}{center}{u}Agente domiciliario: "+clase.Lectura(rutaBD_USRCOB);

            sb+="{br}";
            sb+="{reset}{b}" + DatosCliente.Nombre;
            sb+="{br}";
            sb+="{reset}{i}"+custid+"{br}";

            sb+="{reset}{i}"+DatosCliente.Direccion+"{br}";
            sb+="{br}";
            sb+="{reset}{center}{s}_____________________________________";
            sb+="{br}";

            sb+="{reset}{b}Pagado:";
            sb+="{reset}{right}{b}$"+ DatosCliente.r_Abono +" {br}";

            sb+="{reset}{center}{s}______________________________________";
            sb+="{br}";

            sb+="{reset}{b}Saldo vencido:";
            sb+="{reset}{right}{b}$"+DatosCliente.SaldoV+" {br}";

            sb+="{reset}{i}Saldo total:";
            sb+="{reset}{right}{i}$"+DatosCliente.SaldoT+" {br}";

            sb+="{reset}{center}{s}______________________________________";

            sb+="{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="{br}";
            sb+="{reset}{center}{s}______________________________________";
            sb+="{br}";
            sb+="{reset}{center}{i}COBRADOR: "+ clase.LecturaNombreCob(rutaBD_USRCOB);
            sb+="{br}";
            sb+="{reset}{center}{s}Para dudas o aclaraciones llame gratis al:{br}";
            sb+="{reset}{center}{s}9982963060{br}";
            sb+="{reset}{center}{s}WhatsApp: 9831651446{br}";
            sb+="{reset}{center}{s}Consulte su estado de cuenta en línea:{br}";
            sb+="{reset}{center}{s}www.qualitystores.com.mx{br}";
            sb+="{reset}{center}{s}www.computerama.com.mx{br}";
            //MENSAJE DE LA TIENDA ONLINE - 08-11-2018
            sb+="{br}";
            sb+="{reset}{b}¡AHORA YA PUEDES COMPRAR Y PAGAR EN LÍNEA!";
            sb+="{br}";
            sb+="{reset}{b}VISITA WWW.TIENDASQUALITY.COM.MX";
            sb+="{br}{br}{br}";

        }

        if(tipo.equals("CORTE")){
            try{
                String Cob = clase.Lectura(rutaBD_USRCOB);
                String cad=ImprimeEncabezado();
                String Fecha=FechaImpresion(rutaBD_Cobrador);
                Double Total=0.0;
                /*
                 * Impresión de encabezado
                 * */
                sb+=cad;
                sb+="{reset}{center}{i}"+ Cob +"{br}";
                sb+="{reset}{center}{i}Corte de Caja(Movil){br}";
                sb+="{reset}{center}" + Fecha;
                sb+="{br}";
                sb+="{reset}{center}{s}_____________________________________";
                sb+="{br}";
                sb+="{br}";

                Cursor c2 = EjecutaSQL(sql_,MiClase.TypeBase.COBRADORES,MiClase.TypeSQL.SELECT,rutaBDCobradores);
                if(c2!=null)
                {
                    while (c2.moveToNext()){

                        sb+="{reset}{center}{s}______________________________________";
                        sb+="{br}";
                        sb+="{reset}{center}{b}"+c2.getString(3);

                        sb+="{br}";
                        sb+="{reset}{center}{b}"+c2.getString(0);

                        sb+="{br}";
                        sb+="{reset}{center}{b}"+c2.getString(1);
                        sb+="{br}";

                        Total+=Double.valueOf(c2.getString(4));;
                    }

                }

                sb+="{br}";
                sb+="{reset}{center}{s}______________________________________";

                sb+="{br}";
                sb+="{reset}{center}{b}Monto Total "+ Double.toString(Total);
                sb+="{br}";

                sb+="{br}";
                sb+="{br}";
                sb+="{br}";
                sb+="{br}";
                sb+="{reset}{center}{s}______________________________________";
                sb+="{br}";
                sb+="{reset}{center}{i}COBRADOR: "+clase.LecturaNombreCob(rutaBD_USRCOB) ;
                sb+="{br}";
                sb+="{br}";
                sb+="{br}";
                sb+="{br}";
                sb+="{br}";
                sb+="{reset}{center}{s}______________________________________";
                sb+="{br}";
                sb+="{reset}{center}{i}JEFE DE CARTERA: ";
                sb+="{br}";
                /*
                 * Pie
                 */
                cad=ImprimePie();
                sb+=cad;
            }catch(Exception e){
                sb+="Error print corte 1.0 "+ e.toString() + "sql = "+sql_;
            }
        }
        return sb;
    }

    /*
     * GENERO ENCABEZADO DE IMPRESION
     * */
    public String ImprimeEncabezado(){

        String sb="";
        sb="{reset}{center}{s}ELECTRONICOS Y MAS, S.A. DE C.V.{br}";
        sb+="{reset}{center}{s}EMA960527GY3{br}";
        sb+="{reset}{center}{s}AV. HEROES No. 71 2DO. PISO LOCAL B{br}";
        sb+="{reset}{center}{s}COL. CENTRO, CP 77000, CHETUMAL, Q.ROO{br}";
        sb+="{reset}{center}{b}QUALITY STORES/COMPUTERAMA";
        sb+="{br}";

        return sb;
    }

    public String ImprimePie(){
        String sb ="{reset}{center}{s}______________________________________";
        sb+="{br}";

        sb+="{reset}{center}{b}Actualice su cuenta para evitar ";
        sb+="{reset}{center}{b}que sigan aumentando sus {br}";
        sb+="{reset}{center}{b}intereses moratorios{br}";

        sb+="{reset}{center}{s}______________________________________";

        sb+="{br}";
        sb+="{reset}{center}{s}Para dudas o aclaraciones llame gratis al:{br}";
        sb+="{reset}{center}{s}9982963060{br}";
        sb+="{reset}{center}{s}WhatsApp: 9831651446{br}";
        sb+="{reset}{center}{s}Consulte su estado de cuenta en linea:{br}";
        sb+="{reset}{center}{s}www.qualitystores.com.mx{br}";
        sb+="{reset}{center}{s}www.computerama.com.mx{br}";
        sb+="{reset}{center}{s}NOTA: Es posible que este reporte{br}";
        sb+="{reset}{center}{s} no cuente con abonos de {br}";
        sb+="{reset}{center}{s} las ultimas 24 hrs{br}";
        //MENSAJE DE LA TIENDA ONLINE - 08-11-2018
        sb+="{br}";
        sb+="{reset}{b}¡AHORA YA PUEDES COMPRAR Y PAGAR EN LÍNEA!";
        sb+="{br}";
        sb+="{reset}{b}VISITA WWW.TIENDASQUALITY.COM.MX";
        sb+="{br}{br}{br}";

        return sb;
    }

    public String FechaImpresion(String ruta){
        /*
         * CONSULTA PARA OBTENER FECHA
         */

        String sql="SELECT strftime('%d-%m-%Y','now','localtime')  Fecha";
        Cursor cFecha = EjecutaSQL(sql,MiClase.TypeBase.Id_COBRADOR,MiClase.TypeSQL.SELECT,ruta);

        if (cFecha!=null){
            cFecha.moveToFirst();
            return  cFecha.getString(0);
        }
        return "";
    }

    public Cursor EjecutaSQL(String sql, MiClase.TypeBase Conec, MiClase.TypeSQL tipoSQL,String ruta){

        try{

            SQLiteDatabase db= null;

            if (Conec== MiClase.TypeBase.COBRADORES)
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);

            if (Conec== MiClase.TypeBase.Id_COBRADOR){
                /*
                 * Lectura del archivo cobrador
                 * */

                /******************************/
                db = SQLiteDatabase.openDatabase(ruta,null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
            }
            if(tipoSQL== MiClase.TypeSQL.SELECT){
                return  db.rawQuery(sql, null);
            }else{
                db.execSQL(sql);
            }
        }catch(Exception e){
        }
        return  null;
    }

    public String ImprimePagare(String idVenta){
        return "";
    }
}