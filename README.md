# WIM 100 · Postulación y evaluación de candidatas

Aplicación Android que digitaliza el proceso del programa **WIM 100** de DYM
Cyber: registrar postulaciones, revisar antecedentes, aplicar la matriz de
evaluación, evitar evaluaciones duplicadas, dejar trazabilidad y consolidar
resultados para que Dirección resuelva.

> MVP académico del ramo DSY1105 (Duoc UC). No es un sistema productivo y
> **todos los datos son ficticios**: no hay nombres, correos, teléfonos, CV ni
> perfiles reales, ni conexión con ninguna API externa.

## Integrantes

| Nombre | Usuario de GitHub |
|---|---|
| Agustín Boeri | aguboeri |
| _(completar)_ | _(completar)_ |

Equipo N° _(completar)_

## Qué hace la aplicación

- **Dos perfiles.** La *Evaluadora* registra postulaciones y las valora.
  *Dirección* consulta el ranking y resuelve. Entrar como Dirección exige
  verificación biométrica.
- **Registro con validación en vivo.** Cada campo muestra su error con ícono y
  mensaje mientras se escribe. Solo se aceptan dominios de correo de prueba.
- **Matriz de evaluación WIM.** Cinco criterios con peso, puntaje de 1 a 5 y
  puntaje ponderado de 0 a 100 calculado en vivo.
- **Justificación obligatoria.** Un puntaje de 1 o 2 no se puede guardar sin una
  explicación escrita.
- **Sin evaluaciones duplicadas.** Una misma evaluadora no puede valorar dos
  veces a la misma postulante, protegido con doble barrera dentro de una
  transacción.
- **Trazabilidad.** Una bitácora registra ingresos, registros, valoraciones,
  intentos duplicados y resoluciones, con autora y hora.
- **Resultados consolidados.** Ranking por promedio, calculado por SQLite.
- **Adjuntar y ver el CV.** PDF de prueba elegido con el selector del sistema y
  previsualizado dentro de la app.

## Reglas de negocio

| Regla | Detalle |
|---|---|
| Puntaje por criterio | Entero de 1 a 5 |
| Pesos de la matriz | 25, 20, 20, 20 y 15; deben sumar 100 |
| Puntaje total | Σ(puntaje × peso) ÷ (peso total × 5) × 100 |
| Niveles | Sobresaliente ≥ 85 · Sólida ≥ 70 · En desarrollo ≥ 50 · Insuficiente bajo eso |
| Justificación | Obligatoria (10 caracteres o más) si el puntaje es ≤ 2 |
| Observación general | Entre 20 y 400 caracteres |
| Duplicados | Una valoración por evaluadora y postulante |
| Etapas | Recibida → En revisión → Evaluada (2 valoraciones) → Seleccionada / Descartada |
| Resolución | Solo Dirección, solo sobre una postulación Evaluada |
| Datos de contacto | Correos de prueba, teléfono +56 9 XXXX XXXX, CV en PDF de hasta 5 MB |

## Arquitectura

MVVM con una regla simple: **cada capa habla solo con la siguiente**.

```
Pantalla (Composable)  →  ViewModel (StateFlow)  →  Repositorio  →  DAO  →  Room
       ↑                                                                     │
       └──────────────── Flow: Room avisa y la pantalla se redibuja ─────────┘
```

| Carpeta | Qué contiene |
|---|---|
| `core/dominio` | Etapas, perfiles, niveles y el cálculo del puntaje |
| `core/reglas` | Validadores en Kotlin puro, con pruebas unitarias |
| `core/formato` | Fechas, puntajes y tamaños de archivo |
| `datos/bd` | Entidades Room, DAOs, base de datos y datos de muestra |
| `datos/acceso` | Repositorios: reglas de datos y transacciones |
| `funciones/…` | Una carpeta por función, con su pantalla y su ViewModel |
| `dispositivo` | Recursos nativos del teléfono |
| `disenio` | Paleta, tipografía, tema y componentes reutilizables |
| `navegacion` | Rutas y grafo de navegación |

Las anotaciones de Room viven en `datos/`, no en el dominio: así `core` no
depende de Android y sus pruebas corren en segundos sin emulador.

## Recursos nativos

| Recurso | Permiso | Dónde se usa |
|---|---|---|
| Selector de documentos + PdfRenderer | Ninguno | Adjuntar y ver el CV |
| Intents de contacto | Ninguno | Llamar, escribir o abrir el perfil |
| Notificaciones locales | `POST_NOTIFICATIONS` (Android 13+) | Aviso al registrar una valoración |
| Verificación biométrica | `USE_BIOMETRIC` (normal) | Ingreso del perfil Dirección |

## Cómo ejecutar

1. Clonar el repositorio y abrirlo con Android Studio (versión compatible con
   AGP 9.2).
2. **File → Sync Project with Gradle Files**.
3. Crear o elegir un emulador con API 34 o superior.
4. **Run ▶**. La base de datos se crea sola y se llena con datos de muestra la
   primera vez.
5. Para probar la verificación biométrica en el emulador: Ajustes de Android →
   Seguridad → registrar una huella.

Pruebas unitarias: clic derecho sobre `app/src/test` → **Run tests**.

## Tecnologías

Kotlin · Jetpack Compose · Material 3 · MVVM · Room (SQLite) · KSP ·
Navigation Compose · Coroutines y Flow · BiometricPrompt

## Planificación

El trabajo se repartió en un tablero de Trello por historia de usuario.
Enlace: _(completar)_

## Créditos

Desarrollado para el caso WIM 100 de DYM Cyber, en el marco de DSY1105 de
Duoc UC. El proyecto guía del docente se usó como referencia de orden y de las
reglas del caso; el código de esta aplicación es propio.
