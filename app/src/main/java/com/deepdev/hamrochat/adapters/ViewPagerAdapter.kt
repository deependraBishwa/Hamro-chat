package com.deepdev.hamrochat.adapters

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {
    private val fragments = mutableListOf<Fragment>()
    val fragmentTitles = mutableListOf<String>()
    val fragmentIcons = mutableListOf<Drawable?>()

    fun addFragment(fragment: Fragment, title: String, icon : Drawable?) {
        fragments.add(fragment)
        fragmentTitles.add(title)
        fragmentIcons.add(icon)
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
