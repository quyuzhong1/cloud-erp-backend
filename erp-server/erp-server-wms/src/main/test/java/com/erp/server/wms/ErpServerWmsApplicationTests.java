package com.erp.server.wms;

import com.erp.model.wms.entity.CfgOperateLogFieldEntity;
import com.erp.model.wms.entity.OtherInstockEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.model.wms.enums.InstockTypeEnum;
import com.erp.model.wms.enums.InventoryDirectionEnum;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.CfgOperateLogFieldService;
import com.erp.server.wms.service.QcResultService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author Lambda
 * @Classname ErpServerScmApplicationTests
 * @Description TODO
 * @Date 2023-04-03 9:21
 * @Created by yl
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerWmsApplicationTests {

    @Resource
    private CfgOperateLogFieldService logFieldService;

    @Resource
    private QcResultService qcResultService;


    @Test
    public void addLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(WarehouseReceiveEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("purchaseOrderCode").setFieldName("采购订单编号").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("supplierName").setFieldName("供应商名称").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("receiveUserName").setFieldName("收货人名称").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("receiveDeptName").setFieldName("收货人部门名称").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("receiveOrgName").setFieldName("收货人组织名称").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("billDate").setFieldName("收货日期").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("deliveryWarehouseName").setFieldName("交货仓库名称").setClassPath(classPath).setType(0).setEnumClass("")
        );
        logFieldService.saveBatch(logFields);
        System.out.println("sss");
    }


    @Test
    public void testSendMsg() {

        qcResultService.sendQcResultMsg(Arrays.asList("1653969938324520962"));
    }


}
