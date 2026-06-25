package com.sdk.oms.shopify.api.rest.mappers;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeDeserializer;
import com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * Instead of using the default Spring ObjectMapper we are using a custom one for the Shopify REST API. This way, we can customize the serialization
 * and deserialization process for the Shopify REST API, seperately from any Jackson based REST services we may wish to vend from our application
 * (which can then use the default Spring ObjectMapper).
 * 
 * 
 */
public class ShopifySdkObjectMapper {

	private ShopifySdkObjectMapper() {}


	/**
	 * @return ObjectMapper
	 *
	 * 仅使用 Jackson 注解内省（非 JAXB）：Shopify REST 模型已用 {@code @JsonProperty}/{@code @JsonDeserialize} 标注，
	 * LocalDateTime 由 {@link com.sdk.oms.shopify.api.rest.model.serializer.LocalDateTimeDeserializer} 模块处理。
	 * 预发需回归订单/退款/履约全链路反序列化；若有个别字段仍依赖 JAXB，改为显式 Jackson 注解。
	 */
	public static ObjectMapper buildMapper() {
		ObjectMapper objectMapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(MapperFeature.USE_ANNOTATIONS, true)
				.annotationIntrospector(new JacksonAnnotationIntrospector()).serializationInclusion(Include.NON_NULL).build();
		SimpleModule shopifyDateTimeModule = new SimpleModule("ShopifyDateTimeModule");
		shopifyDateTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
		shopifyDateTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
		objectMapper.registerModule(shopifyDateTimeModule);
		return objectMapper;
	}
}
