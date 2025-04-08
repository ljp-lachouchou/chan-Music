package com.software.mymusicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent

import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message

import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.manager.PlaySongFragmentManager

import java.net.URLDecoder


class PlayMusicService :Service(){
    private var musicPlayer: ExoPlayer? = null
    private  var playingSong:Song? = null
    val binder = LocalBinder()
    private var isPlaying = false
    private lateinit var progressHandler : Handler
    private lateinit var manager: NotificationManager
    private val updateProgressTask = object : Runnable {
        override fun run() {
            updateSeekBarProgress()
            progressHandler.postDelayed(this, 500) // 间隔 500ms
        }
    }
    private fun updateSeekBarProgress() {
        val duration = musicPlayer?.duration!!
        if (duration > 0) {
            val progress = (musicPlayer?.currentPosition?.toInt()!! * 100/duration + 0.5).toInt()
            val msg = Message()
            msg.what = 0
            msg.arg1 = progress
            progressHandler.sendMessage(msg)
        }
    }
    fun startProgressUpdate() {
        progressHandler.post(updateProgressTask)
    }

    fun stopProgressUpdate() {
        progressHandler.removeCallbacks(updateProgressTask)
    }

    override fun onCreate() {
        super.onCreate()
        if (musicPlayer == null){
            musicPlayer = ExoPlayer.Builder(baseContext)
                .build()

        }
        musicPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        progressHandler = PlaySongFragmentManager.mHandler


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)//开始服务
        val song = intent?.getParcelableExtra("playingSong") ?: playingSong
        if (song != null){
            playingSong = song
            playingMusic()

        }



        return START_STICKY
    }

    override fun onDestroy() {
        if (musicPlayer != null) {
            if (musicPlayer?.isPlaying()!!) {
                musicPlayer?.stop();
            }
            progressHandler.removeCallbacks(updateProgressTask)
            musicPlayer?.release() // 释放资源
        }
        super.onDestroy()

    }

    private fun notificationDisplay() {
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val areNotificationsEnabled = manager.areNotificationsEnabled()
        if (!areNotificationsEnabled){
            Log.i("NOTIFICATION","没有权限")
            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,packageName)
            startService(intent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelId = "sound_channel"
            val channel = NotificationChannel(
                channelId,
                "声音通知",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "a_sound_channel"
            channel.enableVibration(true)//启用震动
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): LocalBinder {
        return binder
    }
    fun rePlayMusic(){
        if (!isPlaying){
            musicPlayer?.playWhenReady = true
            sendPlayState(1)
            isPlaying = true
        }
    }
    fun playingMusic(){

        val url = URLDecoder.decode(playingSong?.audio,"UTF-8")
        val item = MediaItem.fromUri(url)
        sendPrepareState(false)
        musicPlayer?.setMediaItem(item)
        musicPlayer?.prepare()

        Log.i("sss","允许播放")


        Log.d("SendPlayState","1111")
        sendPlayState(1)
        isPlaying = true

        musicPlayer?.addListener(object  : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                when(playbackState){
                    Player.STATE_READY -> {
                        sendPrepareState(true)
                        musicPlayer?.play()
                        startProgressUpdate()
                    }
                    Player.STATE_ENDED -> {
                        Log.i("STATE_ENDED","STATE_ENDED")
                        val msg = Message()
                        msg.what = 0
                        msg.arg1 = 0
                        progressHandler.sendMessage(msg)
                        stopProgressUpdate()
                    }
                }
            }
        })
    }

    fun pauseMusic(){
        if (isPlaying){
            musicPlayer?.playWhenReady = false
            Log.i("暂停播放音乐","ASD")
            sendPlayState(0)
            isPlaying = false
        }
    }
    fun stopMusic(){
        if (isPlaying){
            musicPlayer?.stop()
            sendPlayState(-1)
            isPlaying = false
        }
    }
    //利用广播传递通知——播放的状态
    fun sendPlayState(isPlaying:Int){
        val intent=Intent("PLAYING_STATE").apply {
            setPackage("com.software.mymusicplayer")
            putExtra("isPlaying",isPlaying)
            putExtra("playingSong",playingSong)
        }
        baseContext.sendBroadcast(intent)
    }
    fun sendPrepareState(isPrepared : Boolean){
        val intent = Intent("PREPARE_STATE").apply {
            setPackage("com.software.mymusicplayer")
            putExtra("isPrepared",isPrepared)
        }
        baseContext.sendBroadcast(intent)
    }
    //inner 通过Binder暴露Service的方法
    inner class LocalBinder : Binder(){
        fun getService() = this@PlayMusicService
    }
    fun isPlayingMusic() = isPlaying
    fun getPlayingSong() = playingSong
    fun jumpTo(position:Int){
        musicPlayer?.seekTo(position.toLong())
    }
    fun getDuration() = musicPlayer?.duration

    fun nextSong(){
        stopMusic()
        val iterator = PlaySongFragmentManager.alPlaySongs.iterator()
        while (iterator.hasNext()){
            if (iterator.next() == playingSong) {
                playingSong = iterator.takeIf { it.hasNext() }?.next()
                    ?: PlaySongFragmentManager.alPlaySongs.firstOrNull()
            }
        }
        playingMusic()
    }
    fun preSong(){
        stopMusic()
        val alSong = PlaySongFragmentManager.alPlaySongs.reversed()
        val iterator = alSong.iterator()
        while (iterator.hasNext()){
            if (iterator.next() == playingSong) {
                playingSong = iterator.takeIf { it.hasNext() }?.next()
                    ?: alSong.firstOrNull()
            }
        }
        playingMusic()
    }
}