# Cómo montar el proyecto

## 1. Crear el proyecto en Android Studio

**New Project → Empty Activity (Compose)**

| Campo | Valor |
|---|---|
| Name | `WIM100` |
| Package name | `cl.dymcyber.wim100` |
| Language | Kotlin |
| Minimum SDK | API 24 |
| Build configuration language | Kotlin DSL |

## 2. Copiar los archivos

1. Descomprime el zip. Queda una carpeta `wim`.
2. Entra a `wim`, selecciona todo con `Ctrl + A` y copia con `Ctrl + C`.
   Copia el **contenido**, no la carpeta.
3. Abre la carpeta del proyecto y pega con `Ctrl + V`.
   Elige **Reemplazar los archivos en el destino**.

## 3. Borrar lo que generó la plantilla

En Android Studio, cambia el panel izquierdo de **Android** a **Project** y
borra la carpeta:

```
app/src/main/java/cl/dymcyber/wim100/ui/
```

Es el tema de ejemplo de la plantilla y ya no se usa: el tema propio está en
`disenio/`. Si queda, el proyecto igual compila, pero deja código muerto.

## 4. Sincronizar y compilar

1. **File → Sync Project with Gradle Files**.
2. **Build → Assemble Project**. Debe terminar en `BUILD SUCCESSFUL`.
3. **Run ▶** en el emulador.

## 5. Probar

Clic derecho sobre `app/src/test` → **Run tests**. Deben pasar las 18 pruebas.

## Si algo falla

Copia el error completo de la pestaña **Build** y pídelo revisar. Los errores
más probables son de versiones de librerías, y se arreglan en
`gradle/libs.versions.toml`.

## Orden sugerido de commits

Para que el historial muestre el trabajo por capas y no un solo commit gigante:

| Commit | Qué incluye |
|---|---|
| `chore: configura proyecto Android con Compose, Room y KSP` | Archivos Gradle y manifiesto |
| `feat: agrega dominio y reglas de negocio con pruebas` | `core/` y `app/src/test/` |
| `feat: agrega persistencia local con Room` | `datos/` |
| `feat: agrega ViewModels y contenedor de dependencias` | `funciones/*Vm.kt`, `Dependencias.kt` |
| `style: agrega tema Material 3 y componentes comunes` | `disenio/` |
| `feat: agrega pantallas y navegación` | `funciones/*Pantalla.kt`, `navegacion/`, `AppWim.kt` |
| `feat: integra recursos nativos del dispositivo` | `dispositivo/` |
| `docs: agrega README del proyecto` | `README.md`, `docs/` |
