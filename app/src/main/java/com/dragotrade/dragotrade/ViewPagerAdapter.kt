package com.dragotrade.dragotrade

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dragotrade.dragotrade.bottomFragment.AnnouncementFragment
import com.dragotrade.dragotrade.bottomFragment.HomeFragment
import com.dragotrade.dragotrade.bottomFragment.ProfileFragment
import com.dragotrade.dragotrade.bottomFragment.WalletFragment

class ViewPagerAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int = 5 // Number of tabs/fragments

    override fun createFragment(position: Int): Fragment {
        // Return the appropriate fragment for each tab
        return when (position) {
            0 -> HomeFragment()
            1 -> WalletFragment()
            2 -> AnnouncementFragment()
            3 -> ProfileFragment()
            else -> HomeFragment() // Default to the first fragment
        }
    }
}

interface LogoutListener {
    fun onLogout()
}
