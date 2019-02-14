package com.mmall.Controller;

import com.mmall.common.TokenCache;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.StringReader;
import java.text.MessageFormat;

@Slf4j
public class TestController {


    @RequestMapping(value = "set_cache.do")
    @ResponseBody
    public String setCache(String key,String value){
        TokenCache.setKey(key,value);
        return MessageFormat.format("set key:{0},value:{1}.ok",key,value);
    }

    @RequestMapping(value = "get_cache.do")
    @ResponseBody
    public String getCache(String key){
        return TokenCache.getKey(key);
    }

    @RequestMapping(value = "test.do")
    @ResponseBody
    public String test(String str){
        log.info("testinfo");
        log.warn("testwarn");
        log.warn("testerror");
        return "testValue:" +str;
    }
}
