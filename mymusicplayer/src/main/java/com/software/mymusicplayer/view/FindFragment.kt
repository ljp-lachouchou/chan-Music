package com.software.mymusicplayer.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.software.mymusicplayer.MusicGenActivity
import com.software.mymusicplayer.PlayMusicService
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.MyRecyclerViewAdapter
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import kotlin.math.min

class FindFragment : Fragment(),MyRecyclerViewAdapter.OnItemListener{
    private lateinit var createTo:ImageView
    private lateinit var banner: Banner<String,BannerImageAdapter<String>>
    private lateinit var recycler:RecyclerView
    private lateinit var imgs:MutableList<String>
    private lateinit var songs:MutableList<Song>
    private lateinit var adapter:MyRecyclerViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.fragment_find,container, false)
        imgs = mutableListOf()
        songs = mutableListOf()
        lifecycleScope.launch (Dispatchers.IO){
            RedisClient.getInstance().getService(RedisService::class.java)
                .sMembers("tones").execute().body()?.forEach {
                    s: String ->  songs.add(Gson().fromJson(s,Song::class.java))
                }
            for (i in 0..<min(songs.size,4)){
                imgs.add(songs[i].image)
            }
            withContext(Dispatchers.Main){
                initView(view)
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
    }
    private fun initView(view: View) {
        createTo = view.findViewById(R.id.music_create_img)
        banner = view.findViewById(R.id.gen_banner)
        banner.setAdapter(object : BannerImageAdapter<String>(imgs){
            @OptIn(UnstableApi::class)
            override fun onBindView(
                holder: BannerImageHolder?,
                data: String?,
                position: Int,
                size: Int
            ) {
                if (holder == null) return
                Glide.with(holder.itemView)
                    .load(data)
                    .into(holder.imageView)
                holder.itemView.setOnClickListener {
                    val player = ExoPlayer.Builder(requireContext())
                        .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
                        .build()
                    val dataSourceFactory = DefaultDataSource.Factory(requireContext(),
                        OkHttpDataSource.Factory(OkHttpClient()))

                    // 创建媒体源
                    val mediaItem = MediaItem.fromUri(songs[position].audio)
                    val mediaSource = ProgressiveMediaSource
                        .Factory(dataSourceFactory)
                        .createMediaSource(mediaItem)

                    // 加载并播放
                    player.setMediaSource(mediaSource)
                    player.prepare()
                    player.play()
                }

            }

        })
        recycler = view.findViewById(R.id.gen_recycler)
        adapter = MyRecyclerViewAdapter(requireContext(),songs,recycler)
        recycler.adapter = adapter
        Glide.with(this)
            .load(R.drawable.ai_image)
            .override(150,150)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(15)))
            .into(createTo)
        clickEvent()
        updateData()
    }

    private fun updateData() {
        lifecycleScope.launch (Dispatchers.IO){
            RedisClient.getInstance().getService(RedisService::class.java)
                .sMembers("tones").execute().body()?.forEach {
                        s: String ->  if (!songs.contains(Gson().fromJson(s,Song::class.java))){
                            songs.add(Gson().fromJson(s,Song::class.java))
                        }
                }
            withContext(Dispatchers.Main){
                adapter.setData(songs)
            }
        }
    }

    private fun clickEvent() {
        createTo.setOnClickListener {
            startActivity(Intent(requireContext(),MusicGenActivity::class.java))
        }
    }

    @OptIn(UnstableApi::class)
    override fun onItemClick(song: Song) {
        val player = ExoPlayer.Builder(requireContext())
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true)
            .build()
        val dataSourceFactory = DefaultDataSource.Factory(requireContext(),
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
    }
}