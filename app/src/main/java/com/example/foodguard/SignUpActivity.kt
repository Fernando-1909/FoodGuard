package com.example.foodguard

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.foodguard.data.FoodDatabase
import com.example.foodguard.data.User
import com.example.foodguard.data.UserManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        userManager = UserManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnRegister).setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.etName).text.toString().trim()
            val email = findViewById<TextInputEditText>(R.id.etEmail).text.toString().trim().lowercase()
            val password = findViewById<TextInputEditText>(R.id.etPassword).text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "E-mail inválido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newUser = User(email, name, password)
                
                lifecycleScope.launch {
                    val userDao = FoodDatabase.getDatabase(this@SignUpActivity).userDao()
                    val existingUser = userDao.getUserByEmail(email)
                    
                    if (existingUser == null) {
                        userDao.insert(newUser)
                        userManager.setLoggedInUser(email)
                        
                        Toast.makeText(this@SignUpActivity, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignUpActivity, "Este e-mail já está cadastrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
