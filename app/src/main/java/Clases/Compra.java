package Clases;

public class Compra {
    private String idCliente;
    private String descripción;
    private String idArtículo;
    private String shipperid;
    private String fechaVenta;

    public Compra(String _idCliente, String _descripción, String _idArtículo, String _shipperid, String _fechaVenta) {
        idCliente = _idCliente;
        descripción = _descripción;
        idArtículo = _idArtículo;
        shipperid = _shipperid;
        fechaVenta = _fechaVenta;
    }

    public String obtenerCustid() {
        return idCliente;
    }

    public String obtenerDescripción() {
        return descripción;
    }

    public String obtenerIdArtículo() {
        return idArtículo;
    }

    public String obtenerEmbarque() {
        return shipperid;
    }

    public String obtenerFechaVenta() {
        return fechaVenta;
    }

    public void establecerIdCliente(String _idCliente) {
        idCliente = _idCliente;
    }

    public void establecerDescripción(String _descripción) {
        descripción = _descripción;
    }

    public void establecerIdArtículo(String _idArtículo) {
        idArtículo = _idArtículo;
    }

    public void establecerEmbarque(String _shipperid) {
        shipperid = _shipperid;
    }

    public void establecerFechaVenta(String _fechaVenta) {
        fechaVenta = _fechaVenta;
    }
}