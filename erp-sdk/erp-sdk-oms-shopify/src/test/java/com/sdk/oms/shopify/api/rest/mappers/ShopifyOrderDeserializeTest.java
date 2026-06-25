package com.sdk.oms.shopify.api.rest.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrdersRoot;
import org.junit.Assert;
import org.junit.Test;

/**
 * 覆盖 {@link ShopifySdkObjectMapper} 纯 Jackson 内省下订单核心字段反序列化，防止 JAXB 移除后回归。
 */
public class ShopifyOrderDeserializeTest {

    @Test
    public void shouldDeserializeOrderIdFromSnakeCaseIdField() throws Exception {
        ObjectMapper mapper = ShopifySdkObjectMapper.buildMapper();
        String json = "{\"orders\":[{\"id\":\"7123456789\",\"name\":\"#1001\"}]}";

        ShopifyOrdersRoot root = mapper.readValue(json, ShopifyOrdersRoot.class);

        Assert.assertNotNull(root.getOrders());
        Assert.assertEquals(1, root.getOrders().size());
        Assert.assertEquals("7123456789", root.getOrders().get(0).getOrderId());
    }

    @Test
    public void shouldDeserializeCancelledAtAndMarkAsCancelled() throws Exception {
        ObjectMapper mapper = ShopifySdkObjectMapper.buildMapper();
        String json = "{"
                + "\"id\":\"1\","
                + "\"financial_status\":\"paid\","
                + "\"cancelled_at\":\"2026-06-24T03:42:54-04:00\""
                + "}";

        ShopifyOrder order = mapper.readValue(json, ShopifyOrder.class);

        Assert.assertNotNull(order.getCancelledAt());
        Assert.assertEquals(2026, order.getCancelledAt().getYear());
        Assert.assertTrue(order.convertIsCancel());
        Assert.assertTrue(order.convertInvalidStatus());
    }
}
