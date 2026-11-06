package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Token;
import java.util.ArrayList;
import java.util.HashMap;
public class Error3 {

    private HashMap<String, String> variables;

    public Error3() {
        variables = new HashMap<>();
    }

    private boolean esTipo(String lexema) {
        return lexema.equals("int")
                || lexema.equals("float")
                || lexema.equals("double")
                || lexema.equals("char")
                || lexema.equals("bool")
                || lexema.equals("string");
    }

    private boolean abre(String lexema) {
        return lexema.equals("(") || lexema.equals("[") || lexema.equals("{");

    }

    private boolean cierra(String lexema) {
        return lexema.equals(")") || lexema.equals("]") || lexema.equals("}");
    }

    private String tipoLiteral(Token token) {
        String tipo = token.getTipo();
        if (tipo.equals("Entero") || tipo.equals("Decimal")
                || tipo.equals("Cadena") || tipo.equals("Caracter")) {
            return tipo;
        }
        if (token.getLexema().equals("true") || token.getLexema().equals("false")) {
            return "Booleano";
        }
        return null;
    }

    private boolean esCompatible(String tipoVariable, String tipoValor) {
        switch (tipoVariable) {
            case "int":
            case "float":
            case "double":
            case "bool":
                return !tipoValor.equals("Cadena");
            case "char":
                return !tipoValor.equals("Cadena") && !tipoValor.equals("Decimal");
            case "string":
                return tipoValor.equals("Cadena");
            default:
                return true;
        }
    }

    private void verificarAsignacion(ArrayList<Token> tokens, int indiceValor,
                                     String nombre, String tipoVariable,
                                     ArrayList<ErrorSemantico> errores) {
        if (indiceValor + 1 >= tokens.size()) {
            return;
        }

        String siguiente = tokens.get(indiceValor + 1).getLexema();
        if (!siguiente.equals(";") && !siguiente.equals(",")) {
            return;
        }

        Token valor = tokens.get(indiceValor);
        String tipoValor = tipoLiteral(valor);
        if (tipoValor == null) {
            return;
        }

        if (!esCompatible(tipoVariable, tipoValor)) {
            String comandoEsperado = "Valor compatible con " + tipoVariable;
            String mensaje = "No se puede asignar un valor de tipo " + tipoValor
                    + " (" + valor.getLexema() + ") a la variable " + nombre
                    + " de tipo " + tipoVariable + ".";
            errores.add(new ErrorSemantico(valor.getLinea(), comandoEsperado, mensaje));
        }
    }


    private void procesarDeclaracion(ArrayList<Token> tokens, int indiceTipo,
                                     ArrayList<ErrorSemantico> errores) {
        String tipo = tokens.get(indiceTipo).getLexema();
        int nivel = 0;

        for (int k = indiceTipo + 1; k < tokens.size(); k++) {
            Token token = tokens.get(k);
            String lexema = token.getLexema();

            if (abre(lexema)) {
                nivel++;
            } else if (cierra(lexema)) {
                nivel--;
                if (nivel < 0) {
                    return;
                }
            } else if (lexema.equals(";") && nivel == 0) {
                return;
            }

            boolean inicioDeclarador = token.getTipo().equals("Identificador")
                    && nivel == 0
                    && (k == indiceTipo + 1 || tokens.get(k - 1).getLexema().equals(","));
            if (!inicioDeclarador) {
                continue;
            }

            boolean sigueParentesis = k + 1 < tokens.size()
                    && tokens.get(k + 1).getLexema().equals("(");
            if (sigueParentesis) {
                if (k == indiceTipo + 1) {
                    return;
                }
                continue;
            }

            variables.put(lexema, tipo);

            boolean sigueIgual = k + 1 < tokens.size()
                    && tokens.get(k + 1).getLexema().equals("=");
            if (sigueIgual) {
                verificarAsignacion(tokens, k + 2, lexema, tipo, errores);
            }
        }
    }

    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        ArrayList<ErrorSemantico> errores = new ArrayList<>();
        variables.clear();

        for (int i = 0; i < tokens.size(); i++) {
            Token actual = tokens.get(i);

            // Caso 1: declaracion con tipo
            if (esTipo(actual.getLexema())) {
                procesarDeclaracion(tokens, i, errores);
                continue;
            }

            // Caso 2: asignacion a una variable ya declarada
            if (actual.getTipo().equals("Identificador")
                    && i + 1 < tokens.size()
                    && tokens.get(i + 1).getLexema().equals("=")
                    && variables.containsKey(actual.getLexema())) {

                String anterior = i > 0 ? tokens.get(i - 1).getLexema() : "";
                boolean esMiembro = anterior.equals(".") || anterior.equals("->")
                        || anterior.equals("::");
                boolean esDeclarador = esTipo(anterior) || anterior.equals(",");

                if (!esMiembro && !esDeclarador) {
                    verificarAsignacion(tokens, i + 2, actual.getLexema(),
                            variables.get(actual.getLexema()), errores);
                }
            }
        }
        return errores;
    }
}