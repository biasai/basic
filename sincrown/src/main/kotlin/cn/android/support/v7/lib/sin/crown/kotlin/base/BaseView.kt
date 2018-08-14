package cn.android.support.v7.lib.sin.crown.kotlin.base

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
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

    fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimatores {
        return ObjectAnimatores(this).ofFloat(propertyName,repeatCount,duration,*value,AnimatorUpdateListener =AnimatorUpdateListener )
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimatores {
        return ObjectAnimatores(this).ofInt(propertyName,repeatCount,duration,*value,AnimatorUpdateListener =AnimatorUpdateListener )
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

        /**
         * 传递的View，不管传的是子类还是父类。都行。清测有效。
         */
        class ObjectAnimatores(var view: View) {
            var animatorSet = AnimatorSet()
            var list = mutableListOf<ObjectAnimator>()

            /**
             * propertyName 属性名称
             * repeatCount  动画次数,从0开始。0表示一次，1表示两次。Integer.MAX_VALUE是最大值。
             * duration  动画时间，单位毫秒。1000表示一秒。
             * value 可变参数。属性的变化值
             * AnimatorUpdateListener 动画监听，返回当前变化的属性值。
             */
            fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimatores {
                var objectAnimator = ObjectAnimator.ofFloat(view, propertyName.trim(), *value)
                if(repeatCount>=Int.MAX_VALUE){
                    objectAnimator.repeatCount = Int.MAX_VALUE-1//防止Int.MAX_VALUE无效。
                }else{
                    objectAnimator.repeatCount = repeatCount
                }
                objectAnimator.duration = duration
                objectAnimator.interpolator=LinearInterpolator()//线性变化，平均变化
                objectAnimator.addUpdateListener {
                    var value = it.getAnimatedValue(propertyName.trim())
                    value?.let {
                        view.invalidate()//fixme 不停的自我刷新，省去了set里面进去刷新。
                        AnimatorUpdateListener?.let {
                            it(value as Float)
                        }
                    }
                }
                list.add(objectAnimator)
                return this
            }

            fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimatores {
                var objectAnimator = ObjectAnimator.ofInt(view, propertyName.trim(), *value)
                if(repeatCount>=Int.MAX_VALUE){
                    objectAnimator.repeatCount = Int.MAX_VALUE-1//防止Int.MAX_VALUE无效。
                }else{
                    objectAnimator.repeatCount = repeatCount
                }
                objectAnimator.duration = duration
                objectAnimator.interpolator=LinearInterpolator()//线性变化，平均变化
                objectAnimator.addUpdateListener {
                    var value = it.getAnimatedValue(propertyName.trim())
                    value?.let {
                        view.invalidate()
                        AnimatorUpdateListener?.let {
                            it(value as Int)
                        }
                    }
                }
                list.add(objectAnimator)
                return this
            }

            /**
             * 动画开始。支持多属性动画。
             */
            fun playTogether() {
                animatorSet.playTogether(*list.toTypedArray())
                animatorSet.start()
            }

        }

    }

}