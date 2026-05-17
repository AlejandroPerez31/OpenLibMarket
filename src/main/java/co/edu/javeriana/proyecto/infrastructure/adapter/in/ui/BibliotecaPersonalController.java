package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.ObtenerBibliotecaPersonalUseCase;
import co.edu.javeriana.proyecto.domain.Libro;
import co.edu.javeriana.proyecto.domain.Usuario;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BibliotecaPersonalController {

    @FXML private TextField txtBuscador;
    @FXML private FlowPane flowEstanteria;
    @FXML private VBox vboxVacio;
    @FXML private Button btnExplorar;
    @FXML private Button btnCerrar;

    private final Usuario usuario;
    private final ObtenerBibliotecaPersonalUseCase obtenerBibliotecaPersonalUseCase;
    
    public BibliotecaPersonalController(Usuario usuario, ObtenerBibliotecaPersonalUseCase obtenerBibliotecaPersonalUseCase) {
        this.usuario = usuario;
        this.obtenerBibliotecaPersonalUseCase = obtenerBibliotecaPersonalUseCase;
    }

    @FXML
    public void initialize() {
        btnCerrar.setOnAction(e -> cerrarVentana());
        btnExplorar.setOnAction(e -> cerrarVentana());
        
        txtBuscador.textProperty().addListener((obs, oldVal, newVal) -> cargarLibros(newVal));

        cargarLibros("");
    }

    private void cargarLibros(String filtro) {
        Task<List<Libro>> task = new Task<>() {
            @Override
            protected List<Libro> call() {
                return obtenerBibliotecaPersonalUseCase.ejecutar(usuario.getId(), filtro);
            }
        };

        task.setOnSucceeded(e -> {
            List<Libro> libros = task.getValue();
            flowEstanteria.getChildren().clear();
            
            if (libros.isEmpty()) {
                if (filtro == null || filtro.isEmpty()) {
                    vboxVacio.setVisible(true);
                    vboxVacio.setManaged(true);
                }
            } else {
                vboxVacio.setVisible(false);
                vboxVacio.setManaged(false);
                for (Libro libro : libros) {
                    flowEstanteria.getChildren().add(crearTarjetaLibro(libro));
                }
            }
        });

        new Thread(task).start();
    }

    private VBox crearTarjetaLibro(Libro libro) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(200);

        ImageView imgPortada = new ImageView();
        try {
            imgPortada.setImage(new Image(libro.getPortada(), 120, 160, true, true));
        } catch (Exception ex) {
            // Ignorar
        }
        
        Label lblTitulo = new Label(libro.getTitulo());
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lblTitulo.setWrapText(true);
        lblTitulo.setAlignment(Pos.CENTER);
        
        Label lblAutor = new Label(libro.getAutor());
        lblAutor.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        Button btnLeer = new Button("Leer ahora");
        btnLeer.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
        btnLeer.setMaxWidth(Double.MAX_VALUE);
        btnLeer.setOnAction(e -> simularLectura(libro));
        
        Button btnDescargar = new Button("Descargar (PDF/EPUB)");
        btnDescargar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        btnDescargar.setMaxWidth(Double.MAX_VALUE);
        btnDescargar.setOnAction(e -> simularDescarga(libro));
        
        card.getChildren().addAll(imgPortada, lblTitulo, lblAutor, btnLeer, btnDescargar);
        return card;
    }

    private void simularLectura(Libro libro) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Visor Web - " + libro.getTitulo());
        alert.setHeaderText("Abriendo visor de PDF/EPUB...");
        alert.setContentText("Simulando la lectura en linea de: " + libro.getTitulo() + " por " + libro.getAutor() + ".\n\nEstado: Leido.");
        alert.showAndWait();
    }

    private void simularDescarga(Libro libro) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Descargar " + libro.getTitulo());
        fileChooser.setInitialFileName(libro.getTitulo().replaceAll(" ", "_") + ".pdf");
        
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Contenido simulado descargado de forma segura via Signed URL.\n\nLibro: " + libro.getTitulo() + "\nAutor: " + libro.getAutor());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Descarga Completada");
                alert.setHeaderText(null);
                alert.setContentText("El libro se ha descargado correctamente en:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
