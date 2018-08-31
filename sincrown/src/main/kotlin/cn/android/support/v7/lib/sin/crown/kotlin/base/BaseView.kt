package cn.android.support.v7.lib.sin.crown.kotlin.base

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import cn.android.support.v7.lib.sin.crown.kotlin.R
import cn.android.support.v7.lib.sin.crown.kotlin.common.px
import cn.android.support.v7.lib.sin.crown.kotlin.utils.SelectorUtils
import cn.android.support.v7.lib.sin.crown.kotlin.widget.RoundRelativeLayout
import cn.android.support.v7.lib.sin.crown.kotlin.widget.RoundTextView

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

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//默认硬件加速
        context?.let {
            val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.RoundCornersRect)
            typedArray?.let {
                var all_radius = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_all, 0f)
                left_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_top, all_radius)
                left_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_left_bottom, all_radius)
                right_top = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_top, all_radius)
                right_bottom = typedArray?.getDimension(R.styleable.RoundCornersRect_radian_right_bottom, all_radius)
            }
        }
    }

    var bindView: View? = null//状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is BaseView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is RoundTextView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is RoundRelativeLayout) {
                    if (value.bindView == null) {
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
            if (it.isSelected != isSelected) {
                it?.isSelected = isSelected//选中状态
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (bindView != null) {
            event?.let {
                when (it.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                        bindView?.isPressed = true//按下状态
                        isPressed = true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        bindView?.isPressed = false
                        isPressed = false
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        //其他异常
                        bindView?.isPressed = false
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

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

    var all_radius: Float = 0F//默认，所有圆角的角度
    var left_top: Float = 0f//左上角
    var left_bottom: Float = 0f//左下角
    var right_top = 0f//右上角
    var right_bottom = 0f//右下角

    var baseStrokeWidth = 0f//边框宽度
    var baseStrokeColor = Color.TRANSPARENT//边框颜色

    //fixme 边框颜色渐变
    var baseStrokeGradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var baseStrokeGradientEndColor = Color.TRANSPARENT//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var baseStrokeGradientColors: IntArray? = null
    var BASE_ORIENTATION_VERTICAL = 0//垂直
    var BASE_ORIENTATION_HORIZONTAL = 1//水平
    var baseStrokeGradientOritation = BASE_ORIENTATION_HORIZONTAL//渐变颜色方向，默认水平

    fun baseStrokeGradientColors(vararg color: Int) {
        baseStrokeGradientColors = color
    }

    fun baseStrokeGradientColors(vararg color: String) {
        baseStrokeGradientColors = IntArray(color.size)
        baseStrokeGradientColors?.apply {
            if (color.size > 1) {
                for (i in 0..color.size - 1) {
                    this[i] = Color.parseColor(color[i])
                }
            } else {
                this[0] = Color.parseColor(color[0])
            }
        }
    }

    var baseAfterDrawRadius = true//fixme 圆角边框是否最后画。默认最后画。不管是先画，还是后面。总之都在背景上面。背景最底层。
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        //圆角，边框最先画。
        if (!baseAfterDrawRadius) {
            drawRadius(canvas)
        }
        canvas?.let {
            draw2(it, getPaint())
            draw?.let {
                it(canvas, getPaint())
            }
        }
        //圆角，边框最后画。
        if (baseAfterDrawRadius) {
            drawRadius(canvas)
        }
    }

    fun drawRadius(canvas: Canvas?) {
        //圆角，边框
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
            if (baseStrokeWidth > 0) {

                rectF = RectF(0f + baseStrokeWidth / 2F, 0f + baseStrokeWidth / 2F, width.toFloat() - baseStrokeWidth / 2F, height.toFloat() - baseStrokeWidth / 2F)
                path.reset()
                path.addRoundRect(rectF, radian, Path.Direction.CW)
                paint.strokeWidth = baseStrokeWidth
                paint.style = Paint.Style.FILL
                paint.color = baseStrokeColor
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))//取下面的交集
                paint.style = Paint.Style.FILL_AND_STROKE
                canvas.drawPath(path, paint)

                //画边框
                paint.style = Paint.Style.STROKE
                paint.setXfermode(null)//正常
                //边框颜色渐变
                var linearGradient: LinearGradient? = null
                if (baseStrokeGradientColors != null) {
                    if (baseStrokeGradientOritation == BASE_ORIENTATION_HORIZONTAL) {
                        //水平渐变
                        linearGradient = LinearGradient(0f, 0f, w.toFloat(), h / 2f, baseStrokeGradientColors, null, Shader.TileMode.CLAMP)
                    } else {
                        //垂直渐变
                        linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), baseStrokeGradientColors, null, Shader.TileMode.CLAMP)
                    }
                } else {
                    if (!(baseStrokeGradientStartColor == Color.TRANSPARENT && baseStrokeGradientEndColor == Color.TRANSPARENT)) {
                        if (baseStrokeGradientOritation == BASE_ORIENTATION_HORIZONTAL) {
                            linearGradient = LinearGradient(0f, 0f, w.toFloat(), h / 2f, baseStrokeGradientStartColor, baseStrokeGradientEndColor, Shader.TileMode.CLAMP)
                        } else {
                            linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), baseStrokeGradientStartColor, baseStrokeGradientEndColor, Shader.TileMode.CLAMP)
                        }
                    }
                }
                linearGradient?.let {
                    paint.setShader(linearGradient)
                }
                canvas.drawPath(path, paint)
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

    //获取文本居中Y坐标
    fun getCenterTextY(paint: Paint): Float {
        var baseline = (h - (paint.descent() - paint.ascent())) / 2 - paint.ascent()
        return baseline
    }

    /**
     * 获取文本实际居中Y坐标。
     */
    fun getTextY(paint: Paint, y: Float): Float {
        var centerY = getCenterTextY(paint)
        var sub = h / 2 - centerY
        var y2 = y - sub
        return y2
    }

    /**
     * 获取文本的高度
     */
    fun getTextHeight(paint: Paint): Float {
        return paint.descent() - paint.ascent()
    }

    var centerX = 0f
        get() = centerX()

    fun centerX(): Float {
        return w / 2f
    }

    var centerY = 0f
        get() = centerY()

    fun centerY(): Float {
        return h / 2f
    }

    //根据宽度，获取该宽度居中值
    fun centerX(width: Float): Float {
        return (w - width) / 2
    }

    //根据高度，获取该高度居中值
    fun centerY(height: Float): Float {
        return (h - height) / 2
    }

    /**
     * 获取新画笔
     */
    fun getPaint(): Paint {
        return BaseView.getPaint()
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

    //属性动画集合
    var objectAnimates = arrayListOf<ObjectAnimator?>()

    //停止所有属性动画
    fun stopAllObjAnim() {
        for (i in 0 until objectAnimates.size) {
            objectAnimates[i]?.let {
                it.end()
            }
        }
        objectAnimates.clear()//清除所有动画
    }

    //属性动画
    fun ofFloat(propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = ofFloat(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        objectAnimates.add(objectAnimator)
        return objectAnimator
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = ofInt(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        objectAnimates.add(objectAnimator)
        return objectAnimator
    }

    //透明动画,透明度 0f(完全透明)到1f(完全不透明)
    fun alpha(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("alpha", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    /**
     * 封装位置移动动画
     * toX,toY相对于父容器的移动的目标坐标点。
     * durationMillis 动画时间，单位毫秒。
     * end 回调，动画结束后(结束了才回调)，返回当前的位置坐标。[位置会实际发生改变]
     * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
     */
    //调用案例：translateAnimation(300f,800f,500){x,y-> }
    fun translateAnimation(toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null): TranslateAnimation {
        return translateAnimation(this, toX, toY, durationMillis, end)
    }

    companion object {

        fun getPaint(): Paint {
            var paint = Paint()
            paint.isAntiAlias = true
            paint.isDither = true
            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER//文本居中
            paint.textSize = px.x(12f)
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 0f
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeJoin = Paint.Join.ROUND
            typeface?.let {
                if (isGlobal) {
                    paint.setTypeface(it)//全局应用自定义字体
                }
            }
            return paint
        }

        fun getPaint(typeface: Typeface?): Paint {
            var paint = getPaint()
            paint.typeface = typeface
            return paint
        }

        //获取自定义字体画笔
        fun getPaintTypefaceFromAsset(path: String): Paint {
            var paint = getPaint()
            paint.typeface = getTypefaceFromAsset(path)
            return paint
        }

        fun getPaintTypefaceFromFile(path: String): Paint {
            var paint = getPaint()
            paint.typeface = getTypefaceFromFile(path)
            return paint
        }

        var isGlobal = false//是否应用全局字体，默认false
        fun isGlobal(isGlobal: Boolean = true) {
            this.isGlobal = isGlobal
        }

        var typeface: Typeface? = null//自定义全局字体
        fun typeface(typeface: Typeface?) {
            this.typeface = typeface
        }

        /**
         * path字体路径，来自assets目录 如："fonts/ALIHYAIHEI.TTF"
         */
        fun getTypefaceFromAsset(path: String): Typeface {
            return Typeface.createFromAsset(BaseApplication.getInstance().getResources().getAssets(), path)//字体必须拷贝在assets文件里
        }

        /**
         * path字体完整路径，来自存储卡。
         */
        fun getTypefaceFromFile(path: String): Typeface {
            return Typeface.createFromFile(path)//字体必须拷贝在assets文件里
        }

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
         * propertyName 属性名称
         * repeatCount  动画次数,从0开始。0表示一次，1表示两次。Integer.MAX_VALUE是最大值。
         * duration  动画时间，单位毫秒。1000表示一秒。
         * value 可变参数。属性的变化值
         * AnimatorUpdateListener 动画监听，返回当前变化的属性值。
         */
        fun ofFloat(view: View, propertyName: String, repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
            var objectAnimator = ObjectAnimator.ofFloat(view, propertyName.trim(), *value)
            if (repeatCount >= Int.MAX_VALUE) {
                objectAnimator.repeatCount = Int.MAX_VALUE - 1//防止Int.MAX_VALUE无效。
            } else {
                objectAnimator.repeatCount = repeatCount
            }
            objectAnimator.duration = duration
            objectAnimator.interpolator = LinearInterpolator()//线性变化，平均变化
            objectAnimator.addUpdateListener {
                var value = it.getAnimatedValue(propertyName.trim())
                value?.let {
                    view.invalidate()//fixme 不停的自我刷新，省去了set里面进去刷新。
                    AnimatorUpdateListener?.let {
                        it(value as Float)
                    }
                }
            }
            objectAnimator.start()//fixme 放心吧。多个属性动画可以同时进行。不要使用AnimatorSet，8.0系统不支持。
            return objectAnimator
        }

        fun ofInt(view: View, propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
            var objectAnimator = ObjectAnimator.ofInt(view, propertyName.trim(), *value)
            if (repeatCount >= Int.MAX_VALUE) {
                objectAnimator.repeatCount = Int.MAX_VALUE - 1//防止Int.MAX_VALUE无效。
            } else {
                objectAnimator.repeatCount = repeatCount
            }
            objectAnimator.duration = duration
            objectAnimator.interpolator = LinearInterpolator()//线性变化，平均变化
            objectAnimator.addUpdateListener {
                var value = it.getAnimatedValue(propertyName.trim())
                value?.let {
                    view.invalidate()
                    AnimatorUpdateListener?.let {
                        it(value as Int)
                    }
                }
            }
            objectAnimator.start()
            return objectAnimator
        }

        /**
         * 封装位置移动动画
         * toX,toY相对于父容器的移动的目标坐标点。
         * durationMillis 动画时间，单位毫秒。
         * end 回调，动画结束后，返回当前的位置坐标。[位置会实际发生改变]
         * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
         */
        fun translateAnimation(view: View, toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null): TranslateAnimation {
            var toXDelta = toX - view.x//动画结束的点离当前View X坐标上的差值
            var toYDelta = toY - view.y//动画开始的点离当前View Y坐标上的差值
            var translateAnimation = TranslateAnimation(0f, toXDelta, 0f, toYDelta)
            //动画时长,单位毫秒
            translateAnimation.setDuration(durationMillis)
            translateAnimation.interpolator = LinearInterpolator()//平滑，速度平均移动
            //view位置停留在动画结束的位置
            translateAnimation.setFillAfter(false)
            translateAnimation.repeatCount = 0//动画次数。0代表一次。
            translateAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {}
                override fun onAnimationEnd(p0: Animation?) {
                    //fixme 动画结束【手动更改控件实际位置】
                    //fixme 注意，位置属性不能出现centerInParent(),centerHorizontally()等设置。只能用外补丁来控制位置。
                    //fixme 除了外补丁，不要出现其他多余的位置属性。不然位置设置无法生效。
                    view.layoutParams.apply {
                        if (this is ViewGroup.MarginLayoutParams) {
                            view.clearAnimation()//动画清除，防止动画结束时抖动
                            setMargins(toX.toInt(), toY.toInt(), rightMargin, bottomMargin)
                            view.requestLayout()
                            end?.let {
                                it(toX, toY)
                            }
                        }
                    }
                }

                override fun onAnimationStart(p0: Animation?) {}
            })
            //开始动画
            view.startAnimation(translateAnimation)
            return translateAnimation
        }

    }

}