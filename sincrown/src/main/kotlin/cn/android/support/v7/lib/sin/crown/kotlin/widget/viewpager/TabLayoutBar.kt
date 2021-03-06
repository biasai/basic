package cn.android.support.v7.lib.sin.crown.kotlin.widget.viewpager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewPager
import android.util.AttributeSet

import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseView
import cn.android.support.v7.lib.sin.crown.utils.AssetsUtils
import cn.android.support.v7.lib.sin.crown.utils.ProportionUtils


/**
 * 菜单滑动条
 *
 *
 * android:src="@drawable/crown_p_line_focus" //滑动条为图片
 * android:src="#3388FF" //滑动条为颜色值
 * android:layout_width="match_parent"//是整个控件的宽度，不是滑动条的宽度
 *
 *
 * 或者代码 setTab(滑动位图)，setColor(滑动颜色)
 *
 *
 * mTabLayout?.setViewPager(viewpager)//setViewPager()//与顺序无关，什么时候添加都行。
 *
 * @author 彭治铭
 */
class TabLayoutBar : android.support.v7.widget.AppCompatImageView, ViewPager.OnPageChangeListener {

    private var tab: Bitmap? = null//位图
    private var color = Color.parseColor("#3388FF")//滑动条颜色，默认为蓝色
    var paint: Paint = BaseView.getPaint()
    var count = 0//页面个数
    var w = 0//单个tab的宽度
    var x = 0
    var y = 0
    var offset = 0//x的偏移量，用于图片居中

    //fixme 设置ViewPager
    internal var viewPager: ViewPager? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint.isAntiAlias = true
        paint?.isDither = true
        paint.color = color
        val src = drawable
        if (src == null) {
            return
        } else if (src is ColorDrawable) {
            color = src.color
            paint.color = color
        } else {
            tab = (src as BitmapDrawable).bitmap
            tab = ProportionUtils.getInstance().adapterBitmap(tab)//适配位图。
        }
        setImageBitmap(null)//src颜色和位图都会清空
    }

    //设置滑动位图
    fun setTab(tab: Bitmap) {
        this.tab = tab
        measure()
    }

    fun setTab(resID: Int) {
        this.tab = AssetsUtils.getInstance().getBitmapFromAssets(null, resID, true)
        measure()
    }

    fun getTab(): Bitmap? {
        return tab
    }

    fun getColor(): Int {
        return color
    }

    //设置滑动颜色
    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    fun setColor(color: String) {
        this.color = Color.parseColor(color)
        invalidate()
    }

    //fixme 自定义画布，根据需求。自主实现,返回当前移动的坐标点【优先级最高】
    protected open var draw: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null

    //自定义，重新绘图,
    open fun draw(draw: ((canvas: Canvas, paint: Paint, x: Float, y: Float) -> Unit)? = null) {
        this.draw = draw
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)//画子View，以下方法必须放在下面。不然就被遮挡了。
        if (this.w <= 0 || this.count <= 0) {
            measure()
        }
        if (this.w > 0) {
            if (draw !== null) {//自定义绘图最优先。
                draw!!(canvas, BaseView.getPaint(), x.toFloat(), height / 2f)
            } else {
                if (this.tab != null && !tab!!.isRecycled) {
                    //位图比颜色优先。
                    canvas.drawBitmap(tab!!, x.toFloat(), y.toFloat(), paint)
                } else {
                    paint.color = color
                    val rectF = RectF(x.toFloat(), 0f, (x + w).toFloat(), height.toFloat())
                    canvas.drawRect(rectF, paint)
                }
            }
        }

    }

    fun measure() {
        if (viewPager != null && viewPager!!.adapter != null) {
            count = viewPager!!.adapter!!.count
        }
        if (count > 0) {
            this.w = width / count
            //Log.e("test","getWidth：\t"+getWidth()+"\tw：\t"+w);
            if (tab != null && !tab!!.isRecycled) {
                offset = (this.w - tab!!.width) / 2
                y = (height - tab!!.height) / 2
                x = 0 * w + offset
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measure()//测量
    }

    fun setViewPager(viewPager: ViewPager?) {
        if (viewPager != null) {
            this.viewPager = viewPager
            viewPager.addOnPageChangeListener(this)//addOnPageChangeListener滑动事件监听，可以添加多个监听。不会冲突。
            measure()
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (positionOffset >= 0 && positionOffset <= 1 && count > 0) {
            //positionOffsetPixels是ViewPager的滑动宽度
            //((float)getWidth()/(float) viewPager.getWidth()) 控件和ViewPager的宽度对比，获取真正的滑动距离。
            x = (positionOffsetPixels * (width.toFloat() / viewPager!!.width.toFloat()) / count + (position * w).toFloat() + offset.toFloat()).toInt()
            invalidate()
            //Log.e("test","滑动x：\t"+x+"\tposition:\t"+position+"\toffset:\t"+offset+"\twidth:\t"+getWidth()+"\tw:\t"+w+"\tcount:\t"+count+"\tpositionOffsetPixels:\t"+positionOffsetPixels);
        }
    }

    override fun onPageSelected(position: Int) {
        //x = position * w + offset;//fixme 这里就不要在做计算了。以免计算的不一样。出现卡顿。
        //invalidate();
        //Log.e("test","选中x：\t"+x);
    }

    override fun onPageScrollStateChanged(state: Int) {

    }
}
