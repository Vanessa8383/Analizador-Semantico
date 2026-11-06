package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Funcion;
import application.analizador.Token;

import java.util.ArrayList;

public class Error4 {
    private ArrayList<Funcion> funciones;

    public Error4(){
        funciones=new ArrayList<>();
    }

    private boolean esTipo(String lexema){
        return lexema.equals("int") || lexema.equals("float") || lexema.equals("double")
                || lexema.equals("char") || lexema.equals("bool") || lexema.equals("void");
    }

    private void agregarFuncion(Funcion funcion){
        funciones.add(funcion);
    }

    private void buscarFunciones(ArrayList<Token> tokens) {
        funciones.clear();

        for (int i = 0; i < tokens.size() - 2; i++) { //recorrido de los tokens
            Token tokenActual = tokens.get(i);
            Token tokenSiguiente = tokens.get(i + 1);
            Token tokenParentesis = tokens.get(i + 2);

            if (esTipo(tokenActual.getLexema()) //identificador de si es una funcion
                    && tokenSiguiente.getTipo().equals("Identificador") && tokenParentesis.getLexema().equals("(")) {

                int cantidadParametros = 0;
                int inicioParametros = i + 3;
                int j = inicioParametros;

                while (j < tokens.size() && !tokens.get(j).getLexema().equals(")")) {
                    j++;
                }
                if (inicioParametros < j) { //contador de parametros
                    cantidadParametros = 1;
                    for (int k = inicioParametros; k < j; k++) {
                        if (tokens.get(k).getLexema().equals(",")) {
                            cantidadParametros++;
                        }
                    }
                }
                Funcion funcion = new Funcion(
                        tokenSiguiente.getLexema(), cantidadParametros, tokenSiguiente.getLinea()
                );
                agregarFuncion(funcion);
            }
        }
    }

    private Funcion buscarFuncion(String nombre){
        for (Funcion funcion: funciones){
            if (funcion.getNombre().equals(nombre)){
                return funcion;
            }
        }
        return  null;
    }


    private int contarArgumentos(ArrayList<Token> tokens, int indiceParentisis){
        int cantidad=0;
        int nivelParentesis= 0;
        boolean hayContenido=false;

        for (int j= indiceParentisis+1; j <tokens.size(); j++){
            String lexema = tokens.get(j).getLexema();
            if (lexema.equals("(")){
                nivelParentesis++;
                hayContenido= true;
            } else if (lexema.equals(")")) {
                if (nivelParentesis==0){
                    if (hayContenido){
                        cantidad++;
                    }
                    break;

                } else {
                    nivelParentesis--;
                    hayContenido=true;
                }
            } else if (lexema.equals(",") && nivelParentesis==0) {
                cantidad ++;
                hayContenido=false;
            }else {
                hayContenido=true;
            }
        }
        return cantidad;
    }

    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        ArrayList<ErrorSemantico> errores = new ArrayList<>();
        buscarFunciones(tokens);

        for (int i = 0; i < tokens.size() - 1; i++) {
            Token tokenActual = tokens.get(i);
            Token tokenSiguiente = tokens.get(i + 1);

            if (tokenActual.getTipo().equals("Identificador")
                    && tokenSiguiente.getLexema().equals("(")) {
                boolean esDeclaracion = false;

                if (i > 0 && esTipo(tokens.get(i - 1).getLexema())) {
                    esDeclaracion = true;
                }
                if (!esDeclaracion) {
                    Funcion funcion = buscarFuncion(tokenActual.getLexema());
                    if (funcion != null) {
                        int cantidadArgumentos = contarArgumentos(tokens, i + 1);
                        if (cantidadArgumentos != funcion.getCantidadParametros()) {
                            String comandoEsperado = funcion.getCantidadParametros() + " argumentos";

                            String mensaje = "La funcion " + funcion.getNombre() + " recibio "
                                            + cantidadArgumentos + " argumentos, pero requiere " + funcion.getCantidadParametros() + ".";

                            ErrorSemantico error = new ErrorSemantico(
                                    tokenActual.getLinea(), comandoEsperado, mensaje
                            );
                            errores.add(error);
                        }
                    }
                }
            }
        }
        return errores;
    }

}


