# Observability Training Project

Dieses Projekt ist ein Quarkus Multi-Module Projekt bestehend aus den Modulen: `london`, `firenze` und `dresden`.

## Struktur
- `o11y-app`: Enthält den `@UseCase` Interceptor, Business Use Cases und den Scheduler. Port 49255.
- `london`: Port 49252.
- `firenze`: Port 49253.
- `dresden`: Port 49254.

Alle drei Services (`london`, `firenze`, `dresden`) können sich nun gegenseitig zufällig aufrufen, solange die Anzahl der Sprünge (`hops`) größer als 0 ist.

## Ausführung
In separaten Terminals oder über die IntelliJ Run-Konfiguration `O11Y_ALL`:
1. `gradle :london:quarkusDev`
2. `gradle :firenze:quarkusDev`
3. `gradle :dresden:quarkusDev`
4. `gradle :o11y-app:quarkusDev`

Der Prozess startet automatisch über den Scheduler in `o11y-app`. Manuell kann ein Prozess wie folgt angestoßen werden:
`http://localhost:49252/london?hops=5`

## Docker Compose
Alle Module können als getrennte Container gestartet werden:

```bash
docker compose up --build
```

Danach erreichbar:
- `http://localhost:49252/london?hops=10`
- `http://localhost:49253/firenze?hops=10`
- `http://localhost:49254/dresden?hops=10`
- `http://localhost:49255/app` (Management endpoint)

Im Compose-Netzwerk sprechen die Services über die Namen `london`, `firenze` und `dresden` miteinander. Die URLs sind deshalb über Umgebungsvariablen konfigurierbar.

## Interceptor
Der `UseCaseInterceptor` loggt den Start und das Ende eines UseCases inkl. Korrelations-ID und Parametern im MDC.
Dies bildet die Basis für späteres Tracing mit OTEL/ELK.
