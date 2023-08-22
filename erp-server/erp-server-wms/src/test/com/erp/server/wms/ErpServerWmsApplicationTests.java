package com.erp.server.wms;

import cn.hutool.http.ContentType;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.wms.entity.CfgOperateLogFieldEntity;
import com.erp.model.wms.entity.StocktakingPlanDetailEntity;
import com.erp.model.wms.entity.StocktakingPlanEntity;
import com.erp.model.wms.entity.TransferInDetailEntity;
import com.erp.model.wms.enums.SeparateRuleEnum;
import com.erp.model.wms.enums.StocktakingModeEnum;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.model.wms.enums.StocktakingTypeEnum;
import com.erp.server.wms.service.CfgOperateLogFieldService;
import com.erp.server.wms.service.PoInstockService;
import com.erp.server.wms.service.QcResultService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Lambda
 * @Classname ErpServerScmApplicationTests

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
        String classPath = String.valueOf(StocktakingPlanDetailEntity.class);
        List<CfgOperateLogFieldEntity> logFields = Arrays.asList(
                new CfgOperateLogFieldEntity().setField("warehouseName").setFieldName("仓库名称").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("warehouseArea").setFieldName("仓库区域").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("warehouseLocation").setFieldName("仓位").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("skuNo").setFieldName("sku编码").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("orgName").setFieldName("组织名称").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("updateUserName").setFieldName("修改人名称").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("updateTime").setFieldName("更新时间").setClassPath(classPath).setType(0).setEnumClass("")
                ,new CfgOperateLogFieldEntity().setField("isDeleted").setFieldName("逻辑删除字段").setClassPath(classPath).setType(0).setEnumClass("")
        );
        logFieldService.saveBatch(logFields);
        System.out.println("sss");
    }


   // @Test
    public void testSendMsg() {

        qcResultService.sendQcResultMsg(Arrays.asList("1653969938324520962"));
    }

    @Resource
    private PoInstockService poInstockService;



}
