package com.easycodingg.socializeapp.ui.userprofile

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.adapters.PostsAdapter
import com.easycodingg.socializeapp.api.responses.MessageResponse
import com.easycodingg.socializeapp.api.responses.Post
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.databinding.ActivityUserProfileBinding
import com.easycodingg.socializeapp.ui.comments.CommentsActivity
import com.easycodingg.socializeapp.ui.userlist.UserListActivity
import com.easycodingg.socializeapp.utils.Constants
import com.easycodingg.socializeapp.utils.Constants.EXTRA_USER
import com.easycodingg.socializeapp.utils.Constants.EXTRA_USER_ID
import com.easycodingg.socializeapp.utils.Constants.EXTRA_USER_LIST_TYPE
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_USER_FOLLOWERS
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_USER_FOLLOWING
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class UserProfileActivity: AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    private var accessToken = ""

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var postsAdapter: PostsAdapter

    private var profileUser: User? = null
    private var currentUser: User? = null

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        profileUser = intent.getParcelableExtra(EXTRA_USER)
        profileUser?.let {
            populateUserData(it)
        }

        setupPostsRecyclerView()
        observeCurrentUserResponse()
        observePostsResponse()
        observeFollowResponses()
        observeAddRemoveLikeResponse()

        userPreferences.authToken.asLiveData().observe(this) { authToken ->
            authToken?.let {
                accessToken = it
                profileUser?.userId?.let { id ->
                    viewModel.onTokenRetrieved(accessToken, id)
                }
            }
        }

        binding.btnFollowToggle.setOnClickListener {
            val currentState = binding.btnFollowToggle.text
            viewModel.onFollowToggleClicked(accessToken, currentState.toString(), profileUser?.userId!!)
        }

        binding.llFollowers.setOnClickListener {
            Intent(this, UserListActivity::class.java).apply {
                putExtra(EXTRA_USER_LIST_TYPE, LIST_TYPE_USER_FOLLOWERS)
                putExtra(EXTRA_USER_ID, profileUser?.userId)
            }.also {
                startActivity(it)
            }
        }

        binding.llFollowing.setOnClickListener {
            Intent(this, UserListActivity::class.java).apply {
                putExtra(EXTRA_USER_LIST_TYPE, LIST_TYPE_USER_FOLLOWING)
                putExtra(EXTRA_USER_ID, profileUser?.userId)
            }.also {
                startActivity(it)
            }
        }
    }

    private fun observeCurrentUserResponse() {
        viewModel.currentUserResponse.observe(this) { response ->
            binding.pbProfile.isVisible = response is Resource.Loading

            if(response is Resource.Success) {
                response.data?.let { user ->

                    if(user.following.contains(profileUser?.userId)) {
                        binding.btnFollowToggle.apply {
                            text = "Unfollow"
                            setBackgroundColor(ContextCompat.getColor(this@UserProfileActivity, R.color.dark_grey))
                            setTextColor(Color.WHITE)
                        }
                    }
                    currentUser = user
                }
            }
            if(response is Resource.Error) {
                Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observePostsResponse() {
        viewModel.postsResponse.observe(this) { response ->
            binding.pbProfile.isVisible = response is Resource.Loading

            if(response is Resource.Success) {
                response.data?.let {
                    val updatedPostList = setLikedValueToPosts(it)

                    postsAdapter.apply {
                        submitList(updatedPostList)
                        notifyDataSetChanged()
                    }
                    binding.tvPosts.text = updatedPostList.size.toString()
                }
            }

            if(response is Resource.Error) {
                Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAddRemoveLikeResponse() {
        viewModel.addLikeResponse.observe(this) { response ->
            handleAddRemovePostLikeResponse(response)
        }

        viewModel.removeLikeResponse.observe(this) { response ->
            handleAddRemovePostLikeResponse(response)
        }
    }

    private fun handleAddRemovePostLikeResponse(response: Resource<Post>) {
        if(response is Resource.Success) {
            response.data?.let { post ->
                postsAdapter.currentList().find {
                    it.postId == post.postId
                }?.apply {
                    isLiked = post.isLiked

                    currentUser?.userId?.let { id ->
                        if(isLiked) {
                            likedBy.add(id)
                        } else {
                            likedBy.remove(id)
                        }
                    }
                }
                postsAdapter.notifyDataSetChanged()
            }
        }

        if(response is Resource.Error) {
            Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeFollowResponses() {
        viewModel.followUserResponse.observe(this) { response ->
           handleToggleFollowResponse(response)
        }

        viewModel.unfollowUserResponse.observe(this) { response ->
            handleToggleFollowResponse(response)
        }
    }

    private fun handleToggleFollowResponse(response: Resource<MessageResponse>) {
        binding.pbProfile.isVisible = response is Resource.Loading

        if(response is Resource.Success) {
            Toast.makeText(this, response.data?.message, Toast.LENGTH_SHORT).show()
            toggleFollowButton()
        }

        if(response is Resource.Error) {
            Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFollowButton() {
        binding.apply {
            val buttonText = btnFollowToggle.text.toString()

            if(buttonText == "Follow") {
                btnFollowToggle.apply {
                    text = "Unfollow"
                    setBackgroundColor(ContextCompat.getColor(this@UserProfileActivity, R.color.dark_grey))
                    setTextColor(Color.WHITE)
                }

            } else if(buttonText == "Unfollow") {
                btnFollowToggle.apply {
                    text = "Follow"
                    setBackgroundColor(Color.WHITE)
                    setTextColor(ContextCompat.getColor(this@UserProfileActivity, R.color.dark_grey))
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        binding.apply {
            tvName.text = user.name
            tvEmail.text = user.email
            tvFollowers.text = user.followers.toString()
            tvFollowing.text = user.following.size.toString()

            Glide.with(this@UserProfileActivity)
                    .load(user.profilePhotoUrl)
                    .placeholder(R.drawable.ic_user)
                    .centerCrop()
                    .into(ivProfileImage)
        }
    }

    private fun setupPostsRecyclerView() {
        postsAdapter = PostsAdapter(
                listOf(),
                onCommentButtonClickedListener = { post ->
                    Intent(this, CommentsActivity::class.java).apply {
                        putExtra(Constants.EXTRA_POST, post)
                    }.also {
                        startActivity(it)
                    }
                },
                onLikeButtonClickedListener = { post ->
                    viewModel.onPostLikeButtonClicked(accessToken, post)
                }
        )

        binding.rvProfilePosts.apply {
            layoutManager = LinearLayoutManager(this@UserProfileActivity)
            adapter = postsAdapter
        }
    }

    private fun setLikedValueToPosts(postList: List<Post>): List<Post> {
        postList.forEach { post ->
            post.isLiked = post.likedBy.contains(currentUser?.userId)
        }
        return postList
    }
}