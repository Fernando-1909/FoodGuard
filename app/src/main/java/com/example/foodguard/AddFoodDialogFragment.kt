package com.example.foodguard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        val etCategory = view.findViewById<TextInputEditText>(R.id.etCategory)
        val etQuantity = view.findViewById<TextInputEditText>(R.id.etQuantity)
        val etDate = view.findViewById<TextInputEditText>(R.id.etExpirationDate)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)

        etDate.setOnClickListener {
            showDatePicker(etDate)
        }

        btnAdd.setOnClickListener {
            val name = etName.text.toString()
            val category = etCategory.text.toString()
            val quantity = etQuantity.text.toString()
            val dateStr = etDate.text.toString()

            if (name.isBlank() || dateStr.isBlank()) {
                Toast.makeText(context, "Nome e Data são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val foodItem = FoodItem(
                name = name,
                category = category,
                quantity = quantity,
                expirationDate = calendar.timeInMillis
            )

            viewModel.insert(foodItem)
            dismiss()
        }
    }

    private fun showDatePicker(etDate: TextInputEditText) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDate.setText(format.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
