package com.erp.server.scm;

import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.server.scm.service.CfgModuleOperateLogFieldService;
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
@SpringBootTest(classes = {ErpServerScmApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Profile("dev")
public class ErpServerScmApplicationTests {

    @Resource
    private CfgModuleOperateLogFieldService logFieldService;


    @Test
    public void addLogField() {

        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
        String classPath = String.valueOf(PurchasePriceChangeEntity.class);
        List<CfgModuleOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgModuleOperateLogFieldEntity().setField("reason").setFieldName("调价原因").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("adjustDate").setFieldName("调价日期").setClassPath(classPath).setType(0).setEnumClass(""),
                new CfgModuleOperateLogFieldEntity().setField("adjustUserName").setFieldName("调价员").setClassPath(classPath).setType(0).setEnumClass("")
        );
        logFieldService.saveBatch(logFields);
    }



}
