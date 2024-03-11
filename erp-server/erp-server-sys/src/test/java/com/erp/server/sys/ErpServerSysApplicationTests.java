package com.erp.server.sys;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ErpServerSysApplicationTests {

    @Test
    public void contextLoads() {
        LinkedList<String> queryFilters = new LinkedList<>();
        //审核状态
        queryFilters.add(StrUtil.format(" FDocumentStatus in ({})", "'C'"));
        //禁用状态
        queryFilters.add(StrUtil.format(" FFORBIDSTATUS ！= ({})", "'B'"));
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.BD_DEPARTMENT.getCode());
        //查询子单据id
        String fieldKeys = "FDEPTID,FNumber,FName,FUseOrgId.FNumber,FParentID.FNumber";
        List<Map<String, Object>> list = apiUtils.queryList("", fieldKeys, 1000, 1, 0);
        System.out.println("-----------"+ JSONUtil.toJsonStr(list));

    }

}
