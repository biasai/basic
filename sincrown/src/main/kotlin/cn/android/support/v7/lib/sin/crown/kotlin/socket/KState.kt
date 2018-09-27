package cn.android.support.v7.lib.sin.crown.kotlin.socket

/**
 * state 状态，true成功，false失败
 * text 原因，一般是失败的原因。成功一般为空
 */
class KState(var isSuccess: Boolean, var text: String? = null) {}