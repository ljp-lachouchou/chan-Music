package com.software.mymusicplayer
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.software.mymusicplayer.adapter.MyViewPagerAdapter
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.manager.NoteDatabaseManager
import com.software.mymusicplayer.manager.PlaySongFragmentManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.view.CountFragment
import com.software.mymusicplayer.view.FindFragment
import com.software.mymusicplayer.view.NoteFragment
import com.software.mymusicplayer.view.RecommendFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity :BaseActivity() {
    val context:FragmentActivity = this
    private lateinit var tl: TabLayout
    private lateinit var vp: ViewPager2
    private lateinit var mpa:MyViewPagerAdapter
    private lateinit var fragments:List<Fragment>
    val NOTIFICATION_PERMISSION_CODE = 113
    private var receiver:BroadcastReceiver
    init {
//        checkNotificationPermission()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.i("PREPARE","SAD")

            }
        }
    }
    // 在Activity中请求权限
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ))  {
                    // 显示权限解释弹窗
                    AlertDialog.Builder(this)
                        .setTitle("需要通知权限")
                        .setMessage("请允许通知权限以接收生成状态")
                        .setPositiveButton("去设置") { _, _ ->
                            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                NOTIFICATION_PERMISSION_CODE)
                        }
                        .show()
                }else{
                    requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_CODE)}
            }

        }
    }

    override fun initData() {
        val sharedPreferences = getSharedPreferences("auto_login", MODE_PRIVATE)
        lifecycleScope.launch(Dispatchers.IO) {
            val job = async {
                UserDatabaseManager.user = UserDatabaseManager.getInstance(this@MainActivity)
                    .getDatabase().getUserDao().queryUserByUserName(
                        sharedPreferences.getString("userName","")
                            .toString())
            }
            job.await()
            Log.i("USER_NAME",UserDatabaseManager.user?.userName.toString())
            PlaySongFragmentManager.getData()
        }
        fragments = listOf(RecommendFragment(),FindFragment(),NoteFragment(),CountFragment())


    }

    override fun onDestroy() {
        Log.i("MAINACTIVITYDESTROY","YES")
        val intent = Intent(context,PlayMusicService::class.java)
        stopService(intent)
        val intentMusicGen = Intent(context,MusicGenService::class.java)
        stopService(intentMusicGen)
        super.onDestroy()

    }



    override fun onPause() {
        Log.i("mainPause","YES")
        super.onPause()
    }
    override fun initView() {
        tl = findViewById(R.id.activity_tl)
        vp = findViewById(R.id.activity_vp)
        mpa = MyViewPagerAdapter(fragments,context)

        vp.adapter = mpa
        vp.isUserInputEnabled = false
        val tlm = TabLayoutMediator(tl,vp, {
            tab,position ->
            when(position){
                0 -> {
                    tab.text = "推荐"
                    tab.setIcon(R.drawable.baseline_home_24)
                }
                1 -> {
                    tab.text = "发现"
                    tab.setIcon(R.drawable.baseline_find_replace_24)
                }
                2 -> {
                    tab.text = "笔记"
                    tab.setIcon(R.drawable.baseline_insert_comment_24)
                }
                3 -> {
                    tab.text = "我的"
                    tab.setIcon(R.drawable.baseline_self_improvement_24)

                }
            }
        })
        tlm.attach()

        val playSongFragment = PlaySongFragment()
        PlaySongFragmentManager
            .playSongFragmentManager["MainActivity"] =
            playSongFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.play_music_fragment,playSongFragment )
            .commitAllowingStateLoss()


    }
    override fun getLayoutId(): Int = R.layout.activity_main
    override fun onResume() {
        Log.i("resume","YES")

        super.onResume()
//        ContextCompat.registerReceiver(context,receiver,
//            IntentFilter("PREPARE_STATE"),
//            ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStart() {
        val playSongFragment = PlaySongFragment()
        PlaySongFragmentManager
            .playSongFragmentManager["MainActivity"] =
            playSongFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.play_music_fragment,playSongFragment )
            .commitAllowingStateLoss()
        super.onStart()
    }
    override fun onStop() {
        super.onStop()
//        context.unregisterReceiver(receiver)
    }




}