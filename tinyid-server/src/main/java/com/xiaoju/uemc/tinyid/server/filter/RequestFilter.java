package com.xiaoju.uemc.tinyid.server.filter;


import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


import java.io.IOException;
import java.util.Map;

/**
 * @Author du_imba
 */
@Component
@ServletComponentScan
@WebFilter(urlPatterns = "/*")
public class RequestFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String params = "";
        Map<String, String[]> paramsMap = request.getParameterMap();
        if (paramsMap != null && !paramsMap.isEmpty()) {
            for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
                params += entry.getKey() + ":" + StringUtils.arrayToDelimitedString(entry.getValue(), ",") + ";";
            }
        }
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } catch (Throwable e) {
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - start;
            logger.info("request filter path={}, cost={}, params={}", request.getServletPath(), cost, params);
        }
    }

    @Override
    public void destroy() {

    }
}
