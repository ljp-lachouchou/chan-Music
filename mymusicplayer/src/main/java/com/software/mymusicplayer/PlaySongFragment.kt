package com.software.mymusicplayer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.software.mymusicplayer.adapter.MyRecyclerViewAdapter
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.BaseStatu
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.manager.CommentDatabaseManager
import com.software.mymusicplayer.manager.PlaySongFragmentManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import com.software.mymusicplayer.view.ShrinkLayout
import com.software.mymusicplayer.view.VinylView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaySongFragment :
    Fragment(),
    ShrinkLayout.OnTouchListener,
    PlaySongFragmentManager.ProgressListener,
    MyRecyclerViewAdapter.OnItemListener{
    private var musicService:PlayMusicService?=null
    private var isBind = false
    private lateinit var playBtn:ImageView
    private lateinit var playlist:ImageView
    private lateinit var vinylView: VinylView
    private lateinit var playingSongTitle:TextView
    private lateinit var playingSongSome:TextView
    private lateinit var shrinkLayout: ShrinkLayout
    private var connection:ServiceConnection
    private lateinit var playSongDialogLayout:View
    private lateinit var playingSongDialog:BottomSheetDialog
    private lateinit var playBtnInDialog:ImageView
    private lateinit var thisView: View
    private lateinit var vinylViewInDialog:ImageView
    private lateinit var playingSongTitleInDialog:TextView
    private lateinit var playingSongSomeInDialog:TextView
    private lateinit var playingSongSeekBar: SeekBar
    private lateinit var realLayout:ConstraintLayout
    private lateinit var updateLayout:ConstraintLayout
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var downAnim:ObjectAnimator
    private lateinit var upAnim:ObjectAnimator
    private lateinit var alPlaySongs:MutableList<Song>
    private lateinit var alPlaySongDialog: BottomSheetDialog
    private lateinit var alPlaySongDialogLayout:View
    private lateinit var alplayRecycler:RecyclerView
    private lateinit var alplayAdapter:MyRecyclerViewAdapter
    private lateinit var alplayListImageView: ImageView
    private lateinit var alplaySongSize:TextView
    private lateinit var likeSongImage:ImageView
    private lateinit var likeSongTV:TextView
    private lateinit var commentSongImage:ImageView
    private lateinit var commentSongTV:TextView
    var lastClickTime = 0L
    private lateinit var nexButton:ImageView
    private lateinit var preButton:ImageView
    private var songLikes:Long = 0
    private var songComments:Long = 0
    private var song:Song? = null
    var isDragging = false
    lateinit var updateAnim:AnimatorSet
    init {
        connection = object :ServiceConnection{
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

                Log.i("service","sssss")
                isBind = true
                val binder = service as PlayMusicService.LocalBinder
                musicService = binder.getService()
                Log.i("musicService",musicService.toString())
                if (musicService?.getPlayingSong() != null){
                    upAnim.start()
                    updateResource(thisView.context,musicService?.getPlayingSong())
                    realLayout.visibility = View.VISIBLE
                    updateLayout.visibility = View.GONE

                    if (musicService?.isPlayingMusic()!!){
                        playBtn
                            .setImageResource(R.drawable.baseline_pause_circle_outline_24)
                        playBtnInDialog.setImageResource(R.drawable.baseline_pause_24)
                        vinylView.playMusic()
                    }else{
                        playBtn
                            .setImageResource(R.drawable.baseline_play_circle_outline_24)
                        playBtnInDialog.setImageResource(R.drawable.baseline_play_arrow_24)
                        vinylView.pauseMusic()

                    }
                }

            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBind = false
                musicService = null
            }

        }


//            object : Handler(){
//                override fun handleMessage(msg: Message) {
//                    when(msg.what){
//                        0 -> {
//                            if (!isDragging){
//                                playingSongSeekBar.progress = msg.arg1
//                            }
//                        }
//                    }
//                }
//            }
    }
    private var receiver:BroadcastReceiver
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.play_music_component,container,false)
        Log.i("PlayMusicFragment","------start()---------")
        onBindService()
        thisView = view
        alPlaySongs = PlaySongFragmentManager.alPlaySongs

        initView(view)
        return view
    }
    private fun onBindService() {
        val intent = Intent(requireContext(),PlayMusicService::class.java)
        requireContext().bindService(intent,connection,Context.BIND_AUTO_CREATE)
    }
    override fun onResume() {
        super.onResume()
        Log.i("PlayMusicFragment","------start()---------")
        ContextCompat.registerReceiver(
            requireContext(),
            receiver,
            IntentFilter("PLAYING_STATE"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        ContextCompat.registerReceiver(
            requireContext(),
            receiver,
            IntentFilter("PREPARE_STATE"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }
    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(receiver)
        realLayout.visibility = View.VISIBLE
        updateLayout.visibility = View.GONE
        updateAnim.pause()
    }
    override fun onStart() {
        super.onStart()
        playingSongDialog.setContentView(playSongDialogLayout)
        playingSongDialog.behavior.skipCollapsed = true
        playingSongDialog.behavior.peekHeight = 3000
        playingSongDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        alPlaySongDialog.setContentView(alPlaySongDialogLayout)
        alPlaySongDialog.behavior.state = BottomSheetBehavior.STATE_HIDDEN
        alPlaySongDialog.behavior.peekHeight = resources.displayMetrics.heightPixels / 2
        alPlaySongDialog.behavior.maxHeight = resources.displayMetrics.heightPixels / 2
    }
    @SuppressLint("CutPasteId")
    private fun initView(view:View) {

        realLayout = view.findViewById(R.id.play_music)
        updateLayout = view.findViewById(R.id.update_layout)
        val anim1 = ObjectAnimator.ofFloat(updateLayout,"alpha",1f,0.2f).apply {
            duration = 1000
        }
        val anim2 = ObjectAnimator.ofFloat(updateLayout,"alpha",0.2f,1f).apply {
            duration = 1000
        }
        updateAnim = AnimatorSet().apply {
            playSequentially(anim1,anim2)//有先后
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator) {
                    start()
                }
            })
        }
        playBtn = view.findViewById(R.id.main_song_play)
        playlist = view.findViewById(R.id.main_song_playlist)
        vinylView = view.findViewById(R.id.main_song_img)
        playingSongTitle = view.findViewById(R.id.main_song_title)
        playingSongSome = view.findViewById(R.id.main_song_artist)
        shrinkLayout = view.findViewById(R.id.play_music_in)
        shrinkLayout.setOnTouchListener(this)
        val displayMetrics = resources.displayMetrics
        downAnim = ObjectAnimator.ofFloat(
            thisView,
            "translationY",
            0f,
            displayMetrics.heightPixels / 2 * 1.0f
        ).apply {
            duration = 500
        }
        upAnim = ObjectAnimator.ofFloat(
            thisView,
            "translationY",
            displayMetrics.heightPixels / 2 * 1.0f,
            0f
        ).apply {
            duration = 500
        }
        Log.i("mmm","mmm")
        downAnim.start()
        PlaySongFragmentManager.registerProgressListener(this)
        alplayListImageView = view.findViewById(R.id.main_song_playlist)
        songDialogInitView()
        alPlaySongDialogInitView()
        updateData()
        clickEvent()
    }
    @SuppressLint("SetTextI18n")
    private fun updateData() {
        alPlaySongs = PlaySongFragmentManager.alPlaySongs
        alplaySongSize.text = "已播放过歌曲${alPlaySongs.size}首"
        Log.i("alPlaySongs",alPlaySongs.size.toString())
        alplayAdapter.setData(alPlaySongs.toMutableList())
    }
    @SuppressLint("InflateParams")
    private fun alPlaySongDialogInitView() {

        alPlaySongDialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialog)
        alPlaySongDialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.alplay_song_dialog,null)
        alplaySongSize = alPlaySongDialogLayout.findViewById(R.id.alplaySongSize)
        alPlaySongDialogLayout.minimumHeight = resources.displayMetrics.heightPixels / 2
        alplayRecycler = alPlaySongDialogLayout.findViewById(R.id.alplay_recycler)
        alplayAdapter = MyRecyclerViewAdapter(requireContext(),alPlaySongs.toMutableList(),alplayRecycler)
        alplayAdapter.setOnItemListener(this)
        alplayRecycler.adapter = alplayAdapter
    }
    @SuppressLint("InflateParams")
    private fun songDialogInitView() {
        playingSongDialog = BottomSheetDialog(requireContext(),R.style.BottomSheetDialog)
        playSongDialogLayout = LayoutInflater.from(requireContext()).inflate(R.layout.activity_playing_song,null)
        playingSongTitleInDialog = playSongDialogLayout.findViewById(R.id.playing_song_title)
        playingSongTitleInDialog.layoutParams
            .width = resources.displayMetrics.widthPixels * 2 / 5
        playingSongSomeInDialog = playSongDialogLayout.findViewById(R.id.playing_song_content)
        playingSongSomeInDialog.layoutParams
            .width = resources.displayMetrics.widthPixels * 2 / 5
        vinylViewInDialog = playSongDialogLayout.findViewById(R.id.playing_song_vinyl)//可能需要修改
        playBtnInDialog = playSongDialogLayout.findViewById(R.id.play_btn)
        playingSongSeekBar = playSongDialogLayout.findViewById(R.id.seekBar)
        toolbar = playSongDialogLayout.findViewById(R.id.toolbar_in_dialog)
        nexButton = playSongDialogLayout.findViewById(R.id.next_btn)
        preButton = playSongDialogLayout.findViewById(R.id.pre_btn)
        likeSongImage = playSongDialogLayout.findViewById(R.id.like_song)
        likeSongTV = playSongDialogLayout.findViewById(R.id.like_song_tv)
        commentSongImage = playSongDialogLayout.findViewById(R.id.comment_song)
        commentSongTV = playSongDialogLayout.findViewById(R.id.like_comment_tv)

        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .sCard("song:${song?.songId}:likes")
            .enqueue(object : Callback<BaseResponse<String>>{
                override fun onResponse(
                    p0: Call<BaseResponse<String>>,
                    p1: Response<BaseResponse<String>>
                ) {
                    songLikes = p1.body()?.value?.toLong() ?: 0
                    likeSongTV.text = p1.body()?.value
                }

                override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {

                }

            })

    }
    override fun onDestroyView() {
        super.onDestroyView()
        updateAnim.cancel()
        if (isBind){
            requireContext().unbindService(connection)
            isBind = false
        }
    }
    @SuppressLint("InflateParams")
    private fun clickEvent() {
        playBtn.setOnClickListener {
            if(isBind && musicService?.getPlayingSong() != null){
                when(musicService?.isPlayingMusic()){
                    false -> {
                        musicService?.rePlayMusic()
                    }
                    else -> {
                        musicService?.pauseMusic()
                    }
                }
            }
        }
        playingSongDialogEvent()
        alplayListImageView.setOnClickListener {
            updateData()
            alPlaySongDialog.show()
        }

    }
    @SuppressLint("SetTextI18n")
    private fun playingSongDialogEvent() {
        val constraintLayout = playSongDialogLayout.findViewById<ConstraintLayout>(R.id.playing_song_layout)
        val displayMetrics = requireContext().resources.displayMetrics
        playSongDialogLayout.minimumHeight = displayMetrics.heightPixels
        constraintLayout.minHeight = displayMetrics.heightPixels
        playingSongDialog.behavior.addBottomSheetCallback(object : BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.i("slideOffset",slideOffset.toString())
                if (slideOffset == 0f){//如果为1f就是完全展开了
                    bottomSheet.setBackgroundResource(R.color.white)
                }else{
                    bottomSheet.setBackgroundResource(R.drawable.activity_playing_song_background)
                }
            }

        })
        playingSongDialog.setOnCancelListener {
            if (!playingSongDialog.isShowing){
                upAnim.start()
            }

        }
        playingSongSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isDragging = true
                musicService?.stopProgressUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = (seekBar.progress *
                        musicService?.getDuration()!! / seekBar.max + 0.5).toInt()

                PlaySongFragmentManager.currentProgress = progress
                Log.i("PROGRESS",PlaySongFragmentManager.currentProgress.toString())
                musicService?.jumpTo(progress)
                musicService?.startProgressUpdate()
                isDragging = false


            }

        })
        playBtnInDialog.setOnClickListener {
            if(isBind && musicService?.getPlayingSong() != null){
                Log.i("ISPLAYING_",musicService?.isPlayingMusic().toString())
                when(musicService?.isPlayingMusic()){
                    false -> {
                        musicService?.rePlayMusic()
                    }
                    else -> {
                        musicService?.pauseMusic()
                    }
                }
            }
        }
        toolbar.setNavigationOnClickListener {
            playingSongDialog.cancel()
        }
        likeSongImage.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < 500) return@setOnClickListener
            lastClickTime = System.currentTimeMillis()
            likeSongImage.isClickable = false
            lifecycleScope.launch(Dispatchers.IO) {
                val user = UserDatabaseManager.user
                val isMember = RedisClient.getInstance()
                    .getService(RedisService::class.java)
                    .sIsMember("song:${song?.songId}:likes",user?.userName!!)
                    .execute()
                    .body()?.value.toBoolean()
                withContext(Dispatchers.Main){
                    if (isMember){
                        Glide.with(thisView)
                            .load(R.drawable.baseline_thumb_up_24)
                            .into(likeSongImage)
                        songLikes--
                    }else{
                        Glide.with(thisView)
                            .load(R.drawable.baseline_thumb_up_24_select)
                            .into(likeSongImage)
                        songLikes++
                    }
                    likeSongTV.text = songLikes.toString()
                    RedisClient.getInstance()
                        .getService(RedisService::class.java)
                        .zAdd("songs:likes", mapOf(Pair(Gson().toJson(song)
                            ,songLikes.toDouble())))
                        .enqueue(object : Callback<BaseStatu<String>>{
                            override fun onResponse(
                                p0: Call<BaseStatu<String>>,
                                p1: Response<BaseStatu<String>>
                            ) {
                                Log.i("songs:likesss",p1.body()?.status.toString())

                            }

                            override fun onFailure(p0: Call<BaseStatu<String>>, p1: Throwable) {
                            }

                        })
                    likeSongImage.isClickable = true
                }
                withContext(Dispatchers.IO){
                    if (isMember){
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sRem("song:${song?.songId}:likes",user.userName)
                            .execute()

                    }else{
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sAdd("song:${song?.songId}:likes",user.userName)
                            .execute()
                    }

                }

            }

        }
        commentSongImage.setOnClickListener {
            playingSongDialog.dismiss()
            startActivity(Intent(requireContext(),SongCommentActivity::class.java).apply {
                putExtra("commentSong",song)
            })
        }
        nexButton.setOnClickListener {
            musicService?.nextSong()
        }
        preButton.setOnClickListener {
            musicService?.preSong()
        }
    }
    @SuppressLint("Recycle", "SetTextI18n")
    override fun onShrinkTouch() {
        if (!playingSongDialog.isShowing){
            downAnim.start()
        }
        if(isBind && musicService?.getPlayingSong() != null){
            lifecycleScope.launch(Dispatchers.IO) {
                val comments = CommentDatabaseManager.getInstance(requireContext())
                    .getDatabase().getCommentDao().queryCommentsByThId(song!!.songId)
                songComments = comments.size.toLong()
                for (comment in comments){
                    songComments += CommentDatabaseManager.getInstance(requireContext())
                        .getDatabase().getCommentDao().queryReplayCountByParentId(comment.id)
                }
                withContext(Dispatchers.Main){
                    commentSongTV.text = songComments.toString()
                }
            }
            RedisClient.getInstance()
                .getService(RedisService::class.java)
                .sCard("song:${song?.songId}:likes")
                .enqueue(object : Callback<BaseResponse<String>>{
                    override fun onResponse(
                        p0: Call<BaseResponse<String>>,
                        p1: Response<BaseResponse<String>>
                    ) {
                        songLikes = p1.body()?.value?.toLong() ?:0
                        likeSongTV.text = p1.body()?.value

                    }

                    override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {

                    }

                })
            RedisClient.getInstance()
                .getService(RedisService::class.java)
                .sIsMember("song:${song?.songId}:likes",UserDatabaseManager.user?.userName!!)
                .enqueue(object : Callback<BaseResponse<String>>{
                    override fun onResponse(
                        p0: Call<BaseResponse<String>>,
                        p1: Response<BaseResponse<String>>
                    ) {
                        Log.i("pppp",p1.body()?.value.toString())
                        if (p1.body()?.value.toBoolean()){
                            Glide.with(thisView)
                                .load(R.drawable.baseline_thumb_up_24_select)
                                .into(likeSongImage)
                        }else{
                            Glide.with(thisView)
                                .load(R.drawable.baseline_thumb_up_24)
                                .into(likeSongImage)
                        }
                    }
                    override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {

                    }

                })
            Handler(Looper.getMainLooper()).postDelayed({
                playingSongDialog.show()
            },200)
        }
    }
    fun updateResource(context: Context, playingSong: Song?) {
        song = playingSong
        Log.i("OK","OSKD")
        Glide.with(context)
            .load(playingSong?.image)
            .placeholder(R.drawable.song_item_img)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(60)))
            .override(55,55)
            .into(vinylView)
        playingSongTitle.text = playingSong?.name
        playingSongSome.text =
            playingSong?.artist?.artistName + " - " +
                    "${playingSong?.album?.albumName}"
        playingSongTitleInDialog.text = playingSong?.name
        playingSongSomeInDialog.text = playingSong?.artist?.artistName + " - " +
                "${playingSong?.album?.albumName}"
        Glide.with(context)
            .load(playingSong?.image)
            .placeholder(R.drawable.song_item_img)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(25)))
            .override(350,300)
            .into(vinylViewInDialog)
    }
    init {
        var cnt = false
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val playingSong: Song? = intent.getParcelableExtra("playingSong")

                Log.i("RECEIVER", song.toString())
                if (intent.action == "PLAYING_STATE") {
                    updateResource(context, playingSong)
                    val isPlaying: Int = intent.getIntExtra("isPlaying", 0)
                    when (isPlaying) {
                        0 -> {
                            playBtn
                                .setImageResource(R.drawable.baseline_play_circle_outline_24)
                            playBtnInDialog.setImageResource(R.drawable.baseline_play_arrow_24)
                            vinylView.pauseMusic()
                        }

                        1 -> {
                            playBtn
                                .setImageResource(R.drawable.baseline_pause_circle_outline_24)
                            playBtnInDialog.setImageResource(R.drawable.baseline_pause_24)
                            vinylView.playMusic()
                        }

                        -1 -> {
                            playBtn
                                .setImageResource(R.drawable.baseline_play_circle_outline_24)
                            playBtnInDialog.setImageResource(R.drawable.baseline_play_arrow_24)
                            vinylView.stopMusic()
                        }
                    }


                }
                else if(intent.action == "PREPARE_STATE"){
                    val displayMetrics = requireContext().resources.displayMetrics
                    if (!cnt){
                        ObjectAnimator.ofFloat(
                            thisView,
                            "translationY",
                            displayMetrics.widthPixels / 2 * 1.0f,
                            0f
                        ).apply {
                            duration = 500
                        }.start()

                        cnt=true
                    }
                    Log.i("PREPARE1","SAD")
                    val isPrepared = intent.getBooleanExtra("isPrepared",false)
                    if (isPrepared){
                        Log.i("PREPARE","SAD")
                        realLayout.visibility = View.VISIBLE
                        updateLayout.visibility = View.GONE
                        updateAnim.pause()
                    }else{
                        updateAnim.start()
                        realLayout.visibility = View.GONE
                        updateLayout.visibility = View.VISIBLE
                    }
                }
            }

        }
    }
    override fun getDragging(): Boolean = isDragging
    override fun updateUI(progress: Int) {
        Log.i("progress",progress.toString())
        playingSongSeekBar.progress = progress
    }

    override fun onDestroy() {
        super.onDestroy()
        updateAnim.cancel()
        upAnim.cancel()
        downAnim.cancel()
    }

    override fun onItemClick(song: Song) {
        val intent = Intent(activity, PlayMusicService::class.java)
        intent.putExtra("playingSong",song)
        activity?.startService(intent)
    }


}