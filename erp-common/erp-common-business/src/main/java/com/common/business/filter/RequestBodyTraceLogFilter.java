package com.common.business.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestBodyTraceLogFilter extends OncePerRequestFilter {

    public static final String ATTR_BODY_READ_EXCEPTION = RequestBodyTraceLogFilter.class.getName() + ".BODY_READ_EXCEPTION";

    private static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_USER_AGENT = "User-Agent";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !hasRequestBody(request) || isMultipart(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = request instanceof ContentCachingRequestWrapper
                ? (ContentCachingRequestWrapper) request
                : new ContentCachingRequestWrapper(request);

        long startMillis = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, response);
        } finally {
            logBodyTraceIfNeeded(requestWrapper, response, startMillis);
        }
    }

    private void logBodyTraceIfNeeded(ContentCachingRequestWrapper request, HttpServletResponse response, long startMillis) {
        long contentLength = request.getContentLengthLong();
        byte[] cachedBody = request.getContentAsByteArray();
        int cachedBodyBytes = cachedBody.length;
        boolean bodyReadException = Boolean.TRUE.equals(request.getAttribute(ATTR_BODY_READ_EXCEPTION));
        boolean expectedBodyMissing = contentLength > 0 && cachedBodyBytes == 0;

        if (!bodyReadException && !expectedBodyMissing) {
            return;
        }

        log.info("[RequestBodyTrace] service body read summary, traceId={}, method={}, uri={}, status={}, contentLength={}, contentType={}, transferEncoding={}, cachedBodyBytes={}, cachedBodySha256={}, remoteAddr={}, xForwardedFor={}, userAgent={}, costMs={}, bodyReadException={}",
                TraceContext.traceId(),
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                contentLength,
                request.getContentType(),
                request.getHeader(HEADER_TRANSFER_ENCODING),
                cachedBodyBytes,
                cachedBodyBytes > 0 ? sha256Hex(cachedBody) : null,
                request.getRemoteAddr(),
                request.getHeader(HEADER_X_FORWARDED_FOR),
                request.getHeader(HEADER_USER_AGENT),
                System.currentTimeMillis() - startMillis,
                bodyReadException);
    }

    private boolean hasRequestBody(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    private String sha256Hex(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest is not available", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
