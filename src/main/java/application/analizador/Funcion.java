package application.analizador;

public class Funcion {

    String nombre;
    int cantidadParametros;
    int linea;

    public Funcion(String nuevoNombre, int nuevaCantidadParametros, int nuevaLinea){
        nombre= nuevoNombre;
        cantidadParametros= nuevaCantidadParametros;
        linea= nuevaLinea;
    }

    public String getNombre(){
        return nombre;
    }
    public int getCantidadParametros(){
        return cantidadParametros;
    }
    public int getLinea(){
        return linea;
    }




}
