package com.nhnacademy.front.global.error;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String VIEW = "error/error";
    private static final String ATTRIBUTE_NAME = "errorMessage";

    @ExceptionHandler(ApiException.class)
    public ModelAndView handle(ApiException e) {

        if (ErrorCode.G002.equals(e.getErrorCode())) {
            return new ModelAndView("redirect:/403");
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName(VIEW);

        mav.addObject(ATTRIBUTE_NAME, e.getMessage() != null ? e.getMessage() : "오류가 발생했습니다.");

        return mav;
    }
}
