package cn.android.support.v7.lib.sin.crown.kotlin.widget.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseView
import cn.android.support.v7.lib.sin.crown.kotlin.common.px
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PaintFlagsDrawFilter
import android.util.Log


/**
 * X轴，水平方向。从左到右。
 */
class XAxis : BaseView {
    //关闭硬件加速。不然在部分手机，如小米。线条与线条之间的连接处有锯齿。
    constructor(context: Context?) : super(context, false) {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun draw2(canvas: Canvas, paint: Paint) {
        super.draw2(canvas, paint)
    }

    var default = -1f//默认值。
    var strokeWidth: Float = px.x(1.5f)//边框的宽度
    var strokeColor: Int = Color.parseColor("#bcbec0")//边框颜色
    //起点,终点X坐标
    var startX: Float = default
    var stopX: Float = default
    //起点结束Y坐标(y坐标就一个，如果要画倾斜的线，直接rotation=30f旋转整个控件即可，旋转控件不会有锯齿，直接画斜线会有锯齿。)
    var startAndStopY: Float = default

    var unit: Float = default//单位长度。总长度就是控件本身长度。优先级高于count
    var count: Int = default.toInt()//显示的个数
    var rulerStrokeWidth: Float = strokeWidth//单位线条的宽度
    var rulerStrokeColor: Int = strokeColor//单位线条的颜色
    var rulerStartY: Float = default//单位线条开始Y坐标
    var rulerStopY: Float = default//单位线条结束Y坐标

    var arrowLength: Float = default//X轴最右边箭头的长度。
    var arrowStrokeWidth: Float = default//箭头边框的宽度
    var arrowStrokeColor: Int = strokeColor//箭头线条的颜色

    var realWidth: Float = default//X轴实际长度
    override fun onDraw2(canvas: Canvas, paint: Paint) {
        super.onDraw2(canvas, paint)
        paint.style = Paint.Style.STROKE
        //设置边框线帽，边框小了，看不出效果。
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        if (startX <= default) {
            startX = strokeWidth
        }
        if (stopX <= default) {
            stopX = width.toFloat() - strokeWidth * 2
        }
        if (startAndStopY <= default) {
            startAndStopY = height / 2f//默认垂直居中
        }

        realWidth = stopX - startX
        if (unit <= default && count > 0) {
            unit = (realWidth / count).toFloat()
        }
        if (count <= default && unit > 0) {
            count = (realWidth / unit).toInt()
        }

        if (arrowLength <= default) {
            arrowLength = strokeWidth * 5//箭头的长度
        }

        if (unit > 0 && count > 0) {
            if (rulerStartY <= default) {
                rulerStartY = startAndStopY - strokeWidth / 2
            }
            if (rulerStopY <= default) {
                rulerStopY = rulerStartY - strokeWidth * 5//直尺的长度。
            }
            paint.strokeWidth = rulerStrokeWidth
            paint.color = rulerStrokeColor
            //画X轴上的单位直尺
            for (i in 0..count) {
                var x = i * unit + startX
                if (i != 0) {//fixme 第一个不画
                    if (i == count) {
                        var p = 0f
                        if (arrowLength > 0) {
                            p = arrowLength + strokeWidth
                        }
                        if (x < (stopX - unit / 3 - p)) {//fixme 最后一个距离少于单位长度的三分之一，也不画。
                            canvas.drawLine(x, rulerStartY, x, rulerStopY, paint)
                        }
                    } else {
                        canvas.drawLine(x, rulerStartY, x, rulerStopY, paint)
                    }
                }
                drawText?.let {
                    it(canvas, x, startAndStopY, i)
                }
            }
        }
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        //画X轴
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
        paint.isDither = true
        paint.isAntiAlias = true
        canvas.drawLine(startX, startAndStopY, stopX, startAndStopY, paint)

        //画箭头
        if (arrowLength > 0) {
            if (arrowStrokeWidth <= default) {
                arrowStrokeWidth = strokeWidth
            }
            paint.strokeWidth = arrowStrokeWidth
            paint.color = arrowStrokeColor
            var path = Path()
            path.moveTo(stopX - arrowLength, startAndStopY - arrowLength)
            path.lineTo(stopX + strokeWidth / 2, startAndStopY)
            path.lineTo(stopX - arrowLength, startAndStopY + arrowLength)
            canvas.drawPath(path, paint)
        }
    }

    //画文本，返回文本的x,y坐标，以及当前下标。从0开始
    //循环从 0 到 count 都会调用。
    private var drawText: ((canvas: Canvas, x: Float, y: Float, position: Int) -> Unit)? = null

    fun drawText(drawText: (canvas: Canvas, x: Float, y: Float, position: Int) -> Unit) {
        this.drawText = drawText
    }

    companion object {
        /**
         * 画垂直文本
         * x,y 是文本的起点位置
         * offset 垂直文本直接的间隙
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