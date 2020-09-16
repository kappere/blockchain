package com.wataru.blockchain.core.net.model;

import lombok.Data;
import org.slf4j.Logger;

import java.io.Serializable;

/**
 * Created by zzq on 2018/4/8.
 */
@Data
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer code;
    private String message;
    private T data;
    private Boolean success;

    private static final String OK = "ok";
    private static final String ERROR = "error";
    
    public interface Code {
        Integer success = 0;
        Integer commonError = -1;
        Integer noAuthentication = -999;
        Integer noAuthorization = -989;
    }

    public static <T> Response<T> success() {
        Response<T> response = new Response<>();
        response.code = Code.success;
        response.message = OK;
        response.success = true;
        return response;
    }

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.code = Code.success;
        response.message = OK;
        response.data = data;
        response.success = true;
        return response;
    }

    public static <T> Response<T> error() {
        Response<T> response = new Response<>();
        response.code = Code.commonError;
        response.message = ERROR;
        response.success = false;
        return response;
    }

    public static <T> Response<T> error(String message) {
        Response<T> response = new Response<>();
        response.code = Code.commonError;
        response.message = message;
        response.success = false;
        return response;
    }

    public static <T> Response<T> error(String message, Integer code) {
        Response<T> response = new Response<>();
        response.code = code;
        response.message = message;
        response.success = false;
        return response;
    }

    public static <T> Response<T> error(Logger logger, String message) {
        Response<T> response = new Response<>();
        response.code = Code.commonError;
        response.message = message;
        response.success = false;
        logger.info(response.message);
        return response;
    }

    public static <T> Response<T> error(Logger logger, Exception e, Integer code) {
        Response<T> response = new Response<>();
        response.code = code;
        response.message = e.getMessage();
        response.success = false;
        logger.error(response.message, e);
        return response;
    }
}
