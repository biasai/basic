package cn.android.support.v7.lib.sin.crown.kotlin.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * 无论是自定义view还是普通的layout布局。都不能在async和launch协程里面初始化，要么报错，要么不显示。
 */
open class BaseView : View {
    //默认开启硬件加速
    constructor(context: Context?, HARDWARE: Boolean = true) : super(context) {
        if (HARDWARE) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    //fixme 自定义画布，根据需求。自主实现
    protected open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): BaseView {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //fixme 什么都不做，交给子类去实现绘图
    //fixme 之所以会有这个方法。是为了保证自定义的 draw和onDraw的执行顺序。始终是在最后。
    protected open fun draw2(canvas: Canvas, paint: Paint) {}

    protected open fun onDraw2(canvas: Canvas, paint: Paint) {}

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.let {
            var paint = Paint()
            paint.isAntiAlias = true
            paint.isDither = true
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 0f
            draw2(it, paint)
            draw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }
    }

    //fixme 画自己【onDraw在draw()的super.draw(canvas)流程里面，即在它的前面执行】
    //fixme 可以认为 draw()是前景[上面后画]，onDraw是背景[下面先画]。
    protected var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //fixme 画自己[onDraw与系统名冲突，所以加一个横线]
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): BaseView {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            var paint = Paint()
            paint.isAntiAlias = true
            paint.isDither = true
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 0f
            onDraw2(it, paint)
            onDraw?.let {
                var paint = Paint()
                paint.isAntiAlias = true
                paint.isDither = true
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 0f
                it(canvas, paint)
            }
        }
    }

}