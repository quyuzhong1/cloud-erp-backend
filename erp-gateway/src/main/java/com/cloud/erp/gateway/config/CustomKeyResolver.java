package com.cloud.erp.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class CustomKeyResolver implements KeyResolver{
	
	private String id;

	@Override
	public Mono<String> resolve(ServerWebExchange exchange) {
		return Mono.just(id);
	}

	public void setId(String id) {
		this.id = id;
	}

}
