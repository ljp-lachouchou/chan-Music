package com.software.mymusicplayer.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyViewPagerAdapterInFragment(private var fragments: List<Fragment>,
                                   fragment:Fragment) : FragmentStateAdapter(fragment)  {
    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment =fragments[position]
}