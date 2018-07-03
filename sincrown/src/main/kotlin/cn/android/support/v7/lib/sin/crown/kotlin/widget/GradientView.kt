package cn.android.support.v7.lib.sin.crown.kotlin.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * 颜色渐变视图
 */
class GradientView : View {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    //fixme 水平渐变颜色数组值【均匀渐变】
    var horizontalColors: IntArray? = null

    fun horizontalColors(vararg color: Int) {
        horizontalColors = color
    }

    fun horizontalColors(vararg color: String) {
        horizontalColors = IntArray(color.size)
        horizontalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    //fixme 垂直渐变颜色数组值【均匀】
    var verticalColors: IntArray? = null

    fun verticalColors(vararg color: Int) {
        verticalColors = color
    }

    //fixme 如：verticalColors("#00dedede","#dedede") 向上的阴影线
    fun verticalColors(vararg color: String) {
        verticalColors = IntArray(color.size)
        verticalColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }

    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.apply {
            var paint = Paint()
            paint.isAntiAlias = true
            paint.isDither = true
            //水平渐变
            horizontalColors?.let {
                var shader = LinearGradient(0f, 0f, width.toFloat(), 0f, it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
                drawPaint(paint)
            }

            //fixme 水平渐变 和 垂直渐变 效果会叠加。垂直覆盖在水平的上面。

            //垂直渐变
            verticalColors?.let {
                var shader = LinearGradient(0f, 0f, 0f, height.toFloat(), it, null, Shader.TileMode.MIRROR)
                paint.setShader(shader)
                drawPaint(paint)
            }
        }
    }
}