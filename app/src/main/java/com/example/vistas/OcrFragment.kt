package com.example.vistas

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.vistas.model.EstadoGasto
import com.example.vistas.model.Gasto
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class OcrFragment : Fragment(R.layout.fragment_ocr_validation) {

    private val viewModel: MainViewModel by activityViewModels()
    private val calendar = Calendar.getInstance()
    
    private var photoUri: Uri? = null
    private var photoFile: File? = null
    private var imgPreview: ImageView? = null
    private var progressBar: ProgressBar? = null
    private var btnTomarFoto: Button? = null
    private var btnConfirmar: Button? = null

    // 1. Lanzador para la cámara
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imgPreview?.setImageURI(photoUri)
            imgPreview?.visibility = View.VISIBLE
            btnTomarFoto?.visibility = View.GONE 
        } else {
            Toast.makeText(context, "Error al capturar foto", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Lanzador para permisos
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            abrirCamara()
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // REFERENCIAS
        val editNombre = view.findViewById<EditText>(R.id.editNombre)
        val editFecha = view.findViewById<TextInputEditText>(R.id.editFecha)
        val editMonto = view.findViewById<EditText>(R.id.editMonto)
        val menuCategoria = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        val btnDescartar = view.findViewById<Button>(R.id.btnDescartar)
        btnTomarFoto = view.findViewById(R.id.btnTomarFoto)
        imgPreview = view.findViewById(R.id.imgTicketPreview)
        progressBar = view.findViewById(R.id.progressBarSubida)

        // LÓGICA FECHA
        actualizarCampoFecha(editFecha)
        editFecha.setOnClickListener { mostrarSelectorFecha(editFecha) }

        // CONFIGURAR MENÚ
        val categorias = listOf("Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown_category, categorias)
        menuCategoria.setAdapter(adapter)

        // BOTÓN CÁMARA
        btnTomarFoto?.setOnClickListener {
            verificarPermisosYCamara()
        }

        // CONFIRMAR
        btnConfirmar?.setOnClickListener {
            val nombre = editNombre.text.toString()
            val montoStr = editMonto.text.toString().replace("$", "").replace(",", ".").trim()
            val monto = montoStr.toDoubleOrNull() ?: 0.0
            val categoriaSeleccionada = menuCategoria.text.toString()

            if (nombre.isNotBlank() && monto > 0.0) {
                if (photoUri != null) {
                    subirYGuardar(nombre, monto, categoriaSeleccionada, editFecha.text.toString())
                } else {
                    Toast.makeText(context, "Por favor, toma una foto del ticket", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Revisa los datos", Toast.LENGTH_SHORT).show()
            }
        }

        btnDescartar?.setOnClickListener { findNavController().popBackStack() }
    }

    private fun verificarPermisosYCamara() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                abrirCamara()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun abrirCamara() {
        val file = crearArchivoImagen()
        photoFile = file
        photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
        takePictureLauncher.launch(photoUri)
    }

    private fun crearArchivoImagen(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("TICKET_${timeStamp}_", ".jpg", storageDir)
    }

    private fun subirYGuardar(nombre: String, monto: Double, categoria: String, fecha: String) {
        val uri = photoUri ?: return

        progressBar?.visibility = View.VISIBLE
        btnConfirmar?.isEnabled = false 
        
        // Usamos directamente el Uri con el nuevo método del ViewModel para evitar errores de sesión
        viewModel.subirImagenTicket(uri, 
            onComplete = { url ->
                val currentUser = FirebaseAuth.getInstance().currentUser
                val nuevoGasto = Gasto(
                    id = UUID.randomUUID().toString(),
                    nombreComercio = nombre,
                    fecha = fecha,
                    categoria = categoria,
                    importe = monto,
                    timestamp = calendar.timeInMillis,
                    imagenUrl = url,
                    estado = EstadoGasto.PENDIENTE,
                    userId = currentUser?.uid ?: "",
                    emailUsuario = currentUser?.email ?: ""
                )

                viewModel.agregarGasto(nuevoGasto)
                if (isAdded) {
                    progressBar?.visibility = View.GONE
                    Toast.makeText(context, "Ticket guardado con éxito", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            },
            onError = { e ->
                if (isAdded) {
                    progressBar?.visibility = View.GONE
                    btnConfirmar?.isEnabled = true
                    Toast.makeText(context, "Error al subir: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun mostrarSelectorFecha(editText: EditText) {
        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(year, month, day)
            actualizarCampoFecha(editText)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun actualizarCampoFecha(editText: EditText) {
        val simpleDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        editText.setText(simpleDateFormat.format(calendar.time))
    }
}
