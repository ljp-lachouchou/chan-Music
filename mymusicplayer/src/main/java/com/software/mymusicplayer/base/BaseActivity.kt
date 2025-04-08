package com.software.mymusicplayer.base

import android.app.Activity
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initData()
        initView()
    }

    abstract fun initData()

    abstract fun initView()

    abstract fun getLayoutId():Int
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN){
            val view = currentFocus
            if (view is EditText){
                val rect = Rect()
                view.getGlobalVisibleRect(rect)
                if (!rect.contains(ev.getRawX().toInt(),ev.getRawY().toInt())){
                    hideKeyboard(view)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard(view : View) {
        val keyManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        keyManager.hideSoftInputFromWindow(view.windowToken,0)
    }
}