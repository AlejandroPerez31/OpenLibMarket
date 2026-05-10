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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.UUID;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class BibliotecaController {

    @FXML private ImageView imgFondo1;
    @FXML private ImageView imgFondo2;
    @FXML private StackPane paneFondoBase;
    @FXML private VBox vboxResultadosContainer;

    @FXML private TextField txtBusqueda;
    @FXML private ListView<Libro> listResultados;
    @FXML private ListView<Libro> listTendencias;
    @FXML private Label lblEstado;
    @FXML private Label lblFraseDia;
    
    @FXML private VBox vboxBusqueda;
    @FXML private VBox vboxCarrito;
    @FXML private Button btnToggleView;
    @FXML private Button btnContinuarProceso;
    @FXML private ListView<CarritoItem> listCarrito;
    @FXML private Label lblTotalCarrito;

    private final BuscarLibroUseCase buscarLibroUseCase;
    private final ObtenerTendenciasUseCase obtenerTendenciasUseCase;
    private final IncrementarClicsUseCase incrementarClicsUseCase;
    private final AgregarAlCarritoUseCase agregarAlCarritoUseCase;
    private final EliminarDelCarritoUseCase eliminarDelCarritoUseCase;
    private final VerCarritoUseCase verCarritoUseCase;
    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    private final ObservableList<Libro> resultadosObservable = FXCollections.observableArrayList();
    private final ObservableList<Libro> tendenciasObservable = FXCollections.observableArrayList();
    private final ObservableList<CarritoItem> carritoObservable = FXCollections.observableArrayList();

    private final String sessionId;
    
    private final String[] imagesArray = {"/images/hero_banner.png", "/images/hero_banner_2.png", "/images/hero_banner_3.png"};
    private int currentImageIndex = 0;

    public BibliotecaController(BuscarLibroUseCase buscarLibroUseCase, 
                                ObtenerTendenciasUseCase obtenerTendenciasUseCase,
                                IncrementarClicsUseCase incrementarClicsUseCase,
                                AgregarAlCarritoUseCase agregarAlCarritoUseCase,
                                EliminarDelCarritoUseCase eliminarDelCarritoUseCase,
                                VerCarritoUseCase verCarritoUseCase,
                                RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.buscarLibroUseCase = buscarLibroUseCase;
        this.obtenerTendenciasUseCase = obtenerTendenciasUseCase;
        this.incrementarClicsUseCase = incrementarClicsUseCase;
        this.agregarAlCarritoUseCase = agregarAlCarritoUseCase;
        this.eliminarDelCarritoUseCase = eliminarDelCarritoUseCase;
        this.verCarritoUseCase = verCarritoUseCase;
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.sessionId = UUID.randomUUID().toString();
    }

    @FXML
    public void initialize() {
        listResultados.setItems(resultadosObservable);
        listTendencias.setItems(tendenciasObservable);
        listCarrito.setItems(carritoObservable);

        listResultados.setCellFactory(param -> new LibroCell(false));
        listTendencias.setCellFactory(param -> new LibroCell(true));
        listCarrito.setCellFactory(param -> new CarritoCell());

        listResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) registrarClic(newVal);
        });

        btnToggleView.setOnAction(e -> toggleView());
        btnContinuarProceso.setOnAction(e -> abrirRegistroModal());

        configurarFraseDelDia();
        configurarFondoYAnimaciones();
        cargarTendencias();
        cargarCarrito();
    }

    private void configurarFondoYAnimaciones() {
        try {
            imgFondo1.setImage(new Image(getClass().getResource(imagesArray[0]).toExternalForm()));
            
            imgFondo1.fitWidthProperty().bind(paneFondoBase.widthProperty());
            imgFondo1.fitHeightProperty().bind(paneFondoBase.heightProperty());
            imgFondo2.fitWidthProperty().bind(paneFondoBase.widthProperty());
            imgFondo2.fitHeightProperty().bind(paneFondoBase.heightProperty());

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(7), e -> rotarImagenFondo()));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        } catch (Exception ex) {
            System.err.println("Error cargando imágenes de fondo: " + ex.getMessage());
        }

        GaussianBlur blurEffect = new GaussianBlur(0);
        paneFondoBase.setEffect(blurEffect);

        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                vboxResultadosContainer.setVisible(true);
                vboxResultadosContainer.setManaged(true);
                blurEffect.setRadius(25); 
            } else {
                vboxResultadosContainer.setVisible(false);
                vboxResultadosContainer.setManaged(false);
                blurEffect.setRadius(txtBusqueda.isFocused() ? 10 : 0);
            }
            buscarLibros(newVal);
        });

        txtBusqueda.focusedProperty().addListener((obs, oldVal, isFocused) -> {
            if (txtBusqueda.getText() == null || txtBusqueda.getText().trim().isEmpty()) {
                blurEffect.setRadius(isFocused ? 10 : 0);
            }
        });
    }

    private void rotarImagenFondo() {
        try {
            int nextIndex = (currentImageIndex + 1) % imagesArray.length;
            Image nextImage = new Image(getClass().getResource(imagesArray[nextIndex]).toExternalForm());
            
            imgFondo2.setImage(nextImage);
            
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.5), imgFondo2);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            fadeIn.setOnFinished(e -> {
                imgFondo1.setImage(nextImage);
                imgFondo2.setOpacity(0.0);
                currentImageIndex = nextIndex;
                configurarFraseDelDia(); // Cambiar frase al mismo tiempo
            });
            
            fadeIn.play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void configurarFraseDelDia() {
        if (lblFraseDia != null) {
            String[] frases = {
                "\"La pluma es la lengua del alma.\" – Miguel de Cervantes",
                "\"De los diversos instrumentos del hombre, el más asombroso es el libro. Es una extensión de la memoria y de la imaginación.\" – Jorge Luis Borges",
                "\"El que lee mucho y anda mucho, ve mucho y sabe mucho.\" – Miguel de Cervantes",
                "\"Estar a solas con un buen libro es ser capaz de comprenderte más a ti mismo.\" – Harold Bloom",
                "\"Aprender a leer es encender un fuego; cada sílaba que se deletrea es una chispa.\" – Victor Hugo",
                "\"Siempre imaginé que el Paraíso sería algún tipo de biblioteca.\" – Jorge Luis Borges",
                "\"Un libro debe ser el hacha que rompa el mar helado dentro de nosotros.\" – Franz Kafka",
                "\"Uno no es lo que es por lo que escribe, sino por lo que ha leído.\" – Jorge Luis Borges",
                "\"La vida es un cuento contado por un idiota, lleno de ruido y de furia, que no tiene ningún sentido.\" – William Shakespeare",
                "\"El mundo habrá acabado de joderse el día en que los hombres viajen en primera clase y la literatura en el vagón de carga.\" – Gabriel García Márquez"
            };
            int index = new java.util.Random().nextInt(frases.length);
            lblFraseDia.setText(frases[index]);
        }
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

    private void abrirRegistroModal() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/RegistroView.fxml"));
            RegistroController registroController = new RegistroController(registrarUsuarioUseCase);
            loader.setController(registroController);
            
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Registro de Usuario");
            stage.setScene(new javafx.scene.Scene(root, 400, 450));
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (java.io.IOException e) {
            e.printStackTrace();
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
        private final boolean isDarkTheme;

        public LibroCell(boolean isDarkTheme) {
            this.isDarkTheme = isDarkTheme;
        }

        @Override
        protected void updateItem(Libro libro, boolean empty) {
            super.updateItem(libro, empty);
            if (empty || libro == null) {
                setText(null); setGraphic(null);
            } else {
                HBox box = new HBox(10);
                box.prefWidthProperty().bind(getListView().widthProperty().subtract(30));
                
                VBox textInfo = new VBox();
                textInfo.setMinWidth(0); 
                HBox.setHgrow(textInfo, Priority.ALWAYS); 
                
                String colorPrincipal = isDarkTheme ? "#ecf0f1" : "#2c3e50";
                String colorSecundario = isDarkTheme ? "#bdc3c7" : "#7f8c8d";

                Label lblTitulo = new Label(libro.getTitulo());
                lblTitulo.setWrapText(true);
                lblTitulo.setMinHeight(Region.USE_PREF_SIZE); 
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + colorPrincipal + ";");
                
                Label lblAutor = new Label("Autor: " + libro.getAutor() + " | Precio: $" + libro.getPrecio());
                lblAutor.setStyle("-fx-font-size: 12px; -fx-text-fill: " + colorSecundario + ";");
                lblAutor.setWrapText(true);
                lblAutor.setMinHeight(Region.USE_PREF_SIZE);
                
                textInfo.getChildren().addAll(lblTitulo, lblAutor);

                Button btnAdd = new Button("🛒 Añadir");
                btnAdd.setMinWidth(Region.USE_PREF_SIZE); // EVITAR QUE SE CORTE
                btnAdd.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                btnAdd.setOnAction(e -> {
                    agregarAlCarrito(libro);
                    e.consume(); // Prevent selection trigger
                });

                box.getChildren().addAll(textInfo, btnAdd);
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
                box.prefWidthProperty().bind(getListView().widthProperty().subtract(30));
                
                VBox textInfo = new VBox();
                textInfo.setMinWidth(0);
                HBox.setHgrow(textInfo, Priority.ALWAYS);
                
                Label lblTitulo = new Label(libro.getTitulo() + " (x" + item.getCantidad() + ")");
                lblTitulo.setWrapText(true);
                lblTitulo.setMinHeight(Region.USE_PREF_SIZE);
                lblTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                
                Label lblSubtotal = new Label("Subtotal: $" + item.getSubtotal());
                lblSubtotal.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
                textInfo.getChildren().addAll(lblTitulo, lblSubtotal);

                Button btnRemove = new Button("🗑 Eliminar");
                btnRemove.setMinWidth(Region.USE_PREF_SIZE); // EVITAR QUE SE CORTE
                btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                btnRemove.setOnAction(e -> eliminarDelCarrito(libro));

                box.getChildren().addAll(textInfo, btnRemove);
                setGraphic(box);
            }
        }
    }
}
