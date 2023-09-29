package com.deepdev.hamrochat.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.deepdev.hamrochat.R
import com.deepdev.hamrochat.adapters.ViewPagerAdapter
import com.deepdev.hamrochat.databinding.ActivityMain2Binding
import com.deepdev.hamrochat.fragments.ChatroomFragment
import com.deepdev.hamrochat.fragments.ForYouFragment
import com.deepdev.hamrochat.fragments.MessagesFragment
import com.google.android.material.tabs.TabLayoutMediator

class Main2Activity : AppCompatActivity() {

    private lateinit var onBackPressedCallback : OnBackPressedCallback
    private val binding by lazy { ActivityMain2Binding.inflate(layoutInflater) }
    private var lastBackPressTime: Long = 0
    private lateinit var toast : Toast


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // change the color of three dot menu to white
        val overflowIcon= binding.toolbar.overflowIcon
        overflowIcon?.setTint(Color.WHITE)


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
                onBackButtonPressed()
            }
        }
        onBackPressedDispatcher.addCallback( onBackPressedCallback)
    }


    private fun tabLayoutSetup() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(ForYouFragment(), "", AppCompatResources.getDrawable(this, R.drawable.ic_home))
        adapter.addFragment(ChatroomFragment(), "", AppCompatResources.getDrawable(this, R.drawable.ic_chatroom))
        adapter.addFragment(MessagesFragment(), "", AppCompatResources.getDrawable(this, R.drawable.ic_private_chat))


        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = adapter.fragmentTitles[position]
            tab.icon = adapter.fragmentIcons[position]
        }.attach()

    }
 fun onBackButtonPressed() {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastBackPressTime

        if (timeDiff in 0..DOUBLE_BACK_PRESS_INTERVAL) {
            onBackPressedCallback.remove()
            cancelToast()
            onBackPressedDispatcher.onBackPressed()
        } else {
            lastBackPressTime = currentTime
            showToast("Press back again to exit")
        }
    }

    private fun showToast(message: String) {
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun cancelToast(){
        toast.cancel()
    }
    companion object {
        private const val DOUBLE_BACK_PRESS_INTERVAL = 2000 // Interval for a double back press in milliseconds (e.g., 2000ms or 2 seconds)
    }
}