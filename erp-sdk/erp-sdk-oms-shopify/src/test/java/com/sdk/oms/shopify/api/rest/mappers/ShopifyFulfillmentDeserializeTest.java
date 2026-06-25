package com.sdk.oms.shopify.api.rest.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.shopify.api.rest.model.ShopifyFulfillmentOrder;
import com.sdk.oms.shopify.api.rest.model.ShopifyUpdateFulfillmentPayloadRoot;
import org.junit.Assert;
import org.junit.Test;

public class ShopifyFulfillmentDeserializeTest {

    @Test
    public void shouldDeserializeAssignedLocationSnakeCaseFields() throws Exception {
        ObjectMapper mapper = ShopifySdkObjectMapper.buildMapper();
        String json = "{"
                + "\"assigned_location\":{"
                + "\"country_code\":\"US\","
                + "\"location_id\":\"123456\","
                + "\"name\":\"Main Warehouse\""
                + "}"
                + "}";

        ShopifyFulfillmentOrder order = mapper.readValue(json, ShopifyFulfillmentOrder.class);

        Assert.assertNotNull(order.getAssignedLocation());
        Assert.assertEquals("US", order.getAssignedLocation().getCountryCode());
        Assert.assertEquals("123456", order.getAssignedLocation().getLocationId());
        Assert.assertEquals("Main Warehouse", order.getAssignedLocation().getName());
    }

    @Test
    public void shouldDeserializeUpdateFulfillmentPayloadSnakeCaseFields() throws Exception {
        ObjectMapper mapper = ShopifySdkObjectMapper.buildMapper();
        String json = "{"
                + "\"fulfillment\":{"
                + "\"message\":\"Shipped\","
                + "\"notify_customer\":true,"
                + "\"tracking_info\":{"
                + "\"number\":\"TN001\","
                + "\"company\":\"UPS\""
                + "}"
                + "}"
                + "}";

        ShopifyUpdateFulfillmentPayloadRoot root = mapper.readValue(json, ShopifyUpdateFulfillmentPayloadRoot.class);

        Assert.assertNotNull(root.getFulfillment());
        Assert.assertEquals("Shipped", root.getFulfillment().getMessage());
        Assert.assertTrue(root.getFulfillment().isNotifyCustomer());
        Assert.assertNotNull(root.getFulfillment().getTrackingInfo());
        Assert.assertEquals("TN001", root.getFulfillment().getTrackingInfo().getNumber());
        Assert.assertEquals("UPS", root.getFulfillment().getTrackingInfo().getCompany());
    }
}
