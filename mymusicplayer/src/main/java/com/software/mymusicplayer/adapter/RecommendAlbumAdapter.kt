package com.software.mymusicplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.software.mymusicplayer.R

import com.software.mymusicplayer.entity.SongList.Album

class RecommendAlbumAdapter(
    private val context: Context?,
    private var data:MutableSet<Album>,
    private var recyclerView: RecyclerView

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onItemListener:OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.album_item,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = data.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val program = holder.itemView.layoutParams
        program.height = ViewGroup.LayoutParams.WRAP_CONTENT
        program.width = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = program
        val album = data.toList()[position]
        val myViewHolder = holder as MyViewHolder
        Glide.with(myViewHolder.itemView)
            .load(album.albumImage)
            .placeholder(R.drawable.album_item_img)
            .override(210,240)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(25)))
            .into(myViewHolder.img)
        myViewHolder.title.text = album.albumName
        myViewHolder.itemView.apply {
            setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_UP -> {
                        animate().scaleX(1f).scaleY(1f).setDuration(150L).start()
                        onItemListener?.onItemClick(album)

                    }
                    MotionEvent.ACTION_CANCEL -> {
                        animate().scaleX(1f).scaleY(1f).setDuration(150L).start()

                    }
                    MotionEvent.ACTION_DOWN->{
                        animate().scaleX(0.95f).scaleY(0.95f).setDuration(150L).start()
                    }

                }
                false
            }

        }
    }
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
         var title:TextView
         var img:ImageView
        init {
            title = view.findViewById(R.id.album_title)
            img  = view.findViewById(R.id.album_img)
        }
    }
    fun setData(data: MutableSet<Album>){
        this.data = data
        this.notifyDataSetChanged()
    }
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemListener = onItemClickListener
    }

    interface OnItemClickListener{
        fun onItemClick(album: Album)
    }
}