package org.trickyplay.trickyplayapi.general.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// org.trickyplay.trickyplayapi.general.handlers.GlobalExceptionHandler uses @ControllerAdvice and @ExceptionHandler to handle all the exception to a REST Api. It works fine for exceptions thrown by web mvc controllers, but it does not work for exceptions thrown by spring security custom filters because they run before the controller methods are invoked. The GlobalExceptionHandler will only work if the request is handled by the DispatcherServlet. However, custom security filter (that does a token based auth) exception occurs before that as it is thrown by a Filter
@Component
@Slf4j
public class UnauthorizedHandler implements AuthenticationEntryPoint { // JwtAuthEntryPoint
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        // Called when the user tries to access an endpoint which requires to be authenticated
        log.debug("Unauthorized error. Message - {}", authException.getMessage());

//        Several ways to handle the exception ->
//
//        1. first way:
//        RestError re = new RestError(HttpStatus.UNAUTHORIZED.toString(), "Authentication failed");
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        OutputStream responseStream = response.getOutputStream();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.writeValue(responseStream, re);
//        responseStream.flush();
//
//        2. Second way:
//        ServletServerHttpResponse res = new ServletServerHttpResponse(response);
//        res.setStatusCode(HttpStatus.UNAUTHORIZED);
//        res.getServletResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
//        res.getBody().write(mapper.writeValueAsString(new ErrorResponse("You must authenticated")).getBytes());
//
//        3. Third way: -In this way you can send custom json data along with the 401 unauthorized
//        response.setContentType("application/json");
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.getOutputStream().println("{ \"error\": \"" + authException.getMessage() + "\" }");
//
//        4. Fourth way: -In this way you can just attach error message
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());

//        register like
//        http.exceptionHandling()
//                .authenticationEntryPoint(
//                        (request, response, ex) -> {
//                            response.sendError(
//                                    HttpServletResponse.SC_UNAUTHORIZED,
//                                    ex.getMessage()
//                            );
//                        }
//                );
    }
}

