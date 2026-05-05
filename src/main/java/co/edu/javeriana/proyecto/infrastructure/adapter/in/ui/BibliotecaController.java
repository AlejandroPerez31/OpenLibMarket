package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.BuscarLibroUseCase;
import co.edu.javeriana.proyecto.application.usecase.IncrementarClicsUseCase;
import co.edu.javeriana.proyecto.application.usecase.ObtenerTendenciasUseCase;
import co.edu.javeriana.proyecto.domain.Libro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

public class BibliotecaController {

    @FXML
    private TextField txtBusqueda;

    @FXML
    private ListView<Libro> listResultados;

    @FXML
    private ListView<Libro> listTendencias;

    @FXML
    private Label lblEstado;

    private final BuscarLibroUseCase buscarLibroUseCase;
    private final ObtenerTendenciasUseCase obtenerTendenciasUseCase;
    private final IncrementarClicsUseCase incrementarClicsUseCase;

    private final ObservableList<Libro> resultadosObservable = FXCollections.observableArrayList();
    private final ObservableList<Libro> tendenciasObservable = FXCollections.observableArrayList();

    public BibliotecaController(BuscarLibroUseCase buscarLibroUseCase, 
                                ObtenerTendenciasUseCase obtenerTendenciasUseCase,
                                IncrementarClicsUseCase incrementarClicsUseCase) {
        this.buscarLibroUseCase = buscarLibroUseCase;
        this.obtenerTendenciasUseCase = obtenerTendenciasUseCase;
        this.incrementarClicsUseCase = incrementarClicsUseCase;
    }

    @FXML
    public void initialize() {
        listResultados.setItems(resultadosObservable);
        listTendencias.setItems(tendenciasObservable);

        listResultados.setCellFactory(param -> new LibroCell());
        listTendencias.setCellFactory(param -> new LibroCell());

        // Evento cuando se selecciona un libro (aumenta clics)
        listResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                registrarClic(newVal);
            }
        });

        // Evento de búsqueda mientras escribe
        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            buscarLibros(newVal);
        });

        // Cargar tendencias iniciales
        cargarTendencias();
    }

    private void buscarLibros(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            resultadosObservable.clear();
            return;
        }

        lblEstado.setText("Buscando...");
        Task<List<Libro>> task = new Task<>() {
            @Override
            protected List<Libro> call() throws Exception {
                return buscarLibroUseCase.ejecutar(filtro);
            }
        };

        task.setOnSucceeded(e -> {
            resultadosObservable.setAll(task.getValue());
            lblEstado.setText("Búsqueda completada.");
        });

        task.setOnFailed(e -> lblEstado.setText("Error en búsqueda."));

        new Thread(task).start();
    }

    private void cargarTendencias() {
        Task<List<Libro>> task = new Task<>() {
            @Override
            protected List<Libro> call() throws Exception {
                return obtenerTendenciasUseCase.ejecutar(5);
            }
        };

        task.setOnSucceeded(e -> tendenciasObservable.setAll(task.getValue()));
        new Thread(task).start();
    }

    private void registrarClic(Libro libro) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                incrementarClicsUseCase.ejecutar(libro.getId());
                return null;
            }
        };
        
        task.setOnSucceeded(e -> cargarTendencias()); // Recargar tendencias tras el clic
        new Thread(task).start();
    }

    // Clase interna para dar formato a las celdas
    private static class LibroCell extends ListCell<Libro> {
        @Override
        protected void updateItem(Libro libro, boolean empty) {
            super.updateItem(libro, empty);
            if (empty || libro == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox box = new VBox();
                Label lblTitulo = new Label(libro.getTitulo());
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                
                Label lblAutor = new Label("Autor: " + libro.getAutor() + " | Clics: " + libro.getClics());
                lblAutor.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                
                box.getChildren().addAll(lblTitulo, lblAutor);
                setGraphic(box);
            }
        }
    }
}
