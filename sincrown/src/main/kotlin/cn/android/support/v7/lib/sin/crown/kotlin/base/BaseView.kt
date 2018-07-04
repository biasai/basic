package cn.android.support.v7.lib.sin.crown.kotlin.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BaseView : View {
    //默认开启硬件加速
    constructor(context: Context?, HARDWARE: Boolean = true) : super(context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    //自定义画布，根据需求。自主实现
    var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): BaseView {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            draw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                it(canvas, paint)
            }
        }
    }

}