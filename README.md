# GameVault — IEI-093 · Unidad II · SQLite

Proyecto Android nativo en **Java**, interfaces **XML** y base de datos **SQLite** local.

## Requisitos
- Android Studio reciente, JDK 17 y Android SDK Platform 35 instalado.
- Gradle 8.9 / Android Gradle Plugin 8.7.3.
- Emulador o dispositivo con Android 7.0 (API 24) o superior.
- Internet únicamente durante la **primera sincronización Gradle** para descargar dependencias.

## Apertura
1. Descomprimir `GameVault.zip`.
2. En Android Studio: **File > Open** y seleccionar la carpeta `GameVault`.
3. Esperar la sincronización de Gradle. El proyecto referencia la distribución Gradle 8.9 pero **no incluye el archivo binario gradle-wrapper.jar** por limitaciones de la herramienta de empaquetado. Si Android Studio no permite sincronizar directamente, crear un proyecto **Empty Views Activity** en Java e incorporar la carpeta `app/src/main`, los `build.gradle`, `settings.gradle` y `gradle.properties` de GameVault; o configurar un Gradle local 8.9.
4. Ejecutar `app` en un emulador o teléfono.

## Recorrido de prueba
1. Abrir la aplicación: debe mostrar colección vacía.
2. Pulsar **AGREGAR VIDEOJUEGO**, ingresar título, elegir plataforma, género, estado y puntuación, guardar.
3. Comprobar que aparece en la biblioteca.
4. Pulsar la tarjeta, abrir detalle y seleccionar **EDITAR**; cambiar estado y guardar.
5. Volver al detalle y comprobar actualización, luego eliminar con confirmación.
6. Crear varios registros y comprobar los filtros **Todos / Pendiente / Jugando / Completado** y la búsqueda por título.
7. Cerrar y reabrir la app: los datos deben permanecer (SQLite local).

## Estructura
- `modelo/Videojuego.java`: clase POJO.
- `database/GameVaultDbHelper.java`: creación de tabla `videojuegos` mediante `SQLiteOpenHelper`.
- `database/VideojuegoDAO.java`: operaciones INSERT, SELECT, UPDATE y DELETE, con consultas parametrizadas.
- `adapter/VideojuegoAdapter.java`: tarjetas en RecyclerView.
- `MainActivity.java`: biblioteca, búsqueda y filtros.
- `FormularioJuegoActivity.java`: creación y edición (reutilización de pantalla).
- `DetalleJuegoActivity.java`: detalle y confirmación de eliminación.
- `res/layout`: interfaces visuales XML.

## Inspección de SQLite
Con la aplicación abierta en el emulador, en Android Studio usar **View > Tool Windows > App Inspection > Database Inspector** y buscar `gamevault.db`, tabla `videojuegos`. La disponibilidad exacta depende de la versión de Android Studio y del dispositivo (API 26+ recomendado para inspección).

## Nota de verificación
Se efectuaron revisiones estáticas del XML, nombres de recursos, imports y estructura del código. **No se ejecutó compilación Android ni pruebas instrumentadas** porque el entorno donde se generó el proyecto no tiene Android SDK, Gradle ni un emulador. La ejecución real debe verificarse en Android Studio.

## Validaciones y restricciones (V2)
- Título obligatorio, entre 2 y 100 caracteres, normalización de espacios en formulario.
- Plataforma, género y estado limitados a los catálogos del proyecto.
- Puntuación: entero entre 1 y 5, sin corrección silenciosa de valores inválidos.
- Unicidad: mismo título y plataforma no pueden repetirse (comparación sin distinguir mayúsculas/minúsculas).
- IDs de actualización/eliminación deben ser positivos; se comprueba el número de filas afectadas.
- Protección en tres niveles: formulario, `ValidadorVideojuego` y restricciones SQLite (`NOT NULL`, `CHECK`, `UNIQUE`).
- Consultas con parámetros para impedir interpolación de valores en SQL.
- Base de datos versión 2 con migración desde versión 1. **Atención:** si una instalación de V1 contiene registros duplicados o incompatibles con las nuevas reglas, la migración falla y se revierte, conservando la base previa. Para una primera instalación en aula, instalar V2 desde cero. Antes de actualizar instalaciones con datos valiosos, hacer un respaldo.
- La validación se repite en el DAO: no basta con impedir el error en la pantalla.

### Casos de prueba de validación
1. Título vacío o solo espacios: rechazo.
2. Título con un carácter: rechazo.
3. Título válido de 2 a 100 caracteres: acepta.
4. Mismo título y plataforma (diferentes mayúsculas): rechazo con aviso.
5. Mismo título y diferente plataforma: acepta.
6. Rating sin estrellas: rechazo.
7. EDITAR un juego y usar título/plataforma de otro: rechazo sin modificar registros.
8. CANCELAR confirmación de eliminación: no altera la base.
9. Reabrir app después de guardar: persiste.
10. Desde DAO, intentar estado fuera del catálogo o puntuación mayor a 5: IllegalArgumentException.


## Comentarios docentes (versión comentada)
Los archivos Java incluyen comentarios `/** ... */` para clases/métodos y `//` para decisiones críticas.
Lectura sugerida: `Videojuego.java` → `ValidadorVideojuego.java` → `GameVaultDbHelper.java` → `VideojuegoDAO.java` → Activities → `VideojuegoAdapter.java`.
Los comentarios explican validación, integridad, `Cursor`, `ContentValues`, `Intent`, ciclo de vida y CRUD.
Esta edición agrega documentación sin modificar intencionalmente la lógica de la versión 2. La compilación en Android Studio sigue pendiente de verificación en equipo con SDK.
