package com.software.mymusicplayer

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.software.mymusicplayer.adapter.ReplayAdapter
import com.software.mymusicplayer.adapter.SongCommentAdapter
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.entity.Replay
import com.software.mymusicplayer.entity.SongComment
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.CommentDatabaseManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongCommentActivity : BaseActivity(),SongCommentAdapter.OnClickListener,
ReplayAdapter.OnClickListener{
    private lateinit var comments:MutableList<SongComment>
    private lateinit var bar:Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var songImg:ImageView
    private lateinit var songTile:TextView
    private lateinit var songArtist:TextView
    private lateinit var saySome:EditText
    private lateinit var sayPush:Button
    private var song:Song? = null
    private lateinit var songCommentAdapter: SongCommentAdapter
    private lateinit var avatarInDialog:CircleImageView
    private lateinit var contentInDialog:TextView
    private lateinit var nameInDialog:TextView
    private lateinit var replayDialog:BottomSheetDialog
    private lateinit var replayLayout:View
    private lateinit var recyclerInReplay:RecyclerView
    private lateinit var saySomeInDialog:EditText
    private lateinit var sayPushInDialog:Button
    private lateinit var replayCount:TextView
    private lateinit var replays:MutableList<Replay>
    private lateinit var replayAdapter: ReplayAdapter
    @SuppressLint("InflateParams")
    private fun dialogInitView() {
        replayDialog = BottomSheetDialog(this,R.style.BottomSheetDialog)
        replayLayout = LayoutInflater.from(this).inflate(R.layout.replay_layout,null)
        replayLayout.minimumHeight = resources.displayMetrics.heightPixels * 7 / 10
        recyclerInReplay = replayLayout.findViewById(R.id.recycler_replay)
        recyclerInReplay.layoutParams.height = resources.displayMetrics.heightPixels * 2 / 5
        avatarInDialog = replayLayout.findViewById(R.id.user_avatar_comment_replay)
        nameInDialog = replayLayout.findViewById(R.id.user_name_comment_replay)
        contentInDialog = replayLayout.findViewById(R.id.comment_content_replay)
        replayCount = replayLayout.findViewById(R.id.all_replay_size)
        saySomeInDialog = replayLayout.findViewById(R.id.say_some_replay)
        sayPushInDialog = replayLayout.findViewById(R.id.say_push_replay)
        replayAdapter = ReplayAdapter(this,replays)
        replayAdapter.setListener(this)
        recyclerInReplay.adapter = replayAdapter

    }

    override fun initData() {
        song = intent.getParcelableExtra("commentSong")
        comments = mutableListOf()
        replays = mutableListOf()
    }
    private fun updateData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val commentList = CommentDatabaseManager.getInstance(this@SongCommentActivity)
                .getDatabase()
                .getCommentDao()
                .queryCommentsByThId(song!!.songId)
            comments = commentList.toMutableList()
            withContext(Dispatchers.Main){
                songCommentAdapter.setData(comments)
            }
        }
    }
    override fun initView() {
        bar = findViewById(R.id.song_comment_bar)
        saySome = findViewById(R.id.say_some)
        sayPush = findViewById(R.id.say_push)
        songImg = findViewById(R.id.song_img_in_comment)
        songTile = findViewById(R.id.song_title_in_comment)
        songArtist = findViewById(R.id.song_artist_in_comment)
        recyclerView = findViewById(R.id.song_comment_recycler)
        songCommentAdapter = SongCommentAdapter(this,comments)
        songCommentAdapter.setListener(this)
        recyclerView.adapter = songCommentAdapter
        songTile.text = song?.name
        songArtist.text = song?.artist?.artistName
        Glide.with(this)
            .load(song?.image)
            .into(songImg)
        clickEvent()
        updateData()
        dialogInitView()
    }

    override fun onStart() {
        super.onStart()
        replayDialog.setContentView(replayLayout)
        replayDialog.behavior.state = BottomSheetBehavior.STATE_HIDDEN
        replayDialog.behavior.peekHeight = resources.displayMetrics.heightPixels * 7 / 10
        replayDialog.behavior.maxHeight = resources.displayMetrics.heightPixels * 7 / 10
    }



    private fun clickEvent() {
        bar.setNavigationOnClickListener {
            finish()
        }
        sayPush.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {
                val comment = SongComment(
                    0,saySome.text.toString(),
                    UserDatabaseManager.user!!.userName,
                    song!!.songId
                    )
                val id = CommentDatabaseManager.getInstance(this@SongCommentActivity)
                    .getDatabase()
                    .getCommentDao().insertSongComment(comment)
                Log.i("idComment",id.toString())
                comment.id = id
                RedisClient.getInstance()
                    .getService(RedisService::class.java)
                    .sAdd("song:${song?.songId}:comments",
                        UserDatabaseManager.user!!.userName)
                    .execute()
                withContext(Dispatchers.Main){
                    saySome.setText("")
                    songCommentAdapter.addData(mutableListOf(comment))
                }
            }
        }

    }



    override fun getLayoutId(): Int = R.layout.activity_comment

    override fun onClick(comment: SongComment) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = UserDatabaseManager.getInstance(this@SongCommentActivity)
                .getDatabase().getUserDao()
                .queryUserByUserName(comment.userId)
            replays = CommentDatabaseManager.getInstance(this@SongCommentActivity)
                .getDatabase().getCommentDao().queryReplayByParentId(comment.id).toMutableList()
            sayPushInDialog.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val replay = Replay(
                        0,
                        saySomeInDialog.text.toString(),
                        UserDatabaseManager.user!!.userName,
                        comment.id
                    )
                    val id = CommentDatabaseManager.getInstance(this@SongCommentActivity)
                        .getDatabase()
                        .getCommentDao()
                        .insertReply(replay)
                    replay.id = id
                    RedisClient.getInstance()
                        .getService(RedisService::class.java)
                        .sAdd("comment:${comment.id}:replay",
                            Gson().toJson(replay)).execute()
                    withContext(Dispatchers.Main){
                        saySomeInDialog.setText("")
                        replayAdapter.addData(mutableListOf(replay))
                    }
                }

            }
            withContext(Dispatchers.Main){
                replayAdapter.setData(replays)
                Glide.with(this@SongCommentActivity)
                    .load(user?.avatar)
                    .into(avatarInDialog)
                nameInDialog.text = user?.nickName
                contentInDialog.text = comment.content
                replayDialog.show()
            }
        }

    }

    override fun onClick(user: User) {

    }
}