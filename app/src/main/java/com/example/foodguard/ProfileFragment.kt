package com.example.foodguard

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.foodguard.data.FoodDatabase
import com.example.foodguard.data.UserManager
import com.example.foodguard.viewmodel.FoodViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private lateinit var userManager: UserManager
    private val viewModel: FoodViewModel by activityViewModels()
    private lateinit var ivProfile: ImageView

    private var cameraTempUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data ?: cameraTempUri
            if (imageUri != null) {
                saveProfileImage(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userManager = UserManager(requireContext())
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        ivProfile = view.findViewById(R.id.ivProfile)

        lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                if (user != null) {
                    tvUserName.text = user.name
                    tvUserEmail.text = user.email
                    if (user.profileImageUri != null) {
                        loadProfileImage(user.profileImageUri)
                    }
                }
            }
        }

        ivProfile.setOnClickListener {
            showImagePickerOptions()
        }

        view.findViewById<View>(R.id.btnNotifications).setOnClickListener {
            Toast.makeText(context, "Configurações de Notificações", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btnSecurity).setOnClickListener {
            Toast.makeText(context, "Segurança e Privacidade", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btnStorage).setOnClickListener {
            Toast.makeText(context, "Gerenciar Armazenamento", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btnHelp).setOnClickListener {
            Toast.makeText(context, "Ajuda e Suporte", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        view.findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            userManager.logout()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Câmera", "Galeria")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Selecionar Foto de Perfil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun openCamera() {
        val fileName = "profile_temp.jpg"
        val storageDir = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        val tempFile = File(storageDir, fileName)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            tempFile
        )
        cameraTempUri = uri
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        pickImageLauncher.launch(intent)
    }

    private fun saveProfileImage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "profile_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            
            val user = viewModel.currentUser.value
            if (user != null) {
                val updatedUser = user.copy(profileImageUri = file.absolutePath)
                viewModel.updateUser(updatedUser)
                Toast.makeText(context, "Foto de perfil atualizada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage(path: String) {
        try {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                ivProfile.setImageBitmap(bitmap)
                ivProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                ivProfile.setPadding(0, 0, 0, 0)
            }
        } catch (e: Exception) {
            // Rollback to default icon
        }
    }
}
