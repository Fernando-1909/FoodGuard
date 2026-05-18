package com.example.foodguard

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodguard.data.UserManager
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Garante que o app sempre use o modo claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        userManager = UserManager(this)

        // Se o usuário já estiver logado, vai direto para a MainActivity
        if (userManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botão "Entrar" abre a nova tela de formulário de login
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginFormActivity::class.java))
        }

        // Botão "Criar conta" abre a tela de cadastro
        findViewById<MaterialButton>(R.id.btnCreateAccount).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
