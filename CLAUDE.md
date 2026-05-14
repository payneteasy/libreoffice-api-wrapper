# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Java 21 Maven multi-module project. Thin HTTP wrapper around an external LibreOffice/OpenOffice service (the `hdejager/libreoffice-api` Docker image) plus a small Batik-based SVG→PNG converter. Single module: `libreoffice-api-wrapper-server`, packaged as a fat jar (`*-jar-with-dependencies.jar`) with `Main` as the entry point.

## Common commands

Build (skip tests — tests are integration and need running server + docker, see below):
```bash
mvn -Dmaven.test.skip=true clean package
```

Run the server locally (after `package`):
```bash
java -jar libreoffice-api-wrapper-server/target/libreoffice-api-wrapper-server-<version>-jar-with-dependencies.jar
```

Tests are integration tests that hit `http://localhost:8080`. Before running them you must have BOTH:
1. The wrapper jar running locally, and
2. The LibreOffice backend running: `docker run -t --name libreoffice -p 8100:8100 hdejager/libreoffice-api`

Then:
```bash
mvn test                                                                          # all tests
mvn -pl libreoffice-api-wrapper-server -Dtest=LibreOfficeConverterServletTest test # one class
mvn -pl libreoffice-api-wrapper-server -Dtest=LibreOfficeConverterServletTest#testLibreofficeConverter test
```

OWASP dependency-check runs automatically during the build (`org.owasp:dependency-check-maven` is bound to the default lifecycle); suppressions live in `suppressions.xml` at repo root. JaCoCo coverage is appended to `target/jacoco.exec`.

Release / deploy (publishes to GitHub Packages — `~/.m2/settings.xml` must be configured for `github` server). Order matters; the README is authoritative:
```bash
mvn -Dmaven.test.skip=true clean package
# start jar + docker, run `mvn test`, only continue if green
mvn build-helper:parse-version versions:set \
    -DnewVersion='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.nextIncrementalVersion}' \
    versions:commit
mvn deploy
git commit  # commit the version bump after deploy
```

## Architecture

`Main.createServer()` boots an embedded Jetty 12 (EE10 / Jakarta Servlet) `Server` and registers three servlets on a single `ServletContextHandler`:

- `LibreofficeConverterServlet` → mounted at `${SERVER_SERVLET_PATH}` (default `/convert`). Validates `inputFormat` (csv/doc/docx/xls/xlsx) and `outputFormat` (pdf only) from query params, opens a fresh `SocketOpenOfficeConnection` to the remote LibreOffice service on every request, and streams `request.getInputStream()` → `response.getOutputStream()` through `StreamOpenOfficeDocumentConverter`. The DOCX/XLSX `DocumentFormat` entries are added to the `DefaultDocumentFormatRegistry` manually because jodconverter 2.2.1's defaults don't include them.
- `SvgToPngConverterServlet` → hard-coded at `/convert/svg-to-png`. Uses Batik's `PNGTranscoder`; writes to a temp file then streams it back. Independent of the LibreOffice backend.
- `VersionServlet` → hard-coded at `/management/version.txt`. Reads `version` from `META-INF/maven/com.payneteasy/libreoffice-api-wrapper-server/pom.properties` baked into the jar by Maven.

The full external URL for document conversion is therefore `http://<host>:<SERVER_PORT><SERVER_SERVLET_CONTEXT><SERVER_SERVLET_PATH>` (default `/converter/convert`); `/convert/svg-to-png` and `/management/version.txt` sit under `${SERVER_SERVLET_CONTEXT}` too.

### Configuration

All runtime config flows through `com.payneteasy:startup-parameters` and the two interfaces in `server.config`:

- `IJettyConfiguration` — `SERVER_PORT` (8080), `SERVER_SERVLET_CONTEXT` (`/converter`), `SERVER_SERVLET_PATH` (`/convert`)
- `ILibreofficeServiceConfiguration` — `LIBREOFFICE_HOST` (`localhost`), `LIBREOFFICE_PORT` (`8100`)

`StartupParametersFactory.getStartupParameters(...)` reads these from environment variables or `-D` system properties; the `@AStartupParameter` annotation on each interface method defines name and default. To add a new tunable, add a method to the appropriate interface — there is no separate properties file.

### Module/version coupling to be aware of

- The parent `pom.xml` and `libreoffice-api-wrapper-server/pom.xml` both carry the same `<version>`. The release workflow uses `versions:set` (which updates both) — don't hand-edit one without the other.
- The GitHub Actions workflow at `.github/workflows/deployWithIncrementVesrion.yml` hard-codes the jar version in its `java -jar ...` step. When bumping the version, that file may need updating too.
- Tests reference `src/test/resources/example.docx` / `example.svg` (committed) and write `example.pdf` / `example.png` outputs there at runtime (gitignored / regenerated on each run).
