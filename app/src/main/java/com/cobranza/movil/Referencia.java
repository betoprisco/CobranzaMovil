package com.cobranza.movil;

public class Referencia {
    private String nombre;
    private String parentesco;
    private String domicilio;
    private String cruzamientos;
    private String teléfono;
    private String celular;

    public Referencia (String _nombre,String _parentesco,String _domicilio,String _cruzamientos,String _teléfono,String _celular){

        nombre=_nombre;
        parentesco=_parentesco;
        domicilio=_domicilio;
        cruzamientos=_cruzamientos;
        teléfono=_teléfono;
        celular=_celular;
    }

    public String obtenerNombre()
    {
        return nombre;
    }
    public String obtenerParentesco()
    {
        return parentesco;
    }
    public String obtenerDomicilio()
    {
        return domicilio;
    }
    public String obtenerCruzamientos()
    {
        return cruzamientos;
    }
    public String obtenerTeléfono()
    {
        return teléfono;
    }
    public String obtenerCelular()
    {
        return celular;
    }

    public void establecerNombre(String _nombre)
    {
        nombre = _nombre;
    }
    public void establecerParentesco(String _parentesco)
    {
        parentesco = _parentesco;
    }
    public void establecerDomicilio(String _domicilio)
    {
        domicilio = _domicilio;
    }
    public void establecerCruzamientos(String _cruzamientos)
    {
        cruzamientos = _cruzamientos;
    }
    public void establecerTeléfono(String _teléfono)
    {
        teléfono = _teléfono;
    }
    public void establecerCelular(String _celular)
    {
        celular = _celular;
    }
}
