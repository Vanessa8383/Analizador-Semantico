package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Token;

import java.util.ArrayList;

public class Error19 {

    private ArrayList<String> operadoresAsignacion;
    private ArrayList<String> operadoresCompuestos;
    private ArrayList<String> operadoresBinarios;
    private ArrayList<String> tiposYModificadores;
    private ArrayList<String> palabrasLimite;
    private ArrayList<String> palabrasControl;

    private ArrayList<Token> lista;
    private ArrayList<ErrorSemantico> errores;

    public Error19() {

        operadoresAsignacion = new ArrayList<>();
        operadoresAsignacion.add("=");
        operadoresAsignacion.add("+=");
        operadoresAsignacion.add("-=");
        operadoresAsignacion.add("*=");
        operadoresAsignacion.add("/=");

        operadoresCompuestos = new ArrayList<>();
        operadoresCompuestos.add("%");
        operadoresCompuestos.add("&");
        operadoresCompuestos.add("|");
        operadoresCompuestos.add("^");
        operadoresCompuestos.add("<<");
        operadoresCompuestos.add(">>");

        operadoresBinarios = new ArrayList<>();
        operadoresBinarios.add("+");
        operadoresBinarios.add("-");
        operadoresBinarios.add("*");
        operadoresBinarios.add("/");
        operadoresBinarios.add("%");
        operadoresBinarios.add("&");
        operadoresBinarios.add("|");
        operadoresBinarios.add("^");
        operadoresBinarios.add("<<");
        operadoresBinarios.add(">>");
        operadoresBinarios.add("<");
        operadoresBinarios.add(">");
        operadoresBinarios.add("<=");
        operadoresBinarios.add(">=");
        operadoresBinarios.add("==");
        operadoresBinarios.add("!=");
        operadoresBinarios.add("&&");
        operadoresBinarios.add("||");

        tiposYModificadores = new ArrayList<>();
        tiposYModificadores.add("int");
        tiposYModificadores.add("float");
        tiposYModificadores.add("double");
        tiposYModificadores.add("char");
        tiposYModificadores.add("void");
        tiposYModificadores.add("bool");
        tiposYModificadores.add("const");
        tiposYModificadores.add("static");
        tiposYModificadores.add("struct");
        tiposYModificadores.add("class");
        tiposYModificadores.add("using");

        palabrasLimite = new ArrayList<>();
        palabrasLimite.add("else");
        palabrasLimite.add("do");
        palabrasLimite.add("return");
        palabrasLimite.add("case");
        palabrasLimite.add("default");
        palabrasLimite.add("throw");

        palabrasControl = new ArrayList<>();
        palabrasControl.add("if");
        palabrasControl.add("while");
        palabrasControl.add("for");
        palabrasControl.add("switch");
        palabrasControl.add("catch");

        lista = new ArrayList<>();
        errores = new ArrayList<>();
    }

    private boolean esAsignacion(String operador) {
        return operadoresAsignacion.contains(operador);
    }

    private boolean esSimbolo(Token token, String simbolo) {
        return token.getTipo().equals("Simbolo") && token.getLexema().equals(simbolo);
    }

    private boolean esOperador(Token token, String operador) {
        return token.getTipo().equals("Operador") && token.getLexema().equals(operador);
    }

    private boolean esLiteral(Token token) {

        return token.getTipo().equals("Entero")
                || token.getTipo().equals("Decimal") || token.getTipo().equals("Cadena") || token.getTipo().equals("Caracter") || (token.getTipo().equals("Palabra Reservada")
                && (token.getLexema().equals("true") || token.getLexema().equals("false") || token.getLexema().equals("nullptr")));
    }

    private boolean esOperando(Token token) {
        return token.getTipo().equals("Identificador") || esLiteral(token) || esSimbolo(token, ")")
                || esSimbolo(token, "]") || esOperador(token, "++") || esOperador(token, "--");
    }

    private int buscarCierre(int apertura, int limite) {
        int nivel = 0;
        for (int k = apertura; k < limite; k++) {
            Token token = lista.get(k);
            if (esSimbolo(token, "(")) {
                nivel++;
            } else if (esSimbolo(token, ")")) {
                nivel--;
                if (nivel == 0) {
                    return k;
                }
            }
        }
        return -1;
    }

    private int buscarInicio(int posOperador) {
        int profundidad = 0;
        int cierreExterno = -1;

        for (int k = posOperador - 1; k >= 0; k--) {
            Token token = lista.get(k);
            if (esSimbolo(token, ")") || esSimbolo(token, "]")) {

                if (profundidad == 0) {
                    cierreExterno = esSimbolo(token, ")") ? k : -1;
                }
                profundidad++;
                continue;
            }

            if (esSimbolo(token, "(") || esSimbolo(token, "[")) {
                if (profundidad == 0) {
                    return k + 1;
                }
                profundidad--;
                if (profundidad == 0
                        && cierreExterno != -1 && k > 0 && lista.get(k - 1).getTipo().equals("Palabra Reservada")
                        && palabrasControl.contains(lista.get(k - 1).getLexema())) {

                    return cierreExterno + 1;
                }
                continue;
            }
            if (profundidad > 0) {
                continue;
            }
            if (token.getTipo().equals("Simbolo") && (token.getLexema().equals(";")
                    || token.getLexema().equals("{") || token.getLexema().equals("}") || token.getLexema().equals(","))) {
                return k + 1;
            }
            if (token.getTipo().equals("Operador") && (esAsignacion(token.getLexema())
                    || token.getLexema().equals("?") || token.getLexema().equals(":"))) {
                return k + 1;
            }
            if (token.getTipo().equals("Palabra Reservada") && palabrasLimite.contains(token.getLexema())) {
                return k + 1;
            }
        }
        return 0;
    }

