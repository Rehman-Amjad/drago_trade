package com.dragotrade.dragotrade

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.dragotrade.dragotrade.databinding.ActivityMainBinding
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.dragotrade.dragotrade.start.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import nl.joery.animatedbottombar.AnimatedBottomBar


class MainActivity : AppCompatActivity(), LogoutListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.fragmentContainer.adapter = adapter
        binding.fragmentContainer.offscreenPageLimit = 1

        // Set the default fragment to "HomeFragment"
        binding.fragmentContainer.setCurrentItem(0, false)
        binding.fragmentContainer.isUserInputEnabled = false

//        binding.bottomBar.onTabSelected = {
//            Log.d("bottom_bar", "Selected tab: " + it.title)
//        }
//        binding.bottomBar.onTabReselected = {
//            Log.d("bottom_bar", "Reselected tab: " + it.title)
//        }


        binding.bottomBar.setOnTabSelectListener(object : AnimatedBottomBar.OnTabSelectListener {
            override fun onTabSelected(
                lastIndex: Int,
                lastTab: AnimatedBottomBar.Tab?,
                newIndex: Int,
                newTab: AnimatedBottomBar.Tab
            ) {
                binding.fragmentContainer.setCurrentItem(newIndex, false)
                Log.d("bottom_bar", "Selected index: $newIndex, title: ${newTab.title}")
            }

            // An optional method that will be fired whenever an already selected tab has been selected again.
            override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                Log.d("bottom_bar", "Reselected index: $index, title: ${tab.title}")
            }
        })

        binding.fragmentContainer.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    4 -> {
                        // logout functions
//                        onLogout()
                    }
                }

            }
        })

    }

    override fun onLogout() {
//        preferenceManager.putBoolean(Constants.USER_STATUS, false)
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


}