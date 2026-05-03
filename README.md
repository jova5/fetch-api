# FetchAPI

A desktop API client for HTTP, GraphQL, and gRPC — built with Kotlin Multiplatform and Compose Desktop. Think Postman / Insomnia, but native and written in Kotlin.

## About this project

This is a personal project I'm building to learn and demonstrate my skills with Kotlin and Kotlin Multiplatform. It is **work in progress** — features are incomplete, the codebase is evolving, and there are no automated tests yet (I'm focusing on the engineering and patterns first while I level up on testing in Kotlin).

You're welcome to read the code, fork it, or use it as a reference. See the license below for usage terms.

## Tech stack

- **Kotlin 2.3** targeting JVM 25
- **Compose Multiplatform** for the UI
- **Jewel** (JetBrains UI toolkit) for native-feel decorated windows
- **Ktor** for HTTP, **gRPC** with a custom proto-file parser, GraphQL over HTTP
- **SQLDelight + SQLite** for local storage
- **Koin** for dependency injection
- **Kotlin Coroutines** with a virtual-thread dispatcher for network I/O

## Architecture (short)

Feature-based packages under `composeApp/src/jvmMain/kotlin/ba/fluxor/fetchapi/`, each with `data/`, `ui/`, and `viewmodel/` layers. Protocol engines live under `network/` and implement a shared `NetworkEngine<Req, Resp>` interface.

## Build & run

Requires JDK 25+.

```shell
# Windows
.\gradlew.bat :composeApp:run

# macOS / Linux
./gradlew :composeApp:run
```

## Status

Active development. Expect rough edges, missing features, and breaking changes.

## License

MIT — see [LICENSE](./LICENSE). You may use this code for any purpose, including commercial.
