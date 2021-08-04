package com.easycodingg.socializeapp.ui.home.addpost

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.databinding.FragmentAddPostBinding
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class AddPostFragment: Fragment(R.layout.fragment_add_post) {

    private var _binding: FragmentAddPostBinding? = null
    private val binding get() = _binding!!

    private var accessToken = ""
    private var postImageFile: File? = null

    private val viewModel: AddPostViewModel by viewModels()

    @Inject
    lateinit var userPreferences: UserPreferences

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                postImageFile = saveImage(uri)
                binding.ivPostImage.setImageURI(uri)
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddPostBinding.bind(view)

        collectEvents()
        observeAddPostResponse()

        userPreferences.authToken.asLiveData().observe(viewLifecycleOwner) { authToken ->
            authToken?.let {
                accessToken = it
            }
        }

        binding.apply {
            fabChooseImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                getContent.launch(intent)
            }

            btnPost.setOnClickListener {
                val caption = etCaption.text.trim().toString()
                viewModel.onPostButtonClicked(accessToken, postImageFile, caption)
            }
        }
    }

    private fun observeAddPostResponse() {
        viewModel.addPostResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                pbAddPost.isVisible = response is Resource.Loading
                btnPost.isEnabled = response !is Resource.Loading
            }

            if(response is Resource.Success) {
                Toast.makeText(requireContext(), response.data?.message, Toast.LENGTH_SHORT).show()
                viewModel.onSuccessfullyPosted()
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

        val dirname = "Post Images"
        val fileName = "Post${System.currentTimeMillis()}"

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
        viewModel.events.collect { event->
            when(event) {
                is AddPostViewModel.Event.ShowMessage -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
                is AddPostViewModel.Event.DisableButtons -> {
                    binding.btnPost.isEnabled = false
                }
                is AddPostViewModel.Event.ResetAllViews -> {
                    postImageFile = null
                    binding.apply {
                        etCaption.text.clear()
                        ivPostImage.setImageResource(R.drawable.dot_placeholder)
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