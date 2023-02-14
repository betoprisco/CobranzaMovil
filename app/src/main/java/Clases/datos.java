package Clases;

public class datos {
    private String id;
    private String dato;
    private String dato2;

    public datos(String _id, String _dato, String _dato2)
    {

        id=_id;
        dato=_dato;
        dato2=_dato2;
    }
    public String getId()
    {
        return id;
    }
    public String getDato()
    {
        return dato;
    }
    public String getDato2()
    {
        return dato2;
    }
    public void setId(String _id)
    {
        id = _id;
    }

    public void setDato(String _dato)
    {
        dato = _dato;
    }
    public void setDato2(String _dato2)
    {
        dato2 = _dato2;
    }
}