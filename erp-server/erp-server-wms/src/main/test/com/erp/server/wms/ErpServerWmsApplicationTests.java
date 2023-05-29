package com.erp.server.wms;

import com.erp.model.wms.entity.CfgOperateLogFieldEntity;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.server.wms.service.CfgOperateLogFieldService;
import com.erp.server.wms.service.QcResultService;
import org.junit.Test;
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
        String classPath = String.valueOf(TransferInDetailEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("skuNo").setFieldName("sku").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("qty").setFieldName("调入数量").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("planQty").setFieldName("计划调入数量").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("transitDamageQty").setFieldName("途损数").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("transitDamageResponsible").setFieldName("途损责任方").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgOperateLogFieldEntity().setField("inWarehouseLocation").setFieldName("调入仓位").setClassPath(classPath).setType(0).setEnumClass("")
//                new CfgOperateLogFieldEntity().setField("orderType").setFieldName("订单类型").setClassPath(classPath).setType(2).setEnumClass(BillTypeEnum.class.getName()),
//                new CfgOperateLogFieldEntity().setField("warehouseName").setFieldName("仓库").setClassPath(classPath).setType(0).setEnumClass("")

        );
        logFieldService.saveBatch(logFields);
        System.out.println("sss");
    }


    @Test
    public void testSendMsg() {

        qcResultService.sendQcResultMsg(Arrays.asList("1653969938324520962"));
    }


}
