package com.cobranza.impresora;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cobranza.movil.Listacliente2;
import com.cobranza.movil.R;
import com.cobranza.movil.inicio;
import com.datecs.api.card.FinancialCard;
import com.datecs.api.printer.Printer;
import com.datecs.api.printer.PrinterInformation;
import com.datecs.api.printer.ProtocolAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import Clases.Clase_DatosImpresion;
import Clases.MiClase;

public class PrinterActivity extends Activity {

    String tipo;
    String custid;
    String strSql, sqlx;
    String Cobrador = "";
    String NombreCob = "";
    String Folio = "";
    String dia = "";
    String mes = "";
    String anio = "";
    String folioDecomiso;

    public MiClase clase = new MiClase();
    // Debug
    private static final String LOG_TAG = "PrinterSample";
    private static final boolean DEBUG = true;

    // Request to get the bluetooth device
    private static final int REQUEST_GET_DEVICE = 0;

    // Request to get the bluetooth device
    private static final int DEFAULT_NETWORK_PORT = 9100;

    // A handler to display notifications
    private final Handler mHandler = new Handler();

    // The listener for all printer events
    private final ProtocolAdapter.ChannelListener mChannelListener = new ProtocolAdapter.ChannelListener() {
        public void onReadEncryptedCard() {
            toast(getString(R.string.msg_read_encrypted_card));
        }

        public void onReadCard() {
            readMagstripe();
        }

        public void onReadBarcode() {
            readBarcode(0);
        }


        public void onPaperReady(boolean state) {
            if (state) {
                toast(getString(R.string.msg_paper_ready));
            } else {
                toast(getString(R.string.msg_no_paper));
            }
        }

        public void onOverHeated(boolean state) {
            if (state) {
                toast(getString(R.string.msg_overheated));
            }
        }


        public void onLowBattery(boolean state) {
            if (state) {
                toast(getString(R.string.msg_low_battery));
            }
        }

        @Override
        public void onLowBattery() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onNoPaper() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onOverHeated() {
            // TODO Auto-generated method stub

        }
    };


    // Member variables
    private Printer mPrinter;
    private PrinterInformation mPrinterInfo;
    private BluetoothSocket mBluetoothSocket;
    private PrinterServer mPrinterServer;
    private Socket mPrinterSocket;
    private boolean mRestart;

    /*
     * instacia de la clase de datos de impresion
     * */
    Clase_DatosImpresion clase_Imprime = new Clase_DatosImpresion();

    /*
     * id de la venta a imprimir
     * */

