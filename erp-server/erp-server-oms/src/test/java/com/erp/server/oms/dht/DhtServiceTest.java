package com.erp.server.oms.dht;

import cn.hutool.json.JSONUtil;
import com.erp.server.oms.ErpServerOmsApplication;
import com.sdk.oms.dht.dto.DhtAuthDTO;
import com.sdk.oms.dht.service.DhtService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class DhtServiceTest {

    @Resource
    private DhtService dhtService;

    @Test
    public void getCorpAccessToken() {
        DhtAuthDTO resp = dhtService.getCorpAccessToken();
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}