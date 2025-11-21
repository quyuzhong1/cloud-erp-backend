package com.erp.server.wms.iml;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.oms.aliexpress.api.IopClient;
import com.erp.oms.aliexpress.api.IopClientImpl;
import com.erp.oms.aliexpress.api.IopRequest;
import com.erp.oms.aliexpress.api.IopResponse;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.request.DeclareDeliverRequest;
import com.erp.oms.aliexpress.enums.Protocol;
import com.erp.oms.aliexpress.util.ApiException;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.tms.aliexpress.model.query.request.QueryShipmentOrder;
import com.sdk.wms.iml.dto.ImlBaseResp;
import com.sdk.wms.iml.service.ImlService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;


@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ImlTests {

    @Resource
    private ImlService imlService;

    public ImlTests(){
        Map<String,Object> authMap = new HashMap<>();
        authMap.put("appId","1929841041771364354");
        authMap.put("appSecret","dx-zosnwtgwo3=u=276qgzu+3weguyst");
        authMap.put("appToken","ZOFsMc85N29ly-sA4qKbDXQgJS6QF2A8IzlCWWXH_UgoaGoY6Az8aZuU_uWuQ6s0");
        ThirdWarehouseContext.setAuthMap(authMap);
    }

    @Test
    public void querySellerShipmentInfo() {
        ImlBaseResp<String> resp = imlService.getWarehouse();
        System.out.println(resp);
    }
}
