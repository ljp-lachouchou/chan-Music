package com.software.mymusicplayer


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.animation.addListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.software.mymusicplayer.adapter.MyRecyclerViewAdapter
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.contracts.AlbumContracts
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.BaseStatu
import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.PlaySongFragmentManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.presenter.AlbumPresenter
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.DpAndPx
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListActivity
    : BaseActivity(),AlbumContracts.IView,MyRecyclerViewAdapter.OnItemListener{
    private lateinit var album: Album
    private lateinit var listRecyclerView: RecyclerView
        private lateinit var listImageView: ImageView
        private lateinit var listName: TextView
        private lateinit var listDesc:TextView
        private lateinit var playlists:MutableList<Song>
        private lateinit var toolbar: Toolbar
        private lateinit var recyclerViewAdapter: MyRecyclerViewAdapter
        private lateinit var presenter: AlbumPresenter
        private lateinit var frameLayout: FrameLayout
        private lateinit var linearLayout: LinearLayout
        private lateinit var anim:AnimatorSet
        private lateinit var collectImage:ImageView
        private lateinit var collectTV:TextView
        private var collects:Long = 0
        private var user: User? = null

    override fun initData() {
        playlists = mutableListOf()
        user = UserDatabaseManager.user

    }

    override fun initView() {
        listRecyclerView = findViewById(R.id.list_rv)
        listImageView = findViewById(R.id.list_img)
        listName = findViewById(R.id.list_name)
        listDesc = findViewById(R.id.list_desc)
        toolbar = findViewById(R.id.list_toolbar)
        toolbar.setNavigationOnClickListener {
            intent.putExtra("listActivitySong",true)
            finish()
        }
        recyclerViewAdapter = MyRecyclerViewAdapter(baseContext,playlists,listRecyclerView)
        listRecyclerView.adapter = recyclerViewAdapter
        collectImage = findViewById(R.id.collect_album_list)
        collectTV = findViewById(R.id.collect_count)
        frameLayout = findViewById(R.id.list_activity_frame_layout)
        linearLayout = findViewById(R.id.load_dialog)
        anim = AnimatorSet()
        anim.apply {
            playSequentially(
                ObjectAnimator.ofFloat(
                    linearLayout,
                    "alpha",
                    1f,
                    0.2f
                ).apply { duration = 1000
                },
                ObjectAnimator.ofFloat(
                    linearLayout,
                    "alpha",
                    0.2f,
                    1f
                ).apply {
                    duration = 1000

                }

            )
            addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator) {
                    start()
                }
            })
        }
        updateByIntentData()
        anim.start()
        clickEvent()
        updateRedisView()
        managerData()
    }

    @SuppressLint("SetTextI18n")
    private fun clickEvent() {
        collectImage.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val user = UserDatabaseManager.user
                val isMember = RedisClient.getInstance()
                    .getService(RedisService::class.java)
                    .sIsMember("album:${album.albumId}:collects",user?.userName!!)
                    .execute()
                    .body()?.value.toBoolean()
                withContext(Dispatchers.Main){
                    if (isMember){
                        Glide.with(this@ListActivity)
                            .load(R.drawable.baseline_collections_24)
                            .into(collectImage)
                        collects--
                    }else {
                        Glide.with(this@ListActivity)
                            .load(R.drawable.baseline_check_24)
                            .into(collectImage)
                        collects++

                    }
                    collectTV.text = collects.toString()

                }
                withContext(Dispatchers.IO){
                    if (isMember){
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sRem("album:${album.albumId}:collects",user.userName)
                            .execute()
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sRem("user:${user.userName}:albums",Gson().toJson(album))
                            .execute()
                    }else{
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sAdd("user:${user.userName}:albums",Gson().toJson(album))
                            .execute()
                        RedisClient.getInstance()
                            .getService(RedisService::class.java)
                            .sAdd("album:${album.albumId}:collects",user.userName)
                            .execute()
                    }
                }
            }
        }
    }

    private fun updateRedisView() {
        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .sCard("album:${album.albumId}:collects")
            .enqueue(object : Callback<BaseResponse<String>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    p0: Call<BaseResponse<String>>,
                    p1: Response<BaseResponse<String>>
                ) {
                    collects = p1.body()?.value!!.toLong()
                    collectTV.text = collects.toString()
                }

                override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {

                }

            })
        val collectUser = UserDatabaseManager.user
        var isMember = false
        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .sIsMember("album:${album.albumId}:collects",collectUser?.userName!!)
            .enqueue(object : Callback<BaseResponse<String>>{
                override fun onResponse(
                    p0: Call<BaseResponse<String>>,
                    p1: Response<BaseResponse<String>>
                ) {
                    isMember = p1.body()?.value!!.toBoolean()
                    if (isMember){
                        Glide.with(this@ListActivity)
                            .load(R.drawable.baseline_check_24)
                            .into(collectImage)
                    }else{
                        Glide.with(this@ListActivity)
                            .load(R.drawable.baseline_collections_24)
                            .into(collectImage)
                    }
                }

                override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {

                }

            })
    }

    private fun managerData() {
        lifecycleScope.launch {
            val job1 = async { presenter.managerAlbumData() }
//            if (playlists.size == 0){
            job1.await()
//            }
            withContext(Dispatchers.Main){
                try {

                    delay(3000)
                    Handler().postDelayed({
                        linearLayout.visibility = View.GONE
                        listRecyclerView.visibility = View.VISIBLE
                    },3000)
                    recyclerViewAdapter.setData(playlists)


                }catch (e:Exception){
                    Log.e("EXP",e.printStackTrace().toString())

                }
            }
        }
    }


    private fun updateByIntentData() {
        val intent = getIntent()
        album = intent.getParcelableExtra<Album>("clickAlbum")!!
        presenter = AlbumPresenter(this,album.albumId)
        listDesc.text = "此专辑出版于 " + album.releaseDate
        listName.text = album.albumName
        val displayMetrics = resources.displayMetrics
        val imageSize = DpAndPx.dpToPx(165,listImageView)
        val usedSize = DpAndPx.dpToPx(365,baseContext)
        Log.i("imageSize",imageSize.toString())
        listName.layoutParams.width = (displayMetrics.widthPixels - imageSize).toInt()
        listDesc.layoutParams.width = (displayMetrics.widthPixels - imageSize).toInt()
        frameLayout.layoutParams.height = (displayMetrics.heightPixels - usedSize)
        listRecyclerView.layoutParams.height = (displayMetrics.heightPixels - usedSize)
        val manager = LinearLayoutManager(baseContext)
        manager.orientation = LinearLayoutManager.VERTICAL
        listRecyclerView.layoutManager = manager
        Glide.with(baseContext)
            .load(album.albumImage)
            .placeholder(R.drawable.song_item_img)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(15)))
            .override(150,150)
            .into(listImageView)
        recyclerViewAdapter.setOnItemListener(this)

    }

    override fun onStart() {
        val playSongFragment = PlaySongFragment()
        PlaySongFragmentManager
            .playSongFragmentManager["ListActivity"] =
            playSongFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.play_music_fragment_in_album,playSongFragment)
            .commitAllowingStateLoss()
        super.onStart()
    }
    override fun onDestroy() {
        intent.putExtra("listActivitySong",true)
        super.onDestroy()
        anim.cancel()
    }
    override fun getLayoutId(): Int = R.layout.activity_list
    override fun successTODO(songs: MutableList<Song>) {
        playlists = songs
        Log.i("playlists",playlists.size.toString())
    }

    override fun failTODO(e: Throwable) {
        Log.i("EXPP",e.printStackTrace().toString())

    }

    override fun onItemClick(song: Song) {
        val intent = Intent(baseContext, PlayMusicService::class.java)
        intent.putExtra("playingSong",song)
        intent.putExtra("activity","ListActivity")
        lifecycleScope.launch(Dispatchers.IO) {
//            val job = async {
            Log.i("userName_zAdd","user:${user?.userName}:playlist")
            RedisClient.getInstance()
                .getService(RedisService::class.java)
                .zAdd("user:${user?.userName}:playlist",
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
//            RedisClient.getInstance()
//                .getService(RedisService::class.java)
//                .zIncBy("user:${user?.userName}:rank",1.0,Gson().toJson(song))
//                .execute()
        }
        PlaySongFragmentManager.alPlaySongs.add(0,song)
        baseContext?.startService(intent)
    }
}