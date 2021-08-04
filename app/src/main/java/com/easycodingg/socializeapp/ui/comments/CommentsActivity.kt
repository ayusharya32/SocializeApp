package com.easycodingg.socializeapp.ui.comments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easycodingg.socializeapp.adapters.CommentsAdapter
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.api.responses.Post
import com.easycodingg.socializeapp.databinding.ActivityCommentsBinding
import com.easycodingg.socializeapp.ui.userprofile.UserProfileActivity
import com.easycodingg.socializeapp.utils.Constants
import com.easycodingg.socializeapp.utils.Constants.EXTRA_POST
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class CommentsActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCommentsBinding

    private val viewModel: CommentsViewModel by viewModels()
    private lateinit var commentsAdapter: CommentsAdapter

    @Inject
    lateinit var userPreferences: UserPreferences
    private var accessToken = ""

    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCommentsRecyclerView()
        observeCommentListResponse()
        observeAddRemoveCommentResponse()
        collectEvents()

        currentPost = intent.getParcelableExtra(EXTRA_POST)

        userPreferences.authToken.asLiveData().observe(this) { authToken ->
            authToken?.let {
                accessToken = it
                viewModel.onTokenRetrieved(accessToken, currentPost?.postId!!)
            }
        }

        binding.etComment.apply {
            setOnKeyListener { _, keyCode, event ->
                if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    closeKeyboard()

                    val commentText = this.text.trim().toString()
                    viewModel.onSendButtonClicked(accessToken, currentPost?.postId!!, commentText)

                    this.text.clear()
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    private fun observeCommentListResponse() {
        viewModel.commentListResponse.observe(this) { response ->
            binding.apply {
                pbComments.isVisible = response is Resource.Loading
                tvNoComments.isVisible = response is Resource.Success && response.data?.isEmpty()!!
            }

            if(response is Resource.Success) {
                response.data?.let {
                    commentsAdapter.apply {
                        submitList(it)
                        notifyDataSetChanged()
                    }
                }
            }

            if(response is Resource.Error) {
                Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAddRemoveCommentResponse() {
        viewModel.addCommentResponse.observe(this){ response ->
            handleAddRemoveCommentResponse(response)
        }

        viewModel.removeCommentResponse.observe(this){ response ->
            handleAddRemoveCommentResponse(response)
        }
    }

    private fun handleAddRemoveCommentResponse(response: Resource<MessageResponse>) {
        binding.pbComments.isVisible = response is Resource.Loading

        if(response is Resource.Success) {
            Toast.makeText(this, response.data?.message, Toast.LENGTH_SHORT).show()
            viewModel.refreshCommentList(accessToken, currentPost?.postId!!)
        }

        if(response is Resource.Error) {
            Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCommentsRecyclerView() {
        commentsAdapter = CommentsAdapter(
                listOf(),
                onProfileItemClickListener = { user ->
                    Intent(this, UserProfileActivity::class.java).apply {
                        putExtra(Constants.EXTRA_USER, user)
                    }.also {
                        startActivity(it)
                    }
                },
                onDeleteButtonClickedListener = { comment ->
                    viewModel.onCommentDeleteButtonClicked(accessToken, comment.commentId)
                }
        )

        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = commentsAdapter
        }
    }

    private fun collectEvents() = lifecycleScope.launchWhenStarted {
        viewModel.events.collect { event ->
            when(event) {
                is CommentsViewModel.Event.ShowMessage -> {
                    Toast.makeText(this@CommentsActivity, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus

        view?.let {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}