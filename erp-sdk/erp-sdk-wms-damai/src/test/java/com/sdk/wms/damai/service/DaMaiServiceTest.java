package com.sdk.wms.damai.service;

import cn.hutool.json.JSONUtil;
import com.common.business.constant.BusinessCommonConstants;
import com.sdk.wms.damai.dto.response.DaMaiBaseResp;
import com.sdk.wms.damai.dto.response.DaMaiWarehouseResp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={BusinessCommonConstants.class,DaMaiService.class})
public class DaMaiServiceTest {

    private final Map<String,Object> authMap = new HashMap<>();
    {
        authMap.put("appToken","5cdf2a88fc91cbc2c7befa959a6f0c0a");
        authMap.put("appKey","68cb7beaeaf5f3caac5bba16a632ca13");
    }

    @Resource
    private DaMaiService daMaiService;

    @Test
    public void getWarehouseList() {
        DaMaiBaseResp<List<DaMaiWarehouseResp>> resp = daMaiService.getWarehouseList(authMap);
        System.out.println(JSONUtil.toJsonStr(resp));
    }
}