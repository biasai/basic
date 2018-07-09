package cn.android.support.v7.lib.sin.crown.kotlin.https.lv

import android.app.Activity
import android.util.Log
import cn.android.support.v7.lib.sin.crown.kotlin.https.Https
import cn.android.support.v7.lib.sin.crown.kotlin.type.TypeReference
import cn.android.support.v7.lib.sin.crown.kotlin.utils.JSonUtils
import org.json.JSONObject

//        OkHttp(urlLogin,this).apply {
//            onSuccess {
//                Log.e("test","登录成功:\t"+it)
//            }
//            addParam("account","fk11").addParam("pwd","123")
//            showLoad(true)
//            Post<Mession<User>>//fixme 目前支持，实体类<实体类> ，ArrayList<实体类> ，<实体类可以为泛型T>。 ==================实体类<ArrayList<实体类>> 逻辑太复杂，还不支持。有待后续完善。
//            {
//                Log.e("test",""+it.code+"\t"+it.list?.account+"\t"+it.list?.password+"\t"+it.errmsg)
//            }
//        }

/**
 * Created by 彭治铭 on 2018/7/9.
 */
class OkHttp(override open var url: String?, override open var activity: Activity? = null) : Https(url, activity) {
    //fixme 必须使用内联函数，不然TypeReference无法解析泛型。
    inline fun <reified T> Get(noinline callback: (t: T) -> Unit) {
        var typeReference = object : TypeReference<T>() {}
        Get() {
            var t = JSonUtils.parseObject(it, typeReference.genericClass, typeReference.genericTClass)
            callback?.let {
                it(t as T)
            }
        }
    }

    //TypeReference 泛型再传泛型，泛型必须是具体类型。
    inline fun <reified T> Post(noinline callback: (t: T) -> Unit) {
        var typeReference = object : TypeReference<T>() {}
        Post() {
            var t = JSonUtils.parseObject(it, typeReference.genericClass, typeReference.genericTClass)
            callback?.let {
                it(t as T)
            }
        }
    }

}