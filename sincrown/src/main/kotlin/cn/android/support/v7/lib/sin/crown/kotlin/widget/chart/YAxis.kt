package cn.android.support.v7.lib.sin.crown.kotlin.widget.chart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseView
import cn.android.support.v7.lib.sin.crown.kotlin.common.px
import android.graphics.PaintFlagsDrawFilter

/**
 * X轴，水平方向。从左到右。
 */
class YAxis : BaseView {
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
    //fixme 起点,终点X坐标[方向从下往上],切记，设计是从下往上的思路！
    var startY: Float = default//fixme 因为方向是从下往上。所以这个值其实是最大的
    var stopY: Float = default//fixme 这个最小
    //起点结束Y坐标(y坐标就一个，如果要画倾斜的线，直接rotation=30f旋转整个控件即可，旋转控件不会有锯齿，直接画斜线会有锯齿。)
    var startAndStopX: Float = default

    var unit: Float = default//fixme 单位长度。总长度就是控件本身长度。优先级高于count 。
    /**
     * fixme 根据单位获取X坐标值。1就等于一个unit的长度。
     */
    fun getUnitY(unit: Float): Float {
        return startY - this.unit * unit
    }

    fun getUnitY(unit: Int): Float {
        return getUnitY(unit.toFloat())
    }

    var count: Int = default.toInt()//显示的个数
    var rulerStrokeWidth: Float = strokeWidth//单位线条的宽度
    var rulerStrokeColor: Int = strokeColor//单位线条的颜色
    var rulerStartX: Float = default//单位线条开始X坐标
    var rulerStopX: Float = default//单位线条结束X坐标

    var arrowLength: Float = default//X轴最右边箭头的长度。
    var arrowStrokeWidth: Float = default//箭头边框的宽度
    var arrowStrokeColor: Int = strokeColor//箭头线条的颜色

    var realHeight: Float = default//Y轴实际高度
    override fun onDraw2(canvas: Canvas, paint: Paint) {
        super.onDraw2(canvas, paint)
        paint.style = Paint.Style.STROKE
        //设置边框线帽，边框小了，看不出效果。
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        if (startY <= default) {
            startY = height.toFloat() - strokeWidth * 2
        }
        if (stopY <= default) {
            stopY = strokeWidth
        }
        if (startAndStopX <= default) {
            startAndStopX = width / 2f//默认水平居中
        }

        realHeight = startY - stopY
        if (unit <= default && count > 0) {
            unit = (realHeight / count).toFloat()
        }
        if (count <= default && unit > 0) {
            count = (realHeight / unit).toInt()
        }

        if (arrowLength <= default) {
            arrowLength = strokeWidth * 5//箭头的长度
        }

        if (unit > 0 && count > 0) {
            if (rulerStartX <= default) {
                rulerStartX = startAndStopX - strokeWidth / 2
            }
            if (rulerStopX <= default) {
                rulerStopX = rulerStartX + strokeWidth * 5//直尺的长度。
            }
            paint.strokeWidth = rulerStrokeWidth
            paint.color = rulerStrokeColor
            //画Y轴上的单位直尺
            for (i in 0..count) {
                var y = startY - i * unit
                if (i != 0) {//fixme 第一个不画
                    if (i == count) {
                        var p = 0f
                        if (arrowLength > 0) {
                            p = arrowLength + strokeWidth
                        }
                        if (y > (stopY + unit / 3 + p)) {//fixme 最后一个距离少于单位长度的三分之一，也不画。
                            canvas.drawLine(rulerStartX, y, rulerStopX, y, paint)
                        }
                    } else {
                        canvas.drawLine(rulerStartX, y, rulerStopX, y, paint)
                    }
                }
                //画X轴上的单位文字
                drawUnitText?.let {
                    it(canvas, startAndStopX, y, i)
                }
            }
        }
        canvas.drawFilter = PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        //画X轴
        paint.strokeWidth = strokeWidth
        paint.color = strokeColor
        paint.isDither = true
        paint.isAntiAlias = true
        canvas.drawLine(startAndStopX, startY, startAndStopX, stopY, paint)
        //画原点
        drawOrigin?.let {
            it(canvas, startAndStopX, startY)
        }

        //画终点箭头
        if (arrowLength > 0) {
            if (arrowStrokeWidth <= default) {
                arrowStrokeWidth = strokeWidth
            }
            paint.strokeWidth = arrowStrokeWidth
            paint.color = arrowStrokeColor
            var path = Path()
            path.moveTo(startAndStopX - arrowLength, stopY + arrowLength)
            path.lineTo(startAndStopX, stopY - strokeWidth / 2)
            path.lineTo(startAndStopX + arrowLength, stopY + arrowLength)
            canvas.drawPath(path, paint)
        }

        //画终点
        drawEnd?.let {
            it(canvas, startAndStopX, stopY)
        }

    }

    //画单位文本，返回每个标尺单位的x,y坐标，以及当前下标。从0开始
    //循环从 0 到 count 都会调用。
    private var drawUnitText: ((canvas: Canvas, x: Float, y: Float, position: Int) -> Unit)? = null

    fun drawUnitText(drawUnitText: (canvas: Canvas, x: Float, y: Float, position: Int) -> Unit) {
        this.drawUnitText = drawUnitText
    }

    //画原点，返回原点坐标
    private var drawOrigin: ((canvas: Canvas, x: Float, y: Float) -> Unit)? = null

    fun drawOrigin(drawOrigin: (canvas: Canvas, x: Float, y: Float) -> Unit) {
        this.drawOrigin = drawOrigin
    }

    //画终点，返回终点坐标
    private var drawEnd: ((canvas: Canvas, x: Float, y: Float) -> Unit)? = null

    fun drawEnd(drawEnd: (canvas: Canvas, x: Float, y: Float) -> Unit) {
        this.drawEnd = drawEnd
    }

}