    private String analizarLado(int inicio, int fin) {
        if (inicio >= fin) {
            return null;
        }
        Token primero = lista.get(inicio);
        if (tiposYModificadores.contains(primero.getLexema()) && !primero.getTipo().equals("Cadena")) {
            return null;
        }
        if (fin - inicio >= 2) {
            Token ultimo = lista.get(fin - 1);
            Token penultimo = lista.get(fin - 2);
            boolean ultimoEsNombre = ultimo.getTipo().equals("Identificador");
            boolean antesEsTipo =
                    penultimo.getTipo().equals("Identificador") || esOperador(penultimo, ">") || esOperador(penultimo, ">>");
            if (ultimoEsNombre && antesEsTipo) {
                return null;
            }
            if (primero.getTipo().equals("Identificador") && lista.get(inicio + 1).getTipo().equals("Identificador")) {
                return null;
            }
        }
        while (fin - inicio >= 2 && esSimbolo(lista.get(inicio), "(") && buscarCierre(inicio, fin) == fin - 1) {
            inicio++;
            fin--;
        }
        if (inicio >= fin) {
            return null;
        }
        primero = lista.get(inicio);
        Token ultimo = lista.get(fin - 1);
        if (fin - inicio >= 3 && esSimbolo(primero, "(")
                && tiposYModificadores.contains(lista.get(inicio + 1).getLexema())
                && esSimbolo(lista.get(inicio + 2), ")")) {
            return "una conversion (cast) produce un valor temporal";
        }
        int profundidad = 0;
        for (int k = inicio; k < fin; k++) {
            Token token = lista.get(k);
            if (esSimbolo(token, "(") || esSimbolo(token, "[")) {
                profundidad++;

            } else if (esSimbolo(token, ")") || esSimbolo(token, "]")) {
                profundidad--;
            } else if (profundidad == 0 && k > inicio && token.getTipo().equals("Operador")
                    && operadoresBinarios.contains(token.getLexema()) && esOperando(lista.get(k - 1))) {
                return "es el resultado de una operacion con '" + token.getLexema() + "' (valor temporal, no es lvalue)";
            }
        }

        if (fin - inicio >= 2 && (esOperador(ultimo, "++")
                || esOperador(ultimo, "--")) && esOperando(lista.get(fin - 2))) {
            return "el incremento/decremento posfijo devuelve un valor temporal";
        }
        if (primero.getTipo().equals("Operador") && (primero.getLexema().equals("-") || primero.getLexema().equals("+")
                || primero.getLexema().equals("!") || primero.getLexema().equals("~") || primero.getLexema().equals("&"))) {
            return "el operador unario '" + primero.getLexema() + "' produce un valor temporal";
        }

        if (fin - inicio == 1) {
            if (esLiteral(primero)) {
                return "es un literal (" + primero.getLexema() + "), no ocupa un espacio de memoria modificable";
            }
            return null;
        }

        if (fin - inicio >= 3
                && primero.getTipo().equals("Identificador") && esSimbolo(lista.get(inicio + 1), "(")
                && buscarCierre(inicio + 1, fin) == fin - 1) {
            return "es el resultado de una llamada a funcion " + "(solo seria valido si retorna una referencia)";
        }
        return null;
    }

    private void reportar(int inicio, int posOperador, String motivo) {
        Token tokenInicio = lista.get(inicio);
        Token operador = lista.get(posOperador);
        StringBuilder texto = new StringBuilder();
        for (int k = inicio; k <= posOperador; k++) {
            texto.append(lista.get(k).getLexema()
            ).append(" ");
        }

        String expresion = texto.toString().trim();
        ErrorSemantico error = new ErrorSemantico(
                operador.getLinea(),
                "Operando modificable (lvalue)", "Modificar una expresion no modificable: \""
                        + expresion + " ...\" -> " + motivo + " (columna " + tokenInicio.getInicio() + ")."
        );
        errores.add(error);
    }

    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        errores.clear();
        lista.clear();
        for (Token token : tokens) {
            if (!token.getTipo().equals("Comentario") && !token.getTipo().equals("Directiva")) {
                lista.add(token);
            }
        }

        for (int i = 1; i < lista.size(); i++) {
            Token actual = lista.get(i);
            if (!actual.getTipo().equals("Operador") || !esAsignacion(actual.getLexema())) {
                continue;
            }

            int finIzquierdo = i;
            if (actual.getLexema().equals("=") && i >= 2) {
                Token previo = lista.get(i - 1);
                if (previo.getTipo().equals("Operador")
                        && operadoresCompuestos.contains(
                        previo.getLexema()
                )
                        && previo.getLinea() == actual.getLinea() && previo.getFin() + 1 == actual.getInicio()) {
                    finIzquierdo = i - 1;
                }
            }

            int inicio = buscarInicio(finIzquierdo);
            String motivo = analizarLado(inicio, finIzquierdo);
            if (motivo != null) {
                reportar(inicio, i, motivo);
            }}
        return errores;
    }
}