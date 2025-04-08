package com.software.mymusicplayer

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.software.mymusicplayer.base.BaseActivity
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.ImageService
import com.software.mymusicplayer.utils.HashPass
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.sql.Timestamp
import java.util.UUID
import kotlin.math.abs

class ProfileActivity : BaseActivity(){
    private lateinit var avatar:ImageView
    private lateinit var button:Button
    val SELECT_PHOTO = 202
    private var result:Uri? = null
    private lateinit var ed:TextInputEditText

    override fun initData() {

    }

    override fun initView() {
        avatar = findViewById(R.id.iv_avatar)
        ed = findViewById(R.id.et_nickname)
        button = findViewById(R.id.btn_submit)
        Glide.with(this)
            .load(UserDatabaseManager.user?.avatar)
            .into(avatar)
        ed.setText(UserDatabaseManager.user?.nickName)
        clickEvent()
    }

    private fun clickEvent() {
        avatar.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, SELECT_PHOTO)
        }
        button.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                var fileUrl: String? = null
                if (result == null){
                    UserDatabaseManager.user?.nickName = ed.text.toString()
                    UserDatabaseManager.getInstance(this@ProfileActivity)
                        .getDatabase().getUserDao().updateUser(UserDatabaseManager.user!!)
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@ProfileActivity,
                            "修改成功",Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                    return@launch
                }
                val inputStream = contentResolver.openInputStream(result!!)

                inputStream?.use { stream ->
                    val bytes = stream.readBytes()
                    val requestFile = bytes.toRequestBody("image/*".toMediaType())
                    val filePart = MultipartBody.Part.createFormData(
                        "image", UUID
                            .randomUUID().toString() +
                                ".jpg", requestFile
                    )
                    fileUrl = RedisClient.getInstance().getService(ImageService::class.java)
                        .uploadImage(filePart).execute().body()?.url
                }
                UserDatabaseManager.user?.avatar = fileUrl
                UserDatabaseManager.user?.nickName = ed.text.toString()
                UserDatabaseManager.getInstance(this@ProfileActivity)
                    .getDatabase().getUserDao().updateUser(UserDatabaseManager.user!!)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@ProfileActivity,
                        "修改成功",Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }

            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            if (requestCode == SELECT_PHOTO){
                result = data?.data
                Log.i("SSDA",result.toString())
            }

            Glide.with(this)
                .load(result)
                .into(avatar)
        }
    }
    override fun getLayoutId(): Int =R.layout.activity_profile
    fun backMain(view: View) {
        finish()
    }
}