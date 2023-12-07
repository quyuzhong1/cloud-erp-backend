package com.erp.server.oms.controller.feign;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.CfgOperateLogFieldEntity;
import com.erp.model.oms.entity.RuleDeliveryWarehouseEntity;
import com.erp.model.oms.entity.RuleLogisticsEntity;
import com.erp.model.oms.entity.RuleOrderApprovalEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCancelOutboundReq;
import com.erp.model.wms.dto.third.request.ThirdWarehouseCreateOutboundReq;
import com.erp.rpc.wms.feign.ThirdWarehouseFeign;
import com.erp.server.oms.ErpServerOmsApplication;
import com.erp.server.oms.service.CfgOperateLogFieldService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerOmsApplication.class})
public class OverseasWarehouseTest {

    @Resource
    private ThirdWarehouseFeign feign;

    @Resource
    private CfgOperateLogFieldService cfgOperateLogFieldService;

    // @Test
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
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String  classPath = String.valueOf(RuleLogisticsEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgOperateLogFieldEntity().setField("priority").setFieldName("优先级").setClassPath(classPath).setType(0) .setEnumClass("")


        );
         cfgOperateLogFieldService.saveBatch(logFields);
    }
}