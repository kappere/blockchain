package com.wataru.blockchain.admin.exceptionhandler;

import com.wataru.blockchain.core.exception.BizException;
import com.wataru.blockchain.core.net.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
@Slf4j
public class WebExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Response<Object> exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception e) {
        log.error("", e);
        return Response.error("服务器异常，请联系管理员");
    }
    @ExceptionHandler(BizException.class)
    public Response<Object> bizExceptionHandler(HttpServletRequest request, HttpServletResponse response, BizException e) {
        log.error("{}", e.getMessage());
        return Response.error(e.getMessage());
    }
}
