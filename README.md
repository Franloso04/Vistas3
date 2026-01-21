# ExpenseControl (Vistas3)

**ExpenseControl** es una aplicación Android nativa desarrollada en Kotlin para la gestión corporativa de gastos. Permite a los empleados registrar tickets y facturas, mientras que un panel de administración facilita la validación (aprobación o rechazo) de dichos gastos en tiempo real.

## 📱 Descripción del Proyecto

El objetivo de la aplicación es digitalizar el flujo de gastos de una empresa. Utiliza **Firebase** como backend para asegurar la sincronización instantánea de datos entre los dispositivos de los empleados y el panel de control de los administradores.

La aplicación cuenta con gestión de roles dinámica basada en el correo electrónico, diferenciando entre **Empleados** (subida y consulta) y **Administradores** (gestión y validación).

## 🚀 Funcionalidades Principales

### 👤 Para Empleados
* **Inicio de Sesión Seguro:** Autenticación mediante correo electrónico y contraseña (Firebase Auth).
* **Registro de Gastos:** Formulario para introducir concepto, monto y categoría (Comida, Transporte, Alojamiento, etc.).
* **Historial y Filtros:** Visualización de tickets con filtros avanzados por:
    * Texto (Búsqueda por nombre).
    * Categoría.
    * Estado (Aprobado, Pendiente, Rechazado).
    * Fecha (Más recientes / antiguos).
* **Gestión de Errores:** Posibilidad de eliminar tickets propios mientras están en estado "Pendiente" o "Procesando" (selección múltiple).
* **Feedback Visual:** Estado de los tickets mediante etiquetas de colores (Verde/Rojo/Ámbar).

### 🛡️ Para Administradores
* **Panel Exclusivo:** Acceso a un dashboard especial (oculto para empleados) accesible desde Ajustes.
* **Validación de Gastos:**
    * ✅ **Aprobar:** Marca el ticket como válido.
    * ❌ **Rechazar:** Deniega el gasto.
    * 🗑️ **Eliminar:** Borrado permanente de la base de datos.
* **Visión Global:** El administrador ve los gastos de *todos* los usuarios.

### ⚙️ Generales
* **Modo Oscuro/Claro:** Soporte nativo para cambio de tema visual.
* **Dashboard:** Resumen financiero con el total gastado en el mes y el monto pendiente de aprobación.

---

## 🏗️ Arquitectura e Implementación Ténica

El proyecto sigue el patrón de arquitectura **MVVM (Model-View-ViewModel)** para asegurar un código limpio, escalable y mantenible.

### 1. Capa de Datos (Model & Repository)
* **Firebase Firestore:** Base de datos NoSQL en la nube.
* **FirestoreRepository:** Clase encargada de toda la lógica de conexión a datos.
    * `addGasto()`: Sube nuevos documentos.
    * `getMyGastos()`: Descarga solo los gastos del usuario actual (con `SnapshotListener` para tiempo real).
    * `getAllGastos()`: Descarga todos los gastos (solo para Admin).
    * `updateEstado()`: Cambia el estado del ticket sin sobrescribir otros datos.

### 2. Capa Lógica (ViewModel)
* **MainViewModel:** El cerebro de la aplicación.
    * Gestiona `LiveData` para comunicar cambios a la UI (`_gastosGlobales`, `_gastosFiltrados`).
    * **Lógica de Roles:** Al iniciar sesión, detecta si el email contiene la palabra `"admin"`. Si es así, activa el modo Admin (`isAdmin = true`) y carga la vista global.
    * **Filtros:** Aplica lógica de filtrado en memoria sobre la lista maestra para una respuesta instantánea al usuario.

### 3. Capa de Vista (UI)
* **Fragments:** Navegación fluida utilizando `NavigationComponent` (Login -> Dashboard -> Historial/Ajustes/Admin).
* **RecyclerViews & Adapters:**
    * `GastoAdapter`: Muestra la lista de gastos. Implementa un diseño personalizado tipo "Pill" (Pastilla) que cambia de color y drawable (punto verde/rojo/ámbar) dinámicamente según el estado del gasto.
    * `AdminAdapter`: Adaptador específico para el panel de control con botones de acción rápida.
* **Diseño XML:** Uso de `Material Design 3`, `CardView` y `ShapeDrawables` para interfaces redondeadas y modernas.

---

## 🛠️ Configuración e Instalación

