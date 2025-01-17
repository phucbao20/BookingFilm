package com.config.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NotFoundException {
	@ExceptionHandler(NoHandlerFoundException.class)
	public ModelAndView notFound(NoHandlerFoundException ex)
	{
		ModelAndView view = new ModelAndView("404");
		view.setStatus(HttpStatus.NOT_FOUND);
		return view;
	}
}
