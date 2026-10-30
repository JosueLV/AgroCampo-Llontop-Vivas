# AgroCampo — Llontop Vivas

Aplicación Android con Kotlin Multiplatform, Compose Multiplatform, Ktor, SQLDelight y Koin para el caso **AgroSierra** (Examen Parcial II, Unidad 2, Versión A).

- **Autor:** Josué Llontop Vivas.
- **Curso:** Desarrollo de Aplicaciones Móviles · Ingeniería de Sistemas.
- **Servicio propio:** [Backend-AgroAPI-Llontop-Vivas](https://github.com/JosueLV/Backend-AgroAPI-Llontop-Vivas).
- **Rama de integración:** `develop`; entrega estable: `main` y etiqueta `v1.0-unidad2`.

## 1. Requisitos y ejecución en Windows

1. Instala **JDK 17+**, Android SDK, un emulador Android (API 24+) e IntelliJ IDEA/Android Studio.
2. Inicia **AgroAPI** en tu PC (puerto 8080) desde el repositorio del backend con `.\mvnw.cmd spring-boot:run`; si usas Docker, `docker compose up --build` en el backend.
3. Abre **este proyecto** y ejecuta la configuración `androidApp` sobre el emulador; o verifica desde PowerShell:

```powershell
.\gradlew.bat :shared:testAndroidHostTest
.\gradlew.bat :androidApp:assembleDebug
```

El emulador Android se conecta al servicio del PC por **`http://10.0.2.2:8080/api/v1`**, definido en `shared/src/androidMain/.../data/remote/UrlBaseApi.android.kt`. El `127.0.0.1` dentro del emulador NO apunta a Windows. Para usar un **teléfono físico**, reemplaza esa URL por la IP de la computadora en la misma red y habilita el puerto 8080 en el firewall. El destino iOS de ejemplo usa localhost del simulador, pero **el examen evalúa Android**.

## 2. Arquitectura y estructura de paquetes

| Carpeta | Responsabilidad |
|---|---|
| `shared/src/commonMain/.../domain/model` | Modelos `Lote`, `Labor`, `Sesion`, errores tipados y sesión activa |
| `shared/src/commonMain/.../domain/repository` | Contrato de autenticación |
| `shared/src/commonMain/.../domain/usecase` | Inicio de sesión y filtros combinados |
| `shared/src/commonMain/.../data/remote` | Cliente Ktor único, dirección del API y DTO |
| `shared/src/commonMain/.../data/repository` | Consumo REST, CRUD, mapeo de códigos HTTP |
| `shared/src/commonMain/.../data/local` | Repositorio SQLDelight, cola offline e IDs temporales |
| `shared/src/commonMain/.../presentation` | Pantallas Compose, validaciones, ViewModels y `StateFlow` |
| `shared/src/commonMain/.../di` | Módulos de dependencias Koin |
| `shared/src/commonMain/sqldelight` | Esquema, consultas observables y migraciones |
| `shared/src/androidMain/.../data/secure` | Sesión cifrada con AndroidKeyStore AES-GCM |
| `androidApp/src/main` | Actividad Android, driver SQLDelight y detección de red |
| `shared/src/commonTest` | Pruebas de modelo, casos de uso, MockEngine, errores y sesión |

El flujo en línea es **AgroAPI → Ktor/DTO → dominio → SQLDelight → Flow → ViewModel/UiState → Compose**. La pantalla de lotes observa SQLDelight, no una lista incrustada ni una respuesta HTTP directamente.

## 3. Contrato REST consumido

Todas las rutas están bajo `/api/v1` y, salvo el login, exigen `Authorization: Bearer <jwt>`.

| Método | Endpoint | Permiso | Éxito |
|---|---|---|---|
| POST | `/auth/login` | Público | 200 |
| GET | `/lotes?pagina=0&tamanio=20&valle=&estado=` | `LOTE_LEER` | 200 |
| GET | `/lotes/{id}` | `LOTE_LEER` | 200 |
| GET | `/lotes/{id}/labores?pagina=0&tamanio=20` | `LABOR_LEER` | 200 |
| POST | `/labores` | `LABOR_CREAR` | 201 |
| PUT | `/labores/{id}` | `LABOR_EDITAR` | 200 |
| DELETE | `/labores/{id}` | `LABOR_ELIMINAR` | 204 (sin cuerpo) |
| POST | `/lotes/{id}/cerrar-campania` | `CAMPANIA_CERRAR` | 200 |
| GET | `/usuarios/me` | Usuario autenticado | 200 |

Los listados se leen con `PaginaDto<T>` (`contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas`, `ultima`). Los DTO se convierten al dominio, ignorando propiedades JSON desconocidas. Las labores incluyen `cantidadJornales` (entero ≥1).

Errores: `400` validación, `401` token inválido o caducado (el cliente borra la sesión protegida), `403` falta de privilegio (se conserva la sesión), `404` no encontrado, `409` regla de negocio, `500` fallo remoto, además de falta de red y tiempo agotado. La autenticación usa mensajes distintos para credenciales incorrectas y sin conexión.

## 4. Roles y privilegios

| Permiso | Técnico | Jefe de campo | Administrador | Supervisor de calidad (SC-B) |
|---|---|---|---|---|
| `LOTE_LEER`, `LABOR_LEER` | Sí | Sí | Sí | Sí |
| `LABOR_CREAR` | Sí | Sí | No | No |
| `LABOR_EDITAR` | Solo labores propias y <24 h | Todas | No | Todas |
| `LABOR_ELIMINAR` | No | Sí | No | No |
| `CAMPANIA_CERRAR` | No | Sí | No | No |
| `USUARIO_GESTIONAR` | No | No | Sí | No |

La interfaz oculta acciones sin permiso, pero **la verificación definitiva está en el backend**. Usuarios semilla: `tecnico1`, `jefe1`, `admin`, `supervisor1`; contraseña de práctica `Agro2026*`.

## 5. Offline-first y sincronización

1. Al iniciar o volver a tener conexión, se **envían primero** las labores pendientes al backend.
2. Cada registro sin señal se guarda en SQLDelight con ID negativo y `pendiente=1`.
3. Solo ante respuesta 201 se reemplaza la fila provisional por su ID definitivo **en una transacción**; si falla, permanece pendiente.
4. Después se descargan lotes y labores, se actualiza la caché local y se muestra el resumen y la última fecha de sincronización UTC.
5. Puede forzarse la sincronización con el botón de la pantalla. Una consulta observable proporciona el número de pendientes por lote.

El token JWT está cifrado con clave de **AndroidKeyStore** (AES-GCM) y se elimina al cerrar sesión o cuando se recibe `401`. Si la sesión guardada vence, se abre el login.

## 6. Funcionalidades y solicitudes de cambio

- Login JWT con estados de carga/error; lista de lotes con buscador y filtros por valle y campañas abiertas (**SC-A**).
- Detalle de lote y labores, alta offline, edición en línea, eliminación confirmada y cierre de campaña sujeto a privilegios.
- Rol **SUPERVISOR_CALIDAD** (**SC-B**) con lectura y edición de cualquier labor, sin permiso de cierre.
- Campo **cantidad de jornales** en backend, DTO, dominio, formularios y base local, con migración (**SC-C**).
- Distintivo reactivo de labores pendientes por lote desde consulta SQLDelight (**SC-D**).
- Perfil (usuario, rol, permisos), última sincronización, reintento al recuperar Internet y cierre de sesión.

Las reglas de negocio sobre campaña cerrada, autoría/plazo de edición, cosecha antes de cierre y dosis de fumigación se aplican **en AgroAPI**.

## 7. Pruebas y versionado

```powershell
.\gradlew.bat :shared:testAndroidHostTest
.\gradlew.bat :androidApp:assembleDebug
git log --graph --oneline --all
git log --date=short --pretty="%h %ad %s"
```

El proyecto incluye pruebas en `commonTest` con motor Ktor `MockEngine`, validadores y casos de uso. El resultado real de las pruebas debe adjuntarse como captura al informe final. El historial conserva ramas `feature/*`, `fix/*`, `sc-a-llontop`, `sc-b-llontop`, `sc-c-llontop`, `sc-d-llontop` e integraciones mediante pull requests hacia `develop`; `main` recibe únicamente la integración final.

> Seguridad: las credenciales de demostración y HTTP local son exclusivos del examen. Para despliegue real se requieren HTTPS, servidor accesible y configuración protegida.
