package cn.android.support.v7.lib.sin.crown.kotlin.popu

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup
import android.widget.PopupWindow
import cn.android.support.v7.lib.sin.crown.R
import cn.android.support.v7.lib.sin.crown.utils.PopuWindowUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick

//        //数据
//        var list = mutableListOf<String>()
//        var sp = SpinnerPop(this, list)
//        //创建item视图,参数为下标position
//        sp.onCreateView {
//            UI { }.view
//        }
//        //视图刷新[业务逻辑都在这处理]，返回 视图itemView和下标postion
//        sp.onBindView { itemView, position -> }

//传入的list和原有list是同一个对象，已经绑定。只要不重新赋值=，就会一直相互影响。
class SpinnerPop(var context: Context, var list: MutableList<*>) {
    var pop: PopupWindow? = null
    var recyclerView: RecyclerView? = null
    var contentView: View? = null//最外层组件容器

    //fixme 创建itemView视图 [需要自动手动实现]
    fun onCreateView(itemView: (positon: Int) -> View) {
        contentView = context.UI {
            verticalLayout {
                var layoutParams = ViewGroup.LayoutParams(wrapContent, matchParent)
                setLayoutParams(layoutParams)
                recyclerView = recyclerView {
                    var linearLayoutManager = LinearLayoutManager(context)
                    linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                    layoutManager = linearLayoutManager
                    adapter = MyAdapter(this@SpinnerPop, itemView)

                    setOverScrollMode(OVER_SCROLL_NEVER);//设置滑动到边缘时无效果模式
                    setVerticalScrollBarEnabled(false);//滚动条隐藏

                }.lparams {
                    width = wrapContent
                    height = wrapContent
                }
                //fixme 填充popuwindow的底部
                view {
                    onClick {
                        //关闭
                        dismiss()
                    }
                }.lparams {
                    width = matchParent
                    height = matchParent
                }
            }
        }.view
        pop = PopuWindowUtils.getInstance().showPopuWindow(contentView, R.style.crown_window_alpha_scale_drop)
    }

    var onBindView: ((itemView: View, position: Int) -> Unit)? = null
    //fixme 刷新视图，数据展示+业务逻辑+点击事件 都在这里处理 [需要自动手动实现]
    fun onBindView(onBindView: (itemView: View, position: Int) -> Unit) {
        this.onBindView = onBindView
    }

    //fixme 关闭
    fun dismiss() {
        pop?.dismiss()
    }

    //fixme 显示[每次显示的时候，布局都会重新刷新]
    fun showAsDropDown(view: View?, xoff: Int = 0, yoff: Int = 0) {
        view?.let {
            pop?.showAsDropDown(it, xoff, yoff)//fixme xoff 和 yoff是偏移量。
            //数据刷新
            if (list.size > 1) {
                recyclerView?.adapter?.notifyItemRangeChanged(0, list.size - 1)
            } else {
                recyclerView?.adapter?.notifyItemChanged(0)
            }
        }
    }

    companion object {
        class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {}
        class MyAdapter(var sp: SpinnerPop, var itemView: (positon: Int) -> View) : RecyclerView.Adapter<MyViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                return MyViewHolder(itemView(viewType))//自定义View每次都是重新实例话出来的
            }

            override fun getItemCount(): Int {
                return sp.list.size
            }

            override fun getItemViewType(position: Int): Int {
                return position
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                sp.onBindView?.let {
                    it(holder.itemView, position)
                }
            }
        }
    }
}