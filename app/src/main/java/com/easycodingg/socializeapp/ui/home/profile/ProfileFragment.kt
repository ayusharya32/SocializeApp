package com.easycodingg.socializeapp.ui.home.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.adapters.PostsAdapter
import com.easycodingg.socializeapp.api.responses.Post
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.databinding.FragmentProfileBinding
import com.easycodingg.socializeapp.ui.comments.CommentsActivity
import com.easycodingg.socializeapp.ui.userlist.UserListActivity
import com.easycodingg.socializeapp.utils.Constants.EXTRA_POST
import com.easycodingg.socializeapp.utils.Constants.EXTRA_USER_LIST_TYPE
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_CURRENT_USER_FOLLOWERS
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_CURRENT_USER_FOLLOWING
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var accessToken = ""

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var postsAdapter: PostsAdapter

    private var currentUser: User? = null

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupPostsRecyclerView()
        observeResponses()

        userPreferences.authToken.asLiveData().observe(viewLifecycleOwner) { authToken ->
            authToken?.let {
                accessToken = it
                viewModel.onTokenRetrieved(accessToken)
            }
        }

        binding.llFollowers.setOnClickListener {
            Intent(requireContext(), UserListActivity::class.java).apply {
                putExtra(EXTRA_USER_LIST_TYPE, LIST_TYPE_CURRENT_USER_FOLLOWERS)
            }.also {
                startActivity(it)
            }
        }

        binding.llFollowing.setOnClickListener {
            Intent(requireContext(), UserListActivity::class.java).apply {
                putExtra(EXTRA_USER_LIST_TYPE, LIST_TYPE_CURRENT_USER_FOLLOWING)
            }.also {
                startActivity(it)
            }
        }
    }

    private fun observeResponses() {
        viewModel.userResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                pbProfile.isVisible = response is Resource.Loading
            }

            if(response is Resource.Success) {
                response.data?.let {
                    currentUser = it
                    populateUserData(it)
                }
            }

            if(response is Resource.Error) {
                Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.postsResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                pbProfile.isVisible = response is Resource.Loading
            }

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
                Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deletePostResponse.observe(viewLifecycleOwner) { response ->
            binding.pbProfile.isVisible = response is Resource.Loading

            if(response is Resource.Success) {
                response.data?.let { deletedPostId ->
                    val postList = postsAdapter.currentList()
                    val updatedList = postList.filter { post ->
                        post.postId != deletedPostId
                    }.toList()

                    postsAdapter.apply {
                        submitList(updatedList)
                        notifyDataSetChanged()
                    }
                }
            }

            if(response is Resource.Error) {
                Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.addLikeResponse.observe(viewLifecycleOwner) { response ->
            handleAddRemovePostLikeResponse(response)
        }

        viewModel.removeLikeResponse.observe(viewLifecycleOwner) { response ->
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
            Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateUserData(user: User) {
        binding.apply {
            tvName.text = user.name
            tvEmail.text = user.email
            tvFollowers.text = user.followers.toString()
            tvFollowing.text = user.following.size.toString()

            Glide.with(requireContext())
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
                    Intent(requireContext(), CommentsActivity::class.java).apply {
                        putExtra(EXTRA_POST, post)
                    }.also {
                        startActivity(it)
                    }
                },
                onLikeButtonClickedListener = { post ->
                    viewModel.onPostLikeButtonClicked(accessToken, post)
                }
        )

        postsAdapter.apply {
            setDeleteIconVisibility(true)
            setOnDeleteButtonClickedListener { post ->
                viewModel.onPostDeleteButtonClicked(accessToken, post.postId)
            }
        }

        binding.rvProfilePosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }
    }

    private fun setLikedValueToPosts(postList: List<Post>): List<Post> {
        Log.d("Current"," User: $currentUser")
        postList.forEach { post ->
            post.isLiked = post.likedBy.contains(currentUser?.userId)
        }
        return postList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}