package com.software.mymusicplayer

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.animation.CycleInterpolator
import android.widget.Button
import android.widget.EditText
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.processing.SurfaceProcessorNode.In
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.google.gson.Gson
import com.software.mymusicplayer.adapter.MyRecyclerViewAdapter
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.MusicGenService
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.MusicGenClient
import com.software.mymusicplayer.utils.RedisClient
import com.software.mymusicplayer.view.CustomRecyclerView
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MusicGenActivity : BaseActivity(),MyRecyclerViewAdapter.OnItemListener {
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var recyclerViewAdapter: MyRecyclerViewAdapter
    private lateinit var ed: EditText
    private lateinit var push: Button
    private lateinit var songs:MutableList<Song>
    private lateinit var bar:Toolbar
    private lateinit var animatorX:ObjectAnimator
    private val receiver:BroadcastReceiver
    init {

        receiver  = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if ("MUSIC_GEN_STATUS".equals(intent?.action)){
                    if(intent?.getBooleanExtra("status",
                            false) == true){
                        push.setText("发送")
                    }else push.setText("生成中...")
                }else if ("MUSIC_GEN".equals(intent?.action)){
                    val song:Song? = intent?.getParcelableExtra("genSong")
                    if(songs.contains(song)) return
                    songs.add(song!!)

                    recyclerViewAdapter.addData(mutableListOf(song))
                }
            }

        }
    }
    override fun initData() {

        songs = mutableListOf()
        recyclerView = findViewById(R.id.recycler_view)
        recyclerViewAdapter = MyRecyclerViewAdapter(this
            ,songs,recyclerView)
        recyclerViewAdapter.setOnItemListener(this)
        recyclerView.adapter = recyclerViewAdapter
        updateData()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        val filter = IntentFilter("MUSIC_GEN_STATUS")
        registerReceiver(receiver, filter,RECEIVER_NOT_EXPORTED)
        if (MusicGenService.isRunning){
            push.setText("生成中..")
        }else{
            push.setText("发送")
        }
        updateData()
        super.onResume()
    }
    override fun onPause() {

        unregisterReceiver(receiver)
        super.onPause()
    }
    override fun initView() {
        bar = findViewById(R.id.music_gen_bar)
        ed = findViewById(R.id.input_text)
        push = findViewById(R.id.send_button)

        animatorX = ObjectAnimator.ofFloat(
            push, "translationX",
            0f, 10f, -5f, 4f, -3f, 2f, 0f
        )
        clickEvent()

    }

    private fun updateData() {
        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .sMembers("user:${UserDatabaseManager.user?.userName}:tones").enqueue(object :Callback<Set<String>> {
                override fun onResponse(p0: Call<Set<String>>, p1: Response<Set<String>>) {
                    Log.i("sssSize",p1.body()?.size.toString())
                    p1.body()?.forEach {
                        s ->
                        val song = Gson().fromJson(s,Song::class.java)
                        if(songs.contains(song)) return
                        songs.add(song)
                        recyclerViewAdapter.setData(songs)
                    }
                }

                override fun onFailure(p0: Call<Set<String>>, p1: Throwable) {

                }

            })


    }

    @SuppressLint("ObsoleteSdkInt", "Recycle")
    private fun clickEvent() {
        bar.setNavigationOnClickListener {
            finish()
        }
        push.setOnClickListener {
            if (MusicGenService.isRunning) {
                animatorX.apply {
                    duration = 500
                    interpolator = CycleInterpolator(3f)
                    // 设置循环插值器
                    start()
                }
                return@setOnClickListener
            }
            push.setText("生成中..")
            val intent = Intent(this,
                MusicGenService::class.java).apply {
                putExtra("prompt",ed.text.toString())
            }
            startService(intent)
            ed.setText("")
        }

    }

    override fun getLayoutId(): Int = R.layout.activity_music_gen

    @OptIn(UnstableApi::class)
    override fun onItemClick(song: Song) {
        Log.i("ppppp",song.audio)
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
            .build()
        val dataSourceFactory = DefaultDataSource.Factory(this,
            OkHttpDataSource.Factory(OkHttpClient()))

// 创建媒体源
        val mediaItem = MediaItem.fromUri(song.audio)
        val mediaSource = ProgressiveMediaSource
            .Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

// 加载并播放
        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()
//        startService(Intent(this,PlayMusicService::class.java).apply {
//            putExtra("playingSong",song)
//        })
    }
    override fun onDestroy() {
        animatorX.cancel()
        super.onDestroy()
    }
}