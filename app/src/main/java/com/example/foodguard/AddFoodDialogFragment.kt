package com.example.foodguard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.foodguard.data.FoodItem
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class AddFoodDialogFragment : DialogFragment() {

    private val viewModel: FoodViewModel by activityViewModels()
    private var calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var foodToEdit: FoodItem? = null

    private val categories = arrayOf(
        "Carnes", "Frutas", "Legumes", "Verduras", "Laticínios", 
        "Grãos/Cereais", "Bebidas", "Congelados", "Padaria", "Sobremesa",
        "Massas", "Carboidratos", "Outros"
    )

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
        val etDate = view.findViewById<TextInputEditText>(R.id.etExpirationDate)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)

        // Setup Category Dropdown
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        // Setup Date Picker
        etDate.setOnClickListener {
            showDatePicker(etDate)
        }
        etDate.isFocusable = false // Prevent keyboard from showing

        // If editing, populate fields
        foodToEdit?.let { food ->
            tvTitle?.text = "Editar Alimento"
            etName.setText(food.name)
            etCategory.setText(food.category, false)
            etQuantity.setText(food.quantity)
            calendar.timeInMillis = food.expirationDate
            etDate.setText(dateFormatter.format(calendar.time))
            btnAdd.text = "Atualizar"
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            val category = etCategory.text.toString()
            val quantity = etQuantity.text.toString()
            val dateStr = etDate.text.toString()

            if (name.isBlank()) {
                etName.error = "O nome é obrigatório"
                return@setOnClickListener
            }

            if (dateStr.isBlank()) {
                etDate.error = "A data é obrigatória"
                return@setOnClickListener
            }

            val updatedFood = foodToEdit?.copy(
                name = name,
                category = category,
                quantity = quantity,
                expirationDate = calendar.timeInMillis
            ) ?: FoodItem(
                userId = viewModel.getCurrentUserId(),
                name = name,
                category = category,
                quantity = quantity,
                expirationDate = calendar.timeInMillis
            )

            if (foodToEdit != null) {
                viewModel.update(updatedFood)
                Toast.makeText(context, "Alimento atualizado!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.insert(updatedFood)
                Toast.makeText(context, "Alimento adicionado!", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
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
