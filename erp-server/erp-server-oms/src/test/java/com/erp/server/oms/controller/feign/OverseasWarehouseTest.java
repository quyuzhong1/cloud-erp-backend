package com.erp.server.oms.controller.feign;

import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.rpc.wms.feign.ThirdWarehouseFeign;
import com.erp.server.oms.ErpServerOmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class})
public class OverseasWarehouseTest {

    @Resource
    private ThirdWarehouseFeign feign;

    @Test
    public void createOutboundTest() {
        ThirdWarehouseCreateOutboundReq createOutboundReq = ThirdWarehouseCreateOutboundReq.builder()
                .authId("1726457716430561281")
                .thirdWarehouseProvideCode(OmsPlatformEnum.OMS_GOOD_CANG.getCode())
                .build();
        ApiResult<String> result = feign.createOutboundOrder(createOutboundReq);
        System.out.println(result);
    }

    @Test
    public void cancelOutboundOrderTest() {
        ThirdWarehouseCancelOutboundReq cancelOutboundReq = ThirdWarehouseCancelOutboundReq.builder()
                .authId("1726457716430561281")
                .thirdWarehouseProvideCode(OmsPlatformEnum.OMS_GOOD_CANG.getCode())
                .build();
        ApiResult<String> result = feign.cancelOutboundOrder(cancelOutboundReq);
        System.out.println(result);
    }
}