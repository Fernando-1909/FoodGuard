package com.example.foodguard

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.foodguard.data.FoodItem
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddFoodDialogFragment : DialogFragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private var calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val categories = arrayOf(
        "Carnes", "Frutas", "Legumes", "Verduras", "Laticínios", 
        "Grãos/Cereais", "Bebidas", "Congelados", "Padaria", "Outros"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<TextInputEditText>(R.id.etFoodName)
        val etCategory = view.findViewById<AutoCompleteTextView>(R.id.etCategory)
        val etQuantity = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val etDate = view.findViewById<TextInputEditText>(R.id.etExpirationDate)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)

        // Setup Category Dropdown
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        // Setup Date Picker and Mask
        etDate.setOnClickListener {
            showDatePicker(etDate)
        }
        setupDateMask(etDate)

        btnAdd.setOnClickListener {
            val name = etName.text.toString()
            val category = etCategory.text.toString()
            val quantity = etQuantity.text.toString()
            val dateStr = etDate.text.toString()

            if (name.isBlank() || dateStr.isBlank()) {
                Toast.makeText(context, "Nome e Data são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val date = dateFormatter.parse(dateStr)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: ParseException) {
                Toast.makeText(context, "Formato de data inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val foodItem = FoodItem(
                userId = viewModel.getCurrentUserId(),
                name = name,
                category = category,
                quantity = quantity,
                expirationDate = calendar.timeInMillis
            )

            viewModel.insert(foodItem)
            dismiss()
        }
    }

    private fun setupDateMask(etDate: TextInputEditText) {
        etDate.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "##/##/####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) {
                    isUpdating = false
                    return
                }

                val str = s.toString().replace("[^\\d]".toRegex(), "")
                var formatted = ""
                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        if (i < str.length) {
                            formatted += m
                        }
                        continue
                    }
                    if (i < str.length) {
                        formatted += str[i]
                    }
                    i++
                }

                isUpdating = true
                etDate.setText(formatted)
                etDate.setSelection(formatted.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
