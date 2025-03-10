package com.example.shopsmart

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import fragments.AdminPageFragment
import fragments.CartFragment
import fragments.FragmentAddProduct
import fragments.HomePageFragment
import fragments.LoginFragment
import fragments.MyOrdersFragment
import fragments.ShopFragment
import fragments.UserProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(HomePageFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> HomePageFragment()
                R.id.nav_shop -> ShopFragment()
                R.id.nav_cart -> CartFragment()
                R.id.nav_profile -> UserProfileFragment()
                else -> null
            }

            fragment?.let { loadFragment(it) }

            Log.d("FRAG SWITCH", "Selected Fragment: ${getFragmentName(item.itemId)}")
            true
        }
    }

    // Function to replace/load fragments
    fun loadFragment(fragment: Fragment) {
        // Remove all fragments from the container
        supportFragmentManager.fragments.forEach {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }

        // Replace the fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()

        updateBottomNavSelection(fragment)
    }

    // Function to update BottomNavigationView selection
    private fun updateBottomNavSelection(fragment: Fragment) {
        val menu = bottomNavigationView.menu
        when (fragment) {
            is HomePageFragment -> menu.findItem(R.id.nav_home).isChecked = true
            is ShopFragment -> menu.findItem(R.id.nav_shop).isChecked = true
            is CartFragment -> menu.findItem(R.id.nav_cart).isChecked = true
            is UserProfileFragment,is AdminPageFragment,is FragmentAddProduct,is MyOrdersFragment,is LoginFragment-> menu.findItem(R.id.nav_profile).isChecked = true

        }
    }

    // Function to get fragment name for logging
    private fun getFragmentName(itemId: Int): String {
        return when (itemId) {
            R.id.nav_home -> "HomeFragment"
            R.id.nav_shop -> "ShopFragment"
            R.id.nav_cart -> "CartFragment"
            R.id.nav_profile -> "UserProfileFragment"
            else -> "Unknown Fragment"
        }
    }
}
