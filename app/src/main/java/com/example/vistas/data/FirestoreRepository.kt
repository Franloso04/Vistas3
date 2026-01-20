package com.example.vistas.data

import android.util.Log
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("gastos")

    // Subir un gasto nuevo
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

    // TRAER TODO (Para el Admin)
    fun getAllGastos(onResult: (List<Gasto>) -> Unit) {
        Log.d("FIREBASE", "Pidiendo TODOS los gastos (Admin)")
        collection.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FIREBASE", "Error descargando todo: ${error.message}")
                    return@addSnapshotListener
                }
                val lista = value?.toObjects(Gasto::class.java) ?: emptyList()
                Log.d("FIREBASE", "Descargados ${lista.size} gastos totales")
                onResult(lista)
            }
    }

    // TRAER SOLO LO MÍO (Para el Empleado)
    fun getMyGastos(userId: String, onResult: (List<Gasto>) -> Unit) {
        Log.d("FIREBASE", "Pidiendo gastos del usuario: $userId")

        // INTENTO 1: Con índice (Ideal)
        collection.whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FIREBASE", "Fallo índice ordenado. Intentando sin orden... Error: ${error.message}")
                    // INTENTO 2: Sin índice (Fallback para que no se quede vacío)
                    getMyGastosSinOrden(userId, onResult)
                    return@addSnapshotListener
                }
                val lista = value?.toObjects(Gasto::class.java) ?: emptyList()
                Log.d("FIREBASE", "Descargados ${lista.size} gastos propios")
                onResult(lista)
            }
    }

    private fun getMyGastosSinOrden(userId: String, onResult: (List<Gasto>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .addSnapshotListener { value, _ ->
                val lista = value?.toObjects(Gasto::class.java) ?: emptyList()
                // Ordenamos manual en la app
                onResult(lista.sortedByDescending { it.timestamp })
            }
    }

    // Actualizar Estado
    fun updateEstado(gastoId: String, nuevoEstado: EstadoGasto) {
        collection.document(gastoId).update("estado", nuevoEstado)
    }

    // Eliminar
    fun deleteGasto(gastoId: String) {
        collection.document(gastoId).delete()
    }
}