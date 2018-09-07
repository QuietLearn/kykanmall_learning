package com.mmall.exceptionhandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mmall.exception.MyException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;


public class CustomHandlerException implements HandlerExceptionResolver{
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request, 
			HttpServletResponse response, 
			Object object,
			Exception e) {
		
		String message;
		
		if (e instanceof MyException) {
			//如果是自己定义的异常，那么是预期异常，返回预期异常信息。
			message = e.getMessage();
		} else {
			//不是预期异常
			Writer writer = new StringWriter();
			PrintWriter pw = new PrintWriter(writer);
			e.printStackTrace(pw);
			message = writer.toString();
			
		}
		
		// 把错误信息发给相关人员,邮件,短信等方式
		// TODO
		// 返回错误页面，给用户友好页面显示错误信息
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("msg", message);
		modelAndView.setViewName("error");

		return modelAndView;

	}

}
