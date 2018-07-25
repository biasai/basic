package cn.android.support.v7.lib.sin.crown.kotlin.widget.viewpager

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * 禁止滑动的ViewPager,也可以继承VerticalViewPager
 * 手指不可以滑动，但是可以代码调用setCurrentItem
 *
 * 默认禁止滑动，禁止快速滑动。
 */
class NoScrollViewPager : ViewPager {

    var isScroll: Boolean = false//true 能滑动，false不能滑动。默认不能触摸滑动
    var isFastScroll: Boolean = false//true快速滑动，手指轻轻一划。就到下一页。false不能快速滑动

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    var simpleOnGestureListener: GestureDetector.SimpleOnGestureListener? = null
    var gestureDetectorCompat: GestureDetectorCompat? = null

    init {
        simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (velocityX > 0) {
                    if (currentItem > 0) {
                        //上一页
                        setCurrentItem(currentItem - 1, true)
                    }
                } else if (velocityX < 0) {
                    adapter?.let {
                        if (currentItem < it.count - 1) {
                            //下一页
                            setCurrentItem(currentItem + 1, true)
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }
        gestureDetectorCompat = GestureDetectorCompat(context, simpleOnGestureListener)
        setOnTouchListener { v, event ->
            var b = false
            if (isFastScroll) {
                gestureDetectorCompat?.onTouchEvent(event)//快速滑动。
                b = true
            } else if (!isScroll) {
                b = true//禁止滑动
            }
            b//false 正常，可以滑动。
        }
    }

}
