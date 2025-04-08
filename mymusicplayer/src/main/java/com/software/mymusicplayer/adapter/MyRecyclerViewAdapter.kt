package com.software.mymusicplayer.adapter

import android.content.Context
import android.util.Log
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
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.view.ShrinkLayout

open class MyRecyclerViewAdapter(private val context: Context?,
                                 private var data:MutableList<Song>,
                                 private val recyclerView: RecyclerView) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var inflater: LayoutInflater
        private var onItemListener : OnItemListener? = null
        init {
            inflater = LayoutInflater.from(context)

        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = inflater.inflate(R.layout.song_item,parent,false)
        return  MyViewHolder(view)
    }

    override fun getItemCount(): Int =data.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = data[position]
        val program = holder.itemView.layoutParams
        program.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = program
        val myViewHolder = holder as MyViewHolder
        Glide.with(holder.itemView)
            .load(song.image)
            .placeholder(R.drawable.song_item_img)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
            .override(45,45)
            .into(myViewHolder.img)
        myViewHolder.title.text = song.name
        myViewHolder.disc.text = song.artist.artistName +
                "-" + song.album.albumName
        myViewHolder.itemView.findViewById<ShrinkLayout>(R.id.song_item_eye).apply {
            setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_UP -> {
                        animate().scaleX(1f).scaleY(1f).setDuration(150L).start()
                        onItemListener?.onItemClick(data[position])

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
    @Synchronized
    fun setData(data: MutableList<Song>){
        this.data = data
        Log.i("rv_data的size",this.data.size.toString())
        this.notifyDataSetChanged()
    }
    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        var title:TextView
        var disc:TextView
        var img:ImageView
        init {
            title = view.findViewById(R.id.song_item_title_eye)
            disc = view.findViewById(R.id.song_item_artist_eye)
            img = view.findViewById(R.id.song_item_img_eye)
        }
    }
    fun addData(songs: MutableList<Song>){
        val start = data.size
        data.addAll(songs)
        notifyItemRangeInserted(start,songs.size)
    }


    fun setOnItemListener(onItemListener: OnItemListener) =
        run { this.onItemListener = onItemListener }
    interface OnItemListener{
        fun onItemClick(song: Song)
    }


}