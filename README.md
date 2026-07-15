# WalletTrack — Gestor de Gastos Personales

Proyecto final de Ingeniería de Software.

## Integrantes

| Integrante        | Cargo                         |
|-------------------|--------------------------------|
| Danna Dawkins      | Scrum Master                   |
| Juan Botacio        | Backend Developer               |
| Luz De Leon         | Frontend / Android Developer    |
| Daniella De Leon    | Fullstack Developer             |
| María Quiñones      | Product Owner                   |
| Abigail Koo         | QA / DevOps                     |

## Descripción
App Android (Kotlin + Jetpack Compose) para registrar y visualizar gastos e
ingresos personales, con autenticación JWT, dashboard con gráficos y uso del
acelerómetro (shake para agregar un movimiento rápido).

## Funcionalidades
- **Autenticación** con registro e inicio de sesión (JWT). Validaciones de
  campos requeridos: correo con formato válido, contraseña mínima de 6
  caracteres y limpieza de espacios/saltos de línea.
- **Dashboard** con saludo personalizado (nombre del usuario) y dos tarjetas de
  resumen: total de **Ingresos** y total de **Gastos**.
- **Gráfico de dona** que compara el gasto consumido frente al total de
  ingresos: cada categoría es un color, el resto disponible queda en gris y el
  centro muestra el **% del ingreso consumido**.
- **Filtro por rango de fechas** (Desde / Hasta) que recalcula el resumen y la
  lista de movimientos.
- **Gestión de movimientos**: agregar, **editar** y **eliminar** (con
  confirmación). Categoría por **dropdown** controlado según el tipo, nombre del
  movimiento y selector de fecha que **no permite fechas futuras**.
- **Validación del monto**: solo admite valor monetario (dígitos y un único
  punto decimal, hasta 2 decimales) y mayor a 0.
- **Sensor**: acelerómetro para abrir "Agregar movimiento" al agitar el
  teléfono en el Dashboard.

## Requisitos
- **Android Studio** con soporte de AGP 8.9+ y **JDK 17**.
- `compileSdk`/`targetSdk` = **35**, `minSdk` = 24, Kotlin 2.0.21.
- **Node.js 18+** para el API.

## Estructura del repositorio
```
GestorGastosApp/
├── android/   -> Proyecto de Android Studio (Kotlin, Compose, Retrofit)
└── api/       -> API REST (Node.js + Express + JWT + SQLite)
```

## Cómo correr el API
```bash
cd api
npm install
npm start
```
El API queda disponible en `http://localhost:3000`.
Endpoints principales: `/api/auth/register`, `/api/auth/login`,
`/api/expenses` (GET/POST/PUT/PATCH/DELETE), `/api/expenses/summary`.

## Cómo correr la app Android
1. Abrir la carpeta `android/` con **Android Studio** (no IntelliJ IDEA: solo
   Android Studio puede desplegar la app en un dispositivo).
2. Configurar `BASE_URL` en `app/build.gradle.kts` según el caso:
   - **Emulador**: `http://10.0.2.2:3000/`.
   - **Dispositivo físico en la misma red WiFi que el PC**: la IP local del
     PC, ej. `http://192.168.0.9:3000/` (revisa tu IP con `ipconfig`). Asegúrate
     de que el firewall permita el puerto 3000.
   - **Dispositivo físico por USB (sin depender de la red)**: usa
     `http://localhost:3000/` y ejecuta una vez
     `adb reverse tcp:3000 tcp:3000`.
   - **Producción**: despliega el API (Render/Railway) y usa esa URL pública.
3. Ejecutar la app (Run ▶) con el API corriendo.

## Sensor utilizado
Acelerómetro (`ShakeDetector.kt`): al agitar el teléfono en el Dashboard, se
abre automáticamente la pantalla de "Agregar movimiento".

## Capturas de pantalla


| Login | Registro |
|---|---|
| ![Login](screenshots/login.png) | ![Registro](screenshots/registro.png) |

| Dashboard | Agregar gasto |
|---|---|
| ![Dashboard](screenshots/dashboard.png) | ![Agregar gasto](screenshots/agregar_gasto.png) |


Ver el documento **WalletTrack_Plan_de_Proyecto.docx** para el plan de proyecto
completo (objetivo, caso de negocio, arquitectura, base de datos, cronograma,
riesgos y anexo técnico).
