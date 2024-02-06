package com.yury.trade.tradeHelper.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorResponse {

    private HttpStatus status;
    private String message;

    public ErrorResponse(HttpStatus status, String message) {
        setStatus(status);
        setMessage(message);
    }
}