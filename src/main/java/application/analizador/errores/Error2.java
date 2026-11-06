package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** Error 2: uso de una variable local fuera del bloque donde fue declarada. */
public class Error2 {
    private final Set<String> tipos = Set.of(
            "int", "float", "double", "char", "bool", "string",
            "short", "long", "signed", "unsigned", "auto", "size_t"
    );

    private static class Variable {
        String nombre;
        int inicio;
        int fin;
        int declaracion;

        Variable(String nombre, int inicio, int fin, int declaracion) {
            this.nombre = nombre;
            this.inicio = inicio;
            this.fin = fin;
            this.declaracion = declaracion;
        }
    }

    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        ArrayList<ErrorSemantico> errores = new ArrayList<>();
        ArrayList<Variable> variables = new ArrayList<>();
        ArrayList<Integer> aperturas = new ArrayList<>();
        int[] cierre = new int[tokens.size()];
        int[] bloque = new int[tokens.size()];
        java.util.Arrays.fill(cierre, -1);
        java.util.Arrays.fill(bloque, -1);

        // Relacionar cada llave de apertura con su llave de cierre.
        for (int i = 0; i < tokens.size(); i++) {
            String lexema = tokens.get(i).getLexema();
            if (lexema.equals("{")) {
                aperturas.add(i);
            } else if (lexema.equals("}") && !aperturas.isEmpty()) {
                int apertura = aperturas.remove(aperturas.size() - 1);
                cierre[apertura] = i;
            }
        }

        // Averiguar el bloque al que pertenece cada token.
        aperturas.clear();
        for (int i = 0; i < tokens.size(); i++) {
            String lexema = tokens.get(i).getLexema();
            if (lexema.equals("}") && !aperturas.isEmpty()) {
                aperturas.remove(aperturas.size() - 1);
            }
            bloque[i] = aperturas.isEmpty() ? -1 : aperturas.get(aperturas.size() - 1);
            if (lexema.equals("{")) aperturas.add(i);
        }

        Set<Integer> declaraciones = new HashSet<>();
        for (int i = 0; i + 1 < tokens.size(); i++) {
            if (!tipos.contains(tokens.get(i).getLexema())) continue;
            int j = i + 1;
            while (j < tokens.size() && (tokens.get(j).getLexema().equals("*")
                    || tokens.get(j).getLexema().equals("&"))) j++;
            if (j >= tokens.size() || !tokens.get(j).getTipo().equals("Identificador")) continue;
            // No confundir definiciones de funciones con declaraciones de variables.
            if (j + 1 < tokens.size() && tokens.get(j + 1).getLexema().equals("(")) continue;
            declaraciones.add(j);
            int apertura = bloque[j];
            if (apertura >= 0 && cierre[apertura] >= 0) {
                variables.add(new Variable(tokens.get(j).getLexema(), apertura, cierre[apertura], j));
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (!token.getTipo().equals("Identificador") || declaraciones.contains(i)) continue;
            String anterior = i > 0 ? tokens.get(i - 1).getLexema() : "";
            String siguiente = i + 1 < tokens.size() ? tokens.get(i + 1).getLexema() : "";
            if (siguiente.equals("(") || siguiente.equals("::") || anterior.equals("::")
                    || anterior.equals(".") || anterior.equals("->")) continue;

            Variable fuera = null;
            boolean visible = false;
            for (Variable var : variables) {
                if (!var.nombre.equals(token.getLexema()) || var.declaracion >= i) continue;
                if (i < var.fin && i > var.inicio) {
                    visible = true;
                    break;
                }
                if (i > var.fin && (fuera == null || var.declaracion > fuera.declaracion)) {
                    fuera = var;
                }
            }
            if (visible || fuera == null) continue;
            // No marcar una variable global o local redeclarada en un bloque visible.
            boolean otraDeclaracionVisible = false;
            for (int d : declaraciones) {
                if (d >= i || !tokens.get(d).getLexema().equals(token.getLexema())) continue;
                int apertura = bloque[d];
                if (apertura == -1 || (cierre[apertura] > i && apertura < i)) {
                    otraDeclaracionVisible = true;
                    break;
                }
            }
            if (otraDeclaracionVisible) continue;

            errores.add(new ErrorSemantico(
                    token.getLinea(),
                    "Declarar '" + token.getLexema() + "' en un bloque accesible",
                    "Error 2 - Variable fuera de alcance (scope): '" + token.getLexema()
                            + "' se declaró dentro de un bloque { } que ya terminó. "
                            + "No puede utilizarse fuera de ese bloque."
            ));
        }
        return errores;
    }
}
