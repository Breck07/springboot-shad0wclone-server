# springboot-shad0wclone-server

A small Spring Boot HTTP server that models a few server-side endpoints for a controlled beacon simulation. The current source includes a status response, a fixed beacon response, text logging, raw TAR upload storage, and configured image/audio file delivery.

> **Current source status:** the checked-in application has startup blockers. `MainController` requires `BeaconService`, `UploadService`, and `LoggerService`, but those classes are not registered as Spring beans. The photo and audio `@Value` placeholders in `MainController` are also missing their closing braces. Resolve these issues before expecting the server to start. The endpoint descriptions below document the controller implementation, not a verified running deployment.

## Project structure

```text
src/main/java/com/example/demo/
├── ByteApplication.java   # Spring Boot entry point
├── MainController.java    # /api HTTP routes
├── BeaconService.java     # Fixed beacon response
├── LoggerService.java     # Writes submitted text to a configured path
├── UploadService.java     # Saves request bytes as a .tar file
└── RequestDTO.java        # JSON request model for /txtresponse
src/main/resources/
└── application.properties
pom.xml
mvnw
mvnw.cmd
```

## Requirements and dependencies

- Java 25
- Maven (or the included Maven Wrapper)
- Network access to Maven repositories when dependencies are not cached

The Maven project uses Spring Boot `4.2.0-SNAPSHOT`, Spring MVC, the Spring Boot MVC test starter, and `springboot4-dotenv` 5.1.0. The configured Spring snapshot repository is declared in `pom.xml`.

## Configuration

The checked-in `application.properties` defines these properties. Replace the example paths with paths valid on your machine:

| Property | Purpose |
| --- | --- |
| `api.key` | Expected value of the `x-api-key` request header. Its configured value is read from the `API_KEY` environment variable. |
| `app.upload-dir` | Destination directory for uploaded archive files. |
| `app.log-dir` | Destination **file path** passed to `Files.write` for text submissions. Despite its name, the implementation treats this as a file, not a directory. |
| `app.photo.dir` | Path to the image returned by `GET /api/photo`. |
| `app.audio.dir` | Path to the audio file returned by `GET /api/audio`. |

For example, set `API_KEY` in your shell and update `src/main/resources/application.properties`:

```properties
spring.application.name=byte
api.key=${API_KEY}
app.upload-dir=/absolute/path/to/uploads
app.log-dir=/absolute/path/to/submissions.txt
app.photo.dir=/absolute/path/to/photo.png
app.audio.dir=/absolute/path/to/audio.mp3
```

Use a non-empty secret value for `API_KEY`. The checked-in properties currently use machine-specific `/home/user/...` paths. Do not put a real key in a committed properties file.

## Build and run

After resolving the startup blockers listed above and setting the configuration:

```bash
./mvnw clean package
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` in place of `./mvnw`. No port override is defined in the checked-in properties; Spring Boot's default HTTP port applies.

## HTTP endpoints

Every mapped endpoint requires the `x-api-key` header. An incorrect supplied key returns `401 Unauthorized`. If the header is absent, Spring's required request-header handling normally returns `400 Bad Request`.

| Method | Path | Request | Implemented response |
| --- | --- | --- | --- |
| `GET` | `/api/status` | `x-api-key` | `200` JSON with `status: "up"` and the current local date-time in `time`; sets an in-memory timestamp used by the text and upload handlers. |
| `GET` | `/api/beacon` | `x-api-key` | `200` JSON: `{"response":"confirmed"}`. |
| `POST` | `/api/txtresponse` | `x-api-key`, JSON `{"data":"..." }` | Calls the logger and returns the plain-text body `success` with `200`. |
| `POST` | `/api/upload` | `x-api-key`, `Content-Type: application/x-tar`, raw request bytes | Saves bytes to a randomly named `.tar` file under `app.upload-dir` and returns `Saved archive!` with `200`. |
| `GET` | `/api/photo` | `x-api-key` | Reads the configured file into memory and returns bytes with `image/png`. |
| `GET` | `/api/audio` | `x-api-key` | Reads the configured file into memory and returns bytes with `audio/mpeg`. |

Example request (once the application starts):

```bash
curl -H "x-api-key: $API_KEY" http://localhost:8080/api/beacon
```

Text submission example:

```bash
curl -X POST http://localhost:8080/api/txtresponse \
  -H "x-api-key: $API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"data":"sample text"}'
```

The upload handler stores the incoming body as a TAR file; it does **not** extract or inspect the archive. The text logger writes to the configured path with `Files.write`, which replaces existing file contents on each call rather than appending. Both services catch and print exceptions internally, so their controllers can return success even when a write fails. The timestamp used in text and upload console messages is set by `GET /api/status`; before that request it may be `null`.

## Security and implementation limitations

- Authentication is a direct comparison against one shared API key in controller methods. There are no user identities, roles, key rotation, rate limits, or centralized security filters.
- The application does not configure HTTPS. Do not expose it to an untrusted network without adding transport security and appropriate access controls.
- Uploads have no visible size limit or archive validation. They are saved as opaque files and are not extracted by this code.
- File reads load the complete image or audio file into memory. Missing or unreadable files raise I/O errors.
- The text logger overwrites one configured path and does not append or sanitize submitted text.
- This code should be used only in an isolated environment with systems and data for which you have authorization.
