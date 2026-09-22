# JM5 · Gestión de alquileres de vehículos

Aplicación web para gestionar los vehículos de una organización y los alquileres que hacen sus empleados.

| Carpeta     | Contenido                                                             |
|-------------|-----------------------------------------------------------------------|
| `backend/`  | Servicio REST con Java 21, Spring Boot 3.3, JPA y Maven               |
| `frontend/` | Aplicación Angular 22 (TypeScript)                                    |
| `docker/`   | `docker-compose.yml` y los Dockerfile de MySQL, backend y frontend    |

## Arranque con Docker (recomendado)

```bash
cd docker
docker compose up --build
```

| Contenedor | Imagen                                   | URL                    |
|------------|------------------------------------------|------------------------|
| frontend   | `httpd:alpine` (Apache)                  | http://localhost:8081  |
| backend    | `eclipse-temurin:21-jre-alpine`          | http://localhost:8080  |
| db         | `mysql:8.4.0`                            | `localhost:3306`       |

Los Dockerfile compilan el jar y la aplicación Angular en una primera etapa, así que no hace falta
compilar nada antes. Los datos de MySQL se guardan en el volumen `datos`
(`docker compose down -v` lo borra).

## Arranque sin Docker

**Backend** (puerto 8080, base de datos H2 en memoria):

```bash
cd backend
./mvnw package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

**Frontend** (http://localhost:4200, habla con el backend en http://localhost:8080):

```bash
cd frontend
npm install
npm start
```

## Pruebas

```bash
cd backend
./mvnw test
```

Hay pruebas unitarias de las reglas y los servicios (Mockito) y pruebas de integración de los
controladores contra el servidor arrancado (`TestRestTemplate` y H2). El informe de cobertura
queda en `backend/target/site/jacoco/index.html`.

El frontend tiene sus propias pruebas: `cd frontend && npx ng test --watch=false`.

## API

Los errores se devuelven como `{"error": "descripción"}`: 400 si se incumple una regla de negocio
o los datos no son válidos, 404 si el vehículo o el alquiler no existe y 409 si la matrícula ya existe.

| Método | Ruta                                                    | Descripción                                   |
|--------|---------------------------------------------------------|-----------------------------------------------|
| POST   | `/vehiculos`                                            | Alta de un vehículo                           |
| GET    | `/vehiculos`                                            | Todos los vehículos                           |
| GET    | `/vehiculos/{id}`                                       | Un vehículo                                   |
| GET    | `/vehiculos/{id}/alquileres`                            | Alquileres de un vehículo                     |
| PUT    | `/vehiculos/{id}/kilometraje`                           | Cambia el kilometraje: `{"kilometraje": 123}` o `?kilometraje=123` |
| GET    | `/vehiculos/libres?inicio=…&fin=…&pasajeros=…`          | Vehículos que se pueden alquilar en ese intervalo |
| POST   | `/alquileres`                                           | Alta de un alquiler (queda PENDIENTE)         |
| GET    | `/alquileres` · `/alquileres?estado=…`                  | Todos los alquileres o los de un estado       |
| GET    | `/alquileres/{id}`                                      | Un alquiler                                   |
| POST   | `/alquileres/{id}/confirmar`                            | Confirma un alquiler                          |
| POST   | `/alquileres/{id}/cancelar`                             | Cancela un alquiler                           |

Las fechas van en formato ISO sin zona: `2026-10-01T09:00`. Ejemplo de alta de un alquiler:

```json
{ "vehiculoId": 1, "pasajerosPrevistos": 3, "fechaInicio": "2026-10-01T09:00", "fechaFin": "2026-10-03T09:00" }
```

## Reglas de negocio

Están en `backend/src/main/java/es/uma/alquiler/servicios/`: `ReglasAlquiler` agrupa las que dependen
del vehículo y del intervalo, y `LogicaAlquileres` las que dependen del estado de los alquileres.

| Regla | Dónde se aplica |
|-------|-----------------|
| R1 · Estado inicial PENDIENTE | Alta de alquiler (se ignora cualquier estado enviado) |
| R2 · Capacidad del vehículo | Alta y búsqueda de libres |
| R3 · Fin posterior al inicio | Alta y búsqueda de libres |
| R4 · Mínimo 24 h (furgonetas 2 h) | Alta y búsqueda de libres |
| R5 · Máximo 5 días | Alta y búsqueda de libres |
| R6 · Sin solapamiento entre confirmados | Confirmación y búsqueda de libres |
| R7 · PENDIENTE → CONFIRMADO/CANCELADO, CONFIRMADO → CANCELADO | Confirmación y cancelación |
| R8 · Restricciones por kilometraje | Alta, **confirmación** (el kilometraje puede haber cambiado) y búsqueda de libres |

## Frontend

- **Alquila coches y furgonetas**: busca vehículos libres por fechas, pasajeros y tipo, y los reserva.
- **Vehículos**: alta de vehículos, flota y, por vehículo, sus alquileres y la actualización del kilometraje.
- **Reservas**: listado de alquileres filtrable por estado, con confirmación y cancelación.
