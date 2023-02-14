package Clases;

public class Clase_Encriptacion {
    private String patronBusqueda = "9f0AwB7J8CpDuÑd5E6FQGlxnHMbcI3ñK4LeUNzO1ms2PtRvSVkWXqirTYaghZjoy";
    private String patronEncripta = "wxBU7nIGj9Flm8f0ñAH1bcK3hdi4WJ5ZLCpDeMvTQuVkXqraYE6gosyNzÑOP2RSt";

    public String encriptarCaracter(String caracter, int variable, int indice){
        int ind;
        if(patronBusqueda.indexOf(caracter)!=-1){
            ind = (patronBusqueda.indexOf(caracter) + variable + indice) % patronBusqueda.length();
            return patronEncripta.substring(ind, ind+1);
        }
        return caracter;
    }

    public String encriptarCadena(String cadena){
        String resultado="";
        for(int pos = 0; pos< cadena.length(); pos++){
            if(pos==0){resultado= encriptarCaracter(cadena.substring(pos,pos+1),cadena.length(),pos);   }
            else{resultado+= encriptarCaracter(cadena.substring(pos,pos+1),cadena.length(),pos);   }
        }
        return resultado;
    }

    public String desencriptaCadena(String cadena){
        String original="";
        for(int pos = 0; pos< cadena.length(); pos++){
            if(pos==0){original=desencriptaCaracter(cadena.substring(pos,pos+1),cadena.length(),pos);}
            else{original+=desencriptaCaracter(cadena.substring(pos,pos+1),cadena.length(),pos);}
        }
        return original;
    }

    public String desencriptaCaracter(String caracter, int variable, int indice){
        int ind=0;
        if(patronEncripta.indexOf(caracter) != -1){
            if((patronEncripta.indexOf(caracter) - variable - indice) > 0){
                ind = (patronEncripta.indexOf(caracter) - variable - indice) % patronEncripta.length(); }
            else{
                ind = (patronBusqueda.length()) + ((patronEncripta.indexOf(caracter)- variable - indice) % patronEncripta.length());
            }
            ind = ind % patronEncripta.length();
            return patronBusqueda.substring(ind, ind + 1);
        }
        else{return caracter;}
    }
}
