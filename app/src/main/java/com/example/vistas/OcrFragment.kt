package com.example.vistas

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class OcrFragment : Fragment(R.layout.fragment_ocr_validation) {

    private val viewModel: MainViewModel by activityViewModels()
    private val calendar = Calendar.getInstance()
    private var photoUri: Uri? = null
    private var currentPhotoFile: File? = null

    // UI
    private lateinit var imgPreview: ImageView
    private lateinit var btnConfirmar: Button
    private lateinit var progressBar: ProgressBar

    // Cámara
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && currentPhotoFile != null) {
            imgPreview.setImageURI(photoUri)
            imgPreview.visibility = View.VISIBLE
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) abrirCamara() else Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bindings
        val editNombre = view.findViewById<EditText>(R.id.editNombre) // Nombre comercio
        val editFecha = view.findViewById<TextInputEditText>(R.id.editFecha)
        val editMonto = view.findViewById<EditText>(R.id.editMonto)
        val menuCategoria = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        val btnTomarFoto = view.findViewById<Button>(R.id.btnTomarFoto)
        imgPreview = view.findViewById(R.id.imgTicketPreview)
        progressBar = view.findViewById(R.id.progressBarSubida)
        val btnDescartar = view.findViewById<Button>(R.id.btnDescartar)

        // Configuración inicial
        actualizarCampoFecha(editFecha)

        // Categorías
        val categorias = listOf("Comida", "Transporte", "Alojamiento", "Suministros", "Equipamiento")
        menuCategoria.setAdapter(ArrayAdapter(requireContext(), R.layout.item_dropdown_category, categorias))

        // Listeners
        editFecha.setOnClickListener { mostrarSelectorFecha(editFecha) }

        btnTomarFoto.setOnClickListener { verificarPermisosYCamara() }

        btnConfirmar.setOnClickListener {
            val nombre = editNombre.text.toString()
            val montoStr = editMonto.text.toString().replace(",", ".")
            val monto = montoStr.toDoubleOrNull()
            val categoria = menuCategoria.text.toString()

            if (nombre.isBlank() || monto == null || categoria.isBlank() || currentPhotoFile == null) {
                Toast.makeText(context, "Faltan datos o la foto", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Formato API: YYYY-MM-DD
            val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaApi = formatoApi.format(calendar.time)

            // Enviar al ViewModel
            viewModel.subirGasto(categoria, fechaApi, monto, nombre, currentPhotoFile!!)
        }

        btnDescartar.setOnClickListener { findNavController().popBackStack() }

        // Observar carga
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnConfirmar.isEnabled = !loading
        }

        // Observar respuesta
        viewModel.mensajeOp.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                if (msg.contains("ÉXITO")) {
                    findNavController().popBackStack() // Volver atrás si se guardó
                }
                viewModel.limpiarMensaje()
            }
        }
    }

    private fun verificarPermisosYCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        try {
            val file = File.createTempFile("TICKET_", ".jpg", requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES))
            currentPhotoFile = file
            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error cámara: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarSelectorFecha(editText: EditText) {
        val dpd = DatePickerDialog(requireContext(), { _, y, m, d ->
            calendar.set(y, m, d)
            actualizarCampoFecha(editText)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        dpd.show()
    }

    private fun actualizarCampoFecha(editText: EditText) {
        // Formato visual para el usuario
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        editText.setText(sdf.format(calendar.time))
    }
}