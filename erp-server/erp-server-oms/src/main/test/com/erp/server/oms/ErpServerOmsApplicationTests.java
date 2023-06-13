package com.erp.server.oms;

import com.erp.model.oms.entity.CfgOperateLogFieldEntity;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.server.oms.service.CfgOperateLogFieldService;
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
@SpringBootTest(classes = {ErpServerOmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerOmsApplicationTests {

    @Resource
    private CfgOperateLogFieldService logFieldService;



    @Test
    public void addLogField() {
        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(TransferInDetailEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0).setEnumClass("")
//                new CfgOperateLogFieldEntity().setField("currency").setFieldName("币种").setClassPath(classPath).setType(0).setEnumClass(""),
//                new CfgOperateLogFieldEntity().setField("currencySymbol").setFieldName("币种符号").setClassPath(classPath).setType(0).setEnumClass(""),
//                new CfgOperateLogFieldEntity().setField("isTax").setFieldName("是否含税").setClassPath(classPath).setType(1).setEnumClass(""),
//                new CfgOperateLogFieldEntity().setField("addressType").setFieldName("地址类型").setClassPath(classPath).setType(2).setEnumClass(CustomerAddressTypeEnum.class.getName()),
//                new CfgOperateLogFieldEntity().setField("salesOrgName").setFieldName("销售组织").setClassPath(classPath).setType(0).setEnumClass("")
        );
        logFieldService.saveBatch(logFields);
        System.out.println("sss");
    }


    @Test
    public void testSendMsg() {

    }


}
