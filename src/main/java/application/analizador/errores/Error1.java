
package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Error1 {

    private final Set<String> tipos = Set.of(
            "int", "float", "double", "char", "bool", "string",
            "short", "long", "signed", "unsigned", "auto", "size_t"
    );

    private final Set<String> identificadoresEspeciales = Set.of(
            "std", "cout", "cin", "cerr", "clog", "endl", "flush", "ws",
            "boolalpha", "noboolalpha"
    );

    private boolean esIdentificador(Token token) {
        return token.getTipo().equals("Identificador");
    }

    private boolean esPunteroOReferencia(String lexema) {
        return lexema.equals("*") || lexema.equals("&") || lexema.equals("&&");
    }

    private Map<Integer, String> buscarDeclaraciones(
            ArrayList<Token> tokens, Set<String> tiposValidos) {

        Map<Integer, String> declaraciones = new HashMap<>();

        for (int i = 0; i < tokens.size(); i++) {
            if (!tiposValidos.contains(tokens.get(i).getLexema())) {
                continue;
            }

            int j = i + 1;
            while (j < tokens.size() && esPunteroOReferencia(tokens.get(j).getLexema())) {
                j++;
            }

            if (j >= tokens.size() || !esIdentificador(tokens.get(j))) {
                continue;
            }

            if (j + 1 < tokens.size() && tokens.get(j + 1).getLexema().equals("(")) {
                continue;
            }

            declaraciones.put(j, tokens.get(j).getLexema());

            int parentesis = 0;
            int corchetes = 0;
            int llaves = 0;

            for (int k = j + 1; k < tokens.size(); k++) {
                String lexema = tokens.get(k).getLexema();

                if (lexema.equals(";") && parentesis == 0 && corchetes == 0 && llaves == 0) {
                    break;
                }
                if (lexema.equals(")") && parentesis == 0) {
                    break;
                }
                if (lexema.equals("{") && parentesis == 0 && corchetes == 0 && llaves == 0) {
                    break;
                }

                if (lexema.equals("(")) parentesis++;
                else if (lexema.equals(")")) parentesis--;
                else if (lexema.equals("[")) corchetes++;
                else if (lexema.equals("]")) corchetes--;
                else if (lexema.equals("{")) llaves++;
                else if (lexema.equals("}")) llaves--;

                if (lexema.equals(",") && parentesis == 0 && corchetes == 0 && llaves == 0) {
                    int siguiente = k + 1;
                    while (siguiente < tokens.size()
                            && esPunteroOReferencia(tokens.get(siguiente).getLexema())) {
                        siguiente++;
                    }
                    if (siguiente < tokens.size() && esIdentificador(tokens.get(siguiente))
                            && !tiposValidos.contains(tokens.get(siguiente).getLexema())) {
                        declaraciones.put(siguiente, tokens.get(siguiente).getLexema());
                    }
                }
            }
        }
        return declaraciones;
    }

    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        ArrayList<ErrorSemantico> errores = new ArrayList<>();
        Set<String> tiposValidos = new HashSet<>(tipos);

        for (int i = 0; i + 1 < tokens.size(); i++) {
            String lexema = tokens.get(i).getLexema();
            if ((lexema.equals("class") || lexema.equals("struct"))
                    && esIdentificador(tokens.get(i + 1))) {
                tiposValidos.add(tokens.get(i + 1).getLexema());
            }
        }

        Map<Integer, String> declaraciones = buscarDeclaraciones(tokens, tiposValidos);
        Set<String> variablesDeclaradas = new HashSet<>();
        Set<String> variablesReportadas = new HashSet<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String nombre = token.getLexema();

            if (declaraciones.containsKey(i)) {
                variablesDeclaradas.add(nombre);
                continue;
            }
            if (!esIdentificador(token) || tiposValidos.contains(nombre)
                    || identificadoresEspeciales.contains(nombre)) {
                continue;
            }

            String anterior = i > 0 ? tokens.get(i - 1).getLexema() : "";
            String siguiente = i + 1 < tokens.size() ? tokens.get(i + 1).getLexema() : "";

            if (siguiente.equals("(") || siguiente.equals("::")
                    || anterior.equals("::") || anterior.equals(".")
                    || anterior.equals("->") || anterior.equals("namespace")
                    || anterior.equals("using") || anterior.equals("class")
                    || anterior.equals("struct")) {
                continue;
            }

            if (!variablesDeclaradas.contains(nombre) && variablesReportadas.add(nombre)) {
                errores.add(new ErrorSemantico(
                        token.getLinea(),
                        "Declarar la variable: int " + nombre + ";",
                        "Variable no declarada: '" + nombre
                                + "'. Se utiliza sin haber declarado previamente su tipo de dato "
                                + "y su identificador."
                ));
            }
        }
        return errores;
    }
}
