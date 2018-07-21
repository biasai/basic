package cn.android.support.v7.lib.sin.crown.kotlin.common

import cn.android.support.v7.lib.sin.crown.kotlin.base.BasePx

//可以根据不同需求，创建对应的适配标准。
object px : BasePx() {
    init {
        //创建视图适配标准。
        init(baseWidth = 750f, baseHeight = 1334f)
    }
}