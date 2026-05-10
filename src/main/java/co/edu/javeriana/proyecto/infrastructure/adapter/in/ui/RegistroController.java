package co.edu.javeriana.proyecto.infrastructure.adapter.in.ui;

import co.edu.javeriana.proyecto.application.usecase.RegistrarUsuarioUseCase;
import co.edu.javeriana.proyecto.domain.exception.UsuarioYaExisteException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RegistroController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblEmailError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblMensajeGlobal;
    @FXML private Button btnRegistrar;
    @FXML private ProgressIndicator progressIndicator;

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;

    public RegistroController(RegistrarUsuarioUseCase registrarUsuarioUseCase) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
    }

    @FXML
    public void initialize() {
        // Validaciones en tiempo real
        txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !newVal.contains("@")) {
                lblEmailError.setVisible(true);
                lblEmailError.setManaged(true);
                txtEmail.setStyle("-fx-border-color: #e74c3c; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            } else {
                lblEmailError.setVisible(false);
                lblEmailError.setManaged(false);
                txtEmail.setStyle("-fx-border-color: #2ecc71; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            }
        });

        txtPassword.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 6) {
                lblPasswordError.setVisible(true);
                lblPasswordError.setManaged(true);
                txtPassword.setStyle("-fx-border-color: #e74c3c; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            } else {
                lblPasswordError.setVisible(false);
                lblPasswordError.setManaged(false);
                txtPassword.setStyle("-fx-border-color: #2ecc71; -fx-padding: 8px; -fx-background-radius: 5px; -fx-border-radius: 5px;");
            }
        });

        btnRegistrar.setOnAction(e -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText();
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (nombre.isEmpty() || email.isEmpty() || password.length() < 6 || !email.contains("@")) {
            mostrarMensaje("Por favor, corrige los errores antes de continuar.", Color.web("#e74c3c"));
            return;
        }

        bloquearUI(true);
        mostrarMensaje("Registrando...", Color.web("#3498db"));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                registrarUsuarioUseCase.ejecutar(email, password, nombre);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            bloquearUI(false);
            mostrarMensaje("¡Registro exitoso! Ya puedes iniciar sesión.", Color.web("#2ecc71"));
            // Cerrar el modal después de 2 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> {
                        Stage stage = (Stage) btnRegistrar.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        task.setOnFailed(e -> {
            bloquearUI(false);
            Throwable exception = task.getException();
            if (exception instanceof UsuarioYaExisteException) {
                mostrarMensaje(exception.getMessage(), Color.web("#e74c3c"));
            } else if (exception instanceof IllegalArgumentException) {
                mostrarMensaje(exception.getMessage(), Color.web("#e74c3c"));
            } else {
                mostrarMensaje("Error interno al registrar.", Color.web("#e74c3c"));
                exception.printStackTrace();
            }
        });

        new Thread(task).start();
    }

    private void mostrarMensaje(String mensaje, Color color) {
        lblMensajeGlobal.setText(mensaje);
        lblMensajeGlobal.setTextFill(color);
        lblMensajeGlobal.setVisible(true);
        lblMensajeGlobal.setManaged(true);
    }

    private void bloquearUI(boolean bloqueado) {
        txtNombre.setDisable(bloqueado);
        txtEmail.setDisable(bloqueado);
        txtPassword.setDisable(bloqueado);
        btnRegistrar.setDisable(bloqueado);
        progressIndicator.setVisible(bloqueado);
        progressIndicator.setManaged(bloqueado);
        if (bloqueado) {
            btnRegistrar.setVisible(false);
            btnRegistrar.setManaged(false);
        } else {
            btnRegistrar.setVisible(true);
            btnRegistrar.setManaged(true);
        }
    }
}
