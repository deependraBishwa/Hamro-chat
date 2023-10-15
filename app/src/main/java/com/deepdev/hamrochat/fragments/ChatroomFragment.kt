package com.deepdev.hamrochat.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.activities.CreateChatroomActivity
import com.deepdev.hamrochat.adapters.ChatroomViewPagerAdapter
import com.deepdev.hamrochat.databinding.FragmentChatroom2Binding
import com.deepdev.hamrochat.fragments.subFragments.ChatroomSubFragment
import com.deepdev.hamrochat.fragments.subFragments.ChatroomSubFragment2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator

class ChatroomFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    private lateinit var fab: FloatingActionButton
    private var isFabVisible = true
    private val binding by lazy { FragmentChatroom2Binding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val myView = binding.root
        viewPager = requireActivity().findViewById(R.id.viewPager)
        fab = requireActivity().findViewById(R.id.fab_create_chat_room)
        fab.visibility = View.VISIBLE

        setUpViewPagerForFab()


        fabClick()
        tabLayoutSetup()




//todo work on chatroom creation

        return myView
    }

    private fun tabLayoutSetup() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ChatroomViewPagerAdapter(requireActivity())

        adapter.addFragment(ChatroomSubFragment(),  "chatroom")
        adapter.addFragment(ChatroomSubFragment2(),  "Recent")

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.titles[position].lowercase()
        }.attach()
    }



    private fun fabClick() {
        fab.setOnClickListener{
           val intent = Intent(requireActivity(), CreateChatroomActivity::class.java)
            startActivity(intent)
        }
    }



    private fun setUpViewPagerForFab() {

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 1) {
                    if (!isFabVisible) {
                        slideFabUp()
                    }
                } else {
                    if (isFabVisible) {
                        slideDownFab()
                    }
                }
            }
        })
    }

    private fun slideDownFab() {
        fab.animate()
            .translationY(fab.height.toFloat())
            .alpha(0f)
            .setListener(null)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isFabVisible = false
                }
            })
    }

    private fun slideFabUp() {
        fab.animate()
            .translationY(0f)
            .alpha(1f)
            .setListener(null)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isFabVisible = true
                }
            })
    }

}