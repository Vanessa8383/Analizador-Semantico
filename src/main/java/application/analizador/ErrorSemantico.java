package application.analizador;

public class ErrorSemantico {
    int linea;
    String comandoEsperado;
    String descripcion;

    public ErrorSemantico(int nuevaLines, String nuevoComandoEsperado, String nuevaDescripcion){
        linea= nuevaLines;
        comandoEsperado= nuevoComandoEsperado;
        descripcion= nuevaDescripcion;
    }

    public int getLinea(){
        return linea;
    }
    public String getComandoEsperado(){
        return comandoEsperado;
    }
    public String getDescripcion(){
        return descripcion;
    }




}
