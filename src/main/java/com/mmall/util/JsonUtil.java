package com.mmall.util;

import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        //取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        //忽略空Bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String obj2Json(T obj){
        if (obj == null)
            return null;
        try {
            return obj instanceof String? (String )obj:objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to Json error",e);
            return null;
        }
    }
    public static <T> String obj2PrettyJson(T obj){
        if (obj == null)
            return null;
        try {
            return obj instanceof String? (String )obj:objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Parse Object to Json error",e);
            return null;
        }
    }


    //<T> 代表声明方法持有一个类型T,或者可以理解成将此方法声明为泛型方法
    //T 返回值的类型
    public static <T> T Json2Obj(String json,Class<T> clazz){
        if (json==null)
            return null;
        try {
            return clazz.equals(String.class)?(T)json:objectMapper.readValue(json,clazz);
        } catch (Exception e) {
            log.warn("Parse Json to Object error",e);
            return null;
        }
    }


    public static void main(String[] args) {
        User u1 = new User();
        u1.setId(1);
        u1.setEmail("784510436@qq.com");

        String json = JsonUtil.obj2Json(u1);
        System.out.println(json);
        User user = JsonUtil.Json2Obj(json, User.class);
        System.out.println(user);

        User u2 = new User();
        u2.setId(2);
        u2.setEmail("13065708090@163.com");

        List list = Lists.newArrayList();
        list.add(u1);
        list.add(u2);

        String prettyJson = JsonUtil.obj2PrettyJson(list);
        System.out.println(prettyJson);
    }



}
