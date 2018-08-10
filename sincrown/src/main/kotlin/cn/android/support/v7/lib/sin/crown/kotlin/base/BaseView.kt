package cn.android.support.v7.lib.sin.crown.kotlin.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import cn.android.support.v7.lib.sin.crown.kotlin.common.px

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
            draw2(it, getPaint())
            draw?.let {
                it(canvas, getPaint())
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
            onDraw2(it, getPaint())
            onDraw?.let {
                it(canvas, getPaint())
            }
        }
    }

    /**
     * 获取新画笔
     */
    fun getPaint(): Paint {
        var paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = px.x(12f)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 0f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        return paint
    }

    companion object {
        /**
         * 画垂直文本
         * x,y 是文本的起点位置
         * offset 垂直文本之间的间隙
         */
        fun drawVerticalText(text: String, canvas: Canvas, paint: Paint, x: Float, y: Float, offset: Float) {
            var list = text.toList()
            for (i in 0 until text.length) {
                var h = paint.textSize
                if (i == 0) {
                    canvas.drawText(list[i].toString(), x, y + h, paint)
                } else {
                    canvas.drawText(list[i].toString(), x, y + (i + 1) * h + (i * offset), paint)
                }
            }
        }
    }

}