Para ejecutar este proyecto en tu entorno local:

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/Franloso04/Vistas3.git](https://github.com/Franloso04/Vistas3.git)
    ```
2.  **Configurar Firebase:**
    * Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
    * Habilita **Authentication** (Proveedor: Email/Password).
    * Habilita **Firestore Database**.
    * Descarga el archivo `google-services.json` de tu proyecto.
    * Pega el archivo en la carpeta `app/` del proyecto en Android Studio.

3.  **Ejecutar:**
    * Abre el proyecto en Android Studio (Koala o superior recomendado).
    * Sincroniza Gradle.
    * Ejecuta en un emulador o dispositivo físico.

---

## 📂 Estructura del Proyecto
Aquí tienes un archivo README.md profesional y completo, diseñado específicamente para tu proyecto Vistas3 (ExpenseControl).

Este documento explica tanto qué hace la app como cómo está construida internamente, basándonos en toda la estructura MVVM y Firebase que hemos implementado.

Copia el siguiente código y pégalo en un archivo llamado README.md en la raíz de tu proyecto.

Markdown

# ExpenseControl (Vistas3)

**ExpenseControl** es una aplicación Android nativa desarrollada en Kotlin para la gestión corporativa de gastos. Permite a los empleados registrar tickets y facturas, mientras que un panel de administración facilita la validación (aprobación o rechazo) de dichos gastos en tiempo real.

## 📱 Descripción del Proyecto

El objetivo de la aplicación es digitalizar el flujo de gastos de una empresa. Utiliza **Firebase** como backend para asegurar la sincronización instantánea de datos entre los dispositivos de los empleados y el panel de control de los administradores.

La aplicación cuenta con gestión de roles dinámica basada en el correo electrónico, diferenciando entre **Empleados** (subida y consulta) y **Administradores** (gestión y validación).

## 🚀 Funcionalidades Principales

### 👤 Para Empleados
* **Inicio de Sesión Seguro:** Autenticación mediante correo electrónico y contraseña (Firebase Auth).
* **Registro de Gastos:** Formulario para introducir concepto, monto y categoría (Comida, Transporte, Alojamiento, etc.).
* **Historial y Filtros:** Visualización de tickets con filtros avanzados por:
    * Texto (Búsqueda por nombre).
    * Categoría.
    * Estado (Aprobado, Pendiente, Rechazado).
    * Fecha (Más recientes / antiguos).
* **Gestión de Errores:** Posibilidad de eliminar tickets propios mientras están en estado "Pendiente" o "Procesando" (selección múltiple).
* **Feedback Visual:** Estado de los tickets mediante etiquetas de colores (Verde/Rojo/Ámbar).

### 🛡️ Para Administradores
* **Panel Exclusivo:** Acceso a un dashboard especial (oculto para empleados) accesible desde Ajustes.
* **Validación de Gastos:**
    * ✅ **Aprobar:** Marca el ticket como válido.
    * ❌ **Rechazar:** Deniega el gasto.
    * 🗑️ **Eliminar:** Borrado permanente de la base de datos.
* **Visión Global:** El administrador ve los gastos de *todos* los usuarios.

### ⚙️ Generales
* **Modo Oscuro/Claro:** Soporte nativo para cambio de tema visual.
* **Dashboard:** Resumen financiero con el total gastado en el mes y el monto pendiente de aprobación.

---

## 🏗️ Arquitectura e Implementación Ténica

El proyecto sigue el patrón de arquitectura **MVVM (Model-View-ViewModel)** para asegurar un código limpio, escalable y mantenible.

### 1. Capa de Datos (Model & Repository)
* **Firebase Firestore:** Base de datos NoSQL en la nube.
* **FirestoreRepository:** Clase encargada de toda la lógica de conexión a datos.
    * `addGasto()`: Sube nuevos documentos.
    * `getMyGastos()`: Descarga solo los gastos del usuario actual (con `SnapshotListener` para tiempo real).
    * `getAllGastos()`: Descarga todos los gastos (solo para Admin).
    * `updateEstado()`: Cambia el estado del ticket sin sobrescribir otros datos.

### 2. Capa Lógica (ViewModel)
* **MainViewModel:** El cerebro de la aplicación.
    * Gestiona `LiveData` para comunicar cambios a la UI (`_gastosGlobales`, `_gastosFiltrados`).
    * **Lógica de Roles:** Al iniciar sesión, detecta si el email contiene la palabra `"admin"`. Si es así, activa el modo Admin (`isAdmin = true`) y carga la vista global.
    * **Filtros:** Aplica lógica de filtrado en memoria sobre la lista maestra para una respuesta instantánea al usuario.

### 3. Capa de Vista (UI)
* **Fragments:** Navegación fluida utilizando `NavigationComponent` (Login -> Dashboard -> Historial/Ajustes/Admin).
* **RecyclerViews & Adapters:**
    * `GastoAdapter`: Muestra la lista de gastos. Implementa un diseño personalizado tipo "Pill" (Pastilla) que cambia de color y drawable (punto verde/rojo/ámbar) dinámicamente según el estado del gasto.
    * `AdminAdapter`: Adaptador específico para el panel de control con botones de acción rápida.
* **Diseño XML:** Uso de `Material Design 3`, `CardView` y `ShapeDrawables` para interfaces redondeadas y modernas.

---

## 🛠️ Configuración e Instalación

Para ejecutar este proyecto en tu entorno local:

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/Franloso04/Vistas3.git](https://github.com/Franloso04/Vistas3.git)
    ```
2.  **Configurar Firebase:**
    * Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
    * Habilita **Authentication** (Proveedor: Email/Password).
    * Habilita **Firestore Database**.
    * Descarga el archivo `google-services.json` de tu proyecto.
    * Pega el archivo en la carpeta `app/` del proyecto en Android Studio.

3.  **Ejecutar:**
    * Abre el proyecto en Android Studio (Koala o superior recomendado).
    * Sincroniza Gradle.
    * Ejecuta en un emulador o dispositivo físico.

---

## 📂 Estructura del Proyecto

com.example.vistas ├── data │ ├── FirestoreRepository.kt # Conexión con Firebase ├── model │ ├── Gasto.kt # Data Class (Modelo de datos) │ ├── EstadoGasto.kt # Enum (APROBADO, PENDIENTE, RECHAZADO) ├── ui.theme # (Archivos de Compose/Tema si aplica) ├── AdminAdapter.kt # Adaptador para lista de Admin ├── AdminFragment.kt # Pantalla de gestión Admin ├── DashboardFragment.kt # Pantalla principal (Resumen) ├── ExpensesFragment.kt # Historial de gastos (Filtros y Lista) ├── GastoAdapter.kt # Adaptador principal (Diseño Pastillas) ├── LoginFragment.kt # Pantalla de Login ├── MainActivity.kt # Contenedor principal ├── MainViewModel.kt # Lógica de negocio (MVVM) ├── OcrFragment.kt # Pantalla de creación de ticket └── SettingsFragment.kt # Ajustes y Logout

