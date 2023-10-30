//package com.erp.server.wms;
//
//import com.erp.model.wms.entity.CfgOperateLogFieldEntity;
//import com.erp.model.wms.entity.StocktakingProfitLossEntity;
//import com.erp.server.wms.service.CfgOperateLogFieldService;
//import com.erp.server.wms.service.PoInstockService;
//import com.erp.server.wms.service.QcResultService;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Profile;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.testng.annotations.Test;
//
//import javax.annotation.Resource;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * @author Lambda
// * @Classname ErpServerScmApplicationTests
//
// * @Date 2023-04-03 9:21
// * @Created by yl
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {ErpServerWmsApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@Profile("dev")
//public class ErpServerWmsApplicationTests {
//
//    @Resource
//    private CfgOperateLogFieldService logFieldService;
//
//    @Resource
//    private QcResultService qcResultService;
//
//
//    @Test
//    public void addLogField() {
//        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
//        String classPath = String.valueOf(StocktakingProfitLossEntity.class);
//        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
//                new CfgOperateLogFieldEntity().setField("code").setFieldName("单号").setClassPath(classPath).setType(0).setEnumClass("")
//                ,new CfgOperateLogFieldEntity().setField("sourceCode").setFieldName("盘点任务单号").setClassPath(classPath).setType(0).setEnumClass("")
//                ,new CfgOperateLogFieldEntity().setField("billType").setFieldName("单据类型").setClassPath(classPath).setType(2).setEnumClass("com.erp.model.wms.enums.BillTypeEnum")
//                ,new CfgOperateLogFieldEntity().setField("billDate").setFieldName("单据类型").setClassPath(classPath).setType(0).setEnumClass("")
//                ,new CfgOperateLogFieldEntity().setField("approveUserName").setFieldName("审核人").setClassPath(classPath).setType(0).setEnumClass("")
//                ,new CfgOperateLogFieldEntity().setField("approveTime").setFieldName("审核时间").setClassPath(classPath).setType(0).setEnumClass("")
//                ,new CfgOperateLogFieldEntity().setField("inventoryOrgName").setFieldName("库存组织").setClassPath(classPath).setType(0).setEnumClass("")
//                ,new CfgOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0).setEnumClass("")
//        );
//        logFieldService.saveBatch(logFields);
//        System.out.println("sss");
//    }
//
//
//   // @Test
//    public void testSendMsg() {
//
//        qcResultService.sendQcResultMsg(Arrays.asList("1653969938324520962"));
//    }
//
//    @Resource
//    private PoInstockService poInstockService;
//
//
//
//}
