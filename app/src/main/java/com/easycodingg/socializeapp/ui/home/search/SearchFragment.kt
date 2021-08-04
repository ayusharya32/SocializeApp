package com.easycodingg.socializeapp.ui.home.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.adapters.UsersAdapter
import com.easycodingg.socializeapp.databinding.FragmentSearchBinding
import com.easycodingg.socializeapp.ui.userprofile.UserProfileActivity
import com.easycodingg.socializeapp.utils.Constants.EXTRA_USER
import com.easycodingg.socializeapp.utils.Resource
import com.easycodingg.socializeapp.utils.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment: Fragment(R.layout.fragment_search) {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModels()

    private var accessToken = ""
    private lateinit var usersAdapter: UsersAdapter

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)

        setupUsersRecyclerView()
        observeSearchResponse()

        userPreferences.authToken.asLiveData().observe(viewLifecycleOwner) { authToken ->
            authToken?.let {
                accessToken = authToken
            }
        }

        binding.etSearch.apply {    
            setOnKeyListener { _, keyCode, event ->
                if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    closeKeyboard()

                    val searchQuery = this.text.trim().toString()
                    viewModel.onSearchButtonClicked(accessToken, searchQuery)

                    return@setOnKeyListener true
                }
                false
            }
        }

        collectEvents()
    }

    private fun setupUsersRecyclerView() {
        usersAdapter = UsersAdapter(listOf()) { user ->
            Intent(requireContext(), UserProfileActivity::class.java).apply {
                putExtra(EXTRA_USER, user)
            }.also {
                startActivity(it)
            }
        }

        binding.rvSearch.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeSearchResponse() {
        viewModel.searchResponse.observe(viewLifecycleOwner) { response ->
            binding.pbSearch.isVisible = response is Resource.Loading

            if(response is Resource.Success) {
                response.data?.let {
                    usersAdapter.apply {
                        submitList(it)
                        notifyDataSetChanged()
                    }
                }
            }

            if(response is Resource.Error) {
                Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun collectEvents() = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
        viewModel.events.collect { event ->
            when(event) {
                is SearchViewModel.Event.ShowErrorMessage -> {
                    Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun closeKeyboard() {
        val view = requireActivity().currentFocus

        view?.let {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}