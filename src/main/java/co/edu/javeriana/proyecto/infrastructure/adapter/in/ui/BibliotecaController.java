package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.*;
import co.edu.javeriana.proyecto.domain.CarritoItem;
import co.edu.javeriana.proyecto.domain.Libro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

public class BibliotecaController {

    @FXML private TextField txtBusqueda;
    @FXML private ListView<Libro> listResultados;
    @FXML private ListView<Libro> listTendencias;
    @FXML private Label lblEstado;
    
    // UI Elements for Cart
    @FXML private VBox vboxBusqueda;
    @FXML private VBox vboxCarrito;
    @FXML private Button btnToggleView;
    @FXML private ListView<CarritoItem> listCarrito;
    @FXML private Label lblTotalCarrito;

    private final BuscarLibroUseCase buscarLibroUseCase;
    private final ObtenerTendenciasUseCase obtenerTendenciasUseCase;
    private final IncrementarClicsUseCase incrementarClicsUseCase;
    private final AgregarAlCarritoUseCase agregarAlCarritoUseCase;
    private final EliminarDelCarritoUseCase eliminarDelCarritoUseCase;
    private final VerCarritoUseCase verCarritoUseCase;

    private final ObservableList<Libro> resultadosObservable = FXCollections.observableArrayList();
    private final ObservableList<Libro> tendenciasObservable = FXCollections.observableArrayList();
    private final ObservableList<CarritoItem> carritoObservable = FXCollections.observableArrayList();

    private final String sessionId;

    public BibliotecaController(BuscarLibroUseCase buscarLibroUseCase, 
                                ObtenerTendenciasUseCase obtenerTendenciasUseCase,
                                IncrementarClicsUseCase incrementarClicsUseCase,
                                AgregarAlCarritoUseCase agregarAlCarritoUseCase,
                                EliminarDelCarritoUseCase eliminarDelCarritoUseCase,
                                VerCarritoUseCase verCarritoUseCase) {
        this.buscarLibroUseCase = buscarLibroUseCase;
        this.obtenerTendenciasUseCase = obtenerTendenciasUseCase;
        this.incrementarClicsUseCase = incrementarClicsUseCase;
        this.agregarAlCarritoUseCase = agregarAlCarritoUseCase;
        this.eliminarDelCarritoUseCase = eliminarDelCarritoUseCase;
        this.verCarritoUseCase = verCarritoUseCase;
        this.sessionId = UUID.randomUUID().toString();
    }

    @FXML
    public void initialize() {
        listResultados.setItems(resultadosObservable);
        listTendencias.setItems(tendenciasObservable);
        listCarrito.setItems(carritoObservable);

        listResultados.setCellFactory(param -> new LibroCell());
        listTendencias.setCellFactory(param -> new LibroCell());
        listCarrito.setCellFactory(param -> new CarritoCell());

        listResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) registrarClic(newVal);
        });

        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> buscarLibros(newVal));

        btnToggleView.setOnAction(e -> toggleView());

        cargarTendencias();
        cargarCarrito();
    }

    private void toggleView() {
        if (vboxBusqueda.isVisible()) {
            vboxBusqueda.setVisible(false);
            vboxCarrito.setVisible(true);
            btnToggleView.setText("🏠 Volver al Catálogo");
            cargarCarrito();
        } else {
            vboxCarrito.setVisible(false);
            vboxBusqueda.setVisible(true);
            int totalItems = carritoObservable.stream().mapToInt(CarritoItem::getCantidad).sum();
            btnToggleView.setText("🛒 Ver Carrito (" + totalItems + ")");
        }
    }

    private void buscarLibros(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            resultadosObservable.clear();
            return;
        }
        lblEstado.setText("Buscando...");
        Task<List<Libro>> task = new Task<>() {
            @Override protected List<Libro> call() { return buscarLibroUseCase.ejecutar(filtro); }
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
            @Override protected List<Libro> call() { return obtenerTendenciasUseCase.ejecutar(5); }
        };
        task.setOnSucceeded(e -> tendenciasObservable.setAll(task.getValue()));
        new Thread(task).start();
    }

    private void registrarClic(Libro libro) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { incrementarClicsUseCase.ejecutar(libro.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarTendencias());
        new Thread(task).start();
    }

    private void agregarAlCarrito(Libro libro) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { agregarAlCarritoUseCase.ejecutar(sessionId, libro.getId(), 1); return null; }
        };
        task.setOnSucceeded(e -> cargarCarrito());
        new Thread(task).start();
    }

    private void eliminarDelCarrito(Libro libro) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() { eliminarDelCarritoUseCase.ejecutar(sessionId, libro.getId()); return null; }
        };
        task.setOnSucceeded(e -> cargarCarrito());
        new Thread(task).start();
    }

    private void cargarCarrito() {
        Task<List<CarritoItem>> task = new Task<>() {
            @Override protected List<CarritoItem> call() { return verCarritoUseCase.ejecutar(sessionId); }
        };
        task.setOnSucceeded(e -> {
            carritoObservable.setAll(task.getValue());
            double total = carritoObservable.stream().mapToDouble(CarritoItem::getSubtotal).sum();
            lblTotalCarrito.setText(String.format("$%.2f", total));
            if (vboxBusqueda.isVisible()) {
                int totalItems = carritoObservable.stream().mapToInt(CarritoItem::getCantidad).sum();
                btnToggleView.setText("🛒 Ver Carrito (" + totalItems + ")");
            }
        });
        new Thread(task).start();
    }

    private class LibroCell extends ListCell<Libro> {
        @Override
        protected void updateItem(Libro libro, boolean empty) {
            super.updateItem(libro, empty);
            if (empty || libro == null) {
                setText(null); setGraphic(null);
            } else {
                HBox box = new HBox(10);
                VBox textInfo = new VBox();
                Label lblTitulo = new Label(libro.getTitulo());
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                Label lblAutor = new Label("Autor: " + libro.getAutor() + " | Precio: $" + libro.getPrecio());
                lblAutor.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                textInfo.getChildren().addAll(lblTitulo, lblAutor);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnAdd = new Button("🛒 Añadir");
                btnAdd.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand;");
                btnAdd.setOnAction(e -> {
                    agregarAlCarrito(libro);
                    e.consume(); // Prevent selection trigger
                });

                box.getChildren().addAll(textInfo, spacer, btnAdd);
                setGraphic(box);
            }
        }
    }

    private class CarritoCell extends ListCell<CarritoItem> {
        @Override
        protected void updateItem(CarritoItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null); setGraphic(null);
            } else {
                Libro libro = item.getLibro();
                HBox box = new HBox(10);
                VBox textInfo = new VBox();
                Label lblTitulo = new Label(libro.getTitulo() + " (x" + item.getCantidad() + ")");
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                Label lblSubtotal = new Label("Subtotal: $" + item.getSubtotal());
                lblSubtotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
                textInfo.getChildren().addAll(lblTitulo, lblSubtotal);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button btnRemove = new Button("🗑 Eliminar");
                btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                btnRemove.setOnAction(e -> eliminarDelCarrito(libro));

                box.getChildren().addAll(textInfo, spacer, btnRemove);
                setGraphic(box);
            }
        }
    }
}
