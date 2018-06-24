package cn.android.support.v7.lib.sin.crown.kotlin.dialog

import android.app.Activity
import android.view.View
import android.widget.TextView
import cn.android.support.v7.lib.sin.crown.R
import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseDialog
import cn.android.support.v7.lib.sin.crown.kotlin.common.px
import cn.android.support.v7.lib.sin.crown.utils.ProportionUtils

//            val alert: AlertDialog by lazy { AlertDialog(this) }
//            alert.little(false).title("温馨").mession("是否确认退出？").positive("确定"){
//                ToastUtils.showToastView("点击确定")
//            }.negative("NO"){
//                ToastUtils.showToastView("NO!!!")
//            }.isDismiss(false).show()
class AlertDialog(activity: Activity) : BaseDialog(activity, R.layout.crown_kotlin_dialog_alert) {

    var little = false//是否为小窗口，默认不是。
    fun little(little: Boolean = true): AlertDialog {
        this.little = little
        return this
    }

    val container: View by lazy { findViewById<View>(R.id.crown_alert_parent) }
    //标题栏文本
    var txt_title: String? = ""
    val title: TextView by lazy { findViewById<TextView>(R.id.crown_txt_title) }
    fun title(title: String? = null): AlertDialog {
        txt_title = title
        return this
    }

    //信息文本
    var txt_mession: String? = ""
    val mession: TextView by lazy { findViewById<TextView>(R.id.crown_txt_mession) }
    fun mession(mession: String? = null): AlertDialog {
        txt_mession = mession
        return this
    }

    val negative: TextView by lazy { findViewById<TextView>(R.id.crown_txt_Negative) }
    //左边，取消按钮
    fun negative(negative: String? = "取消", callback: (() -> Unit)? = null): AlertDialog {
        this.negative.setText(negative)
        this.negative.setOnClickListener {
            callback?.run {
                this()
            }
            dismiss()
        }
        return this
    }

    val positive: TextView by lazy { findViewById<TextView>(R.id.crown_txt_Positive) }
    //右边，确定按钮
    fun positive(postive: String? = "确定", callback: (() -> Unit)? = null): AlertDialog {
        this.positive.setText(postive)
        this.positive.setOnClickListener {
            callback?.run {
                this()
            }
            dismiss()
        }
        return this
    }

    init {
        //取消
        negative.setOnClickListener {
            dismiss()
        }
        //确定
        positive.setOnClickListener {
            dismiss()
        }
    }

    override var isDismiss: Boolean = false//默认不消失

    override fun listener() {
        container.layoutParams.width = px.x(500)
        if (little) {
            container.layoutParams.height = px.x(200)
        } else {
            container.layoutParams.height = px.x(300)
        }
        container.requestLayout()
        title.setText(txt_title)
        mession.setText(txt_mession)
    }

    override fun recycleView() {
    }


}