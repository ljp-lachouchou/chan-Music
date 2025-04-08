package com.software.mymusicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.InputEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.core.animateValueAsState
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.software.mymusicplayer.adapter.ImageAdapter
import com.software.mymusicplayer.adapter.MyRecyclerViewAdapter
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.entity.UploadResponse
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.NoteDatabaseManager
import com.software.mymusicplayer.manager.PlaySongFragmentManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.ImageService
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.DpAndPx
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.sql.Time
import java.sql.Timestamp
import java.util.Locale.Category
import java.util.UUID

class InsetNoteActivity : BaseActivity(),MyRecyclerViewAdapter.OnItemListener {
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var imageRecyclerView: RecyclerView
    private lateinit var noteTitle:EditText
    private lateinit var noteContent:EditText
    private lateinit var musicTitle:TextView
    private lateinit var musicImage:ImageView
    private lateinit var musicButton: ImageView
    private lateinit var preEyes:ImageView
    private lateinit var noteInsertButton: Button
    private lateinit var images:MutableList<String>
    private lateinit var imageRecyclerAdapter:ImageAdapter
    private lateinit var  publishTime:Timestamp
    private lateinit var alPlaySongs:MutableList<Song>
    private lateinit var alPlaySongDialog: BottomSheetDialog
    private lateinit var alPlaySongDialogLayout:View
    private lateinit var alplayRecycler:RecyclerView
    private lateinit var alplayAdapter: MyRecyclerViewAdapter
    private lateinit var alplaySongSize:TextView
    private lateinit var insertMusicLayout:LinearLayout
    private var songSelected:Song?=null
    private var user:User? = null
    private val IMAGS = 302
    override fun initData() {
        images = mutableListOf()
        user = UserDatabaseManager.user
        alPlaySongs = PlaySongFragmentManager.alPlaySongs
        Log.i("alPlaySongs",alPlaySongs.size.toString())
    }

