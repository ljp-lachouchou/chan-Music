package com.software.mymusicplayer.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Binder
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.software.mymusicplayer.R


class VinylView @JvmOverloads constructor(
    context: Context,attributeSet: AttributeSet? = null,
    defStyleAttr:Int = 0
) : AppCompatImageView(context,attributeSet,defStyleAttr) {

    private var state:Int
    companion object{
        val STATE_STOP = 0
        val STATE_PAUSE = 1
        val STATE_PLAYING = 2
    }
    private var animator :ObjectAnimator
    //    private var rotationAngle = 0f//旋转角度
//    constructor(context: Context):this(context,null,0)
//    constructor(context: Context,attrs: AttributeSet?):this(context,attrs,0)
    init {
        animator = ObjectAnimator.ofFloat(this,"rotation",
            0f,360f).apply {
            duration = 10000//(10秒每圈),
            interpolator = LinearInterpolator() //图片以匀速运动转动
            repeatCount = ValueAnimator.INFINITE //重复次数
            repeatMode = ValueAnimator.RESTART
        }
        state = STATE_STOP
    }
    fun playMusic(){
        when(state){
            STATE_STOP -> {
                animator.start()
                state = STATE_PLAYING
            }
            STATE_PAUSE -> {
                animator.resume()
                state = STATE_PLAYING
            }

        }
    }
    fun pauseMusic(){
        when(state){
            STATE_PLAYING -> {
                animator.pause()
                state = STATE_PAUSE
            }
        }
    }
    fun stopMusic(){
        animator.cancel()
        rotation = 0f
        state = STATE_STOP
        invalidate()//重绘
    }

    override fun onDetachedFromWindow() {//释放动画资源
        super.onDetachedFromWindow()
        animator.cancel()
    }


}