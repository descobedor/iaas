# gateway_iaas

`gateway_iaas` es un microservicio de Spring Boot que actúa como pasarela para orquestar peticiones hacia diferentes proveedores de inferencia de IA. Incluye un controlador REST para registrar proveedores en memoria y enrutar solicitudes de inferencia a uno de ellos mediante una estrategia round-robin simple.

## Requisitos

- Java 17
- Maven 3.9+

## Ejecución

```bash
mvn spring-boot:run
```

Esto iniciará el servidor en `http://localhost:8080`.

## Endpoints

- `POST /providers`: Registra un proveedor de inferencia.
- `GET /providers`: Lista los proveedores registrados.
- `POST /inference`: Envía una solicitud de inferencia y la enruta a un proveedor disponible.

Consulta `src/main/resources/application.yml` para ver la configuración disponible.

## Tests

```bash
mvn test
```
