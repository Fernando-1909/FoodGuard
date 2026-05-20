package com.example.foodguard

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.foodguard.data.FoodItem
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AddFoodDialogFragment : DialogFragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private var calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private var foodToEdit: FoodItem? = null
    
    private var currentImageUri: Uri? = null
    private var cameraTempUri: Uri? = null

    private lateinit var ivFoodPhoto: ShapeableImageView

    private val categories = arrayOf(
        "Carnes", "Frutas", "Legumes", "Verduras", "Laticínios", 
        "Grãos/Cereais", "Bebidas", "Congelados", "Padaria", "Sobremesa",
        "Massas", "Carboidratos", "Outros"
    )

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedUri = saveImageLocally(it)
            if (savedUri != null) {
                currentImageUri = savedUri
                displayImage(savedUri)
            }
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraTempUri != null) {
            val savedUri = saveImageLocally(cameraTempUri!!)
            if (savedUri != null) {
                currentImageUri = savedUri
                displayImage(savedUri)
            }
        }
    }

    private fun displayImage(uri: Uri?) {
        if (uri == null) return
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                ivFoodPhoto.setImageBitmap(bitmap)
                ivFoodPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
                ivFoodPhoto.clearColorFilter()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveImageLocally(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "food_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        fun newInstance(foodItem: FoodItem? = null): AddFoodDialogFragment {
            val fragment = AddFoodDialogFragment()
            val args = Bundle()
            args.putParcelable("food_item", foodItem)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foodToEdit = arguments?.getParcelable("food_item")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        val etName = view.findViewById<TextInputEditText>(R.id.etFoodName)
        val etCategory = view.findViewById<AutoCompleteTextView>(R.id.etCategory)
        val etQuantity = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val etDate = view.findViewById<TextInputEditText>(R.id.etExpirationDate)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        ivFoodPhoto = view.findViewById(R.id.ivFoodPhoto)
        val btnSelectPhoto = view.findViewById<Button>(R.id.btnSelectPhoto)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        etDate.setOnClickListener { showDatePicker(etDate) }
        etDate.isFocusable = false

        btnSelectPhoto.setOnClickListener { showImagePickerOptions() }

        // Máscara de preço estilo Banco
        etPrice?.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    etPrice.removeTextChangedListener(this)

                    val cleanString = s.toString().replace("[R$,.\\s]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDouble()
                        val formatted = currencyFormat.format(parsed / 100)
                        current = formatted
                        etPrice.setText(formatted)
                        etPrice.setSelection(formatted.length)
                    } else {
                        current = ""
                        etPrice.setText("")
                    }

                    etPrice.addTextChangedListener(this)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        foodToEdit?.let { food ->
            tvTitle?.text = "Editar Alimento"
            etName.setText(food.name)
            etCategory.setText(food.category, false)
            etQuantity.setText(food.quantity)
            
            // Formatar preço inicial se existir
            food.price?.let {
                val formatted = currencyFormat.format(it)
                etPrice.setText(formatted)
            }
            
            calendar.timeInMillis = food.expirationDate
            etDate.setText(dateFormatter.format(calendar.time))
            btnAdd.text = "Atualizar"
            
            food.imageUri?.let { uriString ->
                currentImageUri = Uri.parse(uriString)
                displayImage(currentImageUri)
            }
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val category = etCategory.text.toString()
            val quantity = etQuantity.text.toString()
            val priceStr = etPrice.text.toString()
            val dateStr = etDate.text.toString()

            if (name.isBlank() || dateStr.isBlank()) {
                if (name.isBlank()) etName.error = "O nome é obrigatório"
                if (dateStr.isBlank()) etDate.error = "A data é obrigatória"
                return@setOnClickListener
            }

            // Converter R$ 1.234,56 -> 1234.56
            val price = priceStr.replace("[R$\\s.]".toRegex(), "").replace(",", ".").toDoubleOrNull()

            val updatedFood = foodToEdit?.copy(
                name = name,
                category = category,
                quantity = quantity,
                price = price,
                expirationDate = calendar.timeInMillis,
                imageUri = currentImageUri?.toString()
            ) ?: FoodItem(
                userId = viewModel.getCurrentUserId(),
                name = name,
                category = category,
                quantity = quantity,
                price = price,
                expirationDate = calendar.timeInMillis,
                imageUri = currentImageUri?.toString()
            )

            if (foodToEdit != null) {
                viewModel.update(updatedFood)
            } else {
                viewModel.insert(updatedFood)
            }
            dismiss()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Câmera", "Galeria")
        AlertDialog.Builder(requireContext())
            .setTitle("Selecionar Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private fun openCamera() {
        val fileName = "temp_photo.jpg"
        val storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val tempFile = File(storageDir, fileName)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            tempFile
        )
        cameraTempUri = uri
        takePicture.launch(uri)
    }

    private fun showDatePicker(etDate: TextInputEditText) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                etDate.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}
