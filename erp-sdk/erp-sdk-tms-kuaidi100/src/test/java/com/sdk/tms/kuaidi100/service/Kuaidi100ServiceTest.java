package com.sdk.tms.kuaidi100.service;

import com.sdk.tms.kuaidi100.model.request.Kuaidi100QueryParam;
import com.sdk.tms.kuaidi100.model.response.Kuaidi100QueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 快递100 服务测试类
 */
@Slf4j
@SpringBootTest(classes = Kuaidi100Service.class)
public class Kuaidi100ServiceTest {

    @Resource
    private Kuaidi100Service kuaidi100Service;

    @Test
    public void testGetTrack() {
        // 使用测试环境参数（通常需要替换为真实的测试 key）
        String customer = "TEST_CUSTOMER";
        String key = "TEST_KEY";
        
        Kuaidi100QueryParam param = Kuaidi100QueryParam.builder()
                .com("yuantong")
                .num("YT7470876612046")
                .build();
        
        // 实际上会发起请求，这里仅演示调用
        // Kuaidi100QueryResponse response = kuaidi100Service.getTrack(customer, key, param);
        // assertNotNull(response);
        // log.info("测试响应: {}", response);
        
        log.info("单元测试结构已准备就绪，待填充真实授权信息进行测试。");
    }
}
