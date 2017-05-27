package com.acc.yys.controller;

import com.acc.yys.pojo.JsonBody;
import com.acc.yys.util.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhaoyy on 2017/5/23.
 */
@ControllerAdvice
public final class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public
    @ResponseBody
    Object handleException(HttpServletRequest request, Exception e) {
        logger.error(e.getMessage(), e);
        request.setAttribute("cause", e.getMessage());
        if (HttpRequestUtils.isAjaxRequest(request))
            return JsonBody
                    .builder()
                    .msg(e.getMessage())
                    .build(JsonBody.SERVER_ERROR);
        return String.format("<html><body><h1>%s<h1></body></html>", e.getMessage());
    }
}
