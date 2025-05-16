package hongik.triple.apimodule.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hongik.triple.apimodule.global.common.ErrorResponse;
import hongik.triple.commonmodule.exception.ApplicationException;
import hongik.triple.commonmodule.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    // Jwt Filter에서 발생하는 Exception Handling
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ApplicationException e) {
            setResponse(response);
        }
    }

    // Error 관련 응답 Response 생성 메소드
    private void setResponse(HttpServletResponse response) throws IOException{
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(ErrorCode.FORBIDDEN_EXCEPTION.getHttpStatus().value());

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INTERNAL_SERVER_EXCEPTION);
        String errorJson = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(errorJson);
    }
}
