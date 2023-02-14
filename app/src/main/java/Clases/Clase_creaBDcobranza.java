package Clases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Clase_creaBDcobranza extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cobradores";
    private static final String TITLE = "Title";
    private static final int DATABASE_VERSION = 4;


    public Clase_creaBDcobranza(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

        Log.i("Clase_creaBDcobranza", "***************************Clase_creaBDcobranza********************");
        String Tablas ="";
        Tablas = "CREATE TABLE Abonos (Id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , " +
                "Cobrador TEXT check(typeof(Cobrador) = 'text') ," +
                "Monton FLOAT," +
                "PagoReq FLOAT," +
                "Fecha DATETIME DEFAULT CURRENT_DATE," +
                "Status TEXT DEFAULT 'PENDIENTE'," +
                "Long DOUBLE," +
                "Lati DOUBLE," +
                "CustId CHAR," +
                "Monto FLOAT, " +
                "apoyoCovid19 TEXT," +
                "Nombre CHAR," +
                "Folio CHAR," +
                "FechaAbo DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "A_Mora FLOAT DEFAULT -1," +
                "OpcConvenio CHAR," +
                "convenio FLOAT)";
        db.execSQL(Tablas);

        Tablas = "CREATE TABLE historial (CustID," +
                "UltimaAsig," +
                "UltAsigInd," +
                "UltPeriodoAsig," +
                "UltPerVenc," +
                "Historial," +
                "FechaUpd," +
                "Perspectiva," +
                "PerspectivaDet," +
                "ProcesoStatus," +
                "UserUpd," +
                "UltLetVenc," +
                "Saldo," +
                "TipoAviso," +
                "FechaSeg," +
                "Hora," +
                "Expediente," +
                "Juzgado," +
                "Actuario," +
                "FechaCrt," +
                "MontoAPagar," +
                "Tel," +
                "NumPagos," +
                "MontoPagos," +
                "status," +
                "folio," +
                "Longitude DOUBLE, " +
                "Latitude DOUBLE, " +
                "fechaEnvio DATETIME DEFAULT '1900-01-01 12:00:00', " +
                "idGestion)";
        db.execSQL(Tablas);

        Tablas = "CREATE TABLE dirAlterna(CustID," +
                "Nombre,"+
                "Addr1," +
                "Addr2," +
                "Fax," +
                "Phone,"+
                "FechaCrt DATETIME DEFAULT CURRENT_TIMESTAMP,"+
                "IdRegistro,"+
                "Cobrador,"+
                "Status)";
        db.execSQL(Tablas);

        Tablas = "CREATE TABLE decomisos (Id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , " +
                "Custid CHAR," +
                "ClaveArt CHAR," +
                "NoSerie CHAR," +
                "Embarque CHAR," +
                "FechaDecomiso DATETIME," +
                "Detalles CHAR," +
                "Art CHAR," +
                "FechaCompra," +
                "Cobrador TEXT check(typeof(Cobrador) = 'text') ," +
                "AutorizaDecomiso CHAR," +
                "FolioDecomiso CHAR," +
                "Status TEXT DEFAULT 'RECEPCION'," +
                "BodegaRecibe CHAR," +
                "tiendaDeCompra CHAR," +
                "Longitud DOUBLE, " +
                "Latitud DOUBLE, " +
                "EstadoEnvio TEXT DEFAULT 'PENDIENTE', " +
                "Ruta CHAR, " +
                "Funcionamiento CHAR, " +
                "Accesorios CHAR)";

        db.execSQL(Tablas);

        Tablas = "CREATE TABLE imagenDecomiso(idDeco CHAR,ruta CHAR, enviado CHAR)";
        db.execSQL(Tablas);

        Tablas = "CREATE TABLE Creacion (Id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE , Nuevo CHAR)";
        db.execSQL(Tablas);

        //PARA QUE NO MANDE EL MENSAJE DE INSTALACIÓN, PARA QUE SE MANDE QUE ESTÉ EN 0
        db.execSQL("Insert into Creacion(Nuevo) values('0') ");


        Tablas = "Create Table convenio  (Custid CHAR,"
                + " opcion CHAR,"
                + " Pago CHAR,"
                + " Mora CHAR,"
                + " quincenas CHAR,"
                + " SaldoT CHAR,"
                + " Letra CHAR,"
                + " fechaCRT DATETIME,"
                + " Usuario CHAR,"
                + "estatus CHAR )";

        db.execSQL(Tablas);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS Abonos");
        db.execSQL("DROP TABLE IF EXISTS decomisos");
        db.execSQL("DROP TABLE IF EXISTS historial");
        db.execSQL("DROP TABLE IF EXISTS convenio");
        onCreate(db);
    }
}