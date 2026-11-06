package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Token;

import java.util.ArrayList;
import java.util.Set;

public class Error17 {

    private final Set<String> tipos = Set.of(
            "int", "float", "double", "char", "bool", "string",
            "short", "long", "signed", "unsigned", "auto", "size_t"
    );

    private boolean esLiteral(Token token) {
        String tipo = token.getTipo();
        String lexema = token.getLexema();
        return tipo.equals("Entero") || tipo.equals("Decimal")
                || tipo.equals("Cadena") || tipo.equals("Caracter")
                || lexema.equals("true") || lexema.equals("false")
                || lexema.equals("nullptr");
    }

    private boolean esOperadorAritmetico(String lexema) {
        return lexema.equals("+") || lexema.equals("-") || lexema.equals("*")
                || lexema.equals("/") || lexema.equals("%");
    }

    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        ArrayList<ErrorSemantico> errores = new ArrayList<>();

        for (int i = 0; i + 3 < tokens.size(); i++) {
            // Patron: tipo & identificador =
            if (!tipos.contains(tokens.get(i).getLexema())) continue;
            if (!tokens.get(i + 1).getLexema().equals("&")) continue;
            if (!tokens.get(i + 2).getTipo().equals("Identificador")) continue;
            if (!tokens.get(i + 3).getLexema().equals("=")) continue;

            // "const int &r = 10;" si es valido
            if (i > 0 && tokens.get(i - 1).getLexema().equals("const")) continue;

            // Expresion a la derecha del "=" hasta el ";"
            ArrayList<Token> derecha = new ArrayList<>();
            for (int k = i + 4; k < tokens.size()
                    && !tokens.get(k).getLexema().equals(";"); k++) {
                derecha.add(tokens.get(k));
            }
            if (derecha.isEmpty()) continue;

            boolean esRvalue = false;

            if (derecha.size() == 1 && esLiteral(derecha.get(0))) {
                esRvalue = true;
            } else {
                boolean tieneLlamada = false;
                for (Token t : derecha) {
                    if (t.getLexema().equals("(")) tieneLlamada = true;
                }
                // Un '*' o '-' inicial es unario, no cuenta
                for (int k = 1; k < derecha.size() && !tieneLlamada; k++) {
                    if (esOperadorAritmetico(derecha.get(k).getLexema())) {
                        esRvalue = true;
                        break;
                    }
                }
            }

            if (esRvalue) {
                Token variable = tokens.get(i + 2);
                StringBuilder expresion = new StringBuilder();
                for (Token t : derecha) {
                    if (expresion.length() > 0) expresion.append(" ");
                    expresion.append(t.getLexema());
                }
                errores.add(new ErrorSemantico(
                        variable.getLinea(),
                        tokens.get(i).getLexema() + " &" + variable.getLexema()
                                + " = <variable>;",
                        "Referencia incorrecta: '" + variable.getLexema()
                                + "' se inicializa con '" + expresion
                                + "', un valor temporal (rvalue). Una referencia debe "
                                + "asignarse a una variable con posicion valida en memoria (lvalue)."
                ));
            }
        }
        return errores;
    }
}