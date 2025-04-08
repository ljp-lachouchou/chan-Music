package com.software.mymusicplayer.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Display.Mode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuPresenter.Callback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.MyRecyclerViewAdapter
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.PlayMusicService
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.BaseStatu
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.PlaySongFragmentManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import java.sql.Timestamp


class ViewPagerItem(private var data:MutableList<Song>) :
    Fragment(),MyRecyclerViewAdapter.OnItemListener
{
    constructor() : this(mutableListOf())
    private var user:User? = null
    private lateinit var recycler:RecyclerView
    private var myRecyclerViewAdapter: MyRecyclerViewAdapter? = null
//    private
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.view_pager_item,container,false)
        user = UserDatabaseManager.user
        initView(view)
        return view
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initView(view: View) {
        recycler = view.findViewById(R.id.vp_item_recycler)
//        recycler.setOnTouchListener(AngleTouchListener(recycler))
        myRecyclerViewAdapter = MyRecyclerViewAdapter(context,data,recycler)
        Log.i("VIEW_PAGER_ITEM","初始化成功")
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = linearLayoutManager
        myRecyclerViewAdapter?.setOnItemListener(this)
        recycler.adapter = myRecyclerViewAdapter
        recycler.isNestedScrollingEnabled = false
    }
    fun setData(data:MutableList<Song>){
        this.data = data
        Log.i("data的size",this.data.size.toString())
        myRecyclerViewAdapter?.setData(this.data)
    }

    override fun onItemClick(song: Song) {
        val intent = Intent(activity, PlayMusicService::class.java)
        intent.putExtra("playingSong",song)
        lifecycleScope.launch(Dispatchers.IO) {
                Log.i("userName_zAdd",Gson().toJson(song))
                RedisClient.getInstance()
                    .getService(RedisService::class.java)
                    .zAdd("user:${user?.userName}:playlist",
                        mapOf(Pair(Gson().toJson(song),
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
            }
//        RedisClient.getInstance()
//            .getService(RedisService::class.java)
//            .zIncBy("user:${user?.userName}:rank",1.0,Gson().toJson(song))
//            .execute()

//            withContext(Dispatchers.Main){
//                job.await()
                activity?.startService(intent)
//            }
        PlaySongFragmentManager.alPlaySongs.add(0,song)
        }

}

