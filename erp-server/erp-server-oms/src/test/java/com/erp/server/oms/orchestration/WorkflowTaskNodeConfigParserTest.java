package com.erp.server.oms.orchestration;

import com.erp.model.oms.dto.WorkflowTaskNodeConfigDTO;
import org.junit.Assert;
import org.junit.Test;

/**
 * 节点配置解析单测
 */
public class WorkflowTaskNodeConfigParserTest {

    @Test
    public void parseLegacyClassPathFormat() {
        WorkflowTaskNodeConfigDTO config = WorkflowTaskNodeConfigParser.parse(
                "com.erp.server.wms.controller.api.OtherInstockController#generateOtherApprove");
        Assert.assertEquals("com.erp.server.wms.controller.api.OtherInstockController", config.getClassPath());
        Assert.assertEquals("generateOtherApprove", config.getMethodName());
        Assert.assertEquals("feign_invoke", config.getHandlerType());
        Assert.assertNotNull(config.getServiceCode());
    }

    @Test
    public void parseJsonFormat() {
        String json = "{"
                + "\"serviceCode\":\"wms\","
                + "\"handlerType\":\"feign_invoke\","
                + "\"classPath\":\"com.erp.server.wms.controller.api.OtherInstockController\","
                + "\"methodName\":\"generateOtherApprove\","
                + "\"timeoutSeconds\":120,"
                + "\"maxRetry\":3,"
                + "\"idempotent\":true"
                + "}";
        WorkflowTaskNodeConfigDTO config = WorkflowTaskNodeConfigParser.parse(json);
        Assert.assertEquals("wms", config.getServiceCode());
        Assert.assertEquals("generateOtherApprove", config.getMethodName());
        Assert.assertEquals(Integer.valueOf(120), config.getTimeoutSeconds());
        Assert.assertEquals(Integer.valueOf(3), config.getMaxRetry());
    }

    @Test
    public void buildTargetEndpoint() {
        WorkflowTaskNodeConfigDTO config = new WorkflowTaskNodeConfigDTO();
        config.setClassPath("com.example.FooController");
        config.setMethodName("bar");
        Assert.assertEquals("com.example.FooController#bar", WorkflowTaskNodeConfigParser.buildTargetEndpoint(config));
    }
}
