package com.github.tomitakussaari.phaas.api;

import com.github.tomitakussaari.phaas.user.SecurityConfig;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class RootEndpoint implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @Autowired
    ErrorAttributes errorAttributes;

    @RequestMapping(method = RequestMethod.GET)
    public View printHelp() {
        return new InternalResourceView("index.html");
    }

    @RequestMapping(value = ERROR_PATH)
    public ExceptionAdvisor.ErrorMessage showErrorMessage(HttpServletRequest request, HttpServletResponse response) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> requestErrorAttributes = this.errorAttributes.getErrorAttributes(requestAttributes, false);
        return new ExceptionAdvisor.ErrorMessage(requestErrorAttributes.get("message") + "", requestErrorAttributes.get("error") + "", MDC.get(SecurityConfig.AuditAndLoggingFilter.MDC_REQUEST_ID), response.getStatus());
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
