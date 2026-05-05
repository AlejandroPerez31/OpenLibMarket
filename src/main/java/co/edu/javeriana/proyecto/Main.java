package co.edu.javeriana.proyecto;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.application.usecase.BuscarLibroUseCase;
import co.edu.javeriana.proyecto.application.usecase.IncrementarClicsUseCase;
import co.edu.javeriana.proyecto.application.usecase.ObtenerTendenciasUseCase;
import co.edu.javeriana.proyecto.infrastructure.adapter.in.ui.BibliotecaController;
import co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence.JdbcLibroGateway;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Configuracion manual de dependencias (Arquitectura Limpia)
        LibroGateway libroGateway = new JdbcLibroGateway("jdbc:h2:./mylib");
        BuscarLibroUseCase buscarLibroUseCase = new BuscarLibroUseCase(libroGateway);
        ObtenerTendenciasUseCase obtenerTendenciasUseCase = new ObtenerTendenciasUseCase(libroGateway);
        IncrementarClicsUseCase incrementarClicsUseCase = new IncrementarClicsUseCase(libroGateway);

        // Cargar vista FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BibliotecaView.fxml"));
        
        // Inyectar controlador manualmente
        BibliotecaController controller = new BibliotecaController(
                buscarLibroUseCase, 
                obtenerTendenciasUseCase, 
                incrementarClicsUseCase
        );
        loader.setController(controller);

        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        
        primaryStage.setTitle("MyLib - Biblioteca Virtual");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
