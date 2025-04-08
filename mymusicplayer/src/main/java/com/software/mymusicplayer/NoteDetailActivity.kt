package com.software.mymusicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.BaseStatu
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.PlaySongFragmentManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.DpAndPx
import com.software.mymusicplayer.utils.RedisClient
import com.software.mymusicplayer.view.ShrinkLayout
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Timestamp
import kotlin.math.abs

class NoteDetailActivity:AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private var eyeNote: Note? = null
    private lateinit var avatar: CircleImageView
    private lateinit var banner: Banner<String, BannerImageAdapter<String>>
    private lateinit var nickName: TextView
    private lateinit var title: TextView
    private lateinit var content: TextView
    private lateinit var songLayout: ShrinkLayout
    private lateinit var songImg: ImageView
    private lateinit var songTitle: TextView
    private lateinit var songContent: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var publishTV:TextView
    private lateinit var likesTV:TextView
    private lateinit var likesImage:ImageView
    var lastClickTime = 0L
    private var likes:Long = 0
    private var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eye)
        eyeNote = intent.getParcelableExtra("detailNote")

        lifecycleScope.launch {
            user = UserDatabaseManager.getInstance(this@NoteDetailActivity)
                .getDatabase().getUserDao()
                .queryUserByUserName(eyeNote!!.userId)
            withContext(Dispatchers.Main){
                initView()
            }
        }

    }

    override fun onStart() {
        val playSongFragment = PlaySongFragment()
        PlaySongFragmentManager
            .playSongFragmentManager["NoteDetailActivity"] =
            playSongFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.play_music_fragment_in_detail,playSongFragment)
            .commitAllowingStateLoss()
        super.onStart()
    }
    private fun initView() {
        toolbar = findViewById(R.id.toolbar_eye)
        banner = findViewById(R.id.eye_banner)
        banner.isAutoLoop(false)
        likesTV = findViewById(R.id.detail_note_likes)
        banner.indicator = CircleIndicator(this)
        banner.setAdapter(object : BannerImageAdapter<String>(eyeNote?.images){
            override fun onBindView(
                holder: BannerImageHolder,
                data: String?,
                position: Int,
                size: Int
            ) {
                Glide.with(holder.itemView)
                    .load(data)
                    .into(holder.imageView)
            }
        })
        toolbar.setNavigationOnClickListener {
            intent.putExtra("detailActivitySong",true)
            finish()
        }
        publishTV = findViewById(R.id.eye_tv_publish)
        avatar = findViewById(R.id.user_avatar_in_eye)
        nickName = findViewById(R.id.user_name_in_eye)
        title = findViewById(R.id.title_tv)
        likesImage = findViewById(R.id.likes_eye)
        content = findViewById(R.id.content_tv)
        songLayout = findViewById(R.id.song_item_eye)
        songImg = songLayout.findViewById(R.id.song_item_img_eye)
        songContent = songLayout.findViewById(R.id.song_item_artist_eye)
        songTitle = songLayout.findViewById(R.id.song_item_title_eye)
        recyclerView = findViewById(R.id.note_detail_recycler)
        recyclerView.layoutParams.height = resources.displayMetrics.heightPixels * 2 / 5
        updateView()
        clickEvent()

    }

    private fun updateView() {
        Glide.with(this)
            .load(user?.avatar)
            .into(avatar)
        nickName.text = user?.nickName
        if (!eyeNote?.content.isNullOrEmpty()){
            content.text = eyeNote?.content
            content.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        if (!eyeNote?.title.isNullOrEmpty()){
            title.text = eyeNote?.title
            title.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val currentTime = Timestamp(System.currentTimeMillis())
        val currentInstant = currentTime.toInstant()
        val publishInstant = eyeNote?.publishTime?.toInstant()
        val duration = java.time.Duration.between(currentInstant,publishInstant)
        val minutes = abs(duration.toMinutes())
        if(minutes >= 10){
            publishTV.text = "${minutes}分钟前"
        }
        if (minutes / 60 >= 1){
            publishTV.text = "${minutes / 60}小时前"
        }
        if (minutes / 1440 >= 1){
            publishTV.text = "${minutes / 1440}天前"
        }
        updateLCTI()
    }
    @SuppressLint("SetTextI18n")
    private fun updateLCTI() {
        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .sCard("note:${eyeNote?.id}:likes")
            .enqueue(object : Callback<BaseResponse<String>>{

                override fun onResponse(
                    p0: Call<BaseResponse<String>>,
                    p1: Response<BaseResponse<String>>
                ) {
                    likes = p1.body()?.value!!.toLong()
                    likesTV.text = likes.toString()
                }

                override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {

                }

            })
        val likeUser = UserDatabaseManager.user
        lifecycleScope.launch(Dispatchers.IO) {
            val isMember = RedisClient.getInstance()
                .getService(RedisService::class.java)
                .sIsMember("note:${eyeNote?.id}:likes",likeUser?.userName!!)
                .execute()
                .body()?.value.toBoolean()

            withContext(Dispatchers.Main) {

                if (!isMember) {
                    Glide.with(this@NoteDetailActivity)
                        .load(R.drawable.baseline_thumb_up_24)
                        .into(likesImage)
                } else {
                    Glide.with(this@NoteDetailActivity)
                        .load(R.drawable.baseline_thumb_up_24_select)
                        .into(likesImage)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun clickEvent() {
        val song = Gson().fromJson(eyeNote?.songUrl, Song::class.java)
        if (!eyeNote?.songUrl.isNullOrEmpty() && song != null){
            songLayout.layoutParams.height = DpAndPx.dpToPx(50,songLayout).toInt()
            songContent.text = song.artist.artistName
            songTitle.text = song.name
            Glide.with(songLayout)
                .load(song.image)
                .into(songImg)
            songLayout.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
//            val job = async {
                    Log.i("userName_zAdd",
                        "user:${UserDatabaseManager.user?.userName}:playlist")
                    RedisClient.getInstance()
                        .getService(RedisService::class.java)
                        .zAdd("user:${UserDatabaseManager.user?.userName}:playlist",
                            mapOf(Pair(
                                Gson().toJson(song),
                                System.currentTimeMillis().toDouble())))
                        .enqueue(object : retrofit2.Callback<BaseStatu<String>>{

                            override fun onResponse(
                                p0: Call<BaseStatu<String>>,
                                p1: Response<BaseStatu<String>>
                            ) {
                                Log.i("p1_zAdd",p1.body().toString())

                            }
                            override fun onFailure(p0: Call<BaseStatu<String>>, p1: Throwable) {
                                Log.i("p1_zAdd1",p1.message.toString())
                            }

                        })
                    RedisClient.getInstance()
                        .getService(RedisService::class.java)
                        .zIncBy("songs:view",1.0,Gson().toJson(song))
                        .execute()
//                    RedisClient.getInstance()
//                        .getService(RedisService::class.java)
//                        .zIncBy("user:${user?.userName}:rank",1.0,Gson().toJson(song))
//                        .execute()
                    PlaySongFragmentManager.alPlaySongs.add(0,song)
                    withContext(Dispatchers.Main){
                        startService(Intent(baseContext,PlayMusicService::class.java).apply {
                            putExtra("playingSong",song)
                        })
                    }
                }

            }
        }
        val likeUser = UserDatabaseManager.user
        likesImage.setOnClickListener {
            if (System.currentTimeMillis() - lastClickTime < 500) return@setOnClickListener
            lastClickTime = System.currentTimeMillis()
            likesImage.isClickable = false
            lifecycleScope.launch(Dispatchers.IO) {
                val isMember = RedisClient.getInstance()
                    .getService(RedisService::class.java)
                    .sIsMember("note:${eyeNote?.id}:likes",likeUser?.userName!!)
                    .execute()
                    .body()?.value.toBoolean()
                withContext(Dispatchers.Main) {

                    if (isMember) {
                        Glide.with(this@NoteDetailActivity)
                            .load(R.drawable.baseline_thumb_up_24)
                            .into(likesImage)
                        likes--
                    } else {
                        Glide.with(this@NoteDetailActivity)
                            .load(R.drawable.baseline_thumb_up_24_select)
                            .into(likesImage)
                        likes++
                    }
                    likesTV.text = likes.toString()
                    RedisClient.getInstance()
                        .getService(RedisService::class.java)
                        .zAdd("notes:likes", mapOf(Pair(eyeNote?.id.toString()
                            ,likes.toDouble())))
                        .enqueue(object : Callback<BaseStatu<String>>{
                            override fun onResponse(
                                p0: Call<BaseStatu<String>>,
                                p1: Response<BaseStatu<String>>
                            ) {
                                Log.i("notes:likesss",p1.body()?.status.toString())
                                likesImage.isClickable = true
                            }

                            override fun onFailure(p0: Call<BaseStatu<String>>, p1: Throwable) {
                            }

                        })
                }
                withContext(Dispatchers.IO){
                    if (isMember){
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sRem("note:${eyeNote?.id}:likes",likeUser.userName)
                            .execute()

                    }else{
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sAdd("note:${eyeNote?.id}:likes",likeUser.userName)
                            .execute()
                    }
                }

            }
        }

    }


}