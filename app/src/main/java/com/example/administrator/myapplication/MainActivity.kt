package com.example.administrator.myapplication

import android.os.Bundle
import cn.android.support.v7.lib.sin.crown.kotlin.base.BaseActivity
import cn.android.support.v7.lib.sin.crown.kotlin.dialog.AlertDialog

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AlertDialog(this).mession("你好").title("标题")
    }
}
