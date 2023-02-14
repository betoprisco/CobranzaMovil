package Clases;

public class ClaseConexiones {
    public enum TypeOperacion{HISTORIAL,ABONOS,DIRECCION,DECOMISO,ACTUALIZO_BD,IMGDECO,CCONVENIO,
    VERSIONDEANDROID};

    public enum TypeURL{HISTORIAL_INTER,ABONOS_INTER,DECOMISOS_INTER,ACTUALIZO_BD_INTER};

    public String Cadenas_Metodos(TypeOperacion tipo){
        if (tipo == TypeOperacion.HISTORIAL) return "InsertaComentario2";
        if (tipo == TypeOperacion.ABONOS ) return "ProcAboCovid19";
        if (tipo == TypeOperacion.DECOMISO ) return "procedimientoDecomisos";
        if (tipo == TypeOperacion.DIRECCION ) return "DirAlterna";
        if (tipo == TypeOperacion.ACTUALIZO_BD ) return "verificaInstalacion";
        if (tipo == TypeOperacion.IMGDECO ) return "ImgDecomiso";
        if (tipo == TypeOperacion.CCONVENIO ) return "convenio";
        if (tipo == TypeOperacion.VERSIONDEANDROID ) return "InsertarVersionDeAndroid";
        return "";
    }
    public String Cadenas_AccionSoap(TypeOperacion tipo){
        if (tipo == TypeOperacion.ABONOS) return "" +
                "http://tempuri.org/ProcAboCovid19";
        if (tipo == TypeOperacion.HISTORIAL  ) return "http://tempuri.org/InsertaComentario2";
        if (tipo == TypeOperacion.DIRECCION  ) return "http://tempuri.org/DirAlterna";
        if (tipo == TypeOperacion.DECOMISO  ) return "http://tempuri.org/procedimientoDecomisos";
        if (tipo == TypeOperacion.ACTUALIZO_BD  ) return "http://tempuri.org/verificaInstalacion";
        if (tipo == TypeOperacion.IMGDECO  ) return "http://tempuri.org/ImgDecomiso";
        if (tipo == TypeOperacion.CCONVENIO  ) return "http://tempuri.org/convenio";
        if (tipo == TypeOperacion.VERSIONDEANDROID  )
            return "http://tempuri.org/InsertarVersionDeAndroid";
        return "";
    }

    public String Cadenas_url(TypeURL tipo){

        if (tipo == TypeURL.HISTORIAL_INTER)   return"http://sbvconect.dyndns.org/Pruebas/CobMovilService/service.asmx"; //bueno
        //if (tipo == TypeURL.HISTORIAL_INTER)   return"http://sbvconect.dyndns.org/CobMovilService/service.asmx"; //pruebas mer
        if (tipo == TypeURL.ABONOS_INTER )     return"http://sbvconect.dyndns.org/Pruebas/CobMovilService/service.asmx"; //bueno
        //if (tipo == TypeURL.ABONOS_INTER )     return"http://sbvconect.dyndns.org/CobMovilServicePruebas/service.asmx"; //pruebas mer
        if (tipo == TypeURL.DECOMISOS_INTER )  return"http://sbvconect.dyndns.org/Pruebas/CobMovilService/service.asmx"; //bueno
        //if (tipo == TypeURL.DECOMISOS_INTER )     return"http://sbvconect.dyndns.org/CobMovilService/service.asmx"; //pruebas mer
        if (tipo == TypeURL.ACTUALIZO_BD_INTER )     return"http://sbvconect.dyndns.org/Pruebas/CobMovilService/service.asmx"; //bueno
        //if (tipo == TypeURL.ACTUALIZO_BD_INTER )     return"http://sbvconect.dyndns.org/CobMovilService/service.asmx"; //pruebas mer
        return "";

    }
    public String Cadenas_NameSpace(TypeOperacion tipo){
        if (tipo == TypeOperacion.ABONOS)    return "http://tempuri.org/";
        if (tipo == TypeOperacion.HISTORIAL) return "http://tempuri.org/";
        return "";
    }

}
