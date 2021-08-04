package com.easycodingg.socializeapp.ui.userlist

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.easycodingg.socializeapp.adapters.UsersAdapter
import com.easycodingg.socializeapp.databinding.ActivityUserListBinding
import com.easycodingg.socializeapp.utils.Constants.EXTRA_USER_ID
import com.easycodingg.socializeapp.utils.Constants.EXTRA_USER_LIST_TYPE
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_USER_FOLLOWERS
import com.easycodingg.socializeapp.utils.Constants.LIST_TYPE_USER_FOLLOWING
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserListActivity: AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding

    private val viewModel: UserListViewModel by viewModels()
    private lateinit var usersAdapter: UsersAdapter

    private var accessToken = ""

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUsersRecyclerView()
        observeUserListResponse()

        val userListType = intent.getStringExtra(EXTRA_USER_LIST_TYPE)

        var queryUserId = ""
        if(userListType == LIST_TYPE_USER_FOLLOWING || userListType == LIST_TYPE_USER_FOLLOWERS) {
            queryUserId = intent.getStringExtra(EXTRA_USER_ID).toString()
        }

        userPreferences.authToken.asLiveData().observe(this) { authToken ->
            authToken?.let {
                accessToken = it
                if (userListType != null) {
                    viewModel.onTokenRetrieved(accessToken, userListType, queryUserId)
                }
            }
        }
    }

    private fun observeUserListResponse() {
        viewModel.userListResponse.observe(this) { response ->
            binding.apply {
                pbUsers.isVisible = response is Resource.Loading
                tvNoUsers.isVisible = response is Resource.Success && response.data?.isEmpty()!!
            }

            if(response is Resource.Success) {
                response.data?.let {
                    usersAdapter.apply {
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

    private fun setupUsersRecyclerView() {
        usersAdapter = UsersAdapter(listOf()) { /*NO-OP*/ }

        binding.rvUsers.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(this@UserListActivity)
        }
    }
}