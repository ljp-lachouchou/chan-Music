package com.software.mymusicplayer.view

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.software.mymusicplayer.LoginActivity
import com.software.mymusicplayer.MusicGenActivity
import com.software.mymusicplayer.ProfileActivity
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.MyViewPagerAdapter
import com.software.mymusicplayer.adapter.MyViewPagerAdapterInFragment
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import de.hdodenhof.circleimageview.CircleImageView
import java.sql.Timestamp
import kotlin.math.abs
import kotlin.time.Duration

class CountFragment : Fragment() {
    private lateinit var avatar:CircleImageView
    private lateinit var nickName:TextView
    private lateinit var registerTimeTV:TextView
    private lateinit var tab:TabLayout
    private lateinit var vp:ViewPager2
    private lateinit var fragments:List<Fragment>
    private lateinit var vpAdapter:MyViewPagerAdapterInFragment
    private lateinit var bar:Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var avatarInNavigationView: ImageView
    private lateinit var nickNameInNavigationView: TextView
    private var user: User? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_count,container,false)
        initData()
        initView(view)
        return view
    }
    private fun initData() {
        user = UserDatabaseManager.user
        fragments = listOf(MyListFragment(),MyNoteFragment())
    }

    private fun initView(view: View){
        bar = view.findViewById(R.id.count_tool_bar)
        drawerLayout = view.findViewById(R.id.drawer_layout)
        navigationView = view.findViewById(R.id.nav_view)
        val headLayout = navigationView.getHeaderView(0)
        nickNameInNavigationView = headLayout.findViewById(R.id.txt_username)
        avatarInNavigationView = headLayout.findViewById(R.id.img_avatar)
        avatar = view.findViewById(R.id.my_avatar)
        nickName = view.findViewById(R.id.my_nick_name)
        registerTimeTV = view.findViewById(R.id.my_time)
        tab = view.findViewById(R.id.my_count_tab)
        vp = view.findViewById(R.id.my_count_vp)
        vpAdapter = MyViewPagerAdapterInFragment(fragments,this)
        vp.adapter = vpAdapter
        val tlm = TabLayoutMediator(tab,vp,
            {
                tab,positon->
                when(positon){
                    0 -> {tab.text = "收藏歌单"}
                    1 -> {tab.text = "我的笔记"}
                }
            })
        tlm.attach()
        updateView()
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
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    activity?.finish()
                }
                R.id.nav_profile->{
                    startActivity(Intent(requireContext(), ProfileActivity::class.java))
                }
                R.id.nav_ringtone -> {
                    startActivity(Intent(requireContext(), MusicGenActivity::class.java))
                }
            }
            drawerLayout.closeDrawers()
            true
        }

    }

    override fun onStart() {
        Glide.with(this)
            .load(user?.avatar)
            .into(avatarInNavigationView)
        nickNameInNavigationView.text = user?.nickName
        super.onStart()
    }
    @SuppressLint("SetTextI18n")
    private fun updateView() {
        Glide.with(this)
            .load(user?.avatar)
            .into(avatar)


        nickName.text = user?.nickName

        val registerTime = user?.createdAt?.toInstant()
        val currentTime = Timestamp(System.currentTimeMillis()).toInstant()
        val duration = abs(java.time.Duration.between(registerTime,currentTime).toDays())
        if (duration >= 365){
            registerTimeTV.text = "村零${(duration / 365)}年"
        }else registerTimeTV.text = "村零${duration}天"

    }

}