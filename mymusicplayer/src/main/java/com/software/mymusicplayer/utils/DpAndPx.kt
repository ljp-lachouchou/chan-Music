package com.software.mymusicplayer.utils

import android.content.Context
import android.util.TypedValue
import android.view.View

class DpAndPx {
    companion object{
        fun dpToPx(dp:Int,viewPager2: View):Float{
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                viewPager2.context.resources.displayMetrics
            )
        }
        fun dpToPx(dp:Int,context:Context):Int{
            val density = context.resources.displayMetrics.density
            return (dp * density + 0.5f).toInt()
        }
        fun spToPx(context: Context, spValue: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spValue,
                context.resources.displayMetrics
            )
        }
    }
}