    override fun initView() {
        toolbar = findViewById(R.id.toolbar)
        imageRecyclerView = findViewById(R.id.image_recycler)
        noteTitle = findViewById(R.id.note_title_ed)
        noteContent = findViewById(R.id.note_content_ed)
        musicTitle = findViewById(R.id.music_title)
        musicImage = findViewById(R.id.music_img)
        musicButton = findViewById(R.id.music_button)
        preEyes = findViewById(R.id.pre_eyes)
        noteInsertButton = findViewById(R.id.note_insert)
        Log.i("IMAGES_SIZE",images.size.toString())
        imageRecyclerAdapter = ImageAdapter(baseContext,images)
        imageRecyclerView.adapter = imageRecyclerAdapter
        insertMusicLayout = findViewById(R.id.insert_music_linearLayout)
        alPlaySongDialogInitView()
        clickEvent()
    }
    @SuppressLint("InflateParams")
    private fun alPlaySongDialogInitView() {

        alPlaySongDialog = BottomSheetDialog(this,R.style.BottomSheetDialog)
        alPlaySongDialogLayout = LayoutInflater.from(this).inflate(R.layout.alplay_song_dialog,null)
        alplaySongSize = alPlaySongDialogLayout.findViewById(R.id.alplaySongSize)
        alPlaySongDialogLayout.minimumHeight = resources.displayMetrics.heightPixels / 2
        alplayRecycler = alPlaySongDialogLayout.findViewById(R.id.alplay_recycler)
        alplayAdapter = MyRecyclerViewAdapter(this,alPlaySongs.toMutableList(),alplayRecycler)
        alplayAdapter.setOnItemListener(this)
        alplayRecycler.adapter = alplayAdapter
    }
    private fun clickEvent() {
        toolbar.setOnClickListener {
            finish()
        }
        noteInsertButton.setOnClickListener {
            val imgs:List<String> = images
            publishTime = Timestamp(System.currentTimeMillis())
            lifecycleScope.launch(Dispatchers.IO) {
                val note = Note(0,user!!.userName,
                    noteTitle.text.toString(),noteContent.text.toString(),
                    imgs,Gson().toJson(songSelected),publishTime)
                val id = NoteDatabaseManager
                    .getInstance(this@InsetNoteActivity)
                    .getDatabase()
                    .getDao()
                    .insertNote(note)
//                RedisClient.getInstance()
//                    .getService(RedisService::class.java)
//                    .sAdd("user:${user?.userName}:Note:${id}", listOf(";"))
//                    .enqueue(object : Callback<BaseResponse<String>>{
//                        override fun onResponse(
//                            p0: Call<BaseResponse<String>>,
//                            p1: Response<BaseResponse<String>>
//                        ) {
//                            Log.i("note_sadd",p1.body()?.value.toString())
//                        }
//
//                        override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {
//                            Log.i("note_sadd1",p1.message.toString())
//                        }
//                    })
                withContext(Dispatchers.Main){
                    Toast.makeText(this@InsetNoteActivity,
                        "发布成功",Toast.LENGTH_SHORT)
                        .show()
                }
            }
            finish()

        }
        insertMusicLayout.setOnClickListener {
            Log.i("insertMusicLayout","进来了")
            alPlaySongDialog.show()
        }
        musicButton.setOnClickListener {
            if (musicButton.isClickable == true){
                musicButton.setImageResource(R.drawable.baseline_chevron_right_24)
                Glide.with(this)
                    .load(R.drawable.baseline_music_video_24)
                    .override(55)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
                    .into(musicImage)
                musicTitle.text = "添加音乐"
                musicButton.isClickable = false
                songSelected = null
            }
        }
        preEyes.setOnClickListener {
            startActivity(Intent(this,PreEyeActivity::class.java).apply {
                publishTime = Timestamp(System.currentTimeMillis())
                val eyeNote = Note(0,
                    user?.userName!!,
                    noteTitle.text.toString(),
                    noteContent.text.toString(),
                    images,
                    Gson().toJson(songSelected),
                    publishTime
                    )
                putExtra("eyeNote",eyeNote)

            })
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_insert_note
    @SuppressLint("Recycle")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            Log.i("进入了","进入了")
            when(requestCode) {
                IMAGS -> {
                    Log.d("DEBUG", "Received data: ${data?.clipData?.itemCount ?: data?.data}")
                    val imgs = mutableListOf<String>()
                    val uris = mutableListOf<Uri>()
                    data?.clipData?.let { clipData -> //多选情况处理
                        for (i in 0 ..< clipData.itemCount) {
                            uris.add(clipData.getItemAt(i).uri)
                        }
                    } ?: data?.data?.let {
                        uris.add(it)
                        }//单选情况处理
                    imageRecyclerView.layoutParams.height =ViewGroup.LayoutParams.WRAP_CONTENT
                    noteInsertButton.isClickable = false
                    preEyes.isClickable = false
                    preEyes.foreground = null
                    noteInsertButton.foreground = null
                    lifecycleScope.launch(Dispatchers.IO){
                        images.clear()
                        val job = async {
                            uris.forEach {
                                    uri ->
                                val inputStream = contentResolver.openInputStream(uri)
                                inputStream?.use {
                                    stream->
                                    val bytes = stream.readBytes()
                                    val requestBody = bytes.toRequestBody("image/*".toMediaType())
                                    val filePart = MultipartBody.Part.createFormData("image", UUID
                                        .randomUUID().toString() +
                                            ".jpg", requestBody)
                                    var fileUrl:String?
                                    RedisClient.getInstance().getService(ImageService::class.java)
                                        .uploadImage(filePart).enqueue(object :Callback<UploadResponse>{
                                            override fun onResponse(
                                                p0: Call<UploadResponse>,
                                                p1: Response<UploadResponse>
                                            ) {
                                                Log.i("image_upload",p1.body()?.url.toString())
                                                fileUrl = p1.body()?.url
                                                images.add(fileUrl!!)
                                                imageRecyclerAdapter.setData(images)
                                                if (images.size >= uris.size){
                                                    imageRecyclerView.layoutParams.height =ViewGroup.LayoutParams.WRAP_CONTENT
                                                    noteInsertButton.isClickable = true
                                                    preEyes.isClickable = true
                                                    preEyes.foreground = null
                                                    noteInsertButton.foreground = null
                                                }
                                                Log.i("images",images.toString())
                                            }

                                            override fun onFailure(p0: Call<UploadResponse>, p1: Throwable) {
                                                Log.i("image_upload1",p1.message.toString())
                                            }

                                        })
                                }





                            }
                        }
                        job.await()
                    }

                }
            }
        }

    }


    override fun onStart() {
        super.onStart()
        alPlaySongDialog.setContentView(alPlaySongDialogLayout)
        alPlaySongDialog.behavior.state = BottomSheetBehavior.STATE_HIDDEN
        alPlaySongDialog.behavior.peekHeight = resources.displayMetrics.heightPixels / 2
        alPlaySongDialog.behavior.maxHeight = resources.displayMetrics.heightPixels / 2
    }



    fun insertImage(view: View) {
        //多选相册
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        startActivityForResult(intent,IMAGS)
    }

    override fun onItemClick(song: Song) {
        Glide.with(this)
            .load(song.image)
            .override(55)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
            .into(musicImage)
        musicTitle.text = song.name
        musicButton.setImageResource(R.drawable.baseline_cancel_24)
        musicButton.isClickable = true
        alPlaySongDialog.dismiss()
        songSelected = song
    }
}