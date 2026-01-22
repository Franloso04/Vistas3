package com.example.vistas.data

import android.util.Log
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("gastos")

    // --- SUBIR GASTO ---
    fun addGasto(gasto: Gasto, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("FIREBASE", "Intentando subir gasto: ${gasto.nombreComercio}")
        collection.document(gasto.id).set(gasto)
            .addOnSuccessListener {
                Log.d("FIREBASE", "Subida Exitosa")
                onSuccess()
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "Error subiendo: ${it.message}")
                onFailure(it)
            }
    }

    // --- TRAER TODO (Admin) CON PROTECCIÓN ANTI-CRASH ---
    fun getAllGastos(onResult: (List<Gasto>) -> Unit) {
        Log.d("FIREBASE", "Pidiendo TODOS los gastos (Admin)")
        collection.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FIREBASE", "Error descargando todo: ${error.message}")
                    return@addSnapshotListener
                }

                // CAMBIO CLAVE: Mapeo manual para evitar crash por "PROCESANDO"
                val lista = value?.documents?.mapNotNull { doc ->
                    mapDocumentToGasto(doc)
                } ?: emptyList()

                Log.d("FIREBASE", "Descargados ${lista.size} gastos totales")
                onResult(lista)
            }
    }

    // --- TRAER SOLO LO MÍO (Empleado) CON PROTECCIÓN ANTI-CRASH ---
    fun getMyGastos(userId: String, onResult: (List<Gasto>) -> Unit) {
        Log.d("FIREBASE", "Pidiendo gastos del usuario: $userId")

        collection.whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Fallback si falla el índice
                    getMyGastosSinOrden(userId, onResult)
                    return@addSnapshotListener
                }

                // CAMBIO CLAVE: Mapeo manual
                val lista = value?.documents?.mapNotNull { doc ->
                    mapDocumentToGasto(doc)
                } ?: emptyList()

                Log.d("FIREBASE", "Descargados ${lista.size} gastos propios")
                onResult(lista)
            }
    }

    private fun getMyGastosSinOrden(userId: String, onResult: (List<Gasto>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .addSnapshotListener { value, _ ->
                val lista = value?.documents?.mapNotNull { doc ->
                    mapDocumentToGasto(doc)
                } ?: emptyList()
                onResult(lista.sortedByDescending { it.timestamp })
            }
    }

    // --- FUNCIÓN DE AYUDA PARA LEER EL GASTO Y ARREGLAR EL "PROCESANDO" ---
    private fun mapDocumentToGasto(doc: DocumentSnapshot): Gasto? {
        return try {
            val data = doc.data ?: return null

            val estadoStr = data["estado"] as? String ?: "PENDIENTE"

            val estadoSeguro = if (estadoStr == "PROCESANDO") {
                EstadoGasto.PENDIENTE
            } else {
                try {
                    EstadoGasto.valueOf(estadoStr)
                } catch (e: Exception) {
                    EstadoGasto.PENDIENTE
                }
            }

            Gasto(
                id = doc.id,
                // --- AÑADIDO: LEER EL USER ID ---
                userId = data["userId"] as? String ?: "", // <--- ESTO FALTABA

                nombreComercio = data["nombreComercio"] as? String ?: "",
                fecha = data["fecha"] as? String ?: "",
                categoria = data["categoria"] as? String ?: "",
                monto = (data["monto"] as? Number)?.toDouble() ?: 0.0,
                emailUsuario = data["emailUsuario"] as? String ?: "",
                imagenUrl = data["imagenUrl"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
                estado = estadoSeguro
            )
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error leyendo gasto: ${doc.id}", e)
            null
        }
    }

    // --- REPORTES ---

    fun addReporte(reporte: Map<String, Any>) {
        db.collection("reportes")
            .add(reporte)
            .addOnSuccessListener { Log.d("Firebase", "Reporte guardado") }
            .addOnFailureListener { e -> Log.e("Firebase", "Error reporte", e) }
    }

    fun getReportes(onSuccess: (List<Reporte>) -> Unit) {
        db.collection("reportes")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    val data = doc.data
                    Reporte(
                        id = doc.id,
                        gastoId = data["gastoId"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        comercio = data["comercio"] as? String ?: "",
                        emailUsuario = data["emailUsuario"] as? String ?: ""
                    )
                }
                onSuccess(lista)
            }
            .addOnFailureListener { onSuccess(emptyList()) }
    }

    // --- ACCIONES DE ADMIN (NECESARIAS PARA QUE NO DE ERROR EL VIEWMODEL) ---

    fun updateEstado(gastoId: String, nuevoEstado: EstadoGasto) {
        collection.document(gastoId).update("estado", nuevoEstado)
    }

    fun deleteGasto(gastoId: String) {
        collection.document(gastoId).delete()
    }

    fun deleteReporte(id: String) {
        db.collection("reportes").document(id)
            .delete()
            .addOnSuccessListener {
                android.util.Log.d("FIREBASE", "Reporte eliminado: $id")
            }
            .addOnFailureListener {
                android.util.Log.e("FIREBASE", "Error borrando reporte", it)
            }
    }


}