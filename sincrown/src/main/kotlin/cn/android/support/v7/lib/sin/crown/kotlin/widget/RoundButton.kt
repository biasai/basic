package cn.android.support.v7.lib.sin.crown.kotlin.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.util.AttributeSet
import android.graphics.RectF
import android.graphics.drawable.*
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import cn.android.support.v7.lib.sin.crown.kotlin.R
import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseView
import cn.android.support.v7.lib.sin.crown.kotlin.common.Toast
import cn.android.support.v7.lib.sin.crown.kotlin.common.px
import cn.android.support.v7.lib.sin.crown.kotlin.utils.KTimerUtils
import cn.android.support.v7.lib.sin.crown.kotlin.utils.SelectorUtils
import cn.android.support.v7.lib.sin.crown.utils.RegexUtils
import org.jetbrains.anko.textColor

//            使用案例
//            var button=RoundButton(activity!!).apply {
//                selectorColor(Color.WHITE,Color.RED)
//                text="按钮"
//                tel(tel)
//                code(code)
//                password(pass)
//                confirmPassword(pass2)
//                addEditText(edit)
//                fixme onError会把按钮初始化为不可用，即：isEnabled=false
//                onError { error, eidt ->
//                    //错误信息，只有数据正确时才会处罚点击事件
//                    Toast.show(error)
//                }
//                onClick {
//                    Toast.show("点击事件")
//                }
//            }
//            addView(button)

/**
 * 自定义圆角按钮（自带数据校验）
 * 自带触摸阴影效果，支持波纹点击效果
 * Created by 彭治铭 on 2018/5/20.
 */
open class RoundButton : Button {

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

    //手机号
    private var tel: EditText? = null

    fun tel(tel: EditText?) {
        this.tel = tel
        tel?.apply {
            addTextChanged(this)
        }
    }

    //密码
    private var password: EditText? = null

    fun password(password: EditText?) {
        this.password = password
        password?.apply {
            addTextChanged(this)
        }
    }

    //再次确认密码
    private var confirmPassword: EditText? = null

    fun confirmPassword(confirmPassword: EditText?) {
        this.confirmPassword = confirmPassword
        confirmPassword?.apply {
            addTextChanged(this)
        }
    }

    //验证码
    private var code: EditText? = null

    fun code(code: EditText?) {
        this.code = code
        code?.apply {
            addTextChanged(this)
        }
    }

    //正确的验证码，获取验证码后，需要手动复制。会和验证码文本框的值做比较
    var realCode: String? = null

    fun realCode(realCode: String?) {
        this.realCode = realCode
    }

    //身份证号
    private var idCard: EditText? = null

    fun idCard(idCard: EditText?) {
        this.idCard = idCard
        idCard?.apply {
            addTextChanged(this)
        }
    }

    //银行卡号
    private var bankNo: EditText? = null

    fun bankNo(bankNo: EditText?) {
        this.bankNo = bankNo
        bankNo?.apply {
            addTextChanged(this)
        }
    }

    //邮箱
    private var email: EditText? = null

    fun email(email: EditText?) {
        this.email = email
        email?.apply {
            addTextChanged(this)
        }
    }

    //fixme 其它普通的文本输入框集合
    private var editTextList = mutableListOf<EditText>()

    //普通文本框
    fun addEditText(editText: EditText?) {
        editText?.apply {
            addTextChanged(this)
            editTextList.add(this)
        }
    }

