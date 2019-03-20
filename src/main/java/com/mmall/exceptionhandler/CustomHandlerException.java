package com.mmall.exceptionhandler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mmall.exception.MyException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;


public class CustomHandlerException implements HandlerExceptionResolver{

	/**
	 *
	 * @param request
	 * @param response
	 * @param object 具体的一个handler
	 * @param e
	 * @return 因为是前后端分离的，所以不需要转到一个view，将其转为json
	 * 例如写一个页面，是个异常页面，然后从服务器跳转到那个页面
	 */
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
//		modelAndView.setViewName("error");

		return modelAndView;

	}

}
