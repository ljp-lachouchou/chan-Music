package com.software.mymusicplayer.adapter

import android.annotation.SuppressLint
import android.app.AppOpsManager.OnOpNotedCallback
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.software.mymusicplayer.R
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.zip.Inflater
import kotlin.math.max
import kotlin.math.min

class NoteAdapter(private val context: Context?,
                  private var data:MutableList<Note>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_note,parent,false)
        return MyViewHolder(view)
    }
    private var onClickListener:OnNoteClickListener? = null
    override fun getItemCount(): Int =data.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = data[position]
        var user:User? = null
        scope.launch {
            val job1 = async(Dispatchers.IO) {
                UserDatabaseManager.getInstance(context!!).getDatabase()
                    .getUserDao()
                    .queryUserByUserName(note.userId)
            }
            user = job1.await()
            withContext(Dispatchers.Main){
                val params = holder.itemView.layoutParams
                params.height = (context?.resources?.displayMetrics?.widthPixels?: 0) * 1 / 2 + 100
                params.width = (context?.resources?.displayMetrics?.widthPixels?: 0) * 1 / 2 -  50
                holder.itemView.layoutParams = params
                val itemHolder = holder as MyViewHolder
                itemHolder.title.text = note.title
                Log.i("user.avatar",user.toString())
        if (user?.avatar == null){
                Glide.with(itemHolder.itemView)
                    .load(R.drawable.baseline_self_improvement_24)
                    .into(itemHolder.userAvatar)
        }
        else{
//
            Glide.with(itemHolder.itemView)
                .load(Uri.parse(user?.avatar!!))
                .into(itemHolder.userAvatar)
        }
                Glide.with(itemHolder.itemView)
                    .load(note.images[0])
                    .into(itemHolder.cover)
//        itemHolder.cover.setImageURI(note.images[0].toUri())
                itemHolder.userName.text = user!!.nickName
                var count = 0
                RedisClient.getInstance()
                    .getService(RedisService::class.java)
                    .sCard("note:${note.id}:likes")
                    .enqueue(object : Callback<BaseResponse<String>>{
                        override fun onResponse(
                            p0: Call<BaseResponse<String>>,
                            p1: Response<BaseResponse<String>>
                        ) {
                            Log.i("note_likes_scard1",p1.body()?.value.toString())
                            count = p1.body()?.value!!.toInt()
                            itemHolder.noteLikes.text = count.toString()
                        }

                        override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {
                            Log.i("note_likes_scard1",p1.message.toString())
                        }

                    })

                itemHolder.itemView.setOnClickListener {
                    onClickListener?.onClick(note)
                }
            }
        }


    }
    fun setData(note:MutableList<Note>){
        this.data = note
        notifyDataSetChanged()
    }
    private class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        var title:TextView
        var cover:ImageView
        var userAvatar:CircleImageView
        var userName:TextView
        var noteLikes:TextView
        init {
            title = view.findViewById(R.id.note_title)
            cover = view.findViewById(R.id.note_cover)
            userAvatar = view.findViewById(R.id.user_avatar)
            userName = view.findViewById(R.id.user_name)
            noteLikes = view.findViewById(R.id.note_likes)
        }

    }
    fun setOnClickListener(onClickListener: OnNoteClickListener){
        this.onClickListener = onClickListener
    }
    interface OnNoteClickListener{
        fun onClick(note: Note)
    }
    fun addData(noteData:MutableList<Note>){
        val start = data.size
        data.addAll(noteData)
        notifyItemRangeInserted(start,noteData.size)
    }
}