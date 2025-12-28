# gateway_iaas

`gateway_iaas` es un microservicio de Spring Boot que actúa como pasarela para orquestar peticiones hacia diferentes proveedores de inferencia de IA. Incluye un controlador REST para registrar proveedores en memoria y enrutar solicitudes de inferencia a uno de ellos mediante una estrategia round-robin simple.

Además expone una interfaz **Model Context Protocol (MCP)** vía JSON-RPC en `POST /mcp` para listar herramientas (`tools/list`) y ejecutar `extract_menu_text` con imágenes base64.

## Arquitectura

```mermaid
flowchart LR
    Client[Cliente/Agente MCP] -->|JSON-RPC tools/list| MCP[MCP Controller]
    Client -->|JSON-RPC tools/call| MCP
    Client -->|OCR multipart/base64| OCR[OCR Controller]
    MCP -->|Base64| OCRService[TesseractOcrService]
    OCR -->|Imagen| OCRService
    OCRService -->|OCR| Tesseract[Tesseract (nativo)]
    MCP -->|Respuesta MCP| Client
    OCR -->|JSON| Client
```

## Requisitos

- Java 17
- Maven 3.9+

## Ejecución

```bash
mvn spring-boot:run
```

Esto iniciará el servidor en `http://localhost:8080`.

## Ejecutar con Docker (Tesseract incluido)

El `Dockerfile` instala Tesseract y los datos de idioma en la imagen final para que el OCR funcione sin dependencias locales.

### Build de la imagen

```bash
docker build -t gateway-iaas .
```

### Ejecutar el contenedor

```bash
docker run --rm -p 8080:8080 gateway-iaas
```

El servicio quedará disponible en `http://localhost:8080`.

## Endpoints

- `POST /providers`: Registra un proveedor de inferencia.
- `GET /providers`: Lista los proveedores registrados.
- `POST /inference`: Envía una solicitud de inferencia y la enruta a un proveedor disponible.
- `POST /mcp`: Endpoint MCP (JSON-RPC) para `tools/list` y `tools/call`.

Consulta `src/main/resources/application.yml` para ver la configuración disponible.

## MCP (Model Context Protocol)

### Listar tools

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"tools/list"}'
```

### Ejecutar OCR vía MCP

```bash
BASE64_IMAGE=$(base64 -w 0 menu.jpg)

curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d "{\"jsonrpc\":\"2.0\",\"id\":\"1\",\"method\":\"tools/call\",\"params\":{\"name\":\"extract_menu_text\",\"arguments\":{\"imageBase64\":\"${BASE64_IMAGE}\"}}}"
```

## Tests

```bash
mvn test
```
