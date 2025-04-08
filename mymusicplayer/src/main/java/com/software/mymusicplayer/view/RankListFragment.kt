package com.software.mymusicplayer.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.StringAdapter
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.Song

class RankListFragment(private val rankName:String,
                       private var data:MutableList<Song>)
    : Fragment(),StringAdapter.OnClickListener {
    private lateinit var rankNameText:TextView
    private lateinit var rankRecyclerView: RecyclerView
    private lateinit var stringAdapter: StringAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.rank_list_item,container, false)
        initView(view)
        return view
    }
    fun setData(data: MutableList<Song>){
        this.data = data
        Log.i("songClickRank.size1",this.data.size.toString())
        stringAdapter.setData(this.data)
    }
    private fun initView(view: View) {
        rankNameText = view.findViewById(R.id.rankName)
        stringAdapter = StringAdapter(requireContext(),data)
        rankRecyclerView = view.findViewById(R.id.rank_list_recycler)
        rankRecyclerView.adapter = stringAdapter
        rankNameText.text = rankName
        stringAdapter.setListener(this)
    }


    override fun onClick(item: Song) {

    }


}