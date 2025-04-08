package com.software.mymusicplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.software.mymusicplayer.R
import com.software.mymusicplayer.entity.Replay
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReplayAdapter(private val context: Context
                    , private var data:MutableList<Replay>):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listener: OnClickListener? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return IHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.comment_item, parent, false)
        )
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        val params = holder.itemView.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.layoutParams = params

        val iHolder = holder as IHolder
        scope.launch(Dispatchers.IO) {

            val user = UserDatabaseManager.getInstance(context)
                .getDatabase().getUserDao()
                .queryUserByUserName(item.userId)
            withContext(Dispatchers.Main){
                Glide.with(iHolder.itemView)
                    .load(user?.avatar)
                    .into(iHolder.avatar)
                iHolder.nickName.text = user?.nickName
                iHolder.content.text = item.content
                iHolder.itemView.setOnClickListener {
                    listener?.onClick(user!!)
                }
            }
        }
    }
    fun setListener(listener: OnClickListener){
        this.listener = listener
    }
    interface OnClickListener{
        fun onClick(user:User)
    }
    fun setData(replays: MutableList<Replay>){
        this.data = replays
        notifyDataSetChanged()
    }
    fun addData(replays:MutableList<Replay>){
        val size = data.size
        data.addAll(replays)
        notifyItemRangeInserted(size,replays.size)
    }
    private class IHolder(view: View):RecyclerView.ViewHolder(view){
        var avatar: CircleImageView
        var nickName: TextView
        var content: TextView
        val replaySize: TextView
        init {
            nickName = view.findViewById(R.id.user_name_comment)
            avatar = view.findViewById(R.id.user_avatar_comment)
            content = view.findViewById(R.id.comment_content)
            replaySize = view.findViewById(R.id.replay_size)
            replaySize.layoutParams.height = -100
        }
    }
}