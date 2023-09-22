package com.deepdev.hamrochat.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.deepdev.hamrochat.MyApplication
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
    private val app by lazy { application as MyApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // change the color of three dot menu to white
        val overflowIcon= binding.toolbar.overflowIcon
        overflowIcon?.setTint(Color.WHITE)

        // current userdata prefetched in myapplication class
        val userModel = app.getUserData()
        Log.d("mssss", "onCreate: ${userModel.name}")


        onBackPressedHandle()

        tabLayoutSetup()




    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_profile -> startActivity(Intent(this, ProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_act, menu)
        return super.onCreateOptionsMenu(menu)
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
        adapter.addFragment(ForYouFragment(), "", getDrawable(R.drawable.ic_home))
        adapter.addFragment(ChatroomFragment(), "", getDrawable(R.drawable.ic_chatroom))
        adapter.addFragment(MessagesFragment(), "", getDrawable(R.drawable.ic_private_chat))


        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.fragmentTitles[position]
            tab.icon = adapter.fragmentIcons[position]
        }.attach()

    }
}