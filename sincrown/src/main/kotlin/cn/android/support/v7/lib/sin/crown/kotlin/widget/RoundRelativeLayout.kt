package cn.android.support.v7.lib.sin.crown.kotlin.widget

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import cn.android.support.v7.lib.sin.crown.kotlin.R
import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseView
import cn.android.support.v7.lib.sin.crown.kotlin.common.px
import cn.android.support.v7.lib.sin.crown.kotlin.https.Bitmaps
import cn.android.support.v7.lib.sin.crown.kotlin.utils.KTimerUtils
import cn.android.support.v7.lib.sin.crown.kotlin.utils.SelectorUtils
import cn.android.support.v7.lib.sin.crown.utils.AssetsUtils
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.runOnUiThread


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

    //fixme selectorDrawable(R.mipmap.p_dont_agree,null, R.mipmap.p_agree)
    //fixme 注意，如果要用选中状态，触摸状态最好设置为null空。不会有卡顿冲突。
    //重写选中状态。isSelected=true。选中状态。一定要手动调用。
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        bindView?.let {
            if (it.isSelected != isSelected) {
                it?.isSelected = isSelected//选中状态
            }
        }
        onSelectChangedList.forEach {
            it?.let {
                it(selected)//选中监听
            }
        }
    }

    //fixme 监听选中状态。防止多个监听事件冲突，所以添加事件数组。
    private var onSelectChanged: ((selected: Boolean) -> Unit)? = null
    private var onSelectChangedList = mutableListOf<((selected: Boolean) -> Unit)?>()
    fun addSelected(onSelectChanged: ((selected: Boolean) -> Unit)) {
        onSelectChanged.let {
            onSelectChangedList?.add(it)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        var b = super.dispatchTouchEvent(event)
        //防止点击事件冲突。所以。一定要放到super()后面。
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                    bindView?.isPressed = true//按下状态
                    isPressed = true
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    bindView?.isPressed = false
                    isPressed = false
                    invalidate()
                }
                MotionEvent.ACTION_CANCEL -> {
                    //其他异常
                    bindView?.isPressed = false
                    invalidate()
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
    var strokeGradientStartColor = Color.TRANSPARENT//渐变开始颜色
    var strokeGradientEndColor = Color.TRANSPARENT//渐变结束颜色
    //fixme 渐变颜色数组值【均匀渐变】，gradientColors优先
    var strokeGradientColors: IntArray? = null
    var ORIENTATION_VERTICAL = 0//垂直
    var ORIENTATION_HORIZONTAL = 1//水平
    var strokeGradientOritation = ORIENTATION_HORIZONTAL//渐变颜色方向，默认水平

    fun strokeGradientColors(vararg color: Int) {
        strokeGradientColors = color
    }

    fun strokeGradientColors(vararg color: String) {
        strokeGradientColors = IntArray(color.size)
        strokeGradientColors?.apply {
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

    //fixme 清空原始背景
    fun clearOriBackground() {
        if (Build.VERSION.SDK_INT >= 16) {
            backgroundColor = Color.TRANSPARENT
            background = null
        } else {
            backgroundColor = Color.TRANSPARENT
            backgroundDrawable = null
        }
    }

    //这个背景图片，会铺满整个控件
    private var autoUrlBg: Bitmap? = null//fixme 自定义网络背景图片,对图片是否为空，是否释放，做了判断。防止奔溃。比原生的背景图片更安全。

    fun autoUrlBg(bitmap: Bitmap?) {
        this.autoUrlBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoUrlBg(resId: Int, isRGB_565: Boolean = false) {
        this.autoUrlBg = AssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoUrlAssetsBg(assetsPath: String, isRGB_565: Boolean = false) {
        this.autoUrlBg = AssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    /**
     * url 网络图片地址
     * isLoad 是否显示进度条，默认不显示
     * isRepeat 是否允许重复加载，默认允许
     */
    fun autoUrlBg(url: String?, isLoad: Boolean = false,isRepeat:Boolean=true) {
        if (isLoad && context != null && context is Activity) {
            Bitmaps(url).optionsRGB_565(false).showLoad(context as Activity).repeat(isRepeat).get() {
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        invalidate()
                    }
                }
            }
        } else {
            Bitmaps(url).optionsRGB_565(false).showLoad(false).repeat(isRepeat).get() {
                autoUrlBg = it
                if (context != null && context is Activity) {
                    context.runOnUiThread {
                        invalidate()
                    }
                }
            }
        }
    }

    /**
     * 重新自定义背景图片(为了适配正确，位图最好都放在nodpi文件夹里。)
     */
    private var autoDefaultBg: Bitmap? = null//fixme 默认图片

    fun autoDefaultBg(bitmap: Bitmap?) {
        this.autoDefaultBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoDefaultBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = AssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = px.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }
    }

    fun autoDefaultBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg = AssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoDefaultBg?.let {
            autoDefaultBg = px.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }
    }

    private var autoPressBg: Bitmap? = null//fixme 按下图片
    fun autoPressBg(bitmap: Bitmap?) {
        this.autoPressBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoPressBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = AssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoPressBg?.let {
            autoPressBg = px.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }
        isClickable = true//具备点击能力
    }

    fun autoPressBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoPressBg = AssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoPressBg?.let {
            autoPressBg = px.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }
        isClickable = true//具备点击能力
    }

    private var autoSelectBg: Bitmap? = null//fixme 选中图片（优先级最高）
    fun autoSelectBg(bitmap: Bitmap?) {
        this.autoSelectBg = bitmap
        if (context != null && context is Activity) {
            context.runOnUiThread {
                invalidate()
            }
        }
    }

    fun autoSelectBg(resId: Int, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = AssetsUtils.getInstance().getBitmapFromAssets(null, resId, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = px.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }
        isClickable = true//具备点击能力
    }

    fun autoSelectBg(assetsPath: String, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoSelectBg = AssetsUtils.getInstance().getBitmapFromAssets(assetsPath, 0, isRGB_565)
        autoSelectBg?.let {
            autoSelectBg = px.xBitmap(it, width, height)//自动适配
        }
        if (isAutoWH) {
            requestLayout()
        }
        isClickable = true//具备点击能力
    }

    //fixme 防止触摸状态和选中状态冲突，会出现一闪的情况。把触摸状态制空。
    //fixme autoBg(R.mipmap.p_second_gou_gay,null, R.mipmap.p_second_gou_blue)
    fun autoBg(default: Int, press: Int? = default, select: Int? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    fun autoBg(default: String, press: String? = default, select: String? = press, width: Int = 0, height: Int = 0, isRGB_565: Boolean = false) {
        autoDefaultBg(default, width, height, isRGB_565)
        if (press == default || press.equals(default)) {
            autoPressBg = autoDefaultBg
        } else {
            press?.apply {
                autoPressBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
        if (press == select || press.equals(select)) {
            autoSelectBg = autoPressBg
        } else {
            select?.apply {
                autoSelectBg(this, width, height, isRGB_565)
                isClickable = true//具备点击能力
            }
        }
    }

    var isAutoWH = true//fixme 控件的宽度和高度是否为自定义位图的宽度和高度。默认是
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = 0
        var h = 0
        if (isAutoWH) {
            autoDefaultBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoPressBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (width > w) {
                        w = width
                    }
                    if (height > h) {
                        h = height
                    }
                }
            }
        }
        if (w > 0 && h > 0) {
            //取自定义位图宽度和高度最大的那个。
            setMeasuredDimension(w, h)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    var autoLeftPadding = 0f//左补丁(负数也有效哦)
    var autoTopPadding = 0f//上补丁
    var isAutoCenter = true//位图是否居中,默认居中
    //画自定义背景
    open fun drawAutoBg(canvas: Canvas) {
        if (w <= 0 || h <= 0) {
            return
        }
        var paint = BaseView.getPaint()
        //网络背景位图（铺满整个背景控件）
        autoUrlBg?.apply {
            if (!isRecycled) {
                if (width != w || height != h) {
                    autoUrlBg = px.xBitmap(this, w, h)//位图和控件拉伸到一样大小
                    autoUrlBg?.apply {
                        if (!isRecycled) {
                            canvas.drawBitmap(this, 0f, 0f, paint)
                        }
                    }
                } else {
                    canvas.drawBitmap(this, 0f, 0f, paint)
                }
            }
        }
        //Log.e("test", "isSelected:\t" + isSelected + "\tisPress：\t" + isPressed)
        if (isSelected && autoSelectBg != null) {
            //选中状态图片,优先级最高
            autoSelectBg?.apply {
                if (!isRecycled) {
                    if (isAutoCenter) {
                        canvas.drawBitmap(this, px.centerBitmapX(this, w.toFloat()), px.centerBitmapY(this, h.toFloat()), paint)
                    } else {
                        canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                    }
                }
            }
        } else {
            if (isPressed && autoPressBg != null) {
                //按下状态
                autoPressBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, px.centerBitmapX(this, w.toFloat()), px.centerBitmapY(this, h.toFloat()), paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            } else {
                //普通状态
                autoDefaultBg?.apply {
                    if (!isRecycled) {
                        if (isAutoCenter) {
                            canvas.drawBitmap(this, px.centerBitmapX(this, w.toFloat()), px.centerBitmapY(this, h.toFloat()), paint)
                        } else {
                            canvas.drawBitmap(this, autoLeftPadding, autoTopPadding, paint)
                        }
                    }
                }
            }
        }
    }

    //释放位图
    fun recycle() {
        autoDefaultBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoDefaultBg = null
        autoPressBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoPressBg = null
        autoSelectBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoSelectBg = null
        autoUrlBg?.apply {
            if (!isRecycled) {
                recycle()
            }
        }
        autoUrlBg = null
        System.gc()//提醒内存回收
    }


    //fixme 什么都不做，交给子类去实现绘图
    //fixme 之所以会有这个方法。是为了保证自定义的 draw和onDraw的执行顺序。始终是在最后。
    protected open fun draw2(canvas: Canvas, paint: Paint) {}

    var afterDrawRadius = true//fixme 圆角边框是否最后画。默认最后画。不管是先画，还是后面。总之都在背景上面。背景最底层。
    override fun dispatchDraw(canvas: Canvas?) {
        if (Build.VERSION.SDK_INT <= 19 && (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0 || all_radius > 0)) {//19是4.4系统。这个系统已经很少了。基本上也快淘汰了。
            //防止4.4及以下的系统。背景出现透明黑框。
            //只能解决。父容器有背景颜色的时候。如果没有背景色。那就没有办法了。
            var color = BaseView.getParentColor(this)
            canvas?.drawColor(color)//必不可少，不能为透明色。
            canvas?.saveLayerAlpha(RectF(0f, 0f, w.toFloat(), h.toFloat()), 255, Canvas.ALL_SAVE_FLAG)//必不可少，解决透明黑框。
        }
        //背景
        canvas?.let {
            onDraw?.let {
                it(canvas, BaseView.getPaint())
            }
        }

        //画自定义背景(在super的后面，不然会遮挡子控件)
        canvas?.let {
            drawAutoBg(it)
        }
        super.dispatchDraw(canvas)

        if (!afterDrawRadius) {
            drawRadius(canvas)
        }
        //前景
        canvas?.let {
            draw2(it, BaseView.getPaint())
            draw?.let {
                it(canvas, BaseView.getPaint())
            }
            //画水平进度
            drawHorizontalProgress?.let {
                it(canvas, getPaint(), w * horizontalProgress / 100f)
            }
            //画垂直进度
            drawVerticalProgress?.let {
                it(canvas, getPaint(), h - h * verticalProgress / 100f)//方向从下往上。
            }
        }
        if (afterDrawRadius) {
            drawRadius(canvas)
        }
    }

    //画边框，圆角
    fun drawRadius(canvas: Canvas?) {
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
            paint.strokeCap=Paint.Cap.BUTT
            paint.strokeJoin=Paint.Join.MITER
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
            if(left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0 || all_radius > 0){
                canvas.drawPath(path, paint)
            }
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
                if (strokeGradientColors != null) {
                    if (strokeGradientOritation == ORIENTATION_HORIZONTAL) {
                        //水平渐变
                        linearGradient = LinearGradient(0f, 0f, w.toFloat(), h / 2f, strokeGradientColors, null, Shader.TileMode.CLAMP)
                    } else {
                        //垂直渐变
                        linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), strokeGradientColors, null, Shader.TileMode.CLAMP)
                    }
                } else {
                    if (!(strokeGradientStartColor == Color.TRANSPARENT && strokeGradientEndColor == Color.TRANSPARENT)) {
                        if (strokeGradientOritation == ORIENTATION_HORIZONTAL) {
                            linearGradient = LinearGradient(0f, 0f, w.toFloat(), h / 2f, strokeGradientStartColor, strokeGradientEndColor, Shader.TileMode.CLAMP)
                        } else {
                            linearGradient = LinearGradient(0f, 0f, 0f, h.toFloat(), strokeGradientStartColor, strokeGradientEndColor, Shader.TileMode.CLAMP)
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

    //fixme 颜色,调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorColor(NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor) {
        if (Build.VERSION.SDK_INT <= 19) {
            //fixme 防止按钮圆角不正确，必须对每个圆角都使用GradientDrawable控制。
            SelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, all_radius = this.all_radius, left_top = this.left_top, right_top = this.right_top, right_bottom = this.right_bottom, left_bottom = this.left_bottom, isRipple = false)

        } else {
            SelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
        }

    }

    //fixme 颜色,调用之前一定要先设置圆角的属性。不然圆角不正确
    fun selectorColor(NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor) {
        if (Build.VERSION.SDK_INT <= 19) {
            SelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, all_radius = this.all_radius, left_top = this.left_top, right_top = this.right_top, right_bottom = this.right_bottom, left_bottom = this.left_bottom, isRipple = false)

        } else {
            SelectorUtils.selectorColor(this, NormalColor, PressColor, SelectColor)
        }

    }

    //字体颜色
    fun selectorTextColor(NormalColor: Int, PressColor: Int, SelectColor: Int = PressColor) {
        SelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    fun selectorTextColor(NormalColor: String, PressColor: String, SelectColor: String = PressColor) {
        SelectorUtils.selectorTextColor(this, NormalColor, PressColor, SelectColor)
    }

    //fixme 防止和以下方法冲突，all_radius不要设置默认值
    fun selectorRippleDrawable(NormalColor: String?, PressColor: String?, all_radius: Float) {
        SelectorUtils.selectorRippleDrawable(this, Color.parseColor(NormalColor), Color.parseColor(PressColor),  Color.parseColor(PressColor), left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }
    /**
     * 波纹点击效果
     * all_radius 圆角
     */
    fun selectorRippleDrawable(NormalColor: Int?, PressColor: Int?, all_radius: Float) {
        SelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }

    fun selectorRippleDrawable(NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        SelectorUtils.selectorRippleDrawable(this,Color.parseColor(NormalColor),Color.parseColor(PressColor),Color.parseColor(SelectColor),strokeWidth,strokeColor,all_radius,left_top,right_top,right_bottom,left_bottom)
    }

    /**
     * 波纹点击效果
     * NormalColor 正常背景颜色值
     * PressColor  按下正常背景颜色值 ,也可以理解为波纹点击颜色
     * SelectColor 选中(默认和按下相同)背景颜色值
     */
    fun selectorRippleDrawable(NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        SelectorUtils.selectorRippleDrawable(this,NormalColor,PressColor,SelectColor,strokeWidth,strokeColor,all_radius,left_top,right_top,right_bottom,left_bottom)
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
        var objectAnimator = BaseView.ofFloat(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        objectAnimates.add(objectAnimator)
        return objectAnimator
    }

    fun ofInt(propertyName: String, repeatCount: Int, duration: Long, vararg value: Int, AnimatorUpdateListener: ((values: Int) -> Unit)? = null): ObjectAnimator {
        var objectAnimator = BaseView.ofInt(this, propertyName, repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
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
     * end 回调，动画结束后，返回当前的位置坐标。[位置会实际发生改变]
     * fixme 注意，如果有多个控件同时开启动画，移动的时候可能会卡顿和抖动现象。多个控件最好不要同时进行动画，太耗性能了。
     */
    fun translateAnimation(toX: Float, toY: Float, durationMillis: Long = 300, end: ((x: Float, y: Float) -> Unit)? = null): TranslateAnimation {
        return BaseView.translateAnimation(this, toX, toY, durationMillis, end)
    }

    var objectAnimatorScaleX: ObjectAnimator? = null
    var objectAnimatorScaleY: ObjectAnimator? = null
    //缩放动画(因为有两个属性。就不添加监听了)
    //pivotX,pivotY 变换基准点，默认居中
    fun scale(repeatCount: Int, duration: Long, vararg value: Float, pivotX: Float = w / 2f, pivotY: Float = h / 2f) {
        endScale()
        this.pivotX = pivotX
        this.pivotY = pivotY
        //支持多个属性，同时变化，放心会同时变化的。
        objectAnimatorScaleX = ofFloat("scaleX", repeatCount, duration, *value)
        objectAnimatorScaleY = ofFloat("scaleY", repeatCount, duration, *value)
    }

    //暂停缩放（属性会保持当前的状态）
    fun pauseScale() {
        objectAnimatorScaleX?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
        objectAnimatorScaleY?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
    }

    //继续缩放
    fun resumeScale() {
        objectAnimatorScaleX?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
        objectAnimatorScaleY?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
    }

    //fixme 停止缩放,属性会恢复到原始状态。动画也会结束。
    fun endScale() {
        objectAnimatorScaleX?.let {
            it.end()//fixme 一旦调用了end()属性动画也就结束了，并且属性也会恢复到原始状态。
            objectAnimatorScaleX = null
        }
        objectAnimatorScaleY?.let {
            it.end()
            objectAnimatorScaleY = null
        }
    }

    var objectAnimatorRotation: ObjectAnimator? = null
    //旋转动画
    //pivotX,pivotY 变换基准点，默认居中
    fun rotation(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null, pivotX: Float = w / 2f, pivotY: Float = h / 2f): ObjectAnimator {
        endRotation()
        this.pivotX = pivotX
        this.pivotY = pivotY
        objectAnimatorRotation = ofFloat("rotation", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
        return objectAnimatorRotation!!
    }

    //暂停旋转（属性会保持当前的状态）
    fun pauseRotation() {
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.pause()
            } else {
                it.end()
            }
        }
    }

    //继续旋转
    fun resumeRotation() {
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
        objectAnimatorRotation?.let {
            if (Build.VERSION.SDK_INT >= 19) {
                it.resume()
            } else {
                it.start()//动画会重新开始
            }
        }
    }

    //fixme 停止旋转,属性会恢复到原始状态。动画也会结束。
    fun endRotation() {
        objectAnimatorRotation?.let {
            it.end()//fixme 一旦调用了end()属性动画也就结束了，并且属性也会恢复到原始状态。
            objectAnimatorRotation = null
        }
        objectAnimatorRotation?.let {
            it.end()
            objectAnimatorRotation = null
        }
    }

    var kTimer: KTimerUtils.KTimer? = null
    //定时刷新
    fun refresh(count: Long = 60, unit: Long = 1000, firstUnit: Long = 0, callback: (num: Long) -> Unit): KTimerUtils.KTimer? {
        endRefresh();
        kTimer = KTimerUtils.refreshView(this, count, unit, firstUnit, callback)
        return kTimer
    }

    //暂停
    fun pauseRefresh() {
        kTimer?.let {
            it.pause()
        }
    }

    //判断是否暂停
    fun isPauseRefresh(): Boolean {
        var pause = false
        kTimer?.let {
            pause = it.isPause()
        }
        return pause
    }

    //继续
    fun resumeRefresh() {
        kTimer?.let {
            it.resume()
        }
    }

    //定时器停止
    fun endRefresh() {
        kTimer?.let {
            //一个View就添加一个定时器，防止泄露。
            it.pause()
            it.end()//如果定时器不为空，那一定要先停止之前的定时器。
            kTimer = null
        }
    }

    //水平进度(范围 0F~ 100F),从左往右
    var horizontalProgress = 0f
    fun horizontalProgress(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("horizontalProgress", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    //返回当前水平移动坐标X,
    var drawHorizontalProgress: ((canvas: Canvas, paint: Paint, x: Float) -> Unit)? = null

    fun drawHorizontalProgress(drawHorizontalProgress: ((canvas: Canvas, paint: Paint, x: Float) -> Unit)) {
        this.drawHorizontalProgress = drawHorizontalProgress
    }

    //fixme 垂直进度(范围 0F~ 100F)注意：方向从下往上。0是最底下，100是最顶部。
    var verticalProgress = 0f

    fun verticalProgress(repeatCount: Int, duration: Long, vararg value: Float, AnimatorUpdateListener: ((values: Float) -> Unit)? = null): ObjectAnimator {
        return ofFloat("verticalProgress", repeatCount, duration, *value, AnimatorUpdateListener = AnimatorUpdateListener)
    }

    //返回当前垂直移动坐标Y
    var drawVerticalProgress: ((canvas: Canvas, paint: Paint, y: Float) -> Unit)? = null

    fun drawVerticalProgress(drawVerticalProgress: ((canvas: Canvas, paint: Paint, y: Float) -> Unit)) {
        this.drawVerticalProgress = drawVerticalProgress
    }

}