    //文本监听[主要监听是否为空]
    private fun addTextChanged(editText: EditText?) {
        editText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    isEnable()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
        }
    }

    //判断按钮是否可用
    private var isEnable = true

    private fun isEnable() {
        isEnable = true
        isEmpty(tel)
        isEmpty(password)
        isEmpty(confirmPassword)
        isEmpty(code)
        isEmpty(email)
        isEmpty(idCard)
        isEmpty(bankNo)
        //普陀文本框集合，主要就是判断是否为空。
        editTextList.forEach() {
            isEmpty(it)
        }
        //可用状态和选中状态绑定
        this.isEnabled = isEnable
        isSelected = isEnable
    }

    //判断文本是否为空
    private fun isEmpty(editText: EditText?) {
        editText?.apply {
            if (this.text.toString().trim().length <= 0) {
                isEnable = false//空,不可用
            }
        }
    }

    var errorEditText: EditText? = null
    //fixme 错误信息回调函数，交给调用者去实现。返回校验错误信息
    var onError: ((error: String, eidt: RoundEditText?) -> Unit)? = null

    //返回错误信息，和错误文本框
    fun onError(onError: (error: String, eidt: RoundEditText?) -> Unit) {
        isEnabled = false//默认不可用，只有所有数据不为空的情况下才可用。
        this.onError = onError
    }

    //fixme 点击事件
    fun onClick(onClick: () -> Unit) {
        setOnClickListener {
            var isRegular = true//判断数据是否正确，默认正确。
            onError?.let {
                var error: String? = onRegular()
                error?.apply {
                    isRegular = false//数据错误。
                    if (errorEditText != null) {
                        if (errorEditText is RoundEditText) {
                            it(this, errorEditText as RoundEditText)
                        } else {
                            it(this, null)
                        }
                    } else {
                        it(this, null)
                    }
                }
            }
            if (isRegular) {
                onClick()//fixme 如果数据校验正确，才会触发点击事件
            }
        }
    }

    //fixme 正则判断，返回错误的信息，如果正确则返回为空。
    open fun onRegular(): String? {
        tel?.apply {
            if (!RegexUtils.getInstance().isMobileNO(this.text.toString().trim())) {
                errorEditText = this
                return "手机号格式不正确"
            }else if (this is RoundEditText){
                this.onError(false)//正确，就不显示错误图片。
            }
        }
        var p1 = password?.text.toString()
        var p2 = confirmPassword?.text.toString()
        if (p1 != null && p2 != null && (!p1.equals(p2))) {
            errorEditText = confirmPassword
            return "两次输入的密码不一致"
        }else if (this is RoundEditText){
            this.onError(false)
        }

        code?.apply {
            //真实验证码如果不为空，就进行判断
            realCode?.let {
                if (!this.text.toString().trim().equals(it.trim())) {
                    errorEditText = this
                    return "验证码不正确"
                }else if (this is RoundEditText){
                    this.onError(false)
                }
            }
        }

        email?.apply {
            if (!RegexUtils.getInstance().isEmail(this.text.toString().trim())) {
                errorEditText = this
                return "邮箱格式不正确"
            }else if (this is RoundEditText){
                this.onError(false)
            }
        }

        idCard?.apply {
            if (!RegexUtils.getInstance().isIdCard(this.text.toString().trim())) {
                errorEditText = this
                return "身份证号格式不正确"
            }else if (this is RoundEditText){
                this.onError(false)
            }
        }

        bankNo?.apply {
            if (!RegexUtils.getInstance().isBankCard(this.text.toString().trim())) {
                errorEditText = this
                return "银行卡号格式不正确"
            }else if (this is RoundEditText){
                this.onError(false)
            }
        }
        errorEditText = null
        return null
    }


    var bindView: View? = null//状态绑定的View
        set(value) {
            field = value
            if (value != null) {
                if (value is BaseView) {
                    if (value.bindView == null) {
                        value.bindView = this//相互绑定
                    }
                } else if (value is RoundButton) {
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
        var b = super.dispatchTouchEvent(event)
        //防止点击事件冲突。所以。一定要放到super()后面。
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

    //fixme 初始化=================================================================================
    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)//开启硬件加速
        BaseView.typeface?.let {
            if (BaseView.isGlobal) {
                typeface = it//fixme 设置全局自定义字体
            }
        }
        textSize = px.textSizeX(30f)
        textColor = Color.WHITE
        gravity = Gravity.CENTER
    }

    var afterDrawRadius = true//fixme 圆角边框是否最后画。默认最后画。不管是先画，还是后面。总之都在背景上面。背景最底层。
    override fun draw(canvas: Canvas?) {
        if (Build.VERSION.SDK_INT <= 19 && (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0 || all_radius > 0)) {//19是4.4系统。这个系统已经很少了。基本上也快淘汰了。
            //防止4.4及以下的系统。背景出现透明黑框。
            //只能解决。父容器有背景颜色的时候。如果没有背景色。那就没有办法了。
            var color = BaseView.getParentColor(this)
            canvas?.drawColor(color)//必不可少，不能为透明色。
            canvas?.saveLayerAlpha(RectF(0f, 0f, w.toFloat(), h.toFloat()), 255, Canvas.ALL_SAVE_FLAG)//必不可少，解决透明黑框。
        }
        super.draw(canvas)
        if (!afterDrawRadius) {
            drawRadius(canvas)
        }
        canvas?.let {
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
            //Log.e("test","all_radius：\t"+all_radius+"\tright_top：\t"+right_top)
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
            paint.strokeCap = Paint.Cap.BUTT
            paint.strokeJoin = Paint.Join.MITER
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
            if (left_top > 0 || left_bottom > 0 || right_top > 0 || right_bottom > 0 || all_radius > 0) {
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
    open fun draw(draw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): RoundButton {
        this.draw = draw
        postInvalidate()//刷新
        return this
    }

    //画自己【onDraw在draw()的流程里面，即在它的前面执行】
    var onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null

    //画自己
    fun onDraw_(onDraw: ((canvas: Canvas, paint: Paint) -> Unit)? = null): RoundButton {
        this.onDraw = onDraw
        postInvalidate()//刷新
        return this
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
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

    fun selectorDrawable(NormalDrawable: Drawable?, PressDrawable: Drawable?, SelectDrawable: Drawable? = PressDrawable) {
        SelectorUtils.selectorDrawable(this, NormalDrawable, PressDrawable, SelectDrawable)
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

    //fixme 防止和以下方法冲突，all_radius不要设置默认值
    fun selectorRippleDrawable(NormalColor: String?, PressColor: String?, all_radius: Float) {
        SelectorUtils.selectorRippleDrawable(this, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(PressColor), left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }

    /**
     * 波纹点击效果
     * all_radius 圆角
     */
    fun selectorRippleDrawable(NormalColor: Int?, PressColor: Int?, all_radius: Float) {
        SelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, PressColor, left_top = all_radius, right_top = all_radius, right_bottom = all_radius, left_bottom = all_radius)
    }

    fun selectorRippleDrawable(NormalColor: String?, PressColor: String?, SelectColor: String? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        SelectorUtils.selectorRippleDrawable(this, Color.parseColor(NormalColor), Color.parseColor(PressColor), Color.parseColor(SelectColor), strokeWidth, strokeColor, all_radius, left_top, right_top, right_bottom, left_bottom)
    }

    /**
     * 波纹点击效果
     * NormalColor 正常背景颜色值
     * PressColor  按下正常背景颜色值 ,也可以理解为波纹点击颜色
     * SelectColor 选中(默认和按下相同)背景颜色值
     */
    fun selectorRippleDrawable(NormalColor: Int?, PressColor: Int?, SelectColor: Int? = PressColor, strokeWidth: Int = 0, strokeColor: Int = Color.TRANSPARENT, all_radius: Float = this.all_radius, left_top: Float = this.left_top, right_top: Float = this.right_top, right_bottom: Float = this.right_bottom, left_bottom: Float = this.left_bottom) {
        //Log.e("test","all:\t"+all_radius+"\tleft:\t"+left_top+"\tright:\t"+right_top+"\t"+right_bottom)
        SelectorUtils.selectorRippleDrawable(this, NormalColor, PressColor, SelectColor, strokeWidth, strokeColor, all_radius, left_top, right_top, right_bottom, left_bottom)
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