package com.software.mymusicplayer.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.software.mymusicplayer.R

class ImageAdapter(private val context: Context,
    private var images:MutableList<String>)
    :
RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view1 = LayoutInflater.from(context).inflate(R.layout.item_image,parent,false)
        val view2 = LayoutInflater.from(context)
            .inflate(R.layout.item_add_button,parent,false)
        return ImageViewHolder(view1)
    }

    override fun getItemCount(): Int = images.size
    fun setData(images: MutableList<String>){
        this.images = images
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        val params = holder.itemView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = params
        val iHolder = holder as ImageViewHolder
        val item = images[position]
        Log.i("item00",item.toString())
        Glide.with(iHolder.itemView)
            .load(item)
            .override(100, 100)
            .into(iHolder.imageView)

    }
    private class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.item_image)
    }
//    private class AddButtonViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)


}