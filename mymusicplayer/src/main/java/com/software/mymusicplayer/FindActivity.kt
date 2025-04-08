package com.software.mymusicplayer

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.software.mymusicplayer.adapter.MyViewPagerAdapter
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.DpAndPx
import com.software.mymusicplayer.utils.RedisClient
import com.software.mymusicplayer.view.RankListFragment
import kotlinx.coroutines.delay
import okhttp3.internal.http2.Http2Reader
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

class FindActivity : BaseActivity(){
    private lateinit var fragments:List<Fragment>
    private lateinit var viewPager:ViewPager2
    private lateinit var viewPagerAdapter: MyViewPagerAdapter
    private lateinit var songClickRank:MutableList<Song>
    private lateinit var songLikesRank:MutableList<Song>
    private lateinit var songClickRankFragment: RankListFragment
    private lateinit var songLikesRankFragment: RankListFragment
    private lateinit var ed:EditText
    override fun initData() {
        songLikesRank = mutableListOf()
        songClickRank = mutableListOf()
        songClickRankFragment = RankListFragment(
            "歌曲点击榜",songClickRank)
        songLikesRankFragment = RankListFragment(
            "歌曲点赞榜",songLikesRank)
        fragments = listOf(
            songClickRankFragment,
            songLikesRankFragment,
        )
        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .zRevRange("songs:view",0,9)
            .enqueue(object : Callback<Set<String>>{
                override fun onResponse(p0: Call<Set<String>>, p1: Response<Set<String>>) {
                    Log.i("songs:view",p1.body()?.size.toString())
                    p1.body()?.forEach {
                        s ->
                        val song = Gson().fromJson(s,Song::class.java)
                        if (song != null){
                            songClickRank.add(song)
                        }
                        if (songClickRank.size >= p1.body()!!.size){
                            Log.i("songs:view",songClickRank.size.toString())
                            songClickRankFragment.setData(songClickRank)
                        }
                    }
                }
                override fun onFailure(p0: Call<Set<String>>, p1: Throwable) {
                    Log.i("songs:view1",p1.message.toString())
                }

            })
        RedisClient.getInstance()
            .getService(RedisService::class.java)
            .zRevRange("songs:likes",0,9)
            .enqueue(object : Callback<Set<String>>{
                override fun onResponse(p0: Call<Set<String>>, p1: Response<Set<String>>) {

                    p1.body()?.forEach {
                            s ->
                        val song = Gson().fromJson(s,Song::class.java)
                        Log.i("songs:likes",song.toString())
                        if (song != null){
                            songLikesRank.add(song)
                        }
                        if (songLikesRank.size >= p1.body()!!.size){
                            Log.i("songs:likes",songLikesRank.size.toString())
                            songLikesRankFragment.setData(songLikesRank)
                        }
                    }
                }
                override fun onFailure(p0: Call<Set<String>>, p1: Throwable) {
                    Log.i("songs:likes1",p1.message.toString())
                }

            })
    }
    override fun initView() {
        viewPager = findViewById(R.id.rank_list)
        viewPagerAdapter = MyViewPagerAdapter(fragments,this)
        viewPager.adapter = viewPagerAdapter
        viewPagerGoodDisplay(viewPager)
        findViewById<Toolbar>(R.id.find_search_bar).apply {
            setNavigationIcon(R.drawable.baseline_chevron_left_24)
            setNavigationOnClickListener {
                finish()
            }
            ed = findViewById(R.id.search_ed)
            ed.layoutParams.width = resources.displayMetrics.widthPixels * 2 / 3
            ed.isFocusableInTouchMode = true
        }

    }
    private fun viewPagerGoodDisplay(viewPager2: ViewPager2) {
        viewPager2.offscreenPageLimit = 2
        viewPager2.setPageTransformer { page, position ->
            val offset = (16 * resources
                .displayMetrics.density - 80) * position
            page.translationX = -offset
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_find
}