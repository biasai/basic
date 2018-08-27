package cn.android.support.v7.lib.sin.crown.kotlin.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import cn.android.support.v7.lib.sin.crown.kotlin.R
import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseView
import cn.android.support.v7.lib.sin.crown.kotlin.utils.SelectorUtils


/**
 * 自定义圆角相对布局
 * Created by 彭治铭 on 2018/5/20.
 */
open class RoundRelativeLayout : RelativeLayout {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.RoundCornersRect)
        typedArray?.let {
            var all_radius = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_all, 0f)
            left_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_top, all_radius)
            left_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_bottom, all_radius)
            right_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_top, all_radius)
            right_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_bottom, all_radius)
        }
    }

    var bindView: View? = null//状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is BaseView) {
                    if(value.bindView==null){
                        value.bindView = this//相互绑定
                    }
                } else if (value is RoundTextView) {
                    if(value.bindView==null){
                        value.bindView = this//相互绑定
                    }
                }else if (value is RoundRelativeLayout) {
                    if(value.bindView==null){
                        value.bindView = this//相互绑定
                    }
                }
            }
        }

    fun bindView(bindView: View?) {
        this.bindView = bindView
    }

    //状态同步
    fun bindSycn() {
        bindView?.let {
            it.isSelected = isSelected
            it.isPressed = isPressed
        }
    }

    //重写选中状态。
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        bindView?.let {
            if(it.isSelected!=isSelected){
                it?.isSelected = isSelected//选中状态
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var b = super.dispatchTouchEvent(event)
        if (bindView != null) {
            event?.let {
                when (it.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                        bindView?.isPressed = isPressed//按下状态
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        bindView?.isPressed = false
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        //其他异常
                        bindView?.isPressed = false
                    }
                }
            }
        }
        return b
    }

    var all_radius: Float = 0F//默认，所有圆角的角度
    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角

    var strokeWidth = 0f//边框宽度
    var strokeColor = Color.TRANSPARENT//边框颜色

    //fixme 边框颜色渐变
    var gradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var gradientEndColor = Color.TRANSPARENT//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var gradientColors: IntArray? = null
    var ORIENTATION_VERTICAL = 0//垂直
    var ORIENTATION_HORIZONTAL = 1//水平
    var gradientOritation = ORIENTATION_HORIZONTAL//渐变颜色方向，默认水平

    fun gradientColors(vararg color: Int) {
        gradientColors = color
    }

    fun gradientColors(vararg color: String) {
        gradientColors = IntArray(color.size)
        gradientColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速
    }

    override fun dispatchDraw(canvas: Canvas?) {
        //背景
        canvas?.let {
            onDraw?.let {
                it(canvas, BaseView.getPaint())
            }
        }

        super.dispatchDraw(canvas)

        canvas?.let {
            if (left_top <= 0) {
                left_top = all_radius
            }
            if (left_bottom <= 0) {
                left_bottom = all_radius
            }
            if (right_top <= 0) {
                right_top = all_radius
            }
            if (right_bottom <= 0) {
                right_bottom = all_radius
            }
            //利用内补丁画圆角。只对负补丁有效(防止和正补丁冲突，所以取负)
            var paint = BaseView.getPaint()
            paint.isDither = true
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.strokeWidth = 0f
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//取下面的交集
            // 矩形弧度
            val radian = floatArrayOf(left_top!!, left_top!!, right_top, right_top, right_bottom, right_bottom, left_bottom, left_bottom)
            // 画矩形
            var rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
            var path = Path()
            path.addRoundRect(rectF, radian, Path.Direction.CW)
            canvas.drawPath(path, paint)

            //画矩形边框
            if (strokeWidth > 0) {

                rectF = RectF(0f + strokeWidth / 2F, 0f + strokeWidth / 2F, width.toFloat() - strokeWidth / 2F, height.toFloat() - strokeWidth / 2F)
                path.reset()
                path.addRoundRect(rectF, radian, Path.Direction.CW)
                paint.strokeWidth = strokeWidth
                paint.style = Paint.Style.FILL
                paint.color = strokeColor
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//取下面的交集
                paint.style = Paint.Style.FILL_AND_STROKE
                canvas.drawPath(path, paint)

                //画边框
                paint.style = Paint.Style.STROKE
                paint.setXfermode(null)//正常
                //边框颜色渐变
                var linearGradient: LinearGradient? = null
                if (gradientColors != null) {
                    if (gradientOritation == ORIENTATION_HORIZONTAL) {
                        //水平渐变
                        linearGradient = LinearGradient(0f, 0f, w.toFloat(), h / 2f, gradientColors, null, Shader.TileMode.CLAMP)
                    } else {
                        //垂直渐变
                        linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), gradientColors, null, Shader.TileMode.CLAMP)
                    }
                } else {
                    if (!(gradientStartColor == Color.TRANSPARENT && gradientEndColor == Color.TRANSPARENT)) {
                        if (gradientOritation == ORIENTATION_HORIZONTAL) {
                            linearGradient = LinearGradient(0f, 0f, w.toFloat(), h / 2f, gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
                        } else {
                            linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP)
                        }
                    }
                }
                linearGradient?.let {
                    paint.setShader(linearGradient)
                }
                canvas.drawPath(path, paint)
            }

        }
        //前景
        canvas?.let {
            draw?.let {
                it(canvas, BaseView.getPaint())
            }
        }
    }

    //自定义画布，根据需求。自主实现
    open var draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //自定义，重新绘图
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): RoundRelativeLayout {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //画自己【onDraw在draw()的流程里面，即在它的前面执行】
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //画自己[onDraw与系统名冲突，所以加一个横线]
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): RoundRelativeLayout {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    var w: Int = 0//获取控件的真实宽度
        get() {
            var w = width
            if (layoutParams.width > w) {
                w = layoutParams.width
            }
            return w
        }

    var h: Int = 0//获取控件的真实高度
        get() {
            var h = height
            if (layoutParams.height > h) {
                h = layoutParams.height
            }
            return h
        }

    /**
     * NormalID 默认背景图片id
     * PressID 按下背景图片id
     * SelectID 选中(默认和按下相同)时背景图片id,即选中时状态。需要isSelected=true才有效。
     */
    fun selectorDrawable(NormalID: Int?, PressID: Int?, SelectID: Int? = PressID) {
        SelectorUtils.selectorDrawable(this, NormalID, PressID, SelectID)
    }

    //图片
    fun selectorDrawable(NormalBtmap: Bitmap?, PressBitmap: Bitmap?, SelectBitmap: Bitmap? = PressBitmap) {
        SelectorUtils.selectorDrawable(this, NormalBtmap, PressBitmap, SelectBitmap)
    }

    //颜色
    fun selectorColor(NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor) {
        SelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
    }

    fun selectorColor(NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor) {
        SelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
    }

    //字体颜色
    fun selectorTextColor(NormalColor: Int, PressColor: Int, SelectColor: Int = PressColor) {
        SelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    fun selectorTextColor(NormalColor: String, PressColor: String, SelectColor: String = PressColor) {
        SelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    //属性动画
    fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): BaseView.Companion.ObjectAnimatores {
        return BaseView.Companion.ObjectAnimatores(this).ofFloat(propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): BaseView.Companion.ObjectAnimatores {
        return BaseView.Companion.ObjectAnimatores(this).ofInt(propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    /**
     * 封装位置移动动画
     * toX,toY相对于父容器的移动的目标坐标点。
     * durationMillis 动画时间，单位毫秒。
     * end 回调，动画结束后，返回当前的位置坐标。[位置会实际发生改变]
     * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
     */
    fun translateAnimation(toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null) {
        BaseView.translateAnimation(this, toX, toY, durationMillis, end)
    }


}