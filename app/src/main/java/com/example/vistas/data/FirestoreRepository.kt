package com.example.vistas.data

import android.net.Uri
import android.util.Log
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.example.vistas.model.Reporte
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("gastos")
    
    private val storage by lazy {
        val bucketName = "gs://carsmarobe-test.firebasestorage.app"
        FirebaseStorage.getInstance(bucketName)
    }

    fun uploadImagen(fileUri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val fileName = "tickets/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("STORAGE_DEBUG", "Error al subir imagen: ${exception.message}", exception)
                onFailure(exception)
            }
    }

    fun uploadImagenBytes(bytes: ByteArray, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val fileName = "tickets/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("STORAGE_DEBUG", "Error al subir bytes: ${exception.message}", exception)
                onFailure(exception)
            }
    }

    // --- Resto de funciones (Sin cambios) ---
    fun addGasto(gasto: Gasto, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        collection.document(gasto.id).set(gasto)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getAllGastos(onResult: (List<Gasto>) -> Unit) {
        collection.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) return@addSnapshotListener
                val lista = value?.documents?.mapNotNull { mapDocumentToGasto(it) } ?: emptyList()
                onResult(lista)
            }
    }

    fun getMyGastos(userId: String, onResult: (List<Gasto>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    getMyGastosSinOrden(userId, onResult)
                    return@addSnapshotListener
                }
                val lista = value?.documents?.mapNotNull { mapDocumentToGasto(it) } ?: emptyList()
                onResult(lista)
            }
    }

    private fun getMyGastosSinOrden(userId: String, onResult: (List<Gasto>) -> Unit) {
        collection.whereEqualTo("userId", userId)
            .addSnapshotListener { value, _ ->
                val lista = value?.documents?.mapNotNull { mapDocumentToGasto(it) } ?: emptyList()
                onResult(lista.sortedByDescending { it.timestamp })
            }
    }

    private fun mapDocumentToGasto(doc: DocumentSnapshot): Gasto? {
        return try {
            val data = doc.data ?: return null
            val estadoStr = data["estado"] as? String ?: "PENDIENTE"
            val estadoSeguro = try { EstadoGasto.valueOf(estadoStr) } catch (e: Exception) { EstadoGasto.PENDIENTE }

            val valorImporte = (data["importe"] as? Number ?: data["monto"] as? Number)?.toDouble() ?: 0.0

            Gasto(
                id = doc.id,
                userId = data["userId"] as? String ?: "",
                nombreComercio = data["nombreComercio"] as? String ?: "",
                fecha = data["fecha"] as? String ?: "",
                categoria = data["categoria"] as? String ?: "",
                importe = valorImporte,
                emailUsuario = data["emailUsuario"] as? String ?: "",
                imagenUrl = data["imagenUrl"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L,
                estado = estadoSeguro
            )
        } catch (e: Exception) { null }
    }

    fun addReporte(reporte: Map<String, Any>) { db.collection("reportes").add(reporte) }
    fun getReportes(onSuccess: (List<Reporte>) -> Unit) {
        db.collection("reportes").get().addOnSuccessListener { result ->
            val lista = result.map { doc ->
                Reporte(id = doc.id, gastoId = doc.getString("gastoId") ?: "", descripcion = doc.getString("descripcion") ?: "", comercio = doc.getString("comercio") ?: "", emailUsuario = doc.getString("emailUsuario") ?: "")
            }
            onSuccess(lista)
        }
    }
    fun updateEstado(gastoId: String, nuevoEstado: EstadoGasto) { collection.document(gastoId).update("estado", nuevoEstado) }
    fun deleteGasto(gastoId: String) { collection.document(gastoId).delete() }
    fun deleteReporte(id: String) { db.collection("reportes").document(id).delete() }
}
