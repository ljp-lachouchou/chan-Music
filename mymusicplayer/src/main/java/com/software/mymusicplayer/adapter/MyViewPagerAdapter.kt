package com.software.mymusicplayer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyViewPagerAdapter(private var fragments: List<Fragment>, fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = fragments.size


    override fun createFragment(position: Int): Fragment = fragments[position]
}