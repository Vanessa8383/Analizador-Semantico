package application.analizador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TablaController {


    @FXML
    private TableView<Token> tablaTokens;


    @FXML
    private TableColumn<Token, String> columnaLexema;
    @FXML
    private TableColumn<Token, String> columnaTokenTipo;
    @FXML
    private TableColumn<Token, Integer> columnaTokenLinea;
    @FXML
    private TableColumn<Token, Integer> columnaTokenInicio;
    @FXML
    private TableColumn<Token, Integer> columnaTokenFin;

    @FXML
    private TextArea textArea;
    @FXML
    private TextArea numerosLineas;
    @FXML
    private TableView<ErrorSemantico> tablaErrores;
    @FXML
    private TableColumn<ErrorSemantico, Integer> columnaErrorLinea;
    @FXML
    private TableColumn<ErrorSemantico, String> columnaComandoEsperado;
    @FXML
    private TableColumn<ErrorSemantico, String> columnaDescripcion;



    private File archivoSeleccionado;
    private AnalizadorLexico analizador = new AnalizadorLexico();
    private  AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico();

    @FXML
    public void initialize() {

        columnaLexema.setCellValueFactory(new PropertyValueFactory<>("lexema"));
        columnaTokenTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        columnaTokenLinea.setCellValueFactory(new PropertyValueFactory<>("linea"));
        columnaTokenInicio.setCellValueFactory(new PropertyValueFactory<>("inicio"));
        columnaTokenFin.setCellValueFactory(new PropertyValueFactory<>("fin"));

        tablaTokens.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        columnaLexema.setPrefWidth(180);
        columnaTokenTipo.setPrefWidth(160);
        columnaTokenLinea.setPrefWidth(80);
        columnaTokenInicio.setPrefWidth(80);
        columnaTokenFin.setPrefWidth(80);

        columnaErrorLinea.setCellValueFactory(new PropertyValueFactory<>("linea"));
        columnaComandoEsperado.setCellValueFactory(new PropertyValueFactory<>("comandoEsperado"));

        columnaDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaErrores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        columnaErrorLinea.setPrefWidth(120);
        columnaComandoEsperado.setPrefWidth(180);
        columnaDescripcion.setPrefWidth(400);
        textArea.textProperty().addListener((obs, anterior, nuevo) -> actualizarLineas());
        textArea.scrollTopProperty().addListener((obs, anterior, nuevo) ->
                numerosLineas.setScrollTop(nuevo.doubleValue()));
        actualizarLineas();


    }


    @FXML
    private void abrirArchivo() throws IOException {

        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccionar archivo C++");

        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos C++", "*.cpp"));
        archivoSeleccionado = selector.showOpenDialog(tablaTokens.getScene().getWindow());

        if (archivoSeleccionado !=null) {
            String contenido = Files.readString(archivoSeleccionado.toPath());
            textArea.setText(contenido);
        }
    }


    @FXML
    private void analizarCodigo() {
        if (archivoSeleccionado == null) {

            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Archivo no seleccionado");
            alerta.setHeaderText(null);
            alerta.setContentText("Selecciona un archivo .cpp.");
            alerta.showAndWait();

            return;
        }

        analizador.analizarArchivo(archivoSeleccionado.getAbsolutePath());
        tablaTokens.setItems(FXCollections.observableArrayList(analizador.getTokens()));
        analizadorSemantico.analizar(analizador.getTokens());
        System.out.println("Errores detectados: " + analizadorSemantico.getErrores().size());
        tablaErrores.setItems(FXCollections.observableArrayList(analizadorSemantico.getErrores()));
    }


    @FXML
    private void limpiarEditor() {

        tablaTokens.getItems().clear();
        tablaErrores.getItems().clear();
        textArea.clear();
        archivoSeleccionado = null;
    }

    private void actualizarLineas() {
        int total = textArea.getText().split("\n", -1).length;
        StringBuilder numeros = new StringBuilder();

        for (int i = 1; i <= total; i++) {
            numeros.append(i);
            if (i < total) {
                numeros.append("\n");
            }
        }

        numerosLineas.setText(numeros.toString());
    }

}

