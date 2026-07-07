package com.sdk.oms.shopify.api.rest.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdk.oms.shopify.api.rest.model.ShopifyOrdersRoot;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class ShopifyPaymentSchedulesDeserializeTest {

    @Test
    public void shouldDeserializePaymentSchedulesIssuedAtWithOffset() throws Exception {
        ObjectMapper mapper = ShopifySdkObjectMapper.buildMapper();
        String json = "{"
                + "\"orders\":[{"
                + "\"id\":\"1\","
                + "\"payment_terms\":{"
                + "\"payment_schedules\":[{"
                + "\"issued_at\":\"2026-06-24T03:42:54-04:00\","
                + "\"due_at\":\"2026-06-25T03:42:54-04:00\""
                + "}]"
                + "}"
                + "}]"
                + "}";

        ShopifyOrdersRoot root = mapper.readValue(json, ShopifyOrdersRoot.class);

        Assert.assertNotNull(root.getOrders());
        Assert.assertEquals(1, root.getOrders().size());
        LocalDateTime issuedAt = root.getOrders().get(0).getPaymentTerms().getPaymentSchedules().get(0).getIssuedAt();
        Assert.assertNotNull(issuedAt);
        Assert.assertEquals(2026, issuedAt.getYear());
        Assert.assertEquals(6, issuedAt.getMonthValue());
        Assert.assertEquals(24, issuedAt.getDayOfMonth());
    }
}
