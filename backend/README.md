# Backend

Simple WebSocket server example written with [tapir](https://tapir.softwaremill.com/en/latest/),
[http4s](https://http4s.org/) and [fs2](https://fs2.io/).

## Developer guide

From the `backend/` directory where this README is, start the application with:

```sh
sbt run
```

The REST endpoints will be served at `localhost:8080` while the WebSocket endpoints will be served at `localhost:8081`.