    public String idventa;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.printer);
        Log.i("Entra onC", "PrinterActivity");

        try{
            tipo = getIntent().getExtras().getString("TIPO");
            Log.i("TIPO*******", tipo);
            selecciona_tipo(tipo);

        }catch(Exception e){
            Log.i("imprimeDatos", e.toString());
        }

        findViewById(R.id.print_self_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printSelfTest();
            }
        });

        findViewById(R.id.print_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("print_text", "********print************" + tipo);
                printImage();
                printText();
            }
        });

        findViewById(R.id.print_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printImage();
            }
        });

        findViewById(R.id.print_page).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printImage();
                printPagare();
            }
        });

        findViewById(R.id.print_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printBarcode();
            }
        });

        findViewById(R.id.read_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readMagstripe();
            }
        });

        findViewById(R.id.read_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Read barcode waiting 10 seconds
                readBarcode(10);
            }
        });

        mRestart = true;
        waitForConnection();
    }

    public void selecciona_tipo(String tipo){
        if (tipo.equals("EDOCTA")){
            custid=getIntent().getExtras().getString("custid");

            String rutaBD_USRCOB=PrinterActivity.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";
            Cobrador = clase.Lectura(rutaBD_USRCOB);
            NombreCob= clase.LecturaNombreCob(rutaBD_USRCOB);
            Folio=getIntent().getExtras().getString("folio");
        }
        if (tipo.equals("CORTE")){
            sqlx=getIntent().getExtras().getString("sql");
            Log.i("imprimeDatos2", sqlx);
        }
        if (tipo.equals("REIMPRIME")){
            custid=getIntent().getExtras().getString("custid");
            sqlx=getIntent().getExtras().getString("sql");
            Folio=getIntent().getExtras().getString("folio");
            Log.i("imprimeDatos2", sqlx);
        }
        if (tipo.equals("NOTA")){
            custid=getIntent().getExtras().getString("custid");
            Folio=getIntent().getExtras().getString("folio");
            Log.i("imprimeDatos2", sqlx);
        }
        //SI ES UN AVISO - JAPP - 09-02-2016 - INICIO
        if (tipo.equals("AVISO")){
            custid=getIntent().getExtras().getString("custid");
            Log.i("Imprime Aviso",custid);
        }
        //SI ES UN AVISO - JAPP - 09-02-2016 - FIN

        //SI ES UN DECOMISO - JAPP - 08-03-2016 - INICIO
        if (tipo.equals("DECOMISO")){
            folioDecomiso=getIntent().getExtras().getString("FolioDecomiso");
            Log.i("Imprime Decomiso: ",folioDecomiso);
        }
        //SI ES UN DECOMISO - JAPP - 08-03-2016 - FIN
        Log.i("imprimeDatos", tipo);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRestart = false;
        close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_GET_DEVICE) {
            if (resultCode == device_list.RESULT_OK) {
                String address = data.getStringExtra(device_list.EXTRA_DEVICE_ADDRESS);
                //address = "192.168.11.136:9100";
                if (BluetoothAdapter.checkBluetoothAddress(address)) {
                    establishBluetoothConnection(address);
                } else {
                    establishNetworkConnection(address);
                }
            } else if (resultCode == RESULT_CANCELED) {

            } else {
                finish();
            }
        }
    }

    private void toast(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void dialog(final int iconResId, final String title, final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(PrinterActivity.this);
                builder.setIcon(iconResId);
                builder.setTitle(title);
                builder.setMessage(msg);

                AlertDialog dlg = builder.create();
                dlg.show();
            }
        });
    }

    private void error(final String text, boolean restart) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });

        if (restart) {
            waitForConnection();
        }
    }

    private void doJob(final Runnable job, final int resId) {
        // Start the job from main thread
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Progress dialog title
                String title = getString(R.string.title_please_wait);
                // Progress dialog message
                String text = getString(resId);
                // Progress dialog available due job execution
                final ProgressDialog dialog = ProgressDialog.show(PrinterActivity.this, title, text);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            job.run();
                        } finally {
                            dialog.dismiss();
                        }
                    }
                }).start();
            }
        });
    }

    protected void initPrinter(InputStream inputStream, OutputStream outputStream) throws IOException {
        //InputStream in = inputStream;
        InputStream in = new PatchedInputStream(inputStream);
        OutputStream out = outputStream;
        ProtocolAdapter protocolAdapter = new ProtocolAdapter(in, out);

        if (protocolAdapter.isProtocolEnabled()) {
            final ProtocolAdapter.Channel channel = protocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            channel.setListener(mChannelListener);
            // Create new event pulling thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            mPrinter = new Printer(channel.getInputStream(), channel.getOutputStream());
        } else {
            mPrinter = new Printer(in, out);
        }

        mPrinterInfo = mPrinter.getInformation();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ((ImageView)findViewById(R.id.icon)).setImageResource(R.mipmap.qs);
                ((TextView)findViewById(R.id.name)).setText(mPrinterInfo.getName());
            }
        });
    }

    public synchronized void waitForConnection() {
        Log.i("Entra--", "------waitForConnection");
        close();

        // Show dialog to select a Bluetooth device.
        startActivityForResult(new Intent(this, device_list.class), REQUEST_GET_DEVICE);

        // Start server to listen for network connection.
        try {
            mPrinterServer = new PrinterServer(new PrinterServerListener() {
                @Override
                public void onConnect(Socket socket) {
                    if (DEBUG) Log.d(LOG_TAG, "Accept connection from " + socket.getRemoteSocketAddress().toString());

                    // Close Bluetooth selection dialog
                    finishActivity(REQUEST_GET_DEVICE);

                    mPrinterSocket = socket;
                    try {
                        InputStream in = socket.getInputStream();
                        OutputStream out = socket.getOutputStream();
                        initPrinter(in, out);
                    } catch (IOException e) {
                        error(getString(R.string.msg_failed_to_init) + ". " + e.getMessage(), mRestart);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void establishBluetoothConnection(final String address) {
        closePrinterServer();

        doJob(new Runnable() {
            @Override
            public void run() {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice device = adapter.getRemoteDevice(address);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                InputStream in = null;
                OutputStream out = null;

                adapter.cancelDiscovery();

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Connect to " + device.getName());
                    mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                    mBluetoothSocket.connect();
                    in = mBluetoothSocket.getInputStream();
                    out = mBluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
                }

                try {
                    initPrinter(in, out);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_init) + ". " +  e.getMessage(), mRestart);
                    return;
                }
            }
        }, R.string.msg_connecting);
    }

    private void establishNetworkConnection(final String address) {
        closePrinterServer();

        doJob(new Runnable() {
            @Override
            public void run() {
                Socket s = null;
                try {
                    String[] url = address.split(":");
                    int port = DEFAULT_NETWORK_PORT;

                    try {
                        if (url.length > 1)  {
                            port = Integer.parseInt(url[1]);
                        }
                    } catch (NumberFormatException e) { }

                    s = new Socket(url[0], port);
                    s.setKeepAlive(true);
                    s.setTcpNoDelay(true);
                } catch (UnknownHostException e) {
                    error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
                }

                InputStream in = null;
                OutputStream out = null;

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Connect to " + address);
                    mPrinterSocket = s;
                    in = mPrinterSocket.getInputStream();
                    out = mPrinterSocket.getOutputStream();
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_connect) + ". " +  e.getMessage(), mRestart);
                    return;
                }

                try {
                    initPrinter(in, out);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_init) + ". " +  e.getMessage(), mRestart);
                    return;
                }
            }
        }, R.string.msg_connecting);
    }

    private synchronized void closeBlutoothConnection() {
        // Close Bluetooth connection
        BluetoothSocket s = mBluetoothSocket;
        mBluetoothSocket = null;
        if (s != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Blutooth socket");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closeNetworkConnection() {
        // Close network connection
        Socket s = mPrinterSocket;
        mPrinterSocket = null;
        if (s != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Network socket");
            try {
                s.shutdownInput();
                s.shutdownOutput();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void closePrinterServer() {
        closeNetworkConnection();

        // Close network server
        PrinterServer ps = mPrinterServer;
        mPrinterServer = null;
        if (ps != null) {
            if (DEBUG) Log.d(LOG_TAG, "Close Network server");
            try {
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void close() {
        closeBlutoothConnection();
        closeNetworkConnection();
        closePrinterServer();
    }

    private void printSelfTest() {
        doJob(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DEBUG) Log.d(LOG_TAG, "Print Self Test");
                    mPrinter.printSelfTest();
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_print_self_test) + ". " + e.getMessage(), mRestart);
                }
            }
        }, R.string.msg_printing_self_test);
    }

    //imprimir el resultado

    private void printText() {
        doJob(new Runnable() {
            @Override
            public void run() {
                StringBuffer sb = new StringBuffer();
                //SI ES UN AVISO - JAPP - 09-02-2016

                String rutaBDCobradores=PrinterActivity.this.getApplicationContext().getDatabasePath("cobradores").toString();
                String rutaBD_USRCOB=PrinterActivity.this.getApplicationContext().getFilesDir().toString() + "/BD_USRCOB.zip";

                /*Lectura del archivo CONFIG*/
                MiClase clase = new MiClase();
                String Cobrador=clase.Lectura(rutaBD_USRCOB);
                /*****************************/

                String rutaBD_Cobrador=PrinterActivity.this.getApplicationContext().getFilesDir().toString() + "/" + Cobrador + ".zip";

                if (tipo.equals("AVISO"))
                    sb.append(clase_Imprime.imprimirAviso(custid,rutaBD_Cobrador,rutaBD_USRCOB));
                if (tipo.equals("DECOMISO"))
                    sb.append(clase_Imprime.imprimirDecomiso(folioDecomiso,rutaBDCobradores,rutaBD_Cobrador));
                else
                    sb.append(clase_Imprime.ImprimeTickect(custid, tipo,Folio,sqlx,rutaBD_Cobrador,rutaBDCobradores,rutaBD_USRCOB));

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Print Text");
                    mPrinter.reset();
                    mPrinter.printTaggedText(sb.toString(), "ISO-8859-1");
                    mPrinter.feedPaper(110);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_print_text) + ". " + e.getMessage(), mRestart);
                }
            }
        }, R.string.msg_printing_text);
    }

    private void printPagare() {
        doJob(new Runnable() {
            @Override
            public void run() {

                StringBuffer sb = new StringBuffer();

                sb.append(clase_Imprime.ImprimePagare(idventa));

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Print Text");
                    mPrinter.reset();
                    mPrinter.printTaggedText(sb.toString(), "ISO-8859-1");//JAPP - LATIN-1 PARA QUE IMPRIMA LOS ACENTOS Y LAS EÑES)
                    mPrinter.feedPaper(110);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_print_text) + ". " + e.getMessage(), mRestart);
                }
            }
        }, R.string.msg_printing_text);
    }

    private void printImage() {
        doJob(new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.qs_logo60);
                final int width = bitmap.getWidth();
                final int height = bitmap.getHeight();
                final int[] argb = new int[width * height];
                bitmap.getPixels(argb, 0, width, 0, 0, width, height);
                bitmap.recycle();

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Print Image");
                    mPrinter.reset();
                    mPrinter.printImage(argb, width, height, Printer.ALIGN_CENTER, true);
                    mPrinter.feedPaper(110);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_print_image) + ". " + e.getMessage(), mRestart);
                }
            }
        }, R.string.msg_printing_image);
    }

    private void printPage() {
        doJob(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DEBUG) Log.d(LOG_TAG, "Print Page");
                    mPrinter.reset();
                    mPrinter.selectPageMode();

                    mPrinter.setPageRegion(0, 0, 160, 320, Printer.PAGE_LEFT);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH I{br}");
                    mPrinter.drawPageRectangle(0, 0, 160, 32, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from left to right" +
                            ", feed to bottom. Starting point in left top corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.setPageRegion(160, 0, 160, 320, Printer.PAGE_TOP);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH II{br}");
                    mPrinter.drawPageRectangle(160 - 32, 0, 32, 320, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from top to bottom" +
                            ", feed to left. Starting point in right top corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.setPageRegion(160, 320, 160, 320, Printer.PAGE_RIGHT);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH III{br}");
                    mPrinter.drawPageRectangle(0, 320 - 32, 160, 32, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from right to left" +
                            ", feed to top. Starting point in right bottom corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.setPageRegion(0, 320, 160, 320, Printer.PAGE_BOTTOM);
                    mPrinter.setPageXY(0, 4);
                    mPrinter.printTaggedText("{reset}{center}{b}PARAGRAPH IV{br}");
                    mPrinter.drawPageRectangle(0, 0, 32, 320, Printer.FILL_INVERTED);
                    mPrinter.setPageXY(0, 34);
                    mPrinter.printTaggedText("{reset}Text printed from bottom to top" +
                            ", feed to right. Starting point in left bottom corner of the page.{br}");
                    mPrinter.drawPageFrame(0, 0, 160, 320, Printer.FILL_BLACK, 1);

                    mPrinter.printPage();
                    mPrinter.selectStandardMode();
                    mPrinter.feedPaper(110);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_print_page) + ". " + e.getMessage(), mRestart);
                }
            }
        }, R.string.msg_printing_page);
    }

    private void printBarcode() {
        doJob(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DEBUG) Log.d(LOG_TAG, "Print Barcode");
                    mPrinter.reset();

                    mPrinter.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_BELOW, 100);
                    mPrinter.printBarcode(Printer.BARCODE_EAN13, "123456789012");
                    mPrinter.feedPaper(38);

                    mPrinter.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_BOTH, 100);
                    mPrinter.printBarcode(Printer.BARCODE_CODE128, "ABCDEF123456");
                    mPrinter.feedPaper(38);

                    mPrinter.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_NONE, 100);
                    mPrinter.printBarcode(Printer.BARCODE_PDF417, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                    mPrinter.feedPaper(38);

                    mPrinter.setBarcode(Printer.ALIGN_CENTER, false, 2, Printer.HRI_NONE, 100);
                    mPrinter.printQRCode(4, 3, "http://www.datecs.bg");
                    mPrinter.feedPaper(38);

                    mPrinter.feedPaper(110);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_print_barcode) + ". " + e.getMessage(), mRestart);
                }
            }
        }, R.string.msg_printing_barcode);
    }

    private void readMagstripe() {
        doJob(new Runnable() {
            @Override
            public void run() {
                String[] tracks = null;
                FinancialCard card = null;

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Read Magstripe");
                    if (mPrinterInfo != null && mPrinterInfo.getName().startsWith("CMP-10")) {
                        // The printer CMP-10 can read only two tracks at once.
                        tracks = mPrinter.readCard(true, true, false, 15000);
                    } else {
                        tracks = mPrinter.readCard(true, true, true, 15000);
                    }
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_read_card) + ". " + e.getMessage(), mRestart);
                }

                if (tracks != null) {
                    StringBuffer msg = new StringBuffer();

                    if (tracks[0] == null && tracks[1] == null && tracks[2] == null) {
                        msg.append(getString(R.string.no_card_read));
                    } else {
                        if (tracks[0] != null) {
                            card = new FinancialCard(tracks[0]);
                        } else if (tracks[1] != null) {
                            card = new FinancialCard(tracks[1]);
                        }

                        if (card != null) {
                            msg.append(getString(R.string.card_no) + ": " + card.getNumber());
                            msg.append("\n");
                            msg.append(getString(R.string.holder) + ": " + card.getName());
                            msg.append("\n");
                            msg.append(getString(R.string.exp_date) + ": " + String.format("%02d/%02d",
                                    card.getExpiryMonth(), card.getExpiryYear()));
                            msg.append("\n");
                        }

                        if (tracks[0] != null) {
                            msg.append("\n");
                            msg.append(tracks[0]);

                        }
                        if (tracks[1] != null) {
                            msg.append("\n");
                            msg.append(tracks[1]);
                        }
                        if (tracks[2] != null) {
                            msg.append("\n");
                            msg.append(tracks[2]);
                        }
                    }

                    dialog(R.mipmap.card,
                            getString(R.string.card_info),
                            msg.toString());
                }
            }
        }, R.string.msg_reading_magstripe);
    }

    private void readBarcode(final int timeout) {
        doJob(new Runnable() {
            @Override
            public void run() {
                String barcode = null;

                try {
                    if (DEBUG) Log.d(LOG_TAG, "Read Barcode");
                    barcode = mPrinter.readBarcode(timeout);
                } catch (IOException e) {
                    error(getString(R.string.msg_failed_to_read_barcode) + ". " + e.getMessage(), mRestart);
                }

                if (barcode != null) {
                    dialog(R.mipmap.readbarcode, getString(R.string.barcode), barcode);
                }
            }
        }, R.string.msg_reading_barcode);
    }
}

