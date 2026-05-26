package com.example.foodguard

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.foodguard.data.FoodDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnResetPassword).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim().lowercase()
            val newPassword = findViewById<TextInputEditText>(R.id.etNewPassword).text.toString()
            val confirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword).text.toString()

            if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val userDao = FoodDatabase.getDatabase(this@ResetPasswordActivity).userDao()
                val user = userDao.getUserByEmail(email)

                if (user != null) {
                    val updatedUser = user.copy(password = newPassword)
                    userDao.update(updatedUser)
                    Toast.makeText(this@ResetPasswordActivity, "Senha redefinida com sucesso", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ResetPasswordActivity, "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
