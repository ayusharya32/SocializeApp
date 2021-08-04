package com.easycodingg.socializeapp.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.easycodingg.socializeapp.R
import com.easycodingg.socializeapp.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity: AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navHost: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            navHost = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
            bottomNavMenu.setupWithNavController(navHost.findNavController())

            bottomNavMenu.setOnNavigationItemReselectedListener { /*NO-OP*/ }
        }
    }
}