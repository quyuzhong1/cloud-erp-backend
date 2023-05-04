package com.erp.server.scm;

import com.erp.model.wms.entity.CfgModuleOperateLogFieldEntity;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.CfgModuleOperateLogFieldService;
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
    private CfgModuleOperateLogFieldService logFieldService;


    @Test
    public void addLogField() {

        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(PoInstockDetailEntity.class);
        List<CfgModuleOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgModuleOperateLogFieldEntity().setField("stockInQty").setFieldName("入库数量").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("exceedQty").setFieldName("超出数量").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("warehouseLocationName").setFieldName("库位").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("remark").setFieldName("入库备注").setClassPath(classPath).setType(0).setEnumClass("")
        );
        logFieldService.saveBatch(logFields);
        System.out.println("sss");
    }



}
