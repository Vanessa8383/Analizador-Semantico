package application.analizador;


public class Token {

    String lexema;
    String tipo;
    int linea;
    int inicio;
    int fin;


    public Token(String nuevoLexema, String nuevoTipo, int nuevoLinea, int nuevoLnicio, int nuevoFin){

        lexema = nuevoLexema;
        tipo = nuevoTipo;
        linea = nuevoLinea;
        inicio = nuevoLnicio;
        fin = nuevoFin;
    }


    public String getLexema() {
        return lexema;
    }

    public String getTipo() {
        return tipo;
    }

    public int getLinea() {
        return linea;
    }

    public int getInicio() {
        return inicio;
    }

    public int getFin() {
        return fin;
    }
}
