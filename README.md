# OpenLib Market - Plan de Entrega 1 (MVP)

Este repositorio contiene la primera versión del proyecto **OpenLib Market**, estructurado bajo el patrón de **Arquitectura Limpia (Clean Architecture)**. 

## Alcance Funcional Entrega 1

### MÓDULO DE BUYER (Storefront)
* **Registro y login (PROYECTOSW-26):** Autenticación y control de roles de usuario.
* **Catalogo Interactivo (PROYECTOSW-24):** Panel principal de la tienda.
* **Filtros Avanzados (PROYECTOSW-34):** Búsqueda específica por etiquetas, ISBN y categorías.
* **Recomendaciones (PROYECTOSW-37):** Motor de libros sugeridos al usuario en base al histórico.
* **Ver Detalles y Reviews (PROYECTOSW-36):** Vista ampliada del libro incorporando sistema de reseñas.
* **Lista de Favoritos (PROYECTOSW-38):** Funcionalidad de Wishlist.
* **Carrito de Compras (PROYECTOSW-25):** Gestión de artículos a comprar o descargar.
* **Checkout Simulado (PROYECTOSW-27):** Proceso de facturación y transacción simbólica.
* **Biblioteca y Descargas Seguras (PROYECTOSW-41):** Acceso a descargas protegidas por la sesión del cliente (URLs seguras).
* **Historial de Compras (PROYECTOSW-33):** Registro y control de gastos del usuario.

### MÓDULO SELLER (Creación)
* **Registro y login (PROYECTOSW-26):** Acceso exclusivo como publicador o creador.
* **Gestion de publicacion (PROYECTOSW-28):** Permitir subir materiales, definir portadas, adjuntar el PDF/ePub, y poner metadatos.
* **Editar stock/precios (PROYECTOSW-32):** Actualización de los datos del libro ya publicado.
* **Ver Ventas (PROYECTOSW-31 / PROYECTOSW-40):** Revisar cantidad de descargas/adquisiciones e informes estadísticos.

### MÓDULO ADMIN (Storeback)
* **Dashboard de Metricas (PROYECTOSW-1):** Gráficos o listados rápidos de libros más descargados, usuarios activos, categorías populares.
* **Moderacion y Calidad (PROYECTOSW-30):** Curaduría de reseñas, revisión de catálogos y libros subidos.
* **Gestion de usuarios y plataforma (PROYECTOSW-29):** Control absoluto del catálogo, etiquetas, categorías y banear/gestionar clientes.

---

## Atributos de Calidad y Arquitectura

* **Seguridad:** Autenticación manejada mediante JWT y contraseñas encriptadas.
* **Performance:** Consultas optimizadas con paginación en PostgreSQL para respuesta rápida.
* **Arquitectura:** Controles en capa (MVC) limpios para Spring Boot.

### Stack Tecnológico Práctico
* **Backend:** Java 21 o superior, y Spring Boot 3 o superior (REST, JPA/Hibernate, Spring Security).
* **Base de Datos:** PostgreSQL para transaccionalidad robusta. Guardado local de bytes/pdfs. *(Nota: En esta primera iteración se emplea H2 Embebido para desarrollo y pruebas)*.
* **Frontend:** Aplicación de escritorio moderna en JavaFX separando lógicas de red con llamadas HTTP asíncronas.

---

## Explicación de la Arquitectura - Historia (Buscar Libro)

El proyecto implementa el patrón de **Arquitectura Limpia (Clean Architecture)**, dividiendo el código en capas concéntricas para que la lógica de negocio no dependa de la base de datos ni de la interfaz gráfica. A continuación se detalla la estructura implementada en esta primera entrega:

### 1. Capa de Dominio (`domain/Libro.java`)
Es el corazón de la aplicación. Aquí definimos la entidad `Libro` (id, titulo, autor, clics). Esta clase es Java "puro": no depende de ninguna base de datos ni de interfaces gráficas. Es agnóstica a la tecnología externa.

### 2. Capa de Aplicación (`application/...`)
Define los casos de uso y reglas de negocio.
* **`LibroGateway.java`:** Interfaz (Puerto de salida) que define los contratos de persistencia (buscar libros, ver tendencias, aumentar clics).
* **Casos de Uso (`BuscarLibroUseCase`, `ObtenerTendenciasUseCase`, `IncrementarClicsUseCase`):** Intermediarios que reciben las órdenes de la UI y utilizan el `LibroGateway`. Respetan el Principio de Responsabilidad Única (SOLID).

### 3. Capa de Infraestructura: Persistencia (`infrastructure/adapter/out/persistence/...`)
Implementación técnica de la persistencia de datos usando la base de datos H2.
* **`JdbcLibroGateway.java`:** Implementa la interfaz `LibroGateway`. Crea la tabla de libros mediante SQL si no existe e inyecta libros de prueba. Utiliza la base de datos H2 en modo embebido (`jdbc:h2:./mylib`) guardando los datos en `mylib.mv.db`. Emplea `PreparedStatement` para prevenir inyección SQL y optimizar las búsquedas (LIKE).

### 4. Capa de Infraestructura: Interfaz Gráfica (`infrastructure/adapter/in/ui/...`)
Maneja la presentación y la interacción del usuario.
* **`BibliotecaView.fxml`:** Archivo XML declarativo que define la interfaz gráfica (botones, listas, diseño).
* **`BibliotecaController.java`:** El "cerebro" de la pantalla. Recibe los casos de uso por inyección de dependencias.
* **Hilos Concurrentes (`javafx.concurrent.Task`):** Las consultas a base de datos se envuelven en un `Task` y se ejecutan en un hilo secundario (`new Thread()`). Esto previene que la interfaz se congele, manteniendo la aplicación fluida mientras H2 procesa la solicitud.

### 5. Configuración y Arranque (`Main.java` y `Launcher.java`)
* **`Main.java`:** El punto de ensamblaje (Composición Root). Aquí se instancia la conexión a la base de datos (`JdbcLibroGateway`), se inyecta a los Casos de Uso, y éstos a su vez al Controlador. Se utiliza Inyección de Dependencias Manual.
* **`Launcher.java`:** Clase encargada de arrancar `Main.main()`. Soluciona las restricciones de módulos introducidas desde Java 11 para JavaFX al ejecutar la aplicación directamente desde el IDE.

### 6. Pruebas de Integración (`JdbcLibroGatewayTest.java`)
Entorno de pruebas automatizadas que utiliza una base de datos en memoria (`jdbc:h2:mem:testdb`). Asegura que las operaciones SQL funcionen correctamente y se destruye al terminar la prueba sin afectar los datos reales de la aplicación.

> **Resumen:** Se ha diseñado un sistema donde la UI se comunica con los Casos de Uso, éstos con la Interfaz del Gateway, y finalmente la implementación en H2 maneja la persistencia. Esto garantiza un código escalable, fácil de mantener y de testear.
