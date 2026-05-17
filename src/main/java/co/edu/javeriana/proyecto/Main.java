package co.edu.javeriana.proyecto;

import co.edu.javeriana.proyecto.application.port.out.LibroGateway;
import co.edu.javeriana.proyecto.application.port.out.UsuarioGateway;
import co.edu.javeriana.proyecto.application.port.out.CompraGateway;
import co.edu.javeriana.proyecto.application.usecase.*;
import co.edu.javeriana.proyecto.infrastructure.adapter.in.ui.BibliotecaController;

import co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence.JdbcCarritoGateway;
import co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence.JdbcCompraGateway;
import co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence.JdbcLibroGateway;
import co.edu.javeriana.proyecto.infrastructure.adapter.out.persistence.JdbcUsuarioGateway;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final String DB_URL = "jdbc:h2:./mylib";

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Configuracion manual de dependencias (Arquitectura Limpia)
        LibroGateway libroGateway = new JdbcLibroGateway(DB_URL);
        co.edu.javeriana.proyecto.application.port.out.CarritoGateway carritoGateway = new JdbcCarritoGateway(DB_URL);
        UsuarioGateway usuarioGateway = new JdbcUsuarioGateway(DB_URL);
        CompraGateway compraGateway = new JdbcCompraGateway(DB_URL);

        BuscarLibroUseCase buscarLibroUseCase             = new BuscarLibroUseCase(libroGateway);
        BuscarLibroAvanzadoUseCase buscarLibroAvanzadoUseCase = new BuscarLibroAvanzadoUseCase(libroGateway);
        ObtenerTendenciasUseCase obtenerTendenciasUseCase = new ObtenerTendenciasUseCase(libroGateway);
        IncrementarClicsUseCase incrementarClicsUseCase   = new IncrementarClicsUseCase(libroGateway);
        AgregarAlCarritoUseCase agregarAlCarritoUseCase   = new AgregarAlCarritoUseCase(carritoGateway);
        EliminarDelCarritoUseCase eliminarDelCarritoUseCase = new EliminarDelCarritoUseCase(carritoGateway);
        VerCarritoUseCase verCarritoUseCase               = new VerCarritoUseCase(carritoGateway);
        LimpiarCarritoUseCase limpiarCarritoUseCase       = new LimpiarCarritoUseCase(carritoGateway);
        RegistrarCompraUseCase registrarCompraUseCase     = new RegistrarCompraUseCase(compraGateway);
        ObtenerRecomendacionesUseCase obtenerRecomendacionesUseCase = new ObtenerRecomendacionesUseCase(compraGateway);
        ObtenerHistorialOrdenesUseCase obtenerHistorialOrdenesUseCase = new ObtenerHistorialOrdenesUseCase(compraGateway);
        GenerarFacturaPdfUseCase generarFacturaPdfUseCase = new GenerarFacturaPdfUseCase();
        ObtenerBibliotecaPersonalUseCase obtenerBibliotecaPersonalUseCase = new ObtenerBibliotecaPersonalUseCase(compraGateway);
        RegistrarUsuarioUseCase registrarUsuarioUseCase   = new RegistrarUsuarioUseCase(usuarioGateway);
        LoginUsuarioUseCase loginUsuarioUseCase           = new LoginUsuarioUseCase(usuarioGateway);
        CambiarContrasenaUseCase cambiarContrasenaUseCase = new CambiarContrasenaUseCase(usuarioGateway);

        BibliotecaController controller = new BibliotecaController(
                buscarLibroUseCase,
                buscarLibroAvanzadoUseCase,
                obtenerTendenciasUseCase,
                incrementarClicsUseCase,
                agregarAlCarritoUseCase,
                eliminarDelCarritoUseCase,
                verCarritoUseCase,
                limpiarCarritoUseCase,
                registrarCompraUseCase,
                obtenerRecomendacionesUseCase,
                obtenerHistorialOrdenesUseCase,
                generarFacturaPdfUseCase,
                obtenerBibliotecaPersonalUseCase,
                registrarUsuarioUseCase,
                loginUsuarioUseCase,
                cambiarContrasenaUseCase,
                usuarioGateway
        );
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BibliotecaView.fxml"));
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
