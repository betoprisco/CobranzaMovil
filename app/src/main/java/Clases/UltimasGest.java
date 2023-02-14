package Clases;

public class UltimasGest {

    private String idCliente;
    private String historialgest;
    private Double latitud;
    private Double longitud;

    public UltimasGest(String _idCliente, String _historialgest, Double _latitud,Double _longitud) {
        idCliente = _idCliente;
        historialgest = _historialgest;
        latitud=_latitud;
        longitud=_longitud;
    }

    public String obtenerCustid() {
        return idCliente;
    }

    public String obtenerhistorialgest() {
        return historialgest;
    }

    public Double obtenerLatitud() {
        return latitud;
    }
    public Double obtenerLongitud() {
        return longitud;
    }
}
