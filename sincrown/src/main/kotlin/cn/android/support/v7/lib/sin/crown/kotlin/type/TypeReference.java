package cn.android.support.v7.lib.sin.crown.kotlin.type;

import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

//泛型传入格式：Model<Mode2>
//传入的必须是具体的类型，如果是泛型，也必须是reified 具体的泛型。
public class TypeReference<T> {

    private final Type type;

    public Type getType() {
        return type;
    }

    //最外层类型，Model<Mode2>或者Model<ArrayList<Mode2>> 获取的是Mode1
    public String GenericClassName = null;
    public Class GenericClass = null;

    //第二层类型，Model<Mode2> 获取的是Mode2， 或者Model<ArrayList<Mode2>> 获取的是ArrayList
    public String GenericClass2Name = null;
    public Class GenericClass2 = null;

    //第三层类型，Model<ArrayList<Mode3>> 获取的是Mode3
    public String GenericClass3Name = null;
    public Class GenericClass3 = null;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        //格式为：com.example.myapplication3.Model<com.example.myapplication3.Mode2>
        //com.example.myapplication3.Model<java.util.ArrayList<com.example.myapplication3.Mode3>>
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        //Log.e("test","类型:\t"+type);
        String className = type.toString().trim();
        if (className.contains("<") && className.contains(">")) {
            if (className.indexOf("<") == className.lastIndexOf("<")) {
                //类型为 两层 Model<Mode2>
                try {
                    //第一层
                    GenericClassName = className.substring(0, className.indexOf("<")).trim();
                    GenericClass = Class.forName(GenericClassName);
                    //第二层
                    GenericClass2Name = className.substring(className.indexOf("<") + 1, className.lastIndexOf(">")).trim();
                    GenericClass2 = Class.forName(GenericClass2Name);
                } catch (ClassNotFoundException e) {
                    Log.e("test", "class类型找不到异常2:\t" + e.getMessage());
                }
            } else {
                //类型为 三层 Model<ArrayList<Mode3>> 或 Model<Mode2<Mode3>>
                try {
                    //第一层
                    GenericClassName = className.substring(0, className.indexOf("<")).trim();
                    GenericClass = Class.forName(GenericClassName);
                    //第二层
                    GenericClass2Name = className.substring(className.indexOf("<") + 1, className.lastIndexOf("<")).trim();
                    GenericClass2 = Class.forName(GenericClass2Name);
                    //第三层
                    GenericClass3Name = className.substring(className.lastIndexOf("<") + 1, className.indexOf(">")).trim();
                    GenericClass3 = Class.forName(GenericClass3Name);
                } catch (ClassNotFoundException e) {
                    Log.e("test", "class类型找不到异常3:\t" + e.getMessage());
                }
            }
        } else {
            //第一层
            try {
                GenericClassName = className.substring(5).trim();
                GenericClass = Class.forName(GenericClassName);
            } catch (Exception e) {
                Log.e("test", "class类型找不到异常0:\t" + e.getMessage());
            }
        }
        //Log.e("test", "总类型:\t" + className);
        //Log.e("test", "类型1：\t" + GenericClass + "\t类型2:\t" + GenericClass2 + "\t类型3：\t" + GenericClass3);
    }

    public final static Type LIST_STRING = new TypeReference<List<String>>() {
    }.getType();
}
