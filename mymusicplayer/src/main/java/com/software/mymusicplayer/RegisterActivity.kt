package com.software.mymusicplayer

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.databases.UserDataBase
import com.software.mymusicplayer.entity.UploadResponse
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.ImageService
import com.software.mymusicplayer.utils.HashPass
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
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class RegisterActivity : BaseActivity() {
    private lateinit var avatar:ImageView
    private lateinit var userNameET:EditText
    private lateinit var nikeName:EditText
    private lateinit var registerPwd:EditText
    private lateinit var confirmPwd:EditText
    private lateinit var registerCancel:Button
    private lateinit var confirmButton: Button
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomView:View
    private val TAKE_PHOTO = 1
    private val SELECT_PHOTO = 2
    private lateinit var tempPhotoUri:Uri
    private var result:Uri? = null
    private lateinit var registerTime: Timestamp
    private lateinit var updateTime:Timestamp
    private lateinit var userDataBase: UserDataBase
    override fun initData() {

    }

    override fun initView() {

        avatar = findViewById(R.id.avatar_img)
        userNameET = findViewById(R.id.user_name)
        nikeName = findViewById(R.id.nike_name_ed)
        registerPwd = findViewById(R.id.register_pwd_ed)
        confirmPwd = findViewById(R.id.confirm_pwd_ed)
        registerCancel = findViewById(R.id.register_cancel)
        confirmButton = findViewById(R.id.register_confirm)
        lifecycleScope.launch(Dispatchers.IO) {
            userDataBase= UserDatabaseManager.
            getInstance(this@RegisterActivity)
                .getDatabase()
        }
        clickEvent()

    }

    private fun clickEvent() {
        avatar.setOnClickListener {
            showDialogSelect()
        }
        confirmButton.setOnClickListener {
            if(!isRightUserName(userNameET.text.toString())) return@setOnClickListener
            if (!registerPwd.text.toString().equals(confirmPwd.text.toString())){
                Toast.makeText(this,"两次密码不一致",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            registerTime = Timestamp(System.currentTimeMillis())
            updateTime = Timestamp(System.currentTimeMillis())
            lifecycleScope.launch(Dispatchers.IO){
                try {
                    val job = async {
                        userDataBase.getUserDao().queryUserByUserName(userNameET.text.toString())
                    }
                    val user = job.await()
                    if (user != null){
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@RegisterActivity,
                                "该用户名已被注册",Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    if (result != null){
                        val job1 = launch {
                            var fileUrl:String? = null
                            val inputStream = contentResolver.openInputStream(result!!)

                            inputStream?.use {
                                    stream ->
                                val bytes = stream.readBytes()
                                val requestFile = bytes.toRequestBody("image/*".toMediaType())
                                val filePart = MultipartBody.Part.createFormData("image", UUID
                                    .randomUUID().toString() +
                                        ".jpg", requestFile)
                                fileUrl = RedisClient.getInstance().getService(ImageService::class.java)
                                    .uploadImage(filePart).execute().body()?.url
                            }
                            Log.i("REGISTER",fileUrl.toString())
                            userDataBase.getUserDao().insertUsers(User(0,
                                userNameET.text.toString()
                                ,HashPass.hashSHA(registerPwd.text.toString()), nikeName.text.toString(),
                                fileUrl,
                                registerTime,
                                updateTime
                            )) }

                        job1.join()
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@RegisterActivity,
                                "注册成功",Toast.LENGTH_SHORT).show()
                            getSharedPreferences("auto_login", MODE_PRIVATE).edit()
                                .putString("userName",userNameET.text.toString())
                                .putString("userPwd",registerPwd.text.toString())
                                .putLong("tokenTime",Timestamp(System.currentTimeMillis()).time)
                                .apply()
                            finish()
                        }
                    }else{
                        val job1 = launch {
                            Log.i("REGISTER","YES")
                            userDataBase.getUserDao().insertUsers(User(0,
                                userNameET.text.toString()
                                ,HashPass.hashSHA(registerPwd.text.toString()), nikeName.text.toString(),
                                null,
                                registerTime,
                                updateTime
                            )) }
                        job1.join()
                        withContext(Dispatchers.Main){
                            Toast.makeText(this@RegisterActivity,
                                "注册成功",Toast.LENGTH_SHORT).show()
                            getSharedPreferences("auto_login", MODE_PRIVATE).edit()
                                .putString("userName",userNameET.text.toString())
                                .putString("userPwd",registerPwd.text.toString())
                                .putLong("tokenTime",Timestamp(System.currentTimeMillis()).time)
                                .apply()
                            finish()
                        }
                    }


                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@RegisterActivity,
                            "该用户名已被注册",Toast.LENGTH_SHORT).show()
                    }
                }

            }

        }
        registerCancel.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("InflateParams")
    private fun showDialogSelect() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomView = layoutInflater.inflate(R.layout.dialog_bottom,null)
        bottomSheetDialog.setContentView(bottomView)
        val tvTakePictures = bottomView.findViewById<TextView>(R.id.tv_take_pictures)
        val tvOpenAlbum = bottomView.findViewById<TextView>(R.id.tv_open_album)
        val tvCancel = bottomView.findViewById<TextView>(R.id.tv_cancel)
        tvTakePictures.setOnClickListener {
            takePhoto()
            bottomSheetDialog.cancel()
        }
        tvOpenAlbum.setOnClickListener {
            openAlbum()
            bottomSheetDialog.cancel()
        }
        tvCancel.setOnClickListener {
            bottomSheetDialog.cancel()
        }
        bottomSheetDialog.show()


    }
    private fun takePhoto(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // 显示权限用途解释
            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                AlertDialog.Builder(this)
                    .setTitle("需要相机权限")
                    .setMessage("拍摄头像需要访问相机")
                    .setPositiveButton("允许") { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), TAKE_PHOTO)
                    }
                    .show()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), TAKE_PHOTO)
            }
        } else {
            val tempFile = File(baseContext.filesDir, UUID.randomUUID().toString()
                    + "temp_avatar.jpg")
            tempPhotoUri = FileProvider.getUriForFile(this,
                "${baseContext.packageName}.provider",
                tempFile)
            Log.i("tempFile",tempFile.toString())
            Log.i("URI",tempPhotoUri.toString())
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT,tempPhotoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(this,TAKE_PHOTO)
            }

        }
    }
    private fun openAlbum() {
        val intent = Intent(Intent.ACTION_PICK,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, SELECT_PHOTO)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (RESULT_OK == resultCode){
            if (requestCode == SELECT_PHOTO){
                result = data?.data
            }else if(requestCode == TAKE_PHOTO){
                result =tempPhotoUri
            }

            Glide.with(this).load(result)
                .circleCrop()
                .into(avatar)
        }
    }

    private fun isRightUserName(userName:String):Boolean{

        if (!(userName.length >= 6 && userName.length <= 18)){
            Toast.makeText(this,"长度只能为6~18",Toast.LENGTH_SHORT).show()
            return false
        }
        if (!userName.matches(Regex("^[a-zA-Z0-9]+\$"))){
            Toast.makeText(this,"只允许大小写字母和数字输入",Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    override fun getLayoutId(): Int = R.layout.activity_register


}