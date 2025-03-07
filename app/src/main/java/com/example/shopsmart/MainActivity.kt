package com.example.shopsmart

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import fragments.CartFragment
import fragments.HomePageFragment
import fragments.ShopFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(HomePageFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomePageFragment())
                R.id.nav_search -> loadFragment(ShopFragment())
                R.id.nav_cart -> loadFragment(CartFragment())
            }
            fun getFragmentName(itemId: Int): String {
                return when (itemId) {
                    R.id.nav_home -> "HomeFragment"
                    R.id.nav_search -> "ShopFragment"
                    R.id.nav_cart -> "CartFragment"
                    else -> "Unknown Fragment"
                }
            }
            Log.d("FRAG SWITCH", "Item ID: ${item.itemId}, Fragment: ${getFragmentName(item.itemId)}")
            true
        }
    }

    // Function to replace/load fragments
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
