package com.erp.server.oms.controller.feign;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.CfgOperateLogFieldEntity;
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
        String  classPath = String.valueOf(RuleOrderApprovalEntity.class);
        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
                new CfgOperateLogFieldEntity().setField("name").setFieldName("规则名称").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("remark").setFieldName("规则描述").setClassPath(classPath).setType(0) .setEnumClass(""),

                new CfgOperateLogFieldEntity().setField("flow_status").setFieldName("流向状态").setClassPath(classPath).setType(2) .setEnumClass("com.common.business.enums.ApproveStatusEnum"),
                new CfgOperateLogFieldEntity().setField("left_bracket").setFieldName("左括号").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("field").setFieldName("条件的字段").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("compare").setFieldName("比较符").setClassPath(classPath).setType(2) .setEnumClass("com.common.core.enums.RuleCompareEnum"),
                new CfgOperateLogFieldEntity().setField("value").setFieldName("值").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("right_bracket").setFieldName("右括号").setClassPath(classPath).setType(0) .setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("logic").setFieldName("逻辑关系").setClassPath(classPath).setType(2) .setEnumClass("com.common.core.enums.RuleLogicEnum")
        );
         cfgOperateLogFieldService.saveBatch(logFields);
    }
}