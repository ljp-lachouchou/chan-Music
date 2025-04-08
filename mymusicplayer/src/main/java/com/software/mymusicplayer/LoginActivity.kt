package com.software.mymusicplayer

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.databases.UserDataBase
import com.software.mymusicplayer.entity.BaseResponse
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.HashPass
import com.software.mymusicplayer.utils.NetworkUtils
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import kotlin.math.log

class LoginActivity : BaseActivity() {
    private lateinit var userName:EditText
    private lateinit var pwd:EditText
    private lateinit var login:Button
    private lateinit var register:Button
    private lateinit var userDataBase: UserDataBase
    private lateinit var updateLayout:View
    private lateinit var realLayout:View
    val networkUtils = NetworkUtils(this)
    private val currentTime = Timestamp(System.currentTimeMillis())
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_MyApplication)
        super.onCreate(savedInstanceState)
    }
    override fun initData() {

    }

    override fun initView() {
        updateLayout = findViewById(R.id.update_layout_login)
        realLayout = findViewById(R.id.real_layout_login)
        if (!networkUtils.isNetworkAvailable()){
            realLayout.visibility = View.GONE
            updateLayout.visibility = View.VISIBLE
            return
        }else {
            updateLayout.visibility = View.GONE
            realLayout.visibility = View.VISIBLE
        }
        register = findViewById(R.id.register)
        login = findViewById(R.id.login)
        userName = findViewById(R.id.user_name_ed)
        pwd = findViewById(R.id.password_ed)
        val sharedPreferences = getSharedPreferences("auto_login", MODE_PRIVATE)
        val userNam = sharedPreferences.getString("userName", "0")
        val userPwd = sharedPreferences.getString("userPwd","0")

        lifecycleScope.launch(Dispatchers.IO) {
           userDataBase= UserDatabaseManager.getInstance(this@LoginActivity).getDatabase()

        }
        if (!"0".equals(userNam) && !"0".equals(userPwd)){
            val instant = currentTime.toInstant()
            val lastTime = Timestamp(sharedPreferences.getLong("tokenTime",0)).toInstant()
            val duration = Duration.between(instant, lastTime)
            val daysByDuration = Math.abs(duration.toDays())
            Log.i("IS_LOGIN",
                sharedPreferences.
            getBoolean("isLogin",false).toString())
            Log.i("DAYS",Timestamp(sharedPreferences.getLong("tokenTime",0)).toString())
            if (!(daysByDuration > 4) && sharedPreferences.getBoolean("isLogin",false)){
                lifecycleScope.launch(Dispatchers.IO) {
                    UserDatabaseManager.user =
                     UserDatabaseManager.getInstance(this@LoginActivity)
                    .getDatabase()
                    .getUserDao()
                    .queryUserByUserName(userNam!!)
                }
                getSharedPreferences("auto_login", MODE_PRIVATE).edit()
                    .putString("userName",userNam)
                    .apply()
                startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                finish()
            }else{
                Toast.makeText(this@LoginActivity,
                    "登录过期", Toast.LENGTH_SHORT).show()
                userName.setText(sharedPreferences.getString("userName","0"))
                getSharedPreferences("auto_login", MODE_PRIVATE).edit()
                    .putLong("tokenTime",Timestamp(System.currentTimeMillis()).time)
                    .apply()
                sharedPreferences.edit()
                    .putBoolean("isLogin",false)
                    .apply()
            }

        }else{
            sharedPreferences.edit()
                .putBoolean("isLogin",false)
                .apply()
        }
        clickEvent()

    }

    private fun loginClick() {
        lifecycleScope.launch(Dispatchers.IO){
            val userTest = async {
                userDataBase.getUserDao().queryUserByUserName(userName.text.toString())
            }.await()
            if (userTest == null){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LoginActivity,
                        "用户名未被注册", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val hashPwd = HashPass.hashSHA(pwd.text.toString())
            if (!userTest.pwd.equals(hashPwd)){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@LoginActivity,
                        "密码错误", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            var users:List<User>? = null
            launch {users = userDataBase.getUserDao().getAllUsers() }.join()
            var user:User?= null
            launch {user= userDataBase.getUserDao().login(userName.text.toString(),hashPwd) }.join()
            withContext(Dispatchers.Main){
                Log.i("USERS",users.toString())
                if ( user != null){
                    withContext(Dispatchers.IO){
                        RedisClient.getInstance().getService(RedisService::class.java)
                            .del("user: + ${user?.userName}:playlist")
                            .enqueue(object : retrofit2.Callback<BaseResponse<String>>{
                                override fun onResponse(
                                    p0: Call<BaseResponse<String>>,
                                    p1: Response<BaseResponse<String>>
                                ) {
                                    Log.i("p1!!",p1.body().toString())
                                }

                                override fun onFailure(p0: Call<BaseResponse<String>>, p1: Throwable) {
                                    Log.i("p1",p1.printStackTrace().toString())
                                }

                            })
                    }
                    getSharedPreferences("auto_login", MODE_PRIVATE).edit()
                        .putBoolean("isLogin",true)
                        .apply()
                    getSharedPreferences("auto_login", MODE_PRIVATE).edit()
                        .putString("userName",user?.userName)
                        .apply()
                    Log.i("isLogin",
                        getSharedPreferences("auto_login",
                            MODE_PRIVATE).getBoolean("isLogin",false).toString())
                    startActivity(Intent(this@LoginActivity,MainActivity::class.java).apply {
                        putExtra("userId",userName.text.toString())
                    })
                    finish()
                }
            }

        }



    }

    private fun clickEvent() {
        register.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }
        login.setOnClickListener {
            loginClick()
        }
    }

    override fun onResume() {

        initView()
        super.onResume()
    }
    override fun getLayoutId(): Int=
        R.layout.activity_login

    fun reload(view: View) {
        initView()
    }


}