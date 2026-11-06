package application.analizador.errores;

import application.analizador.ErrorSemantico;
import application.analizador.Token;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class Error16 {

    private final Set<String> tipos = Set.of("int", "float", "double", "char", "bool");


    public ArrayList<ErrorSemantico> validar(ArrayList<Token> tokens) {
        ArrayList<ErrorSemantico> errores = new ArrayList<>();

        ArrayList<Set<String>> ambitos = new ArrayList<>();
        ambitos.add(new HashSet<>());

        ArrayList<Set<String>> ambitosClases = new ArrayList<>();
        ambitosClases.add(new HashSet<>());


        for (int i = 0; i < tokens.size(); i++) {
            Token tokenActual = tokens.get(i);

            if (tokenActual.getLexema().equals("{")) {
                ambitos.add(new HashSet<>());
                ambitosClases.add(new HashSet<>());
                continue;
            }

            if (tokenActual.getLexema().equals("}")) {

                if (ambitos.size() > 1) {
                    ambitos.remove(ambitos.size() - 1);
                    ambitosClases.remove(ambitosClases.size() - 1);
                }
                continue;
            }



            if (tokenActual.getLexema().equals("class") && i + 1 < tokens.size()) {

                Token siguiente = tokens.get(i + 1);

                if (siguiente.getTipo().equals("Identificador")) {

                    Set<String> clasesActuales =
                            ambitosClases.get(ambitosClases.size() - 1);

                    String nombreClase = siguiente.getLexema();

                    if (clasesActuales.contains(nombreClase)) {

                        errores.add(new ErrorSemantico(
                                siguiente.getLinea(), "nombre diferente", "la clase: '" + nombreClase + "' ya se declaro"
                        ));

                    } else {
                        clasesActuales.add(nombreClase);
                    }
                }
            }



            if (tipos.contains(tokenActual.getLexema()) && i + 1 < tokens.size()) {

                Token siguiente = tokens.get(i + 1);

                if (siguiente.getTipo().equals("Identificador")) {

                    Set<String> ambitoActual = ambitos.get(ambitos.size() - 1);
                    String nombre = siguiente.getLexema();

                    if (ambitoActual.contains(nombre)) {
                        errores.add(new ErrorSemantico(siguiente.getLinea(),"variable diferente","la palabra: '" + nombre + "' ya se declaro"));

                    }else {
                        ambitoActual.add(nombre);
                    }
                }

            }

        }



        return errores;
    }



}
