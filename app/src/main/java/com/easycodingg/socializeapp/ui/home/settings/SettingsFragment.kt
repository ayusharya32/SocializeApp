package com.easycodingg.socializeapp.ui.home.settings

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.databinding.FragmentSettingsBinding
import com.easycodingg.socializeapp.ui.login.LoginActivity
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var updateImageFile: File? = null
    private var accessToken = ""

    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferences

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                updateImageFile = saveImage(uri)
                binding.ivUpdateImage.setImageURI(uri)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        collectEvents()
        observeUserResponse()

        userPreferences.authToken.asLiveData().observe(viewLifecycleOwner) { authToken ->
            authToken?.let {
                accessToken = it
            }
        }

        binding.ivUpdateImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            getContent.launch(intent)
        }

        binding.apply {
            btnUpdateProfile.setOnClickListener {
                val updateName = binding.etName.text.toString().trim()
                viewModel.onUpdateButtonClicked(accessToken, updateName, updateImageFile)
            }

            btnLogOut.setOnClickListener {
                viewModel.onLogOutButtonClicked()
            }
        }
    }

    private fun observeUserResponse() {
        viewModel.updateUserResponse.observe(viewLifecycleOwner){ response ->
            Log.d("Inside Observe", "Response: $response")
            binding.apply {
                btnUpdateProfile.isEnabled = response is Resource.Success || response is Resource.Error
                btnLogOut.isEnabled = response is Resource.Success || response is Resource.Error
                pbSettings.isVisible = response is Resource.Loading
            }

            if(response is Resource.Success) {
                Toast.makeText(requireContext(), response.data?.message, Toast.LENGTH_SHORT).show()
                viewModel.onProfileUpdatedSuccessfully()
            }

            if(response is Resource.Error) {
                Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImage(imageUri: Uri): File {
        val imageBitmap = if(Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        } else {
            val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        val dirname = "Profile Images"
        val fileName = "Profile${System.currentTimeMillis()}"

        val filePath = requireContext().getExternalFilesDir(dirname)?.path + "/$fileName.jpg"

        val file = File(filePath)

        try {
            val outputStream = FileOutputStream(file)
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }
        return file
    }

    private fun collectEvents() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        viewModel.events.collect { event ->
            when(event) {
                is SettingsViewModel.Event.ShowMessage -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
                is SettingsViewModel.Event.RedirectToLoginActivity -> {
                    Intent(requireContext(), LoginActivity::class.java).also {
                        startActivity(it)
                        requireActivity().finish()
                    }
                }
                is SettingsViewModel.Event.DisableButtons -> {
                    binding.apply {
                        btnLogOut.isEnabled = false
                        btnUpdateProfile.isEnabled = false
                    }
                }
                is SettingsViewModel.Event.ResetAllViews -> {
                    updateImageFile = null
                    binding.apply {
                        etName.text.clear()
                        ivUpdateImage.setImageResource(R.drawable.ic_user)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}