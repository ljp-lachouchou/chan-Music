package com.software.mymusicplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.software.mymusicplayer.R
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.utils.DpAndPx

class StringAdapter(private val context: Context?,
                    private var data:MutableList<Song>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        private var listener:OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.i("YYYYS","YYYYS")
        return IHolder(
            LayoutInflater.from(context)
            .inflate(R.layout.rank_item,parent,false))
    }
    private class IHolder(view: View): RecyclerView.ViewHolder(view){

        var rankName:TextView
        var rankIndex:TextView
        init {
            Log.i("YYYYS","YYYYS")
            rankIndex = view.findViewById(R.id.rank_item_index)
            rankName = view.findViewById(R.id.rank_item_good)
        }
    }
    override fun getItemCount(): Int = data.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        val params = holder.itemView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = params
        val iHolder = holder as IHolder

//        when(item){

//            is Song -> {
        if (position < 3){
            val colorRes = ContextCompat.getColor(context!!, R.color.red)
            iHolder.rankName.setTextColor(colorRes)
            iHolder.rankIndex.setTextColor(colorRes)
            iHolder.rankName.setTypeface(iHolder.rankName.typeface,
                Typeface.BOLD)
        }
        iHolder.rankIndex.text = (position + 1).toString()
        iHolder.rankName.text = item.name
        iHolder.itemView.setOnClickListener {
            listener?.onClick(item)
        }
//            }
//            is Note -> {
//                if (position < 3){
//                    val colorRes = ContextCompat.getColor(context!!, R.color.red)
//                    iHolder.rankName.setTextColor(colorRes)
//                    iHolder.rankIndex.setTextColor(colorRes)
//                    iHolder.rankName.setTypeface(iHolder.rankName.typeface,
//                        Typeface.BOLD)
//
//                }
//                iHolder.rankIndex.text = (position + 1).toString()
//                iHolder.rankName.text = item.title
//                iHolder.itemView.setOnClickListener {
//                    listener?.onClick(item)
//                }
//            }
//        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: MutableList<Song>){
        this.data = data
        Log.i("songClickRank.size2",this.data.size.toString())
        notifyDataSetChanged()
    }
    fun setListener(onClickListener: OnClickListener){
        listener = onClickListener
    }
    interface OnClickListener{
        fun onClick(item:Song)
    }
}