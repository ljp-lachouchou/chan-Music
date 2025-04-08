package com.software.mymusicplayer.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs

class ShrinkLayout @JvmOverloads constructor(context: Context,
    attributeSet: AttributeSet? = null,
    defStyle:Int = 0
    ) : ConstraintLayout(context,attributeSet,defStyle) {

        private val scaleFactor = 0.95f
        private val duration = 150L
        private var onTouchListener:ShrinkLayout.OnTouchListener? = null

        private var initialX = 0f // 初始触摸点 X
        private var initialY = 0f // 初始触摸点 Y
        init {
            isClickable = true
//            setOnTouchListener(AngleTouchListener(this))
            //触摸监听
            setOnTouchListener { v, event ->
                when(event.action){
                    MotionEvent.ACTION_DOWN ->{
                        initialX = event.x
                        initialY = event.y
                        performClick()
                        animate().scaleX(scaleFactor).scaleY(scaleFactor).setDuration(duration).start()
                    }
//                    MotionEvent.ACTION_MOVE->{
//                        val dx = abs(event.x - initialX)
//                        val dy = abs(event.y - initialY)
//                        if (dx > touchSlop || dy > touchSlop){
////                            parent.requestDisallowInterceptTouchEvent(true)//禁止父容器拦截事件
//                            animate().cancel()//正在滑动取消动画效果
//                            scaleX = 1f
//                            scaleY = 1f
//                        }
//                    }
                    MotionEvent.ACTION_UP->{
                        animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
                        performClick()
                        onTouchListener?.onShrinkTouch()

                    }
                    MotionEvent.ACTION_CANCEL ->{
                        animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
                        performClick()
                    }


                }
                false //传递给子容器
            }
        }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    fun setOnTouchListener(onTouchListener: OnTouchListener){
        this.onTouchListener = onTouchListener
    }
    interface OnTouchListener{
        fun onShrinkTouch()

    }
}