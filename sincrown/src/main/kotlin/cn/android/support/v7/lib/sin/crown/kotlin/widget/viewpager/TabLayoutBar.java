package cn.android.support.v7.lib.sin.crown.kotlin.widget.viewpager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import cn.android.support.v7.lib.sin.crown.utils.AssetsUtils;
import cn.android.support.v7.lib.sin.crown.utils.ProportionUtils;


/**
 * 菜单滑动条
 * <p>
 * android:src="@drawable/crown_p_line_focus" //滑动条为图片
 * android:src="#3388FF" //滑动条为颜色值
 * android:layout_width="match_parent"//是整个控件的宽度，不是滑动条的宽度
 * <p>
 * 或者代码 setTab(滑动位图)，setColor(滑动颜色)
 * <p>
 * mTabLayout?.setViewPager(viewpager)//setViewPager()//与顺序无关，什么时候添加都行。
 *
 * @author 彭治铭
 */
public class TabLayoutBar extends android.support.v7.widget.AppCompatImageView implements ViewPager.OnPageChangeListener {

    public TabLayoutBar(Context context) {
        super(context);
        init();
    }

    public TabLayoutBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        Drawable src = getDrawable();
        if (src == null) {
            return;
        } else if (src instanceof ColorDrawable) {
            ColorDrawable colordDrawable = (ColorDrawable) src;
            color = colordDrawable.getColor();
            paint.setColor(color);
        } else {
            tab = ((BitmapDrawable) src).getBitmap();
            tab = ProportionUtils.getInstance().adapterBitmap(tab);//适配位图。
        }
        setImageBitmap(null);//src颜色和位图都会清空
    }

    private Bitmap tab;//位图
    private int color = Color.parseColor("#3388FF");//滑动条颜色，默认为蓝色
    public Paint paint;
    public int count = 0;//页面个数
    public int w = 0;//单个tab的宽度
    public int x = 0;
    public int y = 0;
    public int offset = 0;//x的偏移量，用于图片居中

    //设置滑动位图
    public void setTab(Bitmap tab) {
        this.tab = tab;
        measure();
    }

    public void setTab(int resID) {
        this.tab = AssetsUtils.getInstance().getBitmapFromAssets(null, resID, true);
        measure();
    }

    public Bitmap getTab() {
        return tab;
    }

    public int getColor() {
        return color;
    }

    //设置滑动颜色
    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setColor(String color) {
        this.color = Color.parseColor(color);
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);//画子View，以下方法必须放在下面。不然就被遮挡了。
        if (this.w <= 0 || this.count <= 0) {
            measure();
        }
        if (this.w > 0) {
            if (this.tab != null && !tab.isRecycled()) {
                //位图比颜色有效。
                canvas.drawBitmap(tab, x, y, paint);
            } else {
                paint.setColor(color);
                RectF rectF = new RectF(x, 0, x + w, getHeight());
                canvas.drawRect(rectF, paint);
            }
        }

    }

    public void measure() {
        if (viewPager != null&&viewPager.getAdapter()!=null) {
            count = viewPager.getAdapter().getCount();
        }
        if (count > 0) {
            this.w = getWidth() / count;
            //Log.e("test","getWidth：\t"+getWidth()+"\tw：\t"+w);
            if (tab != null && !tab.isRecycled()) {
                offset = (this.w - tab.getWidth()) / 2;
                y = (getHeight() - tab.getHeight()) / 2;
                x = 0 * w + offset;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        measure();//测量
    }

    //fixme 设置ViewPager
    ViewPager viewPager = null;

    public void setViewPager(ViewPager viewPager) {
        if (viewPager != null) {
            this.viewPager = viewPager;
            viewPager.addOnPageChangeListener(this);//addOnPageChangeListener滑动事件监听，可以添加多个监听。不会冲突。
            measure();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset >= 0 && positionOffset <= 1 && count > 0) {
            //positionOffsetPixels是ViewPager的滑动宽度
            //((float)getWidth()/(float) viewPager.getWidth()) 控件和ViewPager的宽度对比，获取真正的滑动距离。
            x = (int) (positionOffsetPixels*((float)getWidth()/(float) viewPager.getWidth()) / count + (position * w) + offset);
            invalidate();
            //Log.e("test","滑动x：\t"+x+"\tposition:\t"+position+"\toffset:\t"+offset+"\twidth:\t"+getWidth()+"\tw:\t"+w+"\tcount:\t"+count+"\tpositionOffsetPixels:\t"+positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        x = position * w + offset;
        invalidate();
        //Log.e("test","选中x：\t"+x);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
