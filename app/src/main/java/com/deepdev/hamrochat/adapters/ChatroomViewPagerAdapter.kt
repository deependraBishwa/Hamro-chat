package com.deepdev.hamrochat.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatroomViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

     val fragments = mutableListOf<Fragment>()
     val titles = mutableListOf<String>()

    fun addFragment(fragment : Fragment, title : String){
        fragments.add(fragment)
        titles.add(title)
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}