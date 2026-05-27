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
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private lateinit var userManager: UserManager
    private val viewModel: FoodViewModel by activityViewModels()
    private lateinit var ivProfile: ShapeableImageView

    private var currentImageUri: Uri? = null
    private var cameraTempUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val savedUri = saveImageLocally(it)
            if (savedUri != null) {
                updateProfileImage(savedUri)
            }
        }
    }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraTempUri != null) {
            val savedUri = saveImageLocally(cameraTempUri!!)
            if (savedUri != null) {
                updateProfileImage(savedUri)
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
        val fabEditPhoto = view.findViewById<View>(R.id.fabEditPhoto)

        lifecycleScope.launch {
            viewModel.currentUser.collectLatest { user ->
                if (user != null) {
                    tvUserName.text = user.name
                    tvUserEmail.text = user.email
                    if (!user.profileImageUri.isNullOrEmpty()) {
                        loadProfileImage(user.profileImageUri)
                    } else {
                        ivProfile.setImageResource(R.drawable.ic_profile)
                        ivProfile.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        ivProfile.setPadding(60, 60, 60, 60)
                        ivProfile.setColorFilter(resources.getColor(R.color.primary_green, null))
                    }
                }
            }
        }

        fabEditPhoto.setOnClickListener {
            showImagePickerOptions()
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
        getContent.launch("image/*")
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
        takePicture.launch(uri)
    }

    private fun saveImageLocally(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "profile_${System.currentTimeMillis()}.jpg"
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

    private fun updateProfileImage(uri: Uri) {
        val user = viewModel.currentUser.value
        if (user != null) {
            val updatedUser = user.copy(profileImageUri = uri.toString())
            viewModel.updateUser(updatedUser)
            Toast.makeText(context, "Foto de perfil atualizada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                ivProfile.setImageBitmap(bitmap)
                ivProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                ivProfile.setPadding(0, 0, 0, 0)
                ivProfile.clearColorFilter()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ivProfile.setImageResource(R.drawable.ic_profile)
            ivProfile.scaleType = ImageView.ScaleType.CENTER_INSIDE
            ivProfile.setPadding(60, 60, 60, 60)
            ivProfile.setColorFilter(resources.getColor(R.color.primary_green, null))
        }
    }
}
