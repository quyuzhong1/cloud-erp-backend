package com.erp.server.sys;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerSysApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerSysApplicationTests {

    @Test
    public void contextLoads() {
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_OPERATOR.getCode());
        LinkedList<String> queryFilters = new LinkedList<>();
        //禁用状态
        queryFilters.add(StrUtil.format(" FForbiddenStatus = {}", "'0'"));
        //查询
        String fieldKeys = "FEntity_FEntryId,FOperatorType,FBizOrgId.FNumber,FNumber,FStaffId.FStaffNumber";
        String filterStr = String.join(" and ", queryFilters);
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取10ing条
        Integer pageSize = 1000;
        List<Map<String, Object>> resultList = apiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
        System.out.println("==================="+JSONUtil.toJsonStr(resultList));

    }

}
