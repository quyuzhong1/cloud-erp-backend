package com.sdk.third.lingxing.utils;

import com.sdk.third.lingxing.core.HttpRequest;
import com.sdk.third.lingxing.core.HttpResponse;
import com.sdk.third.lingxing.core.HttpResponseImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

@Slf4j
public class HttpExecutor {


    private static final HttpExecutor INSTANCE = new HttpExecutor();

    private HttpExecutor() {
    }

    public static HttpExecutor create() {
        return INSTANCE;
    }


    public <R> HttpResponse execute(HttpRequest<R> request) throws Exception {
        log.debug("Executing Request: {} -> {}", request.getEndpoint(), request.getPath());
        HttpCommand<R> command = HttpCommand.create(request);
        return invokeRequest(command);
    }

    private <R> HttpResponse invokeRequest(HttpCommand<R> command) throws Exception {
        Response response = command.execute();
        if (command.getRetries() == 0 && response.code() != 200) {
            return invokeRequest(command.incrementRetriesAndReturn());
        }
        return HttpResponseImpl.wrap(response);
    }

}
