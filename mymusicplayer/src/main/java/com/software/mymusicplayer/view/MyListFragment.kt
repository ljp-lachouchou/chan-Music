package com.software.mymusicplayer.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.software.mymusicplayer.ListActivity
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.RecommendAlbumAdapter
import com.software.mymusicplayer.entity.SongList
import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyListFragment : Fragment(),RecommendAlbumAdapter.OnItemClickListener {
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var albumAdapter: RecommendAlbumAdapter
    private lateinit var albums:MutableSet<SongList.Album>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_count_list,container,false)
        albums = mutableSetOf()
        initView(view)
        updateCollectData()
        return view
    }

    override fun onResume() {
        updateCollectData()
        super.onResume()
    }
    private fun updateCollectData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val albumMutableSet = mutableSetOf<Album>()
            RedisClient.getInstance()
                .getService(RedisService::class.java)
                .sMembers("user:${UserDatabaseManager.user!!.userName}:albums")
                .execute().body()?.forEach {
                        s ->
                    albumMutableSet.add(Gson().fromJson(s, Album::class.java))
                }
            albums = albumMutableSet
            Log.i("collectAlbum",albums.size.toString())
            withContext(Dispatchers.Main){
                albumAdapter.setData(albums)
            }

        }

    }
    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.album_recycler)
        albumAdapter = RecommendAlbumAdapter(requireContext(),albums,recyclerView)
        albumAdapter.setOnItemClickListener(this)
        recyclerView.adapter = albumAdapter
    }

    override fun onItemClick(album: SongList.Album) {
        val intent = Intent(getContext(), ListActivity::class.java)
        intent.putExtra("clickAlbum",album)
        startActivity(intent)
    }
}