package cn.android.support.v7.lib.sin.crown.kotlin.utils

import android.util.Log

/**
 * 字符串处理工具类
 * 之所以写这个工具，是因为NumberFormat的四舍五入有问题。不可靠。所以自己写。
 */
object KStringUtils {

    fun doubleString(d: Double, num: Int = 2): String? {
        return doubleString(d, num, true, false)
    }

    fun doubleString(d: Double, num: Int, isKeep0: Boolean = true, isRounded: Boolean = false): String? {
        d.toString().trim().apply {
            return doubleString(this, num, isKeep0, isRounded)
        }
        return d.toString()
    }

    fun doubleString(str: String?, num: Int = 2): String? {
        return doubleString(str, num, true, false)
    }

    /**
     * str 字符串
     * num 小数点保留个数
     * isKeep0 小数点后末尾如果是0,是否保留0。true保留0，false不保留。默认保留。
     * isRounded 小数点后，最后保留的一位数，是否四舍五入。默认false不。
     */
    fun doubleString(str: String?, num: Int, isKeep0: Boolean = true, isRounded: Boolean = false): String? {
        str?.trim()?.let {
            if (it.contains(".")) {
                //有小数点
                var index = it.indexOf(".")//小数点下标
                var front = it.substring(0, index)//小数点前面的数
                var behind = it.substring(index)//小数点后面数（包含小数点）
                if (behind.length > 1 && num > 0) {
                    behind = behind.substring(1)//小数点后面数（不包含小数点）
                    if (behind.length > num) {
                        //小数个数大于保留个数(存在四舍五入)
                        if (isRounded) {
                            //四舍五入
                            var lastBehind = behind.substring(num, num + 1)//最后一个数。
                            behind = behind.substring(0, num)
                            if (lastBehind.toInt() > 4) {
                                var rounde = "0."
                                for (i in 1..num) {
                                    if (i == num) {
                                        rounde += "1"
                                    } else {
                                        rounde += "0"
                                    }
                                }
                                var double = (front + "." + behind).toDouble()
                                double += rounde.toDouble()//fixme 四舍五入进一
                                var str2 = double.toString()
                                index = str2.indexOf(".")//小数点下标
                                front = str2.substring(0, index)//小数点前面的数
                                behind = str2.substring(index)//小数点后面数（包含小数点）
                                if (behind.length > 1) {
                                    behind = behind.substring(1)//小数点后面数（不包含小数点）
                                } else {
                                    behind = ""
                                }
                                if (behind.length > num) {
                                    behind = behind.substring(0, num)
                                }
                            }
                        } else {
                            //保留原始数据
                            behind = behind.substring(0, num)
                        }
                    }
                    if (isKeep0) {
                        if (behind.length > 0) {
                            return front + "." + behind//fixme 保留0
                        } else {
                            return front
                        }
                    } else {
                        return removeZero(front, behind)//fixme 不保留0
                    }
                } else {
                    return front//fixme 小数点后面没有数字了，直接返回小数点前面的数
                }
            } else {
                return it//fixme 没有小数点的情况,返回原数据
            }
        }
        return str//fixme 为空的情况，返回空
    }

    /**
     * 去除小数点末尾的0 （包括末尾相连的0）。如：1.10 ->1.1 ; 1.00->1
     * s1 小数点前面的数，s2小数点后面的数
     */
    private fun removeZero(s1: String, s2: String): String {
        for (i in s2.length downTo 1) {
            if (s2.substring(i - 1, i) != "0") {
                return s1 + "." + s2.substring(0, i)
            }
        }
        return s1
    }

}