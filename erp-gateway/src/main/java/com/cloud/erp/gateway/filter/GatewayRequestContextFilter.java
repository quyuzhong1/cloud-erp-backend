package com.cloud.erp.gateway.filter;

import com.cloud.erp.gateway.context.ContextExtraDataGenerator;
import com.cloud.erp.gateway.context.GatewayContext;
import com.cloud.erp.gateway.context.GatewayContextExtraData;
import com.cloud.erp.gateway.option.FilterOrderEnum;
import com.cloud.erp.gateway.option.GatewayLogTypeEnum;
import com.cloud.erp.gateway.properties.GatewayPluginProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 读取并缓存请求数据
 * @Author Luo_WG
 * @Date 2023/12/6 18:28
 **/
@Slf4j
@AllArgsConstructor
public class GatewayRequestContextFilter<T> implements GlobalFilter, Ordered {

    private GatewayPluginProperties gatewayPluginProperties;

    private ContextExtraDataGenerator<T> contextExtraDataGenerator;

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * default HttpMessageReader
     */
    private static final List<HttpMessageReader<?>> MESSAGE_READERS = HandlerStrategies.withDefaults().messageReaders();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        GatewayContext<T> gatewayContext = new GatewayContext<>();
        gatewayContext.setReadRequestData(shouldReadRequestData(exchange) || shouldReadRequestInjectionData(exchange));
        gatewayContext.setReadResponseData(gatewayPluginProperties.getLogRequest().getResponseLog());
        HttpHeaders headers = request.getHeaders();
        gatewayContext.setRequestHeaders(headers);
        if(Objects.nonNull(contextExtraDataGenerator)){
            GatewayContextExtraData<T> gatewayContextExtraData = contextExtraDataGenerator.generateContextExtraData(exchange);
            gatewayContext.setGatewayContextExtraData(gatewayContextExtraData);
        }
        if(Boolean.FALSE.equals(gatewayContext.getReadRequestData())){
            exchange.getAttributes().put(GatewayContext.CACHE_GATEWAY_CONTEXT, gatewayContext);
            log.debug("[GatewayContext]Properties Set To Not Read Request Data");
            return chain.filter(exchange);
        }
        gatewayContext.getAllRequestData().addAll(request.getQueryParams());
        /*
         * save gateway context into exchange
         */
        exchange.getAttributes().put(GatewayContext.CACHE_GATEWAY_CONTEXT, gatewayContext);
        MediaType contentType = headers.getContentType();
        if(headers.getContentLength() > 0){
            if(MediaType.APPLICATION_JSON.equals(contentType)){
                return readBody(exchange, chain,gatewayContext);
            }
            if(MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)){
                return readFormData(exchange, chain,gatewayContext);
            }
        }
        log.debug("[GatewayContext]ContentType:{},Gateway context is set with {}",contentType, gatewayContext);
        return chain.filter(exchange);

    }


    @Override
    public int getOrder() {
        return FilterOrderEnum.GATEWAY_CONTEXT_FILTER.getOrder();
    }

    /**
     * check should read request data whether or not
     * @return boolean
     */
    private boolean shouldReadRequestData(ServerWebExchange exchange){
        if(Boolean.TRUE.equals(gatewayPluginProperties.getLogRequest().getRequestLog())
                && GatewayLogTypeEnum.ALL.getType().equals(gatewayPluginProperties.getLogRequest().getLogType())){
            log.debug("[GatewayContext]Properties Set Read All Request Data");
            return true;
        }
        boolean lbFlag = false;
        boolean pathFlag = false;

        List<String> readRequestDataServiceIdList = gatewayPluginProperties.getLogRequest().getServiceIdList();

        List<String> readRequestDataPathList = gatewayPluginProperties.getLogRequest().getPathList();

        pathFlag = isPathFlag(!CollectionUtils.isEmpty(readRequestDataPathList)
                && (GatewayLogTypeEnum.PATH.getType().equals(gatewayPluginProperties.getLogRequest().getLogType())
                || GatewayLogTypeEnum.CONFIGURE.getType().equals(gatewayPluginProperties.getLogRequest().getLogType())), exchange, readRequestDataPathList, "[GatewayContext]Properties Set Read Specific Request Data With Request Path:{},Math Pattern:{}", pathFlag);


        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI routeUri = null == route ? null : route.getUri();
        if(null != routeUri && !"lb".equalsIgnoreCase(routeUri.getScheme())){
            lbFlag = true;
        }

        String routeServiceId = null == routeUri ? "" : routeUri.getHost().toLowerCase();
        boolean serviceFlag = isServiceFlag(readRequestDataServiceIdList, routeServiceId);


        if (GatewayLogTypeEnum.CONFIGURE.getType().equals(gatewayPluginProperties.getLogRequest().getLogType())
                && serviceFlag && pathFlag && !lbFlag) {
            return true;
        }
        else if (GatewayLogTypeEnum.SERVICE.getType().equals(gatewayPluginProperties.getLogRequest().getLogType())
                && serviceFlag && !lbFlag) {
            return true;
        } else if (GatewayLogTypeEnum.PATH.getType().equals(gatewayPluginProperties.getLogRequest().getLogType())
                && pathFlag) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 因为加入了SQL注入和XSS注入拦截，这里单独判断
     * @return boolean
     */
    private boolean shouldReadRequestInjectionData(ServerWebExchange exchange){
        if((gatewayPluginProperties.getSqlInjection().getEnable()
                 && CollectionUtils.isEmpty(gatewayPluginProperties.getSqlInjection().getServiceIdList())
                 && CollectionUtils.isEmpty(gatewayPluginProperties.getSqlInjection().getPathList()))
                || (gatewayPluginProperties.getXssInjection().getEnable()
                && CollectionUtils.isEmpty(gatewayPluginProperties.getXssInjection().getServiceIdList())
                && CollectionUtils.isEmpty(gatewayPluginProperties.getXssInjection().getPathList())
        )){
            log.debug("[GatewayContext]Properties Set Read All Request Data");
            return true;
        }
        
        boolean serviceFlag = false;
        boolean pathFlag = false;
    
        List<String> readRequestDataServiceIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(gatewayPluginProperties.getSqlInjection().getServiceIdList()))
        {
            readRequestDataServiceIdList.addAll(gatewayPluginProperties.getSqlInjection().getServiceIdList());
        }

        if (!CollectionUtils.isEmpty(gatewayPluginProperties.getXssInjection().getServiceIdList()))
        {
            readRequestDataServiceIdList.addAll(gatewayPluginProperties.getXssInjection().getServiceIdList());
        }
    
        List<String> readRequestDataPathList = new ArrayList<>();
        
        if (!CollectionUtils.isEmpty(gatewayPluginProperties.getSqlInjection().getPathList()))
        {
            readRequestDataPathList.addAll(gatewayPluginProperties.getSqlInjection().getPathList());
        }
    
        if (!CollectionUtils.isEmpty(gatewayPluginProperties.getXssInjection().getPathList()))
        {
            readRequestDataPathList.addAll(gatewayPluginProperties.getXssInjection().getPathList());
        }
        
        // 因为请求的路径太多，防注入采取白名单模式，如果配置了地址，那么就放过，所以不需要进行参数解析
        pathFlag = isPathFlag(!CollectionUtils.isEmpty(readRequestDataPathList), exchange, readRequestDataPathList, "[GatewayContext]Properties Set Not Read Specific Request Data With Request Path:{},Math Pattern:{}", pathFlag);

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI routeUri = route.getUri();
        
        String routeServiceId = routeUri.getHost().toLowerCase();
        if(!CollectionUtils.isEmpty(readRequestDataServiceIdList) && readRequestDataServiceIdList.contains(routeServiceId)){
                log.debug("[GatewayContext]Properties Set Not Read Specific Request Data With ServiceId:{}",routeServiceId);
                serviceFlag =  true;
        }

        return !serviceFlag || !pathFlag;
    }

    /**
     * ReadFormData
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> readFormData(ServerWebExchange exchange, GatewayFilterChain chain, GatewayContext<T> gatewayContext){
        HttpHeaders headers = exchange.getRequest().getHeaders();
        return exchange.getFormData()
                .doOnNext(multiValueMap -> {
                    gatewayContext.setFormData(multiValueMap);
                    gatewayContext.getAllRequestData().addAll(multiValueMap);
                    log.debug("[GatewayContext]Read FormData Success");
                })
                .then(Mono.defer(() -> {
                    Charset charset = checkAndGetCharset(headers);
                    String charsetName = charset.name();
                    MultiValueMap<String, String> formData = gatewayContext.getFormData();
                    /*
                     * formData is empty just return
                     */
                    if(null == formData || formData.isEmpty()){
                        return chain.filter(exchange);
                    }
                    String formDataBodyString = convertFormDataDodyString(formData, charsetName);
                    /*
                     * get data bytes
                     */
                    byte[] bodyBytes =  formDataBodyString.getBytes(charset);
                    int contentLength = bodyBytes.length;
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.putAll(exchange.getRequest().getHeaders());
                    httpHeaders.remove(HttpHeaders.CONTENT_LENGTH);
                    /*
                     * in case of content-length not matched
                     */
                    httpHeaders.setContentLength(contentLength);
                    /*
                     * use BodyInserter to InsertFormData Body
                     */
                    BodyInserter<String, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromObject(formDataBodyString);
                    CachedBodyOutputMessage cachedBodyOutputMessage = new CachedBodyOutputMessage(exchange, httpHeaders);
                    log.debug("[GatewayContext]Rewrite Form Data :{}",formDataBodyString);
                    return bodyInserter.insert(cachedBodyOutputMessage,  new BodyInserterContext())
                            .then(Mono.defer(() -> {
                                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(
                                        exchange.getRequest()) {
                                    @Override
                                    public HttpHeaders getHeaders() {
                                        return httpHeaders;
                                    }
                                    @Override
                                    public Flux<DataBuffer> getBody() {
                                        return cachedBodyOutputMessage.getBody();
                                    }
                                };
                                return chain.filter(exchange.mutate().request(decorator).build());
                            }));
                }));
    }

    /**
     * ReadJsonBody
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> readBody(ServerWebExchange exchange, GatewayFilterChain chain, GatewayContext<T> gatewayContext){
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    /*
                     * read the body Flux<DataBuffer>, and release the buffer
                     */
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                        DataBufferUtils.retain(buffer);
                        return Mono.just(buffer);
                    });
                    /*
                     * repackage ServerHttpRequest
                     */
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedFlux;
                        }
                    };
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    return ServerRequest.create(mutatedExchange, MESSAGE_READERS)
                            .bodyToMono(String.class)
                            .doOnNext(objectValue -> {
                                gatewayContext.setRequestBody(objectValue);
                                log.debug("[GatewayContext]Read JsonBody Success");
                            }).then(chain.filter(mutatedExchange));
                });
    }

    private static Charset checkAndGetCharset(HttpHeaders headers) {
        Charset charset = StandardCharsets.UTF_8;
        if (null == headers){
            return charset;
        }
        MediaType contentType = headers.getContentType();
        if (null == contentType){
            return charset;
        }
        Charset headersCharset = contentType.getCharset();
        if (null == headersCharset){
            return charset;
        } else {
            return headersCharset;
        }
    }

    private static String convertFormDataDodyString(MultiValueMap<String, String> formData, String charsetName) {
        StringBuilder formDataBodyBuilder = new StringBuilder();
        String entryKey;
        List<String> entryValue;
        try {
            /*
             * repackage form data
             */
            for (Map.Entry<String, List<String>> entry : formData.entrySet()) {
                entryKey = entry.getKey();
                entryValue = entry.getValue();
                if (entryValue.size() > 1) {
                    for(String value : entryValue){
                        formDataBodyBuilder.append(entryKey).append("=").append(URLEncoder.encode(value, charsetName)).append("&");
                    }
                } else {
                    formDataBodyBuilder.append(entryKey).append("=").append(URLEncoder.encode(entryValue.get(0), charsetName)).append("&");
                }
            }
        }catch (UnsupportedEncodingException e){
            log.error("解析formData异常:{}", e.getMessage());
        }
        /*
         * substring with the last char '&'
         */
        String formDataBodyString = "";
        if(formDataBodyBuilder.length()>0){
            formDataBodyString = formDataBodyBuilder.substring(0, formDataBodyBuilder.length() - 1);
        }
        return formDataBodyString;
    }

    private boolean isServiceFlag(List<String> readRequestDataServiceIdList, String routeServiceId) {
        if(!CollectionUtils.isEmpty(readRequestDataServiceIdList)
                && (GatewayLogTypeEnum.SERVICE.getType().equals(gatewayPluginProperties.getLogRequest().getLogType()) || GatewayLogTypeEnum.CONFIGURE.getType().equals(gatewayPluginProperties.getLogRequest().getLogType()))
                && readRequestDataServiceIdList.contains(routeServiceId)){
            log.debug("[GatewayContext]Properties Set Read Specific Request Data With ServiceId:{}", routeServiceId);
            return true;
        }
        return false;
    }

    private boolean isPathFlag(boolean readRequestDataPathList, ServerWebExchange exchange, List<String> readRequestDataPathList1, String s, boolean pathFlag) {
        if (readRequestDataPathList) {
            String requestPath = exchange.getRequest().getPath().pathWithinApplication().value();
            for (String path : readRequestDataPathList1) {
                if (ANT_PATH_MATCHER.match(path, requestPath)) {
                    log.debug(s, requestPath, path);
                    pathFlag = true;
                    break;
                }
            }
        }
        return pathFlag;
    }
}