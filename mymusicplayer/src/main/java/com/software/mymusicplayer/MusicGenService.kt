package com.software.mymusicplayer
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.MusicGenService
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.MusicGenClient
import com.software.mymusicplayer.utils.RedisClient
import okhttp3.OkHttpClient
import okhttp3.Response
import java.net.BindException
import java.net.SocketTimeoutException
import java.util.UUID
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.math.log
import kotlin.math.truncate
import kotlin.random.Random

class MusicGenService:IntentService("musicGen") {
    private val mNotifyId = "MusicGenFinish"
    private val finishNotifyId = 112
    private val mChannelName = "音乐生成结束通知"
    private lateinit var prompt:String
    private lateinit var image:String
    companion object{
        var isRunning = false
    }
    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        try {
            isRunning = true
            sendMusicGenStatus(false)
            Log.i("response","点击了1")
            Log.i("response","点击了")
            Log.i("isNull",(intent == null).toString())
            prompt = intent?.getStringExtra("prompt")!!
            Log.i("response",prompt)
            image = "https://image.pollinations.ai/prompt/" +
                    "${prompt}?width=1600&model=random"

            Log.i("yyy","ssss")
            val text = MusicGenService.AudioRequest(prompt)
            val p1 = MusicGenClient.getInstance()
                .getService(MusicGenService::class.java)
                .generateAudio(text)
                .execute()
            Log.i("response", p1.body()?.audioUrl.toString())
            isRunning = false
            sendMusicGenStatus(true)
            val song = Song(
                UUID.randomUUID().toString(),
                prompt,
                10,
                image,
                p1.body()?.audioUrl!!
                )
            RedisClient.getInstance().getService(RedisService::class.java)
                .sAdd("tones",Gson().toJson(song)).execute()
            RedisClient.getInstance().getService(RedisService::class.java)
                .sAdd("user:${UserDatabaseManager.user?.userName}:tones",
                    Gson().toJson(song)).execute()
            sendMusicGen(song)
            notificationForHigh("AI铃声已生成","${prompt}已生成")
        }catch (e:Exception){
            Log.i("response",e.message.toString())
            notificationForHigh("生成失败","${prompt}生成失败")
        }
        finally {
            stopSelf()
        }


    }

    @SuppressLint("ObsoleteSdkInt", "VisibleForTests")
    private fun notificationForHigh(title:String, content:String){
        val notifyManager:NotificationManager =
            getSystemService(NOTIFICATION_SERVICE)
                as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(mNotifyId,mChannelName,
                NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(true)//是否显示桌面角标
            notifyManager.createNotificationChannel(channel)
        }
        var bitmap:Bitmap? = null
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(800, TimeUnit.SECONDS)  // 连接超时
            .readTimeout(800, TimeUnit.SECONDS)     // 读取超时
            .writeTimeout(800, TimeUnit.SECONDS)    // 写入超时
            .retryOnConnectionFailure(true)        // 启用自动重试
            .addInterceptor { chain ->
                // 自定义重试拦截器（例如最多重试3次）
                val request = chain.request()
                var response: Response? = null
                var retryCount = 0
                while (retryCount < 3 && response == null) {
                    try {
                        response = chain.proceed(request)
                    } catch (e: SocketTimeoutException) {
                        retryCount++
                    }
                }
                response ?: throw SocketTimeoutException("重试失败")
            }
            .build()

        try {
            bitmap = Glide
                .with(this)
                .asBitmap()
                .error(R.drawable.baseline_self_improvement_24)
                .load(image)
                .submit()
                .get(120, TimeUnit.SECONDS) // 设置超时时间

        } catch (e: ExecutionException) {
            // Glide 加载失败
            Toast.makeText(this,
                "图片加载失败: ${e.cause?.message}",Toast.LENGTH_SHORT)
                .show()
        } catch (e: TimeoutException) {
            // 超时重试
            Toast.makeText(this,
                "图片加载失败: ${e.cause?.message}",Toast.LENGTH_SHORT)
                .show()
        }
        val intent = Intent(this@MusicGenService, MusicGenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this,
            Random.nextInt(1000),intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )
        val mBuilder = NotificationCompat.
        Builder(this@MusicGenService,mNotifyId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.baseline_self_improvement_24)
            .setLargeIcon(bitmap)
            .setAutoCancel(true)//点击后是否取消
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE) // 通知类别，"勿扰模式"时系统会决定要不要显示你的通知
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE) // 屏幕可见性，锁屏时，显示icon和标题，内容隐藏
        notifyManager.notify(finishNotifyId,mBuilder.build())
    }
    fun sendMusicGenStatus(status:Boolean){
        val intent = Intent("MUSIC_GEN_STATUS")
        intent.putExtra("status",status)
        sendBroadcast(intent)
    }
    fun sendMusicGen(song:Song){
        val intent = Intent("MUSIC_GEN")
        intent.putExtra("genSong",song)
        sendBroadcast(intent)
    }


}