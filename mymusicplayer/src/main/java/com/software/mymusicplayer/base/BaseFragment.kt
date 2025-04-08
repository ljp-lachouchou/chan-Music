package com.software.mymusicplayer.base

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toolbar.OnMenuItemClickListener
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPresenter
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.paging.LOGGER
import androidx.paging.LOG_TAG
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.software.mymusicplayer.FindActivity
import com.software.mymusicplayer.InsetNoteActivity
import com.software.mymusicplayer.LoginActivity
import com.software.mymusicplayer.MusicGenActivity
import com.software.mymusicplayer.ProfileActivity

import com.software.mymusicplayer.R
import com.software.mymusicplayer.manager.PlaySongFragmentManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import java.sql.Timestamp

abstract class BaseFragment:Fragment() {
    private lateinit var drawerLayout:DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bar:Toolbar
    private lateinit var avatar:ImageView
    private lateinit var nickName:TextView
    private lateinit var headLayout:View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(getLayoutId(),container,false)
        initData()
        initView(view)
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("isCreated",true)
    }
    abstract fun initData()
    abstract fun refreshData()
    override fun onStart() {
        nickName.text = UserDatabaseManager.user?.nickName
        Glide.with(this)
            .load(UserDatabaseManager.user?.avatar)
            .into(avatar)
        super.onStart()
    }

    open fun initView(view: View){
        val ed = view.findViewById<EditText>(R.id.search_ed)
        val displayMetrics = resources.displayMetrics
        drawerLayout = view.findViewById(R.id.drawer_layout)
        navigationView = view.findViewById(R.id.nav_view)
        headLayout = navigationView.getHeaderView(0)
        avatar = headLayout.findViewById(R.id.img_avatar)
        nickName = headLayout.findViewById(R.id.txt_username)

        ed.layoutParams.width = displayMetrics.widthPixels * 2 / 3
        ed.setOnClickListener {
            startActivity(Intent(requireContext(),FindActivity::class.java))
        }
        bar = view.findViewById(getToolBarId())

        when(getToolBarId()){
            R.id.note_search_bar -> {
                bar.setOnMenuItemClickListener(object :
                    Toolbar.OnMenuItemClickListener {
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        if (item?.itemId == R.id.insert_note){
                            Log.i("点击了添加note","dianjile")
                            startActivity(Intent(activity,InsetNoteActivity::class.java).apply {
                            })
                        }
                        return false
                    }


                })
            }
        }
        bar.setNavigationIcon(R.drawable.baseline_reorder_24)
        bar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navigationView.setNavigationItemSelectedListener {
            item->
            when(item.itemId){
                R.id.nav_logout -> {
                    activity?.getSharedPreferences("auto_login", MODE_PRIVATE)
                        ?.edit()
                        ?.putLong("tokenTime",0)
                        ?.putBoolean("isLogin",false)
                        ?.apply()
                    startActivity(Intent(requireContext(),LoginActivity::class.java))
                    activity?.finish()
                }
                R.id.nav_profile->{
                    startActivity(Intent(requireContext(),ProfileActivity::class.java))
                }
                R.id.nav_ringtone -> {
                    startActivity(Intent(requireContext(),MusicGenActivity::class.java))
                }
            }
            drawerLayout.closeDrawers()
            true
        }

    }





    abstract fun getToolBarId(): Int

    abstract fun getLayoutId():Int
}