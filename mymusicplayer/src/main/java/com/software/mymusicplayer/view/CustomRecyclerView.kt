package com.software.mymusicplayer.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class CustomRecyclerView @JvmOverloads
constructor(context: Context,attributeSet: AttributeSet?) :
RecyclerView(context,attributeSet)
{
    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // 滑动到边缘时允许父容器（如 ScrollView）接管事件
                if (!canScrollHorizontally(1) && !canScrollHorizontally(-1)) {
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        })
    }
    private var initX = 0f
    private var initY = 0f
//    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    fun isHorizontalScroll(event: MotionEvent) : Boolean {
        val dx = event.x - initX
        val dy = event.y - initY
        val angle = Math.atan2(dx.toDouble(), dy.toDouble())
        when {
            angle in -Math.PI/4..Math.PI/4 -> return true
            angle in Math.PI/4..3*Math.PI/4 -> return false
            angle in 3*Math.PI/4..Math.PI || angle in -Math.PI..-3*Math.PI/4 -> return true
            else -> return false
        }
    }
    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        when(e.action){
            MotionEvent.ACTION_DOWN ->{
                initX = e.x
                initY = e.y
//                animate().scaleX(0.95f).scaleY(0.95f).start()
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalScroll(e)){
                    parent.requestDisallowInterceptTouchEvent(false)

                }

            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                animate().scaleX(1f).scaleY(1f).start()
                parent.requestDisallowInterceptTouchEvent(true)
            }

        }
        return super.onInterceptTouchEvent(e)
    }
}