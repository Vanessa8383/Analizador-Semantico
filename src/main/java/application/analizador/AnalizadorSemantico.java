
package application.analizador;

import java.util.ArrayList;
import application.analizador.errores.Error4;
import application.analizador.errores.Error1;
import application.analizador.errores.Error2;
import application.analizador.errores.Error17;
import application.analizador.errores.Error16;
import application.analizador.errores.Error19;
import application.analizador.errores.Error3;
import application.analizador.errores.Error18;


public class AnalizadorSemantico {
    private ArrayList<ErrorSemantico> errores;

    public AnalizadorSemantico(){
        errores = new ArrayList<>();
    }

    public void analizar(ArrayList<Token> tokens) {
        errores.clear();

        Error4 error4 = new Error4();
        errores.addAll(error4.validar(tokens));

        Error1 error1 = new Error1();
        errores.addAll(error1.validar(tokens));

        Error2 error2 = new Error2();
        errores.addAll(error2.validar(tokens));

        Error17 error17 = new Error17();
        errores.addAll(error17.validar(tokens));

        Error16 error16 = new Error16();
        errores.addAll(error16.validar(tokens));

        Error19 error19=new Error19();
        errores.addAll(error19.validar(tokens));

        Error3 error3 = new Error3();
        errores.addAll(error3.validar(tokens));

        Error18 error18 = new Error18();
        errores.addAll(error18.validar(tokens));

    }

    public ArrayList<ErrorSemantico> getErrores() {
        return errores;
    }
}
