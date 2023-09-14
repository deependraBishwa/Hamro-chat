package com.deepdev.hamrochat.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.adapters.ViewPagerAdapter
import com.deepdev.hamrochat.databinding.ActivityMain2Binding
import com.deepdev.hamrochat.fragments.ChatroomFragment
import com.deepdev.hamrochat.fragments.ForYouFragment
import com.deepdev.hamrochat.fragments.MessagesFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator

class Main2Activity : AppCompatActivity() {

    private lateinit var onBackPressedCallback : OnBackPressedCallback
    private val binding by lazy { ActivityMain2Binding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        onBackPressedHandle()

        tabLayoutSetup()



        binding.menuImageView.setOnClickListener {
            showBottomDialog()
        }

        //onclick profile image
        binding.imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)

            startActivity(intent)
        }

    }

    private fun onBackPressedHandle() {
        onBackPressedCallback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

            }
        }
        onBackPressedDispatcher.addCallback( onBackPressedCallback)
    }

    private fun showBottomDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout,null)
        bottomSheetDialog.setContentView(view)


        bottomSheetDialog.show()
    }

    private fun tabLayoutSetup() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(ForYouFragment(), "Feed")
        adapter.addFragment(ChatroomFragment(), "Chatroom")
        adapter.addFragment(MessagesFragment(), "Messages")

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.fragmentTitles[position]
        }.attach()

    }
}