package cn.android.support.v7.lib.sin.crown.kotlin.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import cn.android.support.v7.lib.sin.crown.utils.ProportionUtils

/**
 * 自定义小圆点
 */
class DotsView: View {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    var paint: Paint

    init {
        paint = Paint()
        paint.isDither = true
        paint.isAntiAlias = true
        paint.style=Paint.Style.FILL_AND_STROKE
    }

    //默认圆点颜色
    var defaultColor = Color.parseColor("#9FA1A0")

    fun defaultColor(defaultColor: Int = this.defaultColor): DotsView {
        this.defaultColor = defaultColor
        return this
    }

    //选择圆点颜色
    var selectColor = Color.parseColor("#7160C6")

    fun selectColor(selectColor: Int = this.selectColor): DotsView {
        this.selectColor = selectColor
        return this
    }

    var radius = ProportionUtils.getInstance().adapterInt(15 / 2)//园点半径
    fun radius(radius: Int = this.radius): DotsView {
        this.radius = radius
        return this
    }

    var offset = ProportionUtils.getInstance().adapterInt(30)//园点半径
    fun offset(offset: Int = this.offset): DotsView {
        this.offset = offset
        return this
    }

    var count = 3//圆点个数
    fun count(count: Int = this.count): DotsView {
        this.count = count
        return this
    }

    var selectPosition = 0//选中下标
    fun selectPosition(selectPosition: Int=this.selectPosition): DotsView {
        this.selectPosition = selectPosition
        return this
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.run {
            var w=radius*count+offset*(count-1)//圆点+间隙总长度
            var x=(width-w)/2+radius//第一个圆点的x坐标
            var y=height/2
            for (i in 1..count) {
                if (i == selectPosition+1) {
                    paint.color = selectColor
                } else {
                    paint.color = defaultColor
                }
                drawCircle(x.toFloat(),y.toFloat(),radius.toFloat(),paint)
                x+=radius+offset+radius
            }
        }
    }

}