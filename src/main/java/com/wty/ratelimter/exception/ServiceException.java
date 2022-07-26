package com.wty.ratelimter.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServiceException extends RuntimeException{
    private int code;
    private String message;
    private Object data;

    public static ServiceException of(int code, String msg, Object data) {
        return new ServiceException(code, msg, data);
    }
}
