package com.easycodingg.socializeapp.ui.home.feed

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.adapters.PostsAdapter
import com.easycodingg.socializeapp.api.responses.Post
import com.easycodingg.socializeapp.api.responses.User
import com.easycodingg.socializeapp.databinding.FragmentFeedBinding
import com.easycodingg.socializeapp.ui.comments.CommentsActivity
import com.easycodingg.socializeapp.ui.userprofile.UserProfileActivity
import com.easycodingg.socializeapp.utils.Constants
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment: Fragment(R.layout.fragment_feed) {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private var accessToken = ""

    private val viewModel: FeedViewModel by viewModels()

    private lateinit var feedPostsAdapter: PostsAdapter
    private var currentUser: User? = null

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedBinding.bind(view)

        setupFeedPostsRecyclerView()
        observeUserResponse()
        observeFeedPosts()
        observeAddRemoveLikeResponse()

        userPreferences.authToken.asLiveData().observe(viewLifecycleOwner) { authToken ->
            authToken?.let {
                accessToken = it
                viewModel.onTokenRetrieved(accessToken)
            }
        }
    }

    private fun observeUserResponse() {
        viewModel.userResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                pbFeed.isVisible = response is Resource.Loading
            }

            if(response is Resource.Success) {
                response.data?.let {
                    currentUser = it
                }
            }

            if(response is Resource.Error) {
                Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAddRemoveLikeResponse() {
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
                feedPostsAdapter.currentList().find {
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
                feedPostsAdapter.notifyDataSetChanged()
            }
        }

        if(response is Resource.Error) {
            Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFeedPostsRecyclerView() {
        feedPostsAdapter = PostsAdapter(
                listOf(),
                onCommentButtonClickedListener = { post ->
                    Intent(requireContext(), CommentsActivity::class.java).apply {
                        putExtra(Constants.EXTRA_POST, post)
                    }.also {
                        startActivity(it)
                    }

                },
                onLikeButtonClickedListener = {
                    viewModel.onPostLikeButtonClicked(accessToken, it)
                }
        )

        feedPostsAdapter.setOnProfileItemClickedListener { user ->
            Intent(requireContext(), UserProfileActivity::class.java).apply {
                putExtra(Constants.EXTRA_USER, user)
            }.also {
                startActivity(it)
            }
        }

        binding.rvFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedPostsAdapter
        }
    }

    private fun observeFeedPosts() {
        viewModel.feedPostsResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                pbFeed.isVisible = response is Resource.Loading
            }

            if(response is Resource.Success) {
                response.data?.let {
                    val updatedPostList = setLikedValueToPosts(it)

                    feedPostsAdapter.apply {
                        submitList(updatedPostList)
                        notifyDataSetChanged()
                    }
                }
            }
            if(response is Resource.Error) {
                Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLikedValueToPosts(postList: List<Post>): List<Post> {
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