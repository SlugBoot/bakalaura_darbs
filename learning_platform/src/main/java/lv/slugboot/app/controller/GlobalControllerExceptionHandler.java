package lv.slugboot.app.controller;

import java.io.IOException;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {

	private static final String ERROR_PAGE = "show-error";
    private static final String ERROR_CODE_STR = "errorCode";
    private static final String ERROR_MESSAGE_STR = "errorMessage";
    
    private static final String ERROR_CODE_400_STR = "400 (Bad Request)";
    private static final String ERROR_CODE_500_STR = "500 (Internal Server Error)";
    
    @ExceptionHandler({
        IllegalArgumentException.class, 
        NoSuchFieldException.class, 
        NullPointerException.class, 
        IOException.class
    })
    public String handleBadRequestExceptions(Exception ex, Model model) {
       
        if (Thread.currentThread().isInterrupted()) {
            // Keep status if your previous code specifically managed thread states
        }
        
        log.warn("Bad request exception handled: {}", ex.getMessage());
        model.addAttribute(ERROR_CODE_STR, ERROR_CODE_400_STR);
        model.addAttribute(ERROR_MESSAGE_STR, ex.getMessage());
        return ERROR_PAGE;
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unhandled exception caught by global handler", ex);
        model.addAttribute(ERROR_CODE_STR, ERROR_CODE_500_STR);
        model.addAttribute(ERROR_MESSAGE_STR, ex.getMessage());
        return ERROR_PAGE;
    }
}
