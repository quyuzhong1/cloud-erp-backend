package com.cloud.erp.gateway.filter;

import com.cloud.erp.gateway.option.FilterOrderEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class RequestBodyTraceGlobalFilter implements GlobalFilter, Ordered {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_USER_AGENT = "User-Agent";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (!hasRequestBody(request.getMethod())) {
            return chain.filter(exchange);
        }

        HttpHeaders headers = request.getHeaders();
        long contentLength = headers.getContentLength();
        AtomicLong forwardedBodyBytes = new AtomicLong();
        MessageDigest bodyDigest = newSha256Digest();
        long startMillis = System.currentTimeMillis();

        ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return super.getBody().doOnNext(dataBuffer -> {
                    forwardedBodyBytes.addAndGet(dataBuffer.readableByteCount());
                    bodyDigest.update(dataBuffer.asByteBuffer().asReadOnlyBuffer());
                });
            }
        };

        return chain.filter(exchange.mutate().request(requestDecorator).build())
                .doFinally(signalType -> logBodyTraceIfNeeded(exchange, request, signalType, contentLength,
                        forwardedBodyBytes.get(), bodyDigest, startMillis));
    }

    private void logBodyTraceIfNeeded(ServerWebExchange exchange, ServerHttpRequest request, SignalType signalType,
                                      long contentLength, long forwardedBodyBytes, MessageDigest bodyDigest,
                                      long startMillis) {
        if (contentLength <= 0 && request.getHeaders().getFirst(HttpHeaders.TRANSFER_ENCODING) == null) {
            return;
        }

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        log.info("[RequestBodyTrace] gateway body forward summary, traceId={}, method={}, path={}, routeId={}, routeUri={}, status={}, contentLength={}, contentType={}, transferEncoding={}, forwardedBodyBytes={}, bodySha256={}, remoteAddress={}, xForwardedFor={}, userAgent={}, signal={}, costMs={}",
                TraceContext.traceId(),
                request.getMethodValue(),
                request.getURI().getRawPath(),
                route == null ? null : route.getId(),
                route == null ? null : route.getUri(),
                exchange.getResponse().getStatusCode(),
                contentLength,
                request.getHeaders().getContentType(),
                request.getHeaders().getFirst(HttpHeaders.TRANSFER_ENCODING),
                forwardedBodyBytes,
                forwardedBodyBytes > 0 ? toHex(bodyDigest.digest()) : null,
                request.getRemoteAddress(),
                request.getHeaders().getFirst(HEADER_X_FORWARDED_FOR),
                request.getHeaders().getFirst(HEADER_USER_AGENT),
                signalType,
                System.currentTimeMillis() - startMillis);
    }

    private boolean hasRequestBody(HttpMethod method) {
        return HttpMethod.POST.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.PATCH.equals(method)
                || HttpMethod.DELETE.equals(method);
    }

    @Override
    public int getOrder() {
        return FilterOrderEnum.GATEWAY_CONTEXT_FILTER.getOrder() + 1;
    }

    private MessageDigest newSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
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
