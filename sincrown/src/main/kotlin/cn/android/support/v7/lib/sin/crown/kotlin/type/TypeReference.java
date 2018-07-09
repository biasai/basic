package cn.android.support.v7.lib.sin.crown.kotlin.type;

import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

//泛型传入格式：Model<Mode2>
public class TypeReference<T> {

    private final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        //格式为：com.example.myapplication3.Model<com.example.myapplication3.Mode2>
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        //Log.e("test","类型:\t"+type);
    }

    public Type getType() {
        return type;
    }

    //获取类型的具体类型，Model<Mode2>获取的是Mode1
    public Class getGenericClass() {
        if (type.toString().contains("<") && type.toString().contains(">")) {
            //获取类型
            String className = type.toString().substring(0, type.toString().indexOf("<")).trim();
            try {
                Class clazz = Class.forName(className);
                return clazz;
            } catch (ClassNotFoundException e) {
                Log.e("test", "class类型找不到异常:\t" + e.getMessage());
            }
        }
        return null;
    }

    public String getGenericClassName() {
        if (type.toString().contains("<") && type.toString().contains(">")) {
            //获取类型
            String className = type.toString().substring(0, type.toString().indexOf("<")).trim();
            return className;
        }
        return null;
    }

    //获取泛型的具体类型，Model<Mode2>获取的是Mode2
    public Class getGenericTClass() {
        if (type.toString().contains("<") && type.toString().contains(">")) {
            //获取泛型类名
            String classNameT = type.toString().substring(type.toString().indexOf("<") + 1, type.toString().lastIndexOf(">")).trim();
            try {
                Class clazz = Class.forName(classNameT);
                return clazz;
            } catch (ClassNotFoundException e) {
                Log.e("test", "class类型找不到异常:\t" + e.getMessage());
            }
        }
        return null;
    }

    public String getGenericTClassName() {
        if (type.toString().contains("<") && type.toString().contains(">")) {
            //获取泛型类名
            String classNameT = type.toString().substring(type.toString().indexOf("<") + 1, type.toString().lastIndexOf(">")).trim();
            return classNameT;
        }
        return null;
    }

    public final static Type LIST_STRING = new TypeReference<List<String>>() {
    }.getType();
}
