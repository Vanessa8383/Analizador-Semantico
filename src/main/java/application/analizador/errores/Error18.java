package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Token;
import java.util.ArrayList;

public class Error18 {

    private static class Variable {
        String nombre;
        boolean constante;
        int nivel;

        Variable(String nombre, boolean constante, int nivel) {
            this.nombre = nombre;
            this.constante = constante;
            this.nivel = nivel;
        }
    }

    private boolean esTipo(String palabra) {
        return palabra.equals("int") || palabra.equals("float")
                || palabra.equals("double") || palabra.equals("char")
                || palabra.equals("bool") || palabra.equals("string");
    }

    private String obtener(ArrayList<Token> tokens, int i) {
        if (i < 0 || i >= tokens.size()) {
            return "";
        }
        return tokens.get(i).getLexema();
    }

    private void buscarDeclaraciones(ArrayList<Token> tokens, int i, int nivel,
                                     ArrayList<Variable> variables,
                                     ArrayList<Integer> declaraciones) {
        int j = i;
        boolean constante = false;

        if (obtener(tokens, j).equals("const")) {
            constante = true;
            j++;
        }

        if (!esTipo(obtener(tokens, j))) {
            return;
        }
        j++;

        if (obtener(tokens, j).equals("const")) {
            constante = true;
            j++;
        }

        if (j >= tokens.size() || !tokens.get(j).getTipo().equals("Identificador")) {
            return;
        }

        if (obtener(tokens, j + 1).equals("(")) {
            return;
        }

        variables.add(new Variable(obtener(tokens, j), constante, nivel));
        declaraciones.add(j);

        int parentesis = 0;
        int corchetes = 0;

        for (int k = j + 1; k < tokens.size(); k++) {
            String palabra = obtener(tokens, k);

            if (palabra.equals(";") && parentesis == 0 && corchetes == 0) {
                break;
            }

            if (palabra.equals("{") || palabra.equals("}")) {
                break;
            }

            if (palabra.equals("(")) {
                parentesis++;
            }

            if (palabra.equals(")")) {
                if (parentesis == 0) {
                    break;
                }
                parentesis--;
            }

            if (palabra.equals("[")) {
                corchetes++;
            }

            if (palabra.equals("]")) {
                corchetes--;
            }

            if (palabra.equals(",") && parentesis == 0 && corchetes == 0
                    && k + 1 < tokens.size()
                    && tokens.get(k + 1).getTipo().equals("Identificador")) {

                int nombre = k + 1;
                variables.add(new Variable(obtener(tokens, nombre), constante, nivel));
                declaraciones.add(nombre);
            }
        }
    }

    private boolean esModificacion(ArrayList<Token> tokens, int i) {
        String anterior = obtener(tokens, i - 1);
        String siguiente = obtener(tokens, i + 1);

        if (anterior.equals("++") || anterior.equals("--")
                || siguiente.equals("++") || siguiente.equals("--")) {
            return true;
        }

        if (siguiente.equals("=") || siguiente.equals("+=")
                || siguiente.equals("-=") || siguiente.equals("*=")
                || siguiente.equals("/=")) {
            return true;
        }

        if ((siguiente.equals("%") || siguiente.equals("&")
                || siguiente.equals("|") || siguiente.equals("^")
                || siguiente.equals("<<") || siguiente.equals(">>"))
                && obtener(tokens, i + 2).equals("=")) {
            return true;
        }

        return false;
    }

    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        ArrayList<ErrorSemantico> errores = new ArrayList<>();
        ArrayList<Variable> variables = new ArrayList<>();
        ArrayList<Integer> declaraciones = new ArrayList<>();

        int nivel = 0;

        for (int i = 0; i < tokens.size(); i++) {
            Token actual = tokens.get(i);
            String palabra = actual.getLexema();

            if (palabra.equals("{")) {
                nivel++;
                continue;
            }

            if (palabra.equals("}")) {
                for (int j = variables.size() - 1; j >= 0; j--) {
                    if (variables.get(j).nivel == nivel) {
                        variables.remove(j);
                    }
                }

                if (nivel > 0) {
                    nivel--;
                }
                continue;
            }

            String anterior = obtener(tokens, i - 1);

            if ((palabra.equals("const") || esTipo(palabra))
                    && !anterior.equals("const") && !esTipo(anterior)) {
                buscarDeclaraciones(tokens, i, nivel, variables, declaraciones);
            }

            if (!actual.getTipo().equals("Identificador")
                    || declaraciones.contains(i)) {
                continue;
            }

            boolean esConstante = false;

            for (int j = variables.size() - 1; j >= 0; j--) {
                if (variables.get(j).nombre.equals(palabra)) {
                    esConstante = variables.get(j).constante;
                    break;
                }
            }

            if (esConstante && esModificacion(tokens, i)) {
                String comandoEsperado = "No modificar la constante '" + palabra + "'";

                String mensaje = "Error 18: Uso incorrecto de const. La variable '"
                        + palabra + "' fue declarada como constante y no se puede modificar.";

                ErrorSemantico error = new ErrorSemantico(
                        actual.getLinea(), comandoEsperado, mensaje
                );

                errores.add(error);
            }
        }

        return errores;
    }
}