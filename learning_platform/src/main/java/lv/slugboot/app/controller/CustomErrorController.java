package lv.slugboot.app.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {
	
	private static final String ERROR_PAGE = "show-error";
	private static final String ERROR_CODE = "errorCode";
	private static final String ERROR_MESSAGE = "errorMessage";
	private static final String EXCEPTION_MESSAGE = "exceptionMessage";
	private static final String STACK_TRACE = "stackTrace";

	@RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public String handleError(HttpServletRequest request, Model model) {
        // Retrieve the standard Spring Boot error attributes
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        // Default values
        String errorCode = (status != null) ? status.toString() : "Unknown";
        String errorMessage = (message != null && !message.toString().isEmpty()) 
                               ? message.toString() 
                               : "An unexpected error occurred on the server.";

        // If it's a 404, provide a more user-friendly message
        if ("404".equals(errorCode)) {
            errorMessage = "The page you are looking for does not exist.";
        }
        
        String exceptionTypeAndMessage = null;
        String stackTrace = null;

        if (exception instanceof Throwable) {
            Throwable throwable = (Throwable) exception;
            // Capture the exception class name and message (e.g., "java.lang.NullPointerException: Cannot invoke...")
            exceptionTypeAndMessage = throwable.getClass().getName() + ": " + throwable.getMessage();

            // Convert stack trace to a readable String block
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            stackTrace = sw.toString();
        }

        model.addAttribute(ERROR_CODE, errorCode);
        model.addAttribute(ERROR_MESSAGE, errorMessage);
        
        model.addAttribute(EXCEPTION_MESSAGE, exceptionTypeAndMessage);
//        model.addAttribute(STACK_TRACE, stackTrace);

        // Maps to templates/show-error.html
        return ERROR_PAGE;
    }
}
