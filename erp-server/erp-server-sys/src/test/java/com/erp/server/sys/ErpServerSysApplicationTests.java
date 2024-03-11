package com.erp.server.sys;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
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
        LinkedList<String> queryFilters = new LinkedList<>();
        //审核状态
        queryFilters.add(StrUtil.format(" FDocumentStatus = {}", "'C'"));
        //禁用状态
        queryFilters.add(StrUtil.format(" FFORBIDSTATUS ！= {}", "'B'"));

        String filterStr = String.join(" and ", queryFilters);
        System.out.println("拉取金蝶条件为>>>>>>>>>>"+ filterStr);
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_DEPARTMENT.getCode());
        //查询子单据id
        String fieldKeys = "FDEPTID,FNumber,FName,FUseOrgId.FNumber,FParentID.FNumber,FDocumentStatus,FFORBIDSTATUS";
        List<Map<String, Object>> list = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
        System.out.println(">>>>>>>>>"+ JSONUtil.toJsonStr(list));

    }

}
