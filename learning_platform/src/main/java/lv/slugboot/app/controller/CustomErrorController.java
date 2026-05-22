package lv.slugboot.app.controller;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

	private static final String ERROR_PAGE = "show-error";
	private static final String ERROR_CODE = "errorCode";
	private static final String ERROR_MESSAGE = "errorMessage";

	@RequestMapping
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

        model.addAttribute(ERROR_CODE, errorCode);
        model.addAttribute(ERROR_MESSAGE, errorMessage);

        // Maps to templates/show-error.html
        return ERROR_PAGE;
    }
}
