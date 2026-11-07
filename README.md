# springboot-shad0wclone-server
A Java-based Command and Control (C2) server simulation built with **Spring Boot**. This project models communication pathways between a centralized server and simulated client beacons, demonstrating core concepts of HTTP-based command handling, client check-ins, data ingestion, file transfers, and binary asset delivery.

Designed as a controlled cybersecurity lab project for exploring backend infrastructure, HTTP communication, API authentication, and server-side data handling.

## Features

- **API Authentication:** Header-based API key authentication using `x-api-key`.
- **Server Health Checks:** Endpoint for monitoring server availability and retrieving timestamps.
- **Beacon Simulation:** Simulates client beacon check-ins through a centralized service.
- **Text Data Ingestion:** Receives and processes text-based data from simulated clients.
- **Archive Uploads:** Accepts `.tar` archives and delegates processing to an upload service.
- **Binary Asset Delivery:** Serves configured image and audio files over HTTP.
- **Service Separation:** Uses dedicated services for beacon management, logging, and upload processing.

## Technology Stack

- Java
- Spring Boot
- Spring Web
- Spring Dependency Injection
- HTTP/REST APIs
- Maven

## Project Configuration

The application uses external configuration properties to manage authentication and asset locations.

Add the following properties to your `application.properties`:

```properties
# API Authentication
api.key=your_secure_api_key_here

# Asset Configuration
app.photo.dir=/path/to/your/photos/image.png
app.audio.dir=/path/to/your/audio/sound.mp3
```

Alternatively, these properties can be configured through environment variables or an external configuration provider.

**Important:** Never commit actual API keys, credentials, or sensitive file paths to a public repository.

Ensure `@Value` annotations use correctly formatted property placeholders:

```java
@Value("${app.photo.dir}")
private String photoPath;

@Value("${app.audio.dir}")
private String audioPath;
```

## API Documentation

All endpoints require authentication through the following HTTP request header:

```http
x-api-key: your_secure_api_key_here
```

Requests with missing or invalid API keys receive an `HTTP 401 Unauthorized` response.

### Endpoint Overview

| Method | Endpoint           | Content Type      | Description                                |
| ------ | ------------------ | ----------------- | ------------------------------------------ |
| GET    | `/api/status`      | application/json  | Returns server health and timestamp.       |
| GET    | `/api/beacon`      | application/json  | Simulates a client beacon check-in.        |
| POST   | `/api/txtresponse` | application/json  | Receives text data from simulated clients. |
| POST   | `/api/upload`      | application/x-tar | Accepts TAR archive uploads.               |
| GET    | `/api/photo`       | image/png         | Serves a configured image asset.           |
| GET    | `/api/audio`       | audio/mpeg        | Serves a configured audio asset.           |

---

## Endpoint Details

### 1. Server Status

`GET /api/status`

Checks whether the server is operational.

**Example Response (200 OK):**

```json
{
  "status": "up",
  "time": "2026-11-07T16:57:00"
}
```

### 2. Client Beacon Simulation

`GET /api/beacon`

Simulates a client beacon checking in with the central server.

The endpoint retrieves tasking information through `BeaconService` and returns a dynamically generated JSON response.

**Implementation:**

- Receives an authenticated HTTP GET request.
- Invokes `BeaconService`.
- Returns the generated response to the requesting client.

### 3. Text Data Ingestion

`POST /api/txtresponse`

Receives text-based data submitted by simulated clients.

**Request Body:**

```json
{
  "data": "Sample simulated client log data"
}
```

**Server Processing:**

- Accepts a `RequestDTO` containing the submitted data.
- Prints an `[ALERT]` notification to standard output.
- Passes the received data to `LoggerService.logData()`.

### 4. Archive Uploads

`POST /api/upload`

Accepts TAR archive files through an HTTP POST request.

**Request Header:**

```http
Content-Type: application/x-tar
```

**Request Body:**

Raw binary contents of a `.tar` archive.

**Server Processing:**

The incoming request is passed to `UploadService`, which handles archive processing and storage.

### 5. Binary Asset Delivery

`GET /api/photo`

`GET /api/audio`

Serves configured static image and audio assets directly through HTTP responses.

**Supported Assets:**

| Endpoint     | Format | MIME Type    |
| ------------ | ------ | ------------ |
| `/api/photo` | PNG    | `image/png`  |
| `/api/audio` | MP3    | `audio/mpeg` |

**Server Processing:**

1. Resolves the configured asset path.
2. Reads the requested file.
3. Converts the file contents into a byte stream.
4. Configures the appropriate HTTP response headers.
5. Returns the binary content to the requesting client.

---

## Architecture

The application uses a service-oriented structure to separate HTTP request handling from application logic.

| Component              | Responsibility                                        |
| ---------------------- | ----------------------------------------------------- |
| Controller             | Exposes HTTP endpoints and handles incoming requests. |
| BeaconService          | Generates simulated beacon responses.                 |
| LoggerService          | Processes and logs submitted text data.               |
| UploadService          | Handles incoming TAR archive uploads.                 |
| RequestDTO             | Represents structured text ingestion requests.        |
| Application Properties | Stores runtime configuration.                         |

## Security Considerations

This project is intended for authorized, isolated cybersecurity testing and educational simulation.

Important considerations when extending the application:

- Use environment variables or a secrets manager for API credentials.
- Validate uploaded archives before extraction.
- Prevent directory traversal and archive path traversal.
- Enforce file size and request size limits.
- Restrict asset paths to explicitly configured locations.
- Use HTTPS when deploying beyond a local testing environment.
- Implement appropriate request logging and access controls.

The API key mechanism provides basic request authentication and should not be considered a complete production security architecture.

## Learning Objectives

This project explores several practical backend and cybersecurity concepts:

- Building RESTful APIs with Spring Boot.
- Implementing HTTP request authentication.
- Managing server-to-client communication patterns.
- Working with JSON request and response bodies.
- Handling binary data and file streams.
- Separating business logic using service layers.
- Managing application configuration through external properties.

## Disclaimer

This project is an educational C2 server simulation intended for controlled laboratory environments, authorized testing, and cybersecurity research.

It should only be used with systems and data for which you have explicit authorization.
**Language:** Java
**Framework:** Spring Boot
