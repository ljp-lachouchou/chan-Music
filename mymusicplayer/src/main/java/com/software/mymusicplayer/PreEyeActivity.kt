package com.software.mymusicplayer

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.software.mymusicplayer.adapter.ImageAdapter
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.NoteDatabaseManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.utils.DpAndPx
import com.software.mymusicplayer.view.ShrinkLayout
import com.youth.banner.Banner
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import com.youth.banner.util.BannerUtils
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreEyeActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private var eyeNote:Note? = null
    private lateinit var avatar: CircleImageView
    private lateinit var banner:Banner<String,BannerImageAdapter<String>>
    private lateinit var nickName:TextView
    private lateinit var title:TextView
    private lateinit var content:TextView
    private lateinit var songLayout:ShrinkLayout
    private lateinit var songImg:ImageView
    private lateinit var songTitle:TextView
    private lateinit var songContent:TextView
    private var user:User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eye)
        eyeNote = intent.getParcelableExtra("eyeNote")
        lifecycleScope.launch {
            user = UserDatabaseManager.getInstance(this@PreEyeActivity)
                .getDatabase().getUserDao()
                .queryUserByUserName(eyeNote!!.userId)
            withContext(Dispatchers.Main){
                initView()
            }
        }

    }

    private fun initView() {
        toolbar = findViewById(R.id.toolbar_eye)
        banner = findViewById(R.id.eye_banner)
        banner.isAutoLoop(false)
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
            finish()
        }
        avatar = findViewById(R.id.user_avatar_in_eye)
        nickName = findViewById(R.id.user_name_in_eye)
        title = findViewById(R.id.title_tv)
        content = findViewById(R.id.content_tv)
        songLayout = findViewById(R.id.song_item_eye)
        songImg = songLayout.findViewById(R.id.song_item_img_eye)
        songContent = songLayout.findViewById(R.id.song_item_artist_eye)
        songTitle = songLayout.findViewById(R.id.song_item_title_eye)
        clickEvent()

    }

    private fun clickEvent() {
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
        val song = Gson().fromJson(eyeNote?.songUrl,Song::class.java)
        if (!eyeNote?.songUrl.isNullOrEmpty() && song != null){
            songLayout.layoutParams.height = DpAndPx.dpToPx(50,songLayout).toInt()
            songContent.text = song.artist.artistName
            songTitle.text = song.name
            Glide.with(songLayout)
                .load(song.image)
                .into(songImg)
        }
    }

}