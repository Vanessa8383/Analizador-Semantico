package application.analizador;

import java.util.ArrayList;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class AnalizadorLexico {

    ArrayList<String> palabrasReservadas;
    ArrayList<String> operadores;
    ArrayList<Character> simbolos;

    ArrayList<Token> tokens;

    public AnalizadorLexico(){

        palabrasReservadas = new ArrayList<>();

        palabrasReservadas.add("int");
        palabrasReservadas.add("float");
        palabrasReservadas.add("double");
        palabrasReservadas.add("char");
        palabrasReservadas.add("void");
        palabrasReservadas.add("bool");
        palabrasReservadas.add("if");
        palabrasReservadas.add("else");
        palabrasReservadas.add("for");
        palabrasReservadas.add("while");
        palabrasReservadas.add("do");
        palabrasReservadas.add("return");
        palabrasReservadas.add("break");
        palabrasReservadas.add("continue");
        palabrasReservadas.add("switch");
        palabrasReservadas.add("case");
        palabrasReservadas.add("default");
        palabrasReservadas.add("class");
        palabrasReservadas.add("struct");
        palabrasReservadas.add("public");
        palabrasReservadas.add("private");
        palabrasReservadas.add("protected");
        palabrasReservadas.add("new");
        palabrasReservadas.add("delete");
        palabrasReservadas.add("const");
        palabrasReservadas.add("static");
        palabrasReservadas.add("virtual");
        palabrasReservadas.add("this");
        palabrasReservadas.add("namespace");
        palabrasReservadas.add("using");
        palabrasReservadas.add("try");
        palabrasReservadas.add("catch");
        palabrasReservadas.add("throw");
        palabrasReservadas.add("true");
        palabrasReservadas.add("false");
        palabrasReservadas.add("nullptr");



        operadores = new ArrayList<>();

        operadores.add("+");
        operadores.add("-");
        operadores.add("*");
        operadores.add("/");
        operadores.add("%");
        operadores.add("=");
        operadores.add("==");
        operadores.add("!=");
        operadores.add(">");
        operadores.add("<");
        operadores.add(">=");
        operadores.add("<=");
        operadores.add("&&");
        operadores.add("||");
        operadores.add("!");
        operadores.add("++");
        operadores.add("--");
        operadores.add("+=");
        operadores.add("-=");
        operadores.add("*=");
        operadores.add("/=");
        operadores.add("&");
        operadores.add("|");
        operadores.add("^");
        operadores.add("~");
        operadores.add("<<");
        operadores.add(">>");
        operadores.add("::");
        operadores.add("->");
        operadores.add(".");
        operadores.add("?");
        operadores.add(":");



        simbolos = new ArrayList<>();

        simbolos.add('(');
        simbolos.add(')');
        simbolos.add('{');
        simbolos.add('}');
        simbolos.add('[');
        simbolos.add(']');
        simbolos.add(';');
        simbolos.add(',');




        tokens = new ArrayList<>();
    }

    public boolean esPalabraReservada(String palabra){
        return palabrasReservadas.contains(palabra);
    }

    public boolean esOperador(String operador){
        return operadores.contains(operador);
    }

    public boolean esSimbolo(char simbolo) {
        return simbolos.contains(simbolo);
    }




    public void analizarArchivo(String ruta) {

        tokens.clear();

        try {
            FileReader archivo = new FileReader(ruta);
            BufferedReader lector = new BufferedReader(archivo);

            String linea;
            linea = lector.readLine();

            int numeroLinea = 1;




            boolean dentroComentarioBloque = false;

            while (linea != null){

                for(int i=0; i<linea.length(); i++){

                    char caracterActual = linea.charAt(i);





                    if (dentroComentarioBloque) {

                        if (caracterActual == '*' && i + 1 < linea.length() && linea.charAt(i + 1) == '/') {

                            dentroComentarioBloque = false;
                            i++;
                        }
                        continue;
                    }






                    if (caracterActual == '/' && i + 1 < linea.length() && linea.charAt(i + 1) == '*') {
                        dentroComentarioBloque = true;
                        i++;
                        continue;
                    }






                    if (Character.isWhitespace(caracterActual)) {
                        continue;
                    }






                    if (Character.isLetter(caracterActual) || caracterActual == '_') {

                        String palabra = "";
                        int j = i;
                        while (j <linea.length() && (Character.isLetterOrDigit(linea.charAt(j)) || linea.charAt(j) == '_')){

                            palabra += linea.charAt(j);
                            j++;
                        }
                        int inicioPalabra = i+1;
                        int finPalabra = j;
                        i = j-1;

                        String tipoPalabra;

                        if (esPalabraReservada(palabra)){
                             tipoPalabra = "Palabra Reservada";

                        }else{
                             tipoPalabra = "Identificador";
                        }

                        tokens.add(new Token(palabra, tipoPalabra, numeroLinea, inicioPalabra, finPalabra));

                    }






                    if (Character.isDigit(caracterActual)) {


                        String numero = "";
                        int j = i;
                        boolean tienePunto = false;


                        while (j <linea.length() ){
                            char caracterNumero = linea.charAt(j);

                            if (Character.isDigit(caracterNumero)){



                                numero += caracterNumero;
                                j++;

                            } else if( caracterNumero == '.' && !tienePunto) {

                                numero += caracterNumero;
                                tienePunto = true;
                                j++;

                            } else{
                                break;
                            }

                        }
                        int inicioNumero = i+1;
                        int finNumero = j;
                        i = j-1;


                        String tipoNumero;
                        if (tienePunto) {
                            tipoNumero = "Decimal";
                        } else {
                            tipoNumero = "Entero";
                        }

                        tokens.add(new Token(numero, tipoNumero, numeroLinea, inicioNumero, finNumero));


                    }






                    if (caracterActual == '"'){

                        String cadena = "";
                        int j = i;
                        cadena += linea.charAt(j);
                        j++;
                        while (j < linea.length()){

                            char caracterCadena = linea.charAt(j);
                            cadena = cadena + caracterCadena;
                            j++;
                            if (caracterCadena == '"'){
                                break;
                            }
                        }

                        int inicioCadena = i + 1;
                        int finCadena = j;
                        i = j - 1;
                        tokens.add(new Token(cadena,"Cadena", numeroLinea, inicioCadena, finCadena));

                    }






                    if (caracterActual == '\'') {

                        String caracter = "";
                        int j = i;
                        caracter += linea.charAt(j);
                        j++;
                        while (j < linea.length()){

                            char caracterInterno = linea.charAt(j);
                            caracter = caracter + caracterInterno;
                            j++;
                            if (caracterInterno == '\''){
                                break;
                            }
                        }

                        int inicioCaracter = i + 1;
                        int finCaracter = j;
                        i = j - 1;
                        tokens.add(new Token(caracter,"Caracter", numeroLinea, inicioCaracter, finCaracter));
                    }






                    if (esSimbolo(caracterActual)){

                        tokens.add(new Token(String.valueOf(caracterActual),"Simbolo", numeroLinea, i+1, i+1));
                    }






                    if (caracterActual == '/' && i + 1 < linea.length() && linea.charAt(i + 1) == '/') {

                        String comentario= "";
                        int j = i;

                        while (j < linea.length()){

                            comentario += linea.charAt(j);
                            j++;
                        }
                        int inicioComentario = i + 1;
                        int finComentario = j;


                        tokens.add(new Token(comentario, "Comentario", numeroLinea, inicioComentario, finComentario));
                        break;

                    }






                    if (caracterActual == '#') {

                        String directiva= "";
                        int j = i;

                        while (j < linea.length()){

                            directiva += linea.charAt(j);
                            j++;
                        }
                        int inicioDirectiva = i + 1;
                        int finDirectiva = j;


                        tokens.add(new Token(directiva, "Directiva", numeroLinea, inicioDirectiva, finDirectiva));
                        break;
                    }






                    if (i + 1 < linea.length()) {

                        String operadorDoble = String.valueOf(caracterActual) + linea.charAt(i + 1);

                        if (esOperador(operadorDoble)){

                            tokens.add(new Token(operadorDoble,"Operador", numeroLinea, i+1,i+2));
                            i++;
                            continue;
                        }

                    }

                    String operadorSimple = String.valueOf(caracterActual);
                    if (esOperador(operadorSimple)) {

                        tokens.add(new Token(operadorSimple,"Operador", numeroLinea, i+1,i+1));
                    }






                }

                linea = lector.readLine();
                numeroLinea++;

            }


        }catch (IOException e) {

        }
    }




    public void imprimirTokens() {

        System.out.println("Lexema\tTipo\tLinea\tInicio\tFin");
        for (Token token : tokens) {

            System.out.println(token.lexema+ "\t" + token.tipo+ "\t" + token.linea+ "\t" + token.inicio+ "\t" + token.fin);
        }
    }




    public ArrayList<Token> getTokens() {
        return tokens;
    